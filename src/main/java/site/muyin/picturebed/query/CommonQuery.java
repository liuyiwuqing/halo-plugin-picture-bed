package site.muyin.picturebed.query;

import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.extension.router.SortableRequest;

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

    public String getOrder() {
        return queryParams.getFirst("order");
    }
}
