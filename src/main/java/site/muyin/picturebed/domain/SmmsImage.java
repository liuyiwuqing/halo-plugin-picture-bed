package site.muyin.picturebed.domain;

import lombok.Data;

/**
 * @author: lywq
 * @date: 2024/05/21 16:08
 * @version: v1.0.0
 * @description:
 **/
@Data
public class SmmsImage {
    private Integer width;
    private Integer height;
    private String filename;
    private String storename;
    private Integer size;
    private String path;
    private String hash;
    private String created_at;
    private String url;
    private String delete;
    private String page;
}
