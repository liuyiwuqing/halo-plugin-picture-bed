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
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.plugin.ReactiveSettingFetcher;
import site.muyin.picturebed.config.PictureBedConfig;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.service.PictureBedService;
import site.muyin.picturebed.vo.AlbumVO;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.PictureBedVO;
import site.muyin.picturebed.vo.ResultsVO;

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
                                    .response(responseBuilder().implementation(PageResult.class));
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
        return multiValueMapMono.flatMap(multiValueMap -> {
            return pictureBedService.uploadImage(query, multiValueMap).flatMap(resultsVO -> {
                if (resultsVO.getCode() == 200) {
                    return ServerResponse.ok().bodyValue(resultsVO.getMsg());
                } else {
                    return Mono.error(new ServerWebInputException(resultsVO.getMsg()));
                }
            });
        });
    }

    private Mono<ServerResponse> getAlbumList(ServerRequest serverRequest) {
        CommonQuery query = new CommonQuery(serverRequest.exchange());
        return pictureBedService.getAlbumList(query).flatMap(albumVOList -> {
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).bodyValue(albumVOList);
        });
    }

    private Mono<ServerResponse> getImageList(ServerRequest serverRequest) {
        CommonQuery query = new CommonQuery(serverRequest.exchange());
        return pictureBedService.getImageList(query).flatMap(pageResult -> {
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).bodyValue(pageResult);
        });
    }

    private Mono<ServerResponse> deleteImage(ServerRequest serverRequest) {
        CommonQuery query = new CommonQuery(serverRequest.exchange());
        return pictureBedService.deleteImage(query).flatMap(result -> {
            if (result) {
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).bodyValue(result);
            } else {
                return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).bodyValue(result);
            }
        });
    }

    private Mono<ServerResponse> getPictureBeds(ServerRequest serverRequest) {
        return settingFetcher.fetch(GROUP, PictureBedConfig.class)
                .map(config -> config.getPictureBeds().stream()
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
                .setEnabled(pictureBed.getPictureBedEnabled());
        return pictureBedVO;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("picturebed.muyin.site/v1alpha1");
    }
}
