package site.muyin.picturebed.domain;

import lombok.Data;

/**
 * @author: lywq
 * @date: 2024/04/29 10:55
 * @version: v1.0.0
 * @description: 图片
 **/
@Data
public class LskyProImage {
    private String key;
    private String name;
    private String origin_name;
    private String pathname;
    private Float size;
    private Integer width;
    private Integer height;
    private String intro;
    private String md5;
    private String sha1;
    private String mimetype;
    private String extension;
    private String humanDate;
    private String date;
    private ImageLink links;

    @Data
    public class ImageLink {
        private String url;
        private String thumbnail_url;
        private String html;
        private String bbcode;
        private String markdown;
        private String markdown_with_link;
    }
}
