package site.muyin.picturebed.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 返回图床实例列表，并保证每条都有唯一的 pictureBedId。
     * <p>
     * 表单侧的自动编号依赖 FormKit 的 schema 变量，历史上出现过静默失效（Halo 2.23 起 $index 不再可用），
     * 且 $pictureBeds.length 取的是已保存的数组长度，一次会话内连加多条会拿到相同的值。
     * 这里做最终兜底：已有且不重复的 ID 原样保留，保证旧配置的实例 ID 不变；
     * 为空或重复的条目重新分配一个未被占用的最小非负整数。
     * <p>
     * 归一化写在 getter 里，是因为所有消费方（各图床服务实现和 PictureBedEndpoint）都已经通过它取值，
     * 不需要改任何调用点。该方法是幂等的，且 ReactiveSettingFetcher#fetch 只缓存 ConfigMap，
     * 每次调用都会重新反序列化出新实例，因此这里的就地修改不会被跨请求共享。
     */
    public List<PictureBed> getPictureBeds() {
        if (pictureBeds == null) {
            return null;
        }

        Set<String> usedIds = new HashSet<>();
        for (PictureBed pictureBed : pictureBeds) {
            String pictureBedId = pictureBed.getPictureBedId();
            // add 返回 false 表示该 ID 已被前面的条目占用，需要重新分配
            if (!StringUtils.hasText(pictureBedId) || !usedIds.add(pictureBedId)) {
                pictureBed.setPictureBedId(null);
            }
        }

        int candidate = 0;
        for (PictureBed pictureBed : pictureBeds) {
            if (StringUtils.hasText(pictureBed.getPictureBedId())) {
                continue;
            }
            while (!usedIds.add(String.valueOf(candidate))) {
                candidate++;
            }
            pictureBed.setPictureBedId(String.valueOf(candidate));
        }

        return pictureBeds;
    }

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
        private String pictureBedClientId;
        private String pictureBedClientSecret;
        private String pictureBedUploadChannel;
        private String pictureBedCdnUrl;
    }
}
