package site.muyin.picturebed.utils;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import site.muyin.picturebed.annotation.GroupName;
import site.muyin.picturebed.config.PictureBedConfig;

import java.util.Map;
import java.util.Optional;

/**
 * @author: lywq
 * @date: 2024/05/21 11:57
 * @version: v1.0.0
 * @description:
 **/
@Component
@RequiredArgsConstructor
public class PluginCacheManager {
    private final ExtensionClient client;

    private static final Cache<String, String> configCache =
            CacheUtil.newTimedCache(24 * DateUnit.HOUR.getMillis());

    public void put(String key, String value) {
        configCache.put(key, value);
    }

    public void put(String key, String value, long timeout) {
        configCache.put(key, value, timeout);
    }

    public String get(String key) {
        return configCache.get(key);
    }

    public void remove(String key) {
        configCache.remove(key);
    }

    public <T> T getObject(String key, Class<T> clazz) {
        String value = configCache.get(key);
        return toObject(value, clazz);
    }

    /**
     * 获取配置
     *
     * @return: site.lywq.linkssubmit.config.LinksSubmitConfig
     * @author: lywq
     * @date: 2024/03/05 15:05
     **/
    public <T> T getConfig(Class<T> clazz) {
        String groupName = clazz.getAnnotation(GroupName.class).value();
        if (ObjectUtil.isNull(groupName)) {
            throw new RuntimeException("配置分组不能为空");
        }
        String configMapStr = get(PictureBedConfig.CONFIG_MAP_NAME);

        if (ObjectUtil.isNull(configMapStr)) {
            Optional<ConfigMap> configMapOptional =
                    client.fetch(ConfigMap.class, PictureBedConfig.CONFIG_MAP_NAME);
            if (configMapOptional.isPresent()) {
                ConfigMap configMap = configMapOptional.get();
                Map<String, String> configMapData = configMap.getData();
                put(PictureBedConfig.CONFIG_MAP_NAME, String.valueOf(configMapData));
                return getConfig(clazz);
            }
        } else {
            Map<String, String> configMapData = JSONUtil.toBean(configMapStr, Map.class);
            String groupConfig = configMapData.get(groupName);
            return JSONUtil.toBean(groupConfig, clazz);
        }
        return null;
    }

    /**
     * 将字符串转换为对象
     *
     * @param value:
     * @param clazz:
     * @return: T
     * @author: lywq
     * @date: 2024/03/05 15:04
     **/
    public <T> T toObject(String value, Class<T> clazz) {
        if (ObjectUtil.isNotNull(value)) {
            // 初始化ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(value, clazz);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}

