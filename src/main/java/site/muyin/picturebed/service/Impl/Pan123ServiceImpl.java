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
import site.muyin.picturebed.domain.Pan123Image;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.service.Pan123Service;
import site.muyin.picturebed.utils.PictureBedUtil;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.ResultsVO;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static site.muyin.picturebed.config.PictureBedConfig.GROUP;
import static site.muyin.picturebed.constant.CommonConstant.PictureBedType.Pan123;

/**
 * @author: lywq
 * @date: 2024/04/29 11:05
 * @version: v1.0.0
 * @description:
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class Pan123ServiceImpl implements Pan123Service {
    private static class TokenCache {
        volatile String accessToken;

        volatile long effectiveExpiresAtMillis;

        boolean isValid(long currentTimeMillis) {
            return accessToken != null && currentTimeMillis < effectiveExpiresAtMillis;
        }
    }

    private final ReactiveSettingFetcher settingFetcher;
    private final Map<String, TokenCache> tokenCaches = new ConcurrentHashMap<>();

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
            .defaultHeader("Platform", "open_platform")
            .build();

    private Mono<String> getAccessToken(String clientId, String clientSecret) {
        if (ObjectUtils.isEmpty(clientId) || ObjectUtils.isEmpty(clientSecret)) {
            return Mono.error(new IllegalArgumentException("123盘图床缺少 client_id 或 client_secret 配置"));
        }

        TokenCache tokenCache = tokenCaches.computeIfAbsent(clientId, key -> new TokenCache());
        long currentTimeMillis = System.currentTimeMillis();
        if (tokenCache.isValid(currentTimeMillis)) {
            return Mono.just(tokenCache.accessToken);
        }

        return webClient.post()
                .uri("https://open-api.123pan.com/api/v1/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(String.format("{\n\t\"clientID\": \"%s\",\n\t\"clientSecret\": \"%s\"\n}", clientId,
                        clientSecret))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .flatMap(response -> {
                    if (response != null && response.get("code") != null && (int) response.get("code") == 0) {
                        log.info("Access token fetched successfully");
                        log.info("Access token response: {}", response);
                        Map<String, Object> data = (Map<String, Object>) response.get("data");
                        if (data != null && data.containsKey("accessToken") && data.containsKey("expiredAt")) {
                            String newToken = Optional.ofNullable(data.get("accessToken"))
                                    .map(Object::toString)
                                    .filter(token -> !token.isBlank())
                                    .orElseThrow(() -> {
                                        tokenCache.accessToken = null;
                                        tokenCache.effectiveExpiresAtMillis = 0;
                                        return new RuntimeException("Empty access token");
                                    });

                            try {
                                OffsetDateTime expiredDateTime = OffsetDateTime.parse(
                                        data.get("expiredAt").toString(),
                                        DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                                long currentTime = System.currentTimeMillis();
                                long effectiveExpiresAtMillis = currentTime
                                                                + Math.max((expiredDateTime.toInstant().toEpochMilli() - currentTime) * 9 / 10, 0);
                                tokenCache.accessToken = newToken;
                                tokenCache.effectiveExpiresAtMillis = effectiveExpiresAtMillis;
                                log.info("Access token updated [Expires at {}]", expiredDateTime);
                                return Mono.just(newToken);
                            } catch (DateTimeException e) {
                                log.error("Invalid date format: {}", data.get("expiredAt"), e);
                                tokenCache.accessToken = null;
                                tokenCache.effectiveExpiresAtMillis = 0;
                                return Mono.error(new RuntimeException("Invalid expiration date format"));
                            }
                        }
                    }
                    tokenCache.accessToken = null;
                    tokenCache.effectiveExpiresAtMillis = 0;
                    log.warn("Invalid token response: {}", response);
                    return Mono.error(new RuntimeException("Invalid token response structure"));
                });
    }

    @Override
    public Mono<ResultsVO> uploadImage(CommonQuery query, MultiValueMap<String, ?> multipartData) {
        Map<String, Object> paramMap = new HashMap(4);
        paramMap.put("parentFileID", query.getAlbumId());
        paramMap.put("file", multipartData);
        paramMap.put("filename", query.getKeyword());
        paramMap.put("size", "文件大小");
        paramMap.put("etag", "文件md5值");
        paramMap.put("type", 1);

        return req(query.getPictureBedId(), "upload", paramMap)
                .map(response -> {
                    if (response.status) {
                        return ResultsVO.success(response.message, response.data);
                    }
                    return ResultsVO.failure(response.message);
                });
    }

    @Override
    public Mono<PageResult<Pan123Image>> getImageList(CommonQuery query) {
        Map<String, Object> paramMap = new HashMap<String, Object>(4);
        paramMap.put("parentFileId", query.getAlbumId());
        paramMap.put("lastFileId", query.getKeyword());
        paramMap.put("limit", query.getSize());
        paramMap.put("type", 1);

        return req(query.getPictureBedId(), "images", paramMap)
                .flatMap(response -> {
                    if (response != null && !"ok".equals(response.message)) {
                        log.error("Failed to get image list: {}", response.message);
                        return Mono.error(new RuntimeException(response.message));
                    }
                    // lastFileId
                    Map<String, Object> data = response.data;
                    var imageList = Optional.ofNullable(data.get("fileList"))
                            .map(d -> PictureBedUtil.convertObjectToList(d, Pan123Image.class))
                            .orElse(Collections.emptyList());
                    if (!Objects.equals(String.valueOf(data.get("lastFileId")), "-1")) {
                        var nextImageRep = new Pan123Image();
                        nextImageRep.setFileId(data.get("lastFileId").toString());
                        nextImageRep.setFilename("lastFileId");
                        nextImageRep.setType(2);
                        nextImageRep.setExtension("");
                        imageList.add(nextImageRep);
                    }
                    return Mono.just(new PageResult<>(1, query.getSize(), 0, 0, imageList));
                })
                .onErrorResume(e -> {
                    log.error("Error occurred while fetching image list: {}", e.getMessage());
                    return Mono.just(new PageResult<>(1, 10, 0, 0, Collections.emptyList()));
                });
    }

    @Override
    public Mono<Boolean> deleteImage(CommonQuery query) {
        Map<String, Object> paramMap = new HashMap(1);
        // 将 imageId 包装成数组添加到 paramMap 中
        paramMap.put("fileIDs", new String[] { query.getImageId() });
        // "{\"fileIDs\": [\"yk6baz03t0l000d7w33fbyt8704m2bohDIYPAIDOBIY0DcxvDwFO\"]}"
        return req(query.getPictureBedId(), "delete", paramMap)
                .map(response -> {
                    return response.status;
                });
    }

    private Mono<Pan123ResponseRecord> req(String pictureBedId, String path, Map<String, Object> paramMap) {
        if (path == null) {
            return Mono.error(new IllegalArgumentException("Path cannot be null"));
        }

        return settingFetcher.fetch(GROUP, PictureBedConfig.class)
                .flatMap(pictureBedConfig -> {
                    PictureBedConfig.PictureBed config = pictureBedConfig.getPictureBeds().stream().filter(
                            p -> p.getPictureBedType().equals(Pan123) && p.getPictureBedId().equals(pictureBedId))
                            .findFirst().orElseThrow();
                    String url = config.getPictureBedUrl();
                    String clientId = config.getPictureBedClientId();
                    String clientSecret = config.getPictureBedClientSecret();
                    // 获取access_token
                    return getAccessToken(clientId, clientSecret).flatMap(accessToken -> {
                        WebClient client = webClient.mutate()
                                .defaultHeader("Authorization", "Bearer " + accessToken)
                                .build();

                        switch (path) {
                            case "albums":
                            case "images":
                                return client.post()
                                        .uri("https://open-api.123pan.com/api/v1/oss/file/list")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(paramMap)
                                        .retrieve()
                                        .bodyToMono(new ParameterizedTypeReference<Pan123ResponseRecord>() {
                                        })
                                        .doOnError(error -> log.error(
                                                "POST request to https://open-api.123pan.com/api/v1/oss/file/list failed",
                                                error))
                                        .onErrorResume(error -> Mono.empty());
                            case "upload":
                                BodyInserters.MultipartInserter fromMultipartData = BodyInserters
                                        .fromMultipartData((MultiValueMap<String, ?>) paramMap.get("file"));
                                return client.post()
                                        .uri(url + path)
                                        .contentType(MediaType.MULTIPART_FORM_DATA)
                                        .body(fromMultipartData)
                                        .retrieve()
                                        .bodyToMono(new ParameterizedTypeReference<Pan123ResponseRecord>() {
                                        })
                                        .doOnError(error -> log.error("POST request failed", error))
                                        .onErrorResume(error -> Mono.empty());
                            default:
                                if (path.startsWith("delete")) {
                                    return client.post()
                                            .uri("https://open-api.123pan.com/api/v1/oss/file/delete")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .bodyValue(paramMap)
                                            .retrieve()
                                            .bodyToMono(new ParameterizedTypeReference<Pan123ResponseRecord>() {
                                            })
                                            .doOnError(error -> log.error("DELETE 123pan image request failed", error))
                                            .onErrorResume(error -> Mono.empty());
                                } else {
                                    return Mono.error(new IllegalArgumentException("Unsupported path: " + path));
                                }
                        }
                    });
                })
                .onErrorResume(error -> {
                    log.error("Configuration fetch failed", error);
                    return Mono.empty();
                });

    }

    public record Pan123ResponseRecord(boolean status, String message, Map<String, Object> data) {
    }
}
