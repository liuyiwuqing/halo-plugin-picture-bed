package site.muyin.picturebed.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: lywq
 * @date: 2024/05/20 15:55
 * @version: v1.0.0
 * @description:
 **/
@Data
@Accessors(chain = true)
public class ImageVO {
    private String id;
    private String url;
    private String name;
    private String mediaType;
    private Float size;
    private Integer width;
    private Integer height;
}
