package site.muyin.picturebed;

import lombok.AllArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.plugin.ReactiveSettingFetcher;
import site.muyin.picturebed.config.PictureBedConfig;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.service.PictureBedService;
import site.muyin.picturebed.vo.AlbumVO;
import site.muyin.picturebed.vo.ImageVO;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.PictureBedVO;
import site.muyin.picturebed.vo.ResultsVO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static site.muyin.picturebed.config.PictureBedConfig.GROUP;

/**
 * @author: lywq
 * @date: 2024/04/14 19:44
 * @version: v1.0.0
 * @description:
 **/
@Component
@AllArgsConstructor
public class PictureBedEndpoint implements CustomEndpoint {

    private final PictureBedService pictureBedService;

    private final ReactiveSettingFetcher settingFetcher;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "picturebed.muyin.site/v1alpha1/PictureBed";
        return SpringdocRouteBuilder.route()
                .GET("albums", this::getAlbumList, builder -> {
                    builder.operationId("albums")
                            .description("albums")
                            .tag(tag)
                            .response(responseBuilder().implementationArray(AlbumVO.class));
                    CommonQuery.buildParameters(builder);
                })
                .GET("images", this::getImageList,
                        builder -> {
                            builder.operationId("images")
                                    .description("images")
                                    .tag(tag)
                                    .response(responseBuilder().implementation(PageResult.class).content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE).schema(schemaBuilder().implementation(ImageVO.class))));
                            CommonQuery.buildParameters(builder);
                        })
                .GET("deleteImage", this::deleteImage,
                        builder -> {
                            builder.operationId("deleteImage")
                                    .description("deleteImage")
                                    .tag(tag)
                                    .response(responseBuilder().implementation(Boolean.class));
                            CommonQuery.buildParameters(builder);
                        })
                .POST("uploadImage", contentType(MediaType.MULTIPART_FORM_DATA), this::uploadImage,
                        builder -> {
                            builder.operationId("uploadImage")
                                    .description("uploadImage")
                                    .tag(tag)
                                    .requestBody(requestBodyBuilder()
                                            .required(true)
                                            .content(contentBuilder()
                                                    .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                                                    .schema(schemaBuilder())
                                            ))
                                    .response(responseBuilder().implementation(ResultsVO.class));
                            CommonQuery.buildParameters(builder);
                        }
                )
                .GET("pictureBeds", this::getPictureBeds,
                        builder -> {
                            builder.operationId("pictureBeds")
                                    .description("pictureBeds")
                                    .tag(tag)
                                    .response(responseBuilder().implementationArray(PictureBedVO.class));
                        })
                .build();
    }

    private Mono<ServerResponse> uploadImage(ServerRequest serverRequest) {
        CommonQuery query = new CommonQuery(serverRequest.exchange());
        Mono<MultiValueMap<String, Part>> multiValueMapMono = serverRequest.multipartData();
        return multiValueMapMono.flatMap(multiValueMap -> pictureBedService.uploadImage(query, multiValueMap)
                .switchIfEmpty(Mono.just(ResultsVO.failure("上传失败，请检查图床配置或网络连接")))
                .flatMap(resultsVO -> {
                    if (resultsVO.getCode() == 200) {
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(resultsVO);
                    }
                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(resultsVO);
                }));
    }

    private Mono<ServerResponse> getAlbumList(ServerRequest serverRequest) {
        CommonQuery query = new CommonQuery(serverRequest.exchange());
        return pictureBedService.getAlbumList(query)
                .defaultIfEmpty(List.of())
                .flatMap(albumVOList -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(albumVOList));
    }

    private Mono<ServerResponse> getImageList(ServerRequest serverRequest) {
        CommonQuery query = new CommonQuery(serverRequest.exchange());
        PageResult<ImageVO> emptyResult = new PageResult<>(query.getPage(), query.getSize(), 0, 0, List.of());
        return pictureBedService.getImageList(query)
                .defaultIfEmpty(emptyResult)
                .flatMap(pageResult -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(pageResult));
    }

    private Mono<ServerResponse> deleteImage(ServerRequest serverRequest) {
        CommonQuery query = new CommonQuery(serverRequest.exchange());
        return pictureBedService.deleteImage(query).defaultIfEmpty(false).flatMap(result -> {
            if (result) {
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(result);
            } else {
                return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON).bodyValue(result);
            }
        });
    }

    private Mono<ServerResponse> getPictureBeds(ServerRequest serverRequest) {
        return settingFetcher.fetch(GROUP, PictureBedConfig.class)
                .defaultIfEmpty(new PictureBedConfig())
                .map(config -> Optional.ofNullable(config.getPictureBeds()).orElse(List.of()).stream()
                        .filter(pictureBed -> pictureBed.getPictureBedType() != null)
                        .map(this::convertToPictureBedVO)
                        .collect(Collectors.toList()))
                .flatMap(pictureBeds -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(pictureBeds));
    }

    private PictureBedVO convertToPictureBedVO(PictureBedConfig.PictureBed pictureBed) {
        PictureBedVO pictureBedVO = new PictureBedVO();
        pictureBedVO.setKey(pictureBed.getPictureBedType() + "_" + pictureBed.getPictureBedId())
                .setName(pictureBed.getPictureBedName())
                .setType(pictureBed.getPictureBedType())
                .setEnabled(Boolean.TRUE.equals(pictureBed.getPictureBedEnabled()));
        return pictureBedVO;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("picturebed.muyin.site/v1alpha1");
    }
}
