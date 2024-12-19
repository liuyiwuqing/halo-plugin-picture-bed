package site.muyin.picturebed.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import io.netty.channel.ChannelOption;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import run.halo.app.plugin.ReactiveSettingFetcher;
import site.muyin.picturebed.config.PictureBedConfig;
import site.muyin.picturebed.domain.ImgtpImage;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.service.ImgtpService;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.ResultsVO;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static site.muyin.picturebed.config.PictureBedConfig.GROUP;
import static site.muyin.picturebed.constant.CommonConstant.PictureBedType.IMGTP;

/**
 * @author: lywq
 * @date: 2024/05/29 09:52
 * @version: v1.0.0
 * @description: Imgtp图床服务接口
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class ImgtpServiceImpl implements ImgtpService {

    private final ReactiveSettingFetcher settingFetcher;

    private final WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                            .responseTimeout(Duration.ofSeconds(5)) // 设置响应超时时间
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 设置连接超时时间
            ))
            .defaultHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
            .defaultHeader(HttpHeaders.PRAGMA, "no-cache")
            .build();

    @Override
    public Mono<ResultsVO> uploadImage(CommonQuery query, MultiValueMap<String, ?> multipartData) {
        Map<String, Object> paramMap = new HashMap(1);
        paramMap.put("file", multipartData);
        return req(query.getPictureBedId(), "upload", paramMap)
                .map(response -> {
                    if (response.code == 200) {
                        return ResultsVO.success(response.msg, response.data);
                    }
                    return ResultsVO.failure(response.msg);
                });
    }

    @Override
    public Mono<PageResult<ImgtpImage>> getImageList(CommonQuery query) {
        Map<String, Object> paramMap = new HashMap(1);
        paramMap.put("page", query.getPage());
        paramMap.put("rows", query.getSize());
        String params = HttpUtil.toParams(paramMap);

        return req(query.getPictureBedId(), "images" + "?" + params, null)
                .mapNotNull(response -> {
                    if (response.code == 200) {
                        ImgtpImagePageRes imgtpImagePageRes = JSONUtil.toBean(JSONUtil.toJsonStr(response.data), ImgtpImagePageRes.class);
                        return new PageResult<>(imgtpImagePageRes.current_page, imgtpImagePageRes.per_page, imgtpImagePageRes.total, imgtpImagePageRes.last_page, imgtpImagePageRes.data);
                    }
                    return null;
                });
    }

    @Override
    public Mono<Boolean> deleteImage(CommonQuery query) {
        if (ObjectUtil.isEmpty(query.getImageId())) {
            return Mono.just(false);
        }
        Map<String, Object> paramMap = new HashMap(1);
        paramMap.put("id", query.getImageId());
        String params = HttpUtil.toParams(paramMap);
        return req(query.getPictureBedId(), "delete" + "?" + params, null)
                .map(response -> {
                    return response.code == 200;
                });
    }

    private Mono<ImgtpResponseRecord> req(String pictureBedId, String path, Map<String, Object> paramMap) {
        if (path == null) {
            return Mono.error(new IllegalArgumentException("Path cannot be null"));
        }

        return settingFetcher.fetch(GROUP, PictureBedConfig.class)
                .flatMap(pictureBedConfig -> {

                    PictureBedConfig.PictureBed config = pictureBedConfig.getPictureBeds().stream()
                            .filter(p -> p.getPictureBedType().equals(IMGTP) && p.getPictureBedId().equals(pictureBedId))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("PictureBed config not found for ID: " + pictureBedId));

                    String url = config.getPictureBedUrl();
                    String authorization = config.getPictureBedToken();

                    WebClient client = webClient.mutate()
                            .defaultHeader("token", authorization)
                            .build();

                    switch (path) {
                        case "images":
                        case "delete":
                            return client.post()
                                    .uri(url + path)
                                    .retrieve()
                                    .bodyToMono(new ParameterizedTypeReference<ImgtpResponseRecord>() {
                                    })
                                    .doOnError(error -> log.error("POST request failed", error))
                                    .onErrorResume(error -> Mono.empty());

                        case "upload":
                            MultiValueMap<String, ?> multiValueMap = (MultiValueMap<String, ?>) paramMap.get("file");
                            if (multiValueMap == null || multiValueMap.isEmpty()) {
                                return Mono.error(new IllegalArgumentException("File parameter is missing"));
                            }

                            MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
                            multipartData.add("image", multiValueMap.getFirst("file"));

                            return client.post()
                                    .uri(url + path)
                                    .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .body(BodyInserters.fromMultipartData(multipartData))
                                    .retrieve()
                                    .bodyToMono(new ParameterizedTypeReference<ImgtpResponseRecord>() {
                                    })
                                    .doOnError(error -> log.error("POST request failed", error))
                                    .onErrorResume(error -> Mono.empty());

                        default:
                            return Mono.error(new IllegalArgumentException("Unsupported path: " + path));
                    }
                })
                .onErrorResume(error -> {
                    log.error("Configuration fetch failed", error);
                    return Mono.empty();
                });
    }

    @Data
    static class ImgtpImagePageRes {
        Integer total;
        Integer per_page;
        Integer current_page;
        Integer last_page;
        List<ImgtpImage> data;
    }

    public record ImgtpResponseRecord(Integer code, String msg, Object data, Integer time) {
    }
}
