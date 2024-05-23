package site.muyin.picturebed;

import cn.hutool.json.JSONObject;
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
import site.muyin.picturebed.config.PictureBedConfig;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.service.PictureBedService;
import site.muyin.picturebed.utils.PluginCacheManager;
import site.muyin.picturebed.vo.ResultsVO;

import java.util.ArrayList;
import java.util.List;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

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

    private final PluginCacheManager pluginCacheManager;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "picturebed.muyin.site/v1alpha1/PictureBed";
        return SpringdocRouteBuilder.route()
                .GET("albums", this::getAlbumList,
                        builder -> builder.operationId("albums")
                                .description("albums").tag(tag))
                .GET("images", this::getImageList,
                        builder -> builder.operationId("images")
                                .description("images").tag(tag))
                .GET("deleteImage", this::deleteImage,
                        builder -> builder.operationId("deleteImage")
                                .description("deleteImage").tag(tag))
                .POST("uploadImage", contentType(MediaType.MULTIPART_FORM_DATA),
                        this::uploadImage, builder -> builder.operationId("uploadImage")
                                .description("uploadImage")
                                .tag(tag)
                                .requestBody(requestBodyBuilder()
                                        .required(true)
                                        .content(contentBuilder()
                                                .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                                                .schema(schemaBuilder())
                                        ))
                                .response(responseBuilder().implementation(ResultsVO.class))
                )
                .GET("pictureBeds", this::getPictureBeds,
                        builder -> builder.operationId("pictureBeds")
                                .description("pictureBeds").tag(tag))
                .build();
    }

    private Mono<ServerResponse> uploadImage(ServerRequest serverRequest) {
        CommonQuery query = new CommonQuery(serverRequest.exchange());
        Mono<MultiValueMap<String, Part>> multiValueMapMono = serverRequest.multipartData();
        return multiValueMapMono.flatMap(multiValueMap -> {
            return pictureBedService.uploadImage(query, multiValueMap).flatMap(resultsVO -> {
                if (resultsVO.getCode() == 200) {
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).bodyValue(resultsVO);
                } else {
                    return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8).bodyValue(resultsVO);
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
        PictureBedConfig config = pluginCacheManager.getConfig(PictureBedConfig.class);
        List<JSONObject> pictureBeds = new ArrayList<>();
        config.getPictureBeds().forEach(pictureBed -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", pictureBed.getPictureBedType());
            jsonObject.put("enabled", pictureBed.getPictureBedEnabled());
            pictureBeds.add(jsonObject);
        });
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).bodyValue(pictureBeds);
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("picturebed.muyin.site/v1alpha1");
    }
}
