package site.muyin.picturebed.service.Impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import site.muyin.picturebed.config.PictureBedConfig;
import site.muyin.picturebed.domain.LskyProAlbum;
import site.muyin.picturebed.domain.LskyProImage;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.service.LskyProService;
import site.muyin.picturebed.utils.PluginCacheManager;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.ResultsVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static site.muyin.picturebed.constant.CommonConstant.PictureBedType.LSKY;

/**
 * @author: lywq
 * @date: 2024/04/29 11:05
 * @version: v1.0.0
 * @description:
 **/
@Service
@RequiredArgsConstructor
public class LskyProServiceImpl implements LskyProService {

    private final PluginCacheManager pluginCacheManager;

    @Override
    public Mono<ResultsVO> uploadImage(MultiValueMap<String, ?> multipartData) {
        Map<String, Object> paramMap = new HashMap(1);
        paramMap.put("file", multipartData);

        return req("upload", paramMap)
                .map(response -> {
                    if (response.status) {
                        return ResultsVO.success(response.message, response.data);
                    }
                    return ResultsVO.failure(response.message);
                });
    }

    @Override
    public Mono<List<LskyProAlbum>> getAlbumList(CommonQuery query) {
        Map<String, Object> paramMap = new HashMap(2);
        paramMap.put("keyword", query.getKeyword());
        paramMap.put("page", query.getPage());
        return req("albums", paramMap)
                .map(response -> {
                    if (response.status) {
                        Map<String, Object> data = response.data;
                        List<LskyProAlbum> albumList = JSONUtil.toList(JSONUtil.parseArray(data.get("data")), LskyProAlbum.class);
                        return albumList;
                    }
                    return null;
                });
    }

    @Override
    public Mono<PageResult<LskyProImage>> getImageList(CommonQuery query) {
        Map<String, Object> paramMap = new HashMap(3);
        paramMap.put("keyword", query.getKeyword());
        paramMap.put("album_id", query.getAlbumId());
        paramMap.put("page", query.getPage());
        return req("images", paramMap)
                .map(response -> {
                    if (response.status) {
                        Map<String, Object> data = response.data;
                        List<LskyProImage> imageList = JSONUtil.toList(JSONUtil.parseArray(data.get("data")), LskyProImage.class);
                        return new PageResult<>((Integer) data.get("current_page"), (Integer) data.get("per_page"), (Integer) data.get("total"), (Integer) data.get("last_page"), imageList);
                    }
                    return null;
                });
    }

    @Override
    public Mono<Boolean> deleteImage(CommonQuery query) {
        Map<String, Object> paramMap = new HashMap(3);
        paramMap.put("keyword", query.getKeyword());
        paramMap.put("album_id", query.getAlbumId());
        paramMap.put("page", query.getPage());
        return req("images/" + query.getImageId(), paramMap)
                .map(response -> {
                    return response.status;
                });
    }

    private Mono<LskyProResponseRecord> req(String path, Map<String, Object> paramMap) {
        PictureBedConfig pictureBedConfig = pluginCacheManager.getConfig(PictureBedConfig.class);
        PictureBedConfig.PictureBed config = pictureBedConfig.getPictureBeds().stream().filter(p -> p.getPictureBedType().equals(LSKY)).findFirst().orElseThrow();
        String url = config.getPictureBedUrl();
        String authorization = config.getPictureBedToken();

        WebClient WEB_CLIENT =
                WebClient.builder()
                        .defaultHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
                        .defaultHeader(HttpHeaders.PRAGMA, "no-cache")
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .defaultHeader("Authorization", "Bearer " + authorization).build();

        switch (path) {
            case "albums":
            case "images":
                paramMap.put("timestamp", System.currentTimeMillis());
                return WEB_CLIENT.get()
                        .uri(url + path + "?" + HttpUtil.toParams(paramMap))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<>() {
                        });
            case "upload":
                return WEB_CLIENT.post()
                        .uri(url + path)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData((MultiValueMap<String, ?>) paramMap.get("file")))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<>() {
                        });
            default:
                if (path.startsWith("images/")) {
                    return WEB_CLIENT.delete()
                            .uri(url + path)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<>() {
                            });
                } else {
                    throw new IllegalArgumentException("Unsupported path: " + path);
                }
        }
    }

    public record LskyProResponseRecord(boolean status, String message, Map<String, Object> data) {
    }
}

