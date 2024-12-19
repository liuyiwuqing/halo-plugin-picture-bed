package site.muyin.picturebed.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author: lywq
 * @date: 2024/05/21 11:50
 * @version: v1.0.0
 * @description:
 **/
@Data
@Accessors(chain = true)
public class PictureBedConfig {

    public static final String GROUP = "basic";
    public static final String CONFIG_MAP_NAME = "picture-bed-config";

    private Map<String, Object> slots;
    private List<PictureBed> pictureBeds;

    @Data
    @Accessors(chain = true)
    public static class PictureBed {
        private String pictureBedId;
        private String pictureBedName;
        private Boolean pictureBedEnabled;
        private String pictureBedType;
        private String pictureBedUrl;
        private String pictureBedToken;
        private String pictureBedStrategyId;
    }
}
