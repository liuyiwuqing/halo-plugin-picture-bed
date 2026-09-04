package site.muyin.picturebed.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: lywq
 * @date: 2024/10/13 14:59
 * @version: v1.0.0
 **/
@Data
@Accessors(chain = true)
public class PictureBedVO {
    private String key;
    private String name;
    private String type;
    private Boolean enabled;
    /**
     * 是否作为编辑器粘贴、拖拽上传的目标。全部实例中最多只有一个为 true
     */
    private Boolean editorUpload;
}
