package site.muyin.picturebed.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import site.muyin.picturebed.config.PictureBedConfig;
import site.muyin.picturebed.domain.SmmsImage;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.service.SmmsService;
import site.muyin.picturebed.utils.PluginCacheManager;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.ResultsVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static site.muyin.picturebed.constant.CommonConstant.PictureBedType.SMMS;

/**
 * @author: lywq
 * @date: 2024/05/21 16:12
 * @version: v1.0.0
 * @description:
 **/
@Service
@RequiredArgsConstructor
public class SmmsServiceImpl implements SmmsService {

    private final PluginCacheManager pluginCacheManager;

    @Override
    public Mono<ResultsVO> uploadImage(MultiValueMap<String, ?> multipartData) {
        Map<String, Object> paramMap = new HashMap(1);
        paramMap.put("file", multipartData);
        return req("upload", paramMap)
                .map(response -> {
                    if (response.success) {
                        return ResultsVO.success(response.message, response.data);
                    }
                    return ResultsVO.failure(response.message);
                });
    }

    @Override
    public Mono<PageResult<SmmsImage>> getImageList(CommonQuery query) {
        Map<String, Object> paramMap = new HashMap(1);
        paramMap.put("page", query.getPage());
        String params = HttpUtil.toParams(paramMap);

        return req("upload_history" + "?" + params, null)
                .map(response -> {
                    if (response.success) {
                        List<SmmsImage> imageList = JSONUtil.toList(JSONUtil.parseArray(response.data), SmmsImage.class);
                        return new PageResult<>(response.CurrentPage, response.PerPage, response.Count, response.TotalPages, imageList);
                    }
                    return null;
                });
    }

    @Override
    public Mono<Boolean> deleteImage(CommonQuery query) {
        if (ObjectUtil.isEmpty(query.getImageId())) {
            return Mono.just(false);
        }
        return req("delete/" + query.getImageId(), null)
                .map(response -> {
                    return response.success;
                });
    }

    private Mono<SmmsResponseRecord> req(String path, Map<String, Object> paramMap) {
        PictureBedConfig pictureBedConfig = pluginCacheManager.getConfig(PictureBedConfig.class);
        PictureBedConfig.PictureBed config = pictureBedConfig.getPictureBeds().stream().filter(p -> p.getPictureBedType().equals(SMMS)).findFirst().orElseThrow();
        String url = config.getPictureBedUrl();
        String authorization = config.getPictureBedToken();

        WebClient WEB_CLIENT =
                WebClient.builder()
                        .defaultHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
                        .defaultHeader(HttpHeaders.PRAGMA, "no-cache")
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
                        .defaultHeader("Authorization", "Basic " + authorization).build();

        if (StrUtil.startWithAny(path, "upload_history", "delete/")) {
            if (StrUtil.startWithAny(path, "upload_history")) {
                path = path + "&timestamp=" + System.currentTimeMillis();
            }
            return WEB_CLIENT.get()
                    .uri(url + path)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<>() {
                    });
        } else if (StrUtil.equals(path, "upload")) {
            MultiValueMap<String, ?> multiValueMap = (MultiValueMap<String, ?>) paramMap.get("file");
            MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
            multipartData.add("smfile", multiValueMap.getFirst("file"));
            return WEB_CLIENT.post()
                    .uri(url + path)
                    .body(BodyInserters.fromMultipartData(multipartData))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<>() {
                    });

        } else {
            throw new IllegalArgumentException("Unsupported path: " + path);
        }
    }

    public record SmmsResponseRecord(boolean success, String code, String message, Object data, String RequestId,
                                     int CurrentPage, int TotalPages, int PerPage, int Count) {
    }
}
