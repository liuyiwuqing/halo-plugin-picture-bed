package site.muyin.picturebed.service.Impl;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import run.halo.app.plugin.ReactiveSettingFetcher;
import site.muyin.picturebed.config.PictureBedConfig;
import site.muyin.picturebed.domain.LskyProAlbum;
import site.muyin.picturebed.domain.LskyProImage;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.service.LskyProService;
import site.muyin.picturebed.utils.PictureBedUtil;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.ResultsVO;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static site.muyin.picturebed.config.PictureBedConfig.GROUP;
import static site.muyin.picturebed.constant.CommonConstant.PictureBedType.LSKY;

/**
 * @author: lywq
 * @date: 2024/04/29 11:05
 * @version: v1.0.0
 * @description:
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class LskyProServiceImpl implements LskyProService {

    private final ReactiveSettingFetcher settingFetcher;

    private final WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                            .responseTimeout(Duration.ofSeconds(5)) // 设置响应超时时间
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 设置连接超时时间
            ))
            .defaultHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
            .defaultHeader(HttpHeaders.PRAGMA, "no-cache")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

    @Override
    public Mono<ResultsVO> uploadImage(CommonQuery query, MultiValueMap<String, ?> multipartData) {
        Map<String, Object> paramMap = new HashMap(1);
        paramMap.put("file", multipartData);
        paramMap.put("album_id", query.getAlbumId());

        return req(query.getPictureBedId(), "upload", paramMap)
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
        return req(query.getPictureBedId(), "albums", paramMap)
                .map(response -> {
                    List<LskyProAlbum> albumList = Collections.emptyList();
                    if (response.status) {
                        Map<String, Object> data = response.data;
                        albumList = PictureBedUtil.convertObjectToList(data.get("data"), LskyProAlbum.class);
                    }
                    return albumList;
                });
    }

    @Override
    public Mono<PageResult<LskyProImage>> getImageList(CommonQuery query) {
        Map<String, Object> paramMap = new HashMap(3);
        paramMap.put("keyword", query.getKeyword());
        paramMap.put("album_id", query.getAlbumId());
        paramMap.put("page", query.getPage());
        return req(query.getPictureBedId(), "images", paramMap)
                .map(response -> {
                    List<LskyProImage> imageList = Collections.emptyList();
                    Integer currentPage = 1;
                    Integer perPage = 10;
                    Integer total = 0;
                    Integer lastPage = 1;

                    if (response != null && response.status) {
                        Map<String, Object> data = response.data;
                        imageList = Optional.ofNullable(data.get("data"))
                                .map(d -> PictureBedUtil.convertObjectToList(d, LskyProImage.class))
                                .orElse(Collections.emptyList());

                        currentPage = (Integer) data.getOrDefault("current_page", 1);
                        perPage = (Integer) data.getOrDefault("per_page", 10);
                        total = (Integer) data.getOrDefault("total", 0);
                        lastPage = (Integer) data.getOrDefault("last_page", 1);

                    }
                    return new PageResult<>(currentPage, perPage, total, lastPage, imageList);
                })
                .onErrorResume(e -> {
                    log.error("Error occurred while fetching image list: {}", e.getMessage());
                    return Mono.just(new PageResult<>(1, 10, 0, 0, Collections.emptyList()));
                });
    }

    @Override
    public Mono<Boolean> deleteImage(CommonQuery query) {
        Map<String, Object> paramMap = new HashMap(3);
        paramMap.put("keyword", query.getKeyword());
        paramMap.put("album_id", query.getAlbumId());
        paramMap.put("page", query.getPage());
        return req(query.getPictureBedId(), "images/" + query.getImageId(), paramMap)
                .map(response -> {
                    return response.status;
                });
    }

    private Mono<LskyProResponseRecord> req(String pictureBedId, String path, Map<String, Object> paramMap) {
        if (path == null) {
            return Mono.error(new IllegalArgumentException("Path cannot be null"));
        }

        return settingFetcher.fetch(GROUP, PictureBedConfig.class)
                .flatMap(pictureBedConfig -> {
                    PictureBedConfig.PictureBed config = pictureBedConfig.getPictureBeds().stream().filter(p -> p.getPictureBedType().equals(LSKY) && p.getPictureBedId().equals(pictureBedId)).findFirst().orElseThrow();
                    String url = config.getPictureBedUrl();
                    String authorization = config.getPictureBedToken();
                    String pictureBedStrategyId = config.getPictureBedStrategyId();

                    WebClient client = webClient.mutate()
                            .defaultHeader("Authorization", "Bearer " + authorization)
                            .build();

                    switch (path) {
                        case "albums":
                        case "images":
                            paramMap.put("timestamp", System.currentTimeMillis());
                            return client.get()
                                    .uri(url + path + "?" + PictureBedUtil.convertMapToUrlParams(paramMap))
                                    .retrieve()
                                    .bodyToMono(new ParameterizedTypeReference<LskyProResponseRecord>() {
                                    })
                                    .doOnError(error -> log.error("GET request failed", error))
                                    .onErrorResume(error -> Mono.empty());
                        case "upload":
                            BodyInserters.MultipartInserter fromMultipartData = BodyInserters.fromMultipartData((MultiValueMap<String, ?>) paramMap.get("file"));

                            if (!ObjectUtils.isEmpty(pictureBedStrategyId)) {
                                fromMultipartData.with("strategy_id", Integer.valueOf(pictureBedStrategyId));
                            }
                            if (!ObjectUtils.isEmpty(paramMap.get("album_id"))) {
                                fromMultipartData.with("album_id", Integer.valueOf(paramMap.get("album_id").toString()));
                            }
                            return client.post()
                                    .uri(url + path)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .body(fromMultipartData)
                                    .retrieve()
                                    .bodyToMono(new ParameterizedTypeReference<LskyProResponseRecord>() {
                                    })
                                    .doOnError(error -> log.error("POST request failed", error))
                                    .onErrorResume(error -> Mono.empty());
                        default:
                            if (path.startsWith("images/")) {
                                return client.delete()
                                        .uri(url + path)
                                        .retrieve()
                                        .bodyToMono(new ParameterizedTypeReference<LskyProResponseRecord>() {
                                        })
                                        .doOnError(error -> log.error("DELETE request failed", error))
                                        .onErrorResume(error -> Mono.empty());
                            } else {
                                return Mono.error(new IllegalArgumentException("Unsupported path: " + path));
                            }
                    }
                })
                .onErrorResume(error -> {
                    log.error("Configuration fetch failed", error);
                    return Mono.empty();
                });

    }

    public record LskyProResponseRecord(boolean status, String message, Map<String, Object> data) {
    }
}

