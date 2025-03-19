package site.muyin.picturebed.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图床工具类
 *
 *@author lywq
 *@date 2025/03/19 11:31
 *@version v1.0.0
 **/
public class PictureBedUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知字段

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 将 Map 转换为 URL 参数
     * @param params:
     * @return java.lang.String
     * @author lywq (https://lywq.muyin.site)
     * @date 2025/03/19 11:35
     **/
    public static String convertMapToUrlParams(Map<String, Object> params) {
        return params.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null) // 过滤掉值为null的键
                .map(entry -> {
                    String encodedKey = encodeValue(entry.getKey());
                    String encodedValue = encodeValue(entry.getValue().toString());
                    return encodedKey + "=" + encodedValue;
                })
                .collect(Collectors.joining("&"));
    }

    /**
     * 将任意对象转换为指定类型的列表
     * @param data  原始数据对象（Map/List等）
     * @param clazz 目标类型
     * @return 转换后的对象列表
     */
    public static <T> List<T> convertObjectToList(Object data, Class<T> clazz) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            return parseList(jsonData, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转换列表失败", e);
        }
    }

    /**
     * 将任意对象转换为指定类型的对象
     * @param data  原始数据对象（Map/List等）
     * @param clazz 目标类型
     * @return 转换后的对象
     */
    public static <T> T convertObject(Object data, Class<T> clazz) {
        try {
            return objectMapper.convertValue(data, clazz);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("对象转换失败", e);
        }
    }

    /**
     * 将JSON字符串转换为对象列表
     * @param json      JSON字符串
     * @param clazz     目标对象的类型
     * @return 对象列表
     * @throws RuntimeException 解析失败时抛出异常
     */
    public static <T> List<T> parseList(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
            );
        } catch (Exception e) {
            throw new RuntimeException("JSON解析失败: " + e.getMessage(), e);
        }
    }

    private static String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
