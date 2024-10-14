package site.muyin.picturebed.query;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.SortableRequest;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;

/**
 * @author: lywq
 * @date: 2024/04/15 23:50
 * @version: v1.0.0
 * @description:
 **/
public class CommonQuery extends SortableRequest {

    public final MultiValueMap<String, String> queryParams;

    public CommonQuery(ServerWebExchange exchange) {
        super(exchange);
        this.queryParams = exchange.getRequest().getQueryParams();
    }

    public String getPictureBedId() {
        return queryParams.getFirst("pictureBedId");
    }

    public String getType() {
        return queryParams.getFirst("type");
    }

    public String getAlbumId() {
        return queryParams.getFirst("albumId");
    }

    public String getImageId() {
        return queryParams.getFirst("imageId");
    }

    public String getKeyword() {
        return queryParams.getFirst("keyword");
    }

    public Integer getPage() {
        String page = queryParams.getFirst("page");
        if (page == null) {
            return 1;
        }
        return Integer.parseInt(page);
    }

    public Integer getSize() {
        String size = queryParams.getFirst("size");
        if (size == null) {
            return 10;
        }
        return Integer.parseInt(size);
    }

    public static void buildParameters(Builder builder) {
        IListRequest.buildParameters(builder);
        builder.parameter(sortParameter())
                .parameter(parameterBuilder()
                        .in(ParameterIn.QUERY)
                        .name("pictureBedId")
                        .description("pictureBedId")
                        .implementation(String.class)
                        .required(false))
                .parameter(parameterBuilder()
                        .in(ParameterIn.QUERY)
                        .name("type")
                        .description("type")
                        .implementation(String.class)
                        .required(false))
                .parameter(parameterBuilder()
                        .in(ParameterIn.QUERY)
                        .name("albumId")
                        .description("albumId")
                        .implementation(String.class)
                        .required(false))
                .parameter(parameterBuilder()
                        .in(ParameterIn.QUERY)
                        .name("imageId")
                        .description("imageId")
                        .implementation(String.class)
                        .required(false))
                .parameter(parameterBuilder()
                        .in(ParameterIn.QUERY)
                        .name("keyword")
                        .implementation(String.class)
                        .description("keyword")
                        .required(false))
        ;
    }
}
