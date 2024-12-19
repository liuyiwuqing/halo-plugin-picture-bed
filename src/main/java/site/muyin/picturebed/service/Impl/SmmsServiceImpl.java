package site.muyin.picturebed.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import io.netty.channel.ChannelOption;
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
import site.muyin.picturebed.domain.SmmsImage;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.service.SmmsService;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.ResultsVO;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static site.muyin.picturebed.config.PictureBedConfig.GROUP;
import static site.muyin.picturebed.constant.CommonConstant.PictureBedType.SMMS;

/**
 * @author: lywq
 * @date: 2024/05/21 16:12
 * @version: v1.0.0
 * @description:
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class SmmsServiceImpl implements SmmsService {

    private final ReactiveSettingFetcher settingFetcher;

    private final WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                            .responseTimeout(Duration.ofSeconds(5)) // 设置响应超时时间
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 设置连接超时时间
            ))
            .defaultHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
            .defaultHeader(HttpHeaders.PRAGMA, "no-cache")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
            .build();

    @Override
    public Mono<ResultsVO> uploadImage(CommonQuery query, MultiValueMap<String, ?> multipartData) {
        Map<String, Object> paramMap = new HashMap(1);
        paramMap.put("file", multipartData);
        return req(query.getPictureBedId(), "upload", paramMap)
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

        return req(query.getPictureBedId(), "upload_history" + "?" + params, null)
                .mapNotNull(response -> {
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
        return req(query.getPictureBedId(), "delete/" + query.getImageId(), null)
                .map(response -> {
                    return response.success;
                });
    }

    private Mono<SmmsResponseRecord> req(String pictureBedId, String path, Map<String, Object> paramMap) {
        if (path == null) {
            return Mono.error(new IllegalArgumentException("Path cannot be null"));
        }

        return settingFetcher.fetch(GROUP, PictureBedConfig.class)
                .flatMap(pictureBedConfig -> {

                    PictureBedConfig.PictureBed config = pictureBedConfig.getPictureBeds().stream()
                            .filter(p -> p.getPictureBedType().equals(SMMS) && p.getPictureBedId().equals(pictureBedId))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("PictureBed config not found for ID: " + pictureBedId));

                    String url = config.getPictureBedUrl();
                    String authorization = config.getPictureBedToken();

                    WebClient client = webClient.mutate()
                            .defaultHeader("Authorization", "Basic " + authorization)
                            .build();

                    String modifiedPath = path;

                    if (modifiedPath.startsWith("upload_history") || modifiedPath.startsWith("delete/")) {
                        if (modifiedPath.startsWith("upload_history")) {
                            modifiedPath = modifiedPath + "&timestamp=" + System.currentTimeMillis();
                        }
                        return client.get()
                                .uri(url + modifiedPath)
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<SmmsResponseRecord>() {
                                })
                                .doOnError(error -> log.error("GET request failed", error))
                                .onErrorResume(error -> Mono.empty());

                    } else if ("upload".equals(modifiedPath)) {
                        MultiValueMap<String, ?> fileData = (MultiValueMap<String, ?>) paramMap.get("file");
                        if (fileData == null || fileData.isEmpty()) {
                            return Mono.error(new IllegalArgumentException("File parameter is missing"));
                        }

                        MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
                        multipartData.add("smfile", fileData.getFirst("file"));

                        return client.post()
                                .uri(url + modifiedPath)
                                .body(BodyInserters.fromMultipartData(multipartData))
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<SmmsResponseRecord>() {
                                })
                                .doOnError(error -> log.error("POST request failed", error))
                                .onErrorResume(error -> Mono.empty());

                    } else {
                        return Mono.error(new IllegalArgumentException("Unsupported path: " + modifiedPath));
                    }
                })
                .onErrorResume(error -> {
                    log.error("Configuration fetch failed", error);
                    return Mono.empty();
                });
    }

    public record SmmsResponseRecord(boolean success, String code, String message, Object data, String RequestId,
                                     int CurrentPage, int TotalPages, int PerPage, int Count) {
    }
}
