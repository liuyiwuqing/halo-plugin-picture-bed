package site.muyin.picturebed.domain;

import lombok.Data;

/**
 * @author: lywq
 * @date: 2024/04/29 10:48
 * @version: v1.0.0
 * @description: 相册
 **/
@Data
public class LskyProAlbum {
    private Integer id;
    private String name;
    private String intro;
    private Integer image_num;
}
