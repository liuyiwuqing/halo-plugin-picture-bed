package site.muyin.picturebed.service.Impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import run.halo.app.plugin.ReactiveSettingFetcher;
import site.muyin.picturebed.config.PictureBedConfig;
import site.muyin.picturebed.domain.CfImgBedDirectory;
import site.muyin.picturebed.domain.CfImgBedImage;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.service.CfImgBedService;
import site.muyin.picturebed.utils.PictureBedUtil;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.ResultsVO;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static site.muyin.picturebed.config.PictureBedConfig.GROUP;
import static site.muyin.picturebed.constant.CommonConstant.PictureBedType.CFIMGBED;

/**
 * @author: lywq
 * @date: 2026/08/05 10:00
 * @version: v1.0.0
 * @description: CloudFlare ImgBed 图床服务实现
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class CfImgBedServiceImpl implements CfImgBedService {

    private static final String DEFAULT_UPLOAD_CHANNEL = "telegram";

    private static final int UPLOAD_BUFFER_SIZE = 32 * 1024 * 1024;

    private final ReactiveSettingFetcher settingFetcher;

    /**
     * 列表、目录树和删除都是轻量 JSON 请求
     */
    private final WebClient apiClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                            .responseTimeout(Duration.ofSeconds(10))
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            ))
            .defaultHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
            .defaultHeader(HttpHeaders.PRAGMA, "no-cache")
            .build();

    /**
     * 上传要经 Cloudflare 转发到 R2、Telegram 等后端存储，耗时远超普通接口
     */
    private final WebClient uploadClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                            .responseTimeout(Duration.ofSeconds(120))
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            ))
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(UPLOAD_BUFFER_SIZE))
            .build();

    @Override
    public Mono<ResultsVO> uploadImage(CommonQuery query, MultiValueMap<String, ?> multipartData) {
        Object file = multipartData == null ? null : multipartData.getFirst("file");
        if (file == null) {
            return Mono.just(ResultsVO.failure("上传失败，缺少文件参数"));
        }
        return resolveConfig(query.getPictureBedId())
                .flatMap(config -> {
                    Map<String, Object> paramMap = new LinkedHashMap<>();
                    paramMap.put("uploadChannel", StringUtils.hasText(config.getPictureBedUploadChannel())
                            ? config.getPictureBedUploadChannel() : DEFAULT_UPLOAD_CHANNEL);
                    paramMap.put("returnFormat", "full");
                    if (StringUtils.hasText(query.getAlbumId())) {
                        paramMap.put("uploadFolder", query.getAlbumId());
                    }

                    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                    body.add("file", file);

                    return uploadClient.post()
                            .uri(buildUri(config, "upload", paramMap))
                            .header(HttpHeaders.AUTHORIZATION, bearer(config))
                            .body(BodyInserters.fromMultipartData(body))
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, CfImgBedServiceImpl::toReadableError)
                            .bodyToMono(new ParameterizedTypeReference<List<CfImgBedUploadRecord>>() {
                            })
                            .map(results -> {
                                if (ObjectUtils.isEmpty(results)) {
                                    return ResultsVO.failure("上传失败，CloudFlare ImgBed 未返回结果");
                                }
                                return ResultsVO.success("上传成功", results.get(0));
                            });
                })
                .doOnError(error -> log.error("CloudFlare ImgBed 上传图片失败", error))
                .onErrorResume(error -> Mono.just(ResultsVO.failure(readableMessage(error,
                        "上传失败，请检查图床配置或网络连接"))));
    }

    @Override
    public Mono<List<CfImgBedDirectory>> getAlbumList(CommonQuery query) {
        return resolveConfig(query.getPictureBedId())
                .flatMap(config -> apiClient.get()
                        .uri(buildUri(config, "api/directoryTree", Map.of()))
                        .header(HttpHeaders.AUTHORIZATION, bearer(config))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, CfImgBedServiceImpl::toReadableError)
                        .bodyToMono(CfImgBedDirectoryTreeRecord.class))
                .map(response -> {
                    List<CfImgBedDirectory> directories = new ArrayList<>();
                    flattenDirectoryTree(response.tree(), directories);
                    return directories;
                })
                .doOnError(error -> log.error("CloudFlare ImgBed 获取目录列表失败", error))
                .onErrorResume(error -> Mono.just(List.of()));
    }

    @Override
    public Mono<PageResult<CfImgBedImage>> getImageList(CommonQuery query) {
        int page = Math.max(1, query.getPage());
        int size = Math.max(1, query.getSize());
        return resolveConfig(query.getPictureBedId())
                .flatMap(config -> {
                    Map<String, Object> paramMap = new LinkedHashMap<>();
                    paramMap.put("start", (page - 1) * size);
                    paramMap.put("count", size);
                    // 上游的 totalCount 在过滤子目录文件之前统计，只有 recursive=true 时才和分页集合一致
                    paramMap.put("recursive", true);
                    // 这里不能加 fileType=image：上游按 metadata.FileType 前缀过滤，而该字段存的是上传时
                    // 客户端给的 Content-Type。实测未带正确类型上传的图片会存成 application/octet-stream，
                    // 加了这个过滤会把它们连同总数一起排除掉。图片与否改由 ImageVO.mediaType 按扩展名判定。
                    if (StringUtils.hasText(query.getAlbumId())) {
                        paramMap.put("dir", query.getAlbumId());
                    }
                    if (StringUtils.hasText(query.getKeyword())) {
                        paramMap.put("search", query.getKeyword());
                    }

                    return apiClient.get()
                            .uri(buildUri(config, "api/manage/list", paramMap))
                            .header(HttpHeaders.AUTHORIZATION, bearer(config))
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, CfImgBedServiceImpl::toReadableError)
                            .bodyToMono(CfImgBedListRecord.class)
                            .map(response -> {
                                List<CfImgBedImage> imageList = response.files() == null
                                        ? List.<CfImgBedImage>of()
                                        : PictureBedUtil.convertObjectToList(response.files(), CfImgBedImage.class);
                                // 列表接口不返回访问链接，需要按配置自行拼接
                                imageList.forEach(image ->
                                        image.setPublicUrl(buildPublicUrl(config, image.getName())));
                                int totalCount = response.totalCount() == null
                                        ? imageList.size() : response.totalCount();
                                int totalPages = (int) Math.ceil((double) totalCount / size);
                                return new PageResult<>(page, size, totalCount, totalPages, imageList);
                            });
                })
                .doOnError(error -> log.error("CloudFlare ImgBed 获取图片列表失败", error))
                .onErrorResume(error -> Mono.empty());
    }

    @Override
    public Mono<Boolean> deleteImage(CommonQuery query) {
        if (ObjectUtils.isEmpty(query.getImageId())) {
            return Mono.just(false);
        }
        return resolveConfig(query.getPictureBedId())
                .flatMap(config -> apiClient.delete()
                        .uri(URI.create(normalizeBaseUrl(config.getPictureBedUrl())
                                + "api/manage/delete/" + encodePath(query.getImageId())))
                        .header(HttpHeaders.AUTHORIZATION, bearer(config))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, CfImgBedServiceImpl::toReadableError)
                        .bodyToMono(CfImgBedDeleteRecord.class))
                .map(response -> Boolean.TRUE.equals(response.success()))
                .doOnError(error -> log.error("CloudFlare ImgBed 删除图片失败", error))
                .onErrorResume(error -> Mono.just(false));
    }

    private Mono<PictureBedConfig.PictureBed> resolveConfig(String pictureBedId) {
        return settingFetcher.fetch(GROUP, PictureBedConfig.class)
                .flatMap(pictureBedConfig -> Optional
                        .ofNullable(pictureBedConfig.getPictureBeds()).orElse(List.of()).stream()
                        .filter(p -> CFIMGBED.equals(p.getPictureBedType())
                                && Objects.equals(p.getPictureBedId(), pictureBedId))
                        .findFirst()
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new IllegalArgumentException(
                                "未找到 ID 为 " + pictureBedId + " 的 CloudFlare ImgBed 配置"))));
    }

    /**
     * 参数已经手工编码完毕，必须走 URI 重载。WebClient 的 String 重载会把整串当作 URI 模板再编码一次，
     * 导致 %2F 变成 %252F
     */
    private static URI buildUri(PictureBedConfig.PictureBed config, String path, Map<String, Object> paramMap) {
        String url = normalizeBaseUrl(config.getPictureBedUrl()) + path;
        if (!ObjectUtils.isEmpty(paramMap)) {
            url = url + "?" + PictureBedUtil.convertMapToUrlParams(paramMap);
        }
        return URI.create(url);
    }

    private static String buildPublicUrl(PictureBedConfig.PictureBed config, String fileId) {
        String base = StringUtils.hasText(config.getPictureBedCdnUrl())
                ? normalizeBaseUrl(config.getPictureBedCdnUrl())
                : normalizeBaseUrl(config.getPictureBedUrl());
        return base + "file/" + encodePath(fileId);
    }

    /**
     * 文件 ID 可能带目录前缀，逐段编码以保留路径分隔符
     */
    private static String encodePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        return Arrays.stream(path.split("/", -1))
                .map(segment -> URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("/"));
    }

    private static String normalizeBaseUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String trimmed = url.trim();
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }

    private static String bearer(PictureBedConfig.PictureBed config) {
        return "Bearer " + config.getPictureBedToken();
    }

    private static void flattenDirectoryTree(CfImgBedTreeNode node, List<CfImgBedDirectory> collector) {
        if (node == null) {
            return;
        }
        // 根节点 path 为空串，不作为可选目录展示
        if (StringUtils.hasText(node.path())) {
            CfImgBedDirectory directory = new CfImgBedDirectory();
            directory.setPath(node.path());
            directory.setName(node.path());
            collector.add(directory);
        }
        if (node.children() == null) {
            return;
        }
        node.children().forEach(child -> flattenDirectoryTree(child, collector));
    }

    /**
     * CloudFlare ImgBed 失败时返回纯文本，如 You need to login，这里保留原文以便用户排查
     */
    private static Mono<? extends Throwable> toReadableError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> new IllegalStateException("CloudFlare ImgBed 返回 " + response.statusCode().value()
                        + (StringUtils.hasText(body) ? "：" + body.trim() : "")));
    }

    private static String readableMessage(Throwable error, String fallback) {
        return StringUtils.hasText(error.getMessage()) ? error.getMessage() : fallback;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CfImgBedListRecord(Object files, List<String> directories, Integer totalCount,
                                     Integer directFileCount, Integer returnedCount) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CfImgBedDirectoryTreeRecord(CfImgBedTreeNode tree) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CfImgBedTreeNode(String name, String path, List<CfImgBedTreeNode> children) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CfImgBedUploadRecord(String src, String publicUrl) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CfImgBedDeleteRecord(Boolean success, String fileId, String error) {
    }
}
