package site.muyin.picturebed.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: lywq
 * @date: 2024/05/20 15:58
 * @version: v1.0.0
 * @description:
 **/
@Data
@Accessors(chain = true)
public class AlbumVO {
    private String id;
    private String name;
    private String description;
}
