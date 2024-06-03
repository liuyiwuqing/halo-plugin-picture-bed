package site.muyin.picturebed.domain;

import lombok.Data;

/**
 * @author: lywq
 * @date: 2024/05/29 09:41
 * @version: v1.0.0
 * @description:
 **/
@Data
public class ImgtpImage {
    private String id;
    private String strategy;
    private String path;
    private String name;
    private String alias_name;
    private String pathname;
    private Float size;
    private String mime;
    private String sha1;
    private String md5;
    private String ip;
    private Integer suspicious;
    private String upload_time;
    private String upload_date;
    private String url;
}
