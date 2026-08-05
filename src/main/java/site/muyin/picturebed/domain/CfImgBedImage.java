package site.muyin.picturebed.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author: lywq
 * @date: 2026/08/05 10:00
 * @version: v1.0.0
 * @description: CloudFlare ImgBed 文件记录，对应 /api/manage/list 返回的 files 元素
 **/
@Data
public class CfImgBedImage {

    /**
     * 完整文件 ID，可能带 folder/ 前缀，删除和拼接访问链接都要用它
     */
    private String name;

    private Metadata metadata;

    /**
     * 访问链接。接口不返回，由服务层按配置拼好后回填
     */
    private String publicUrl;

    /**
     * CloudFlare ImgBed 的 metadata 采用大驼峰键名，需显式映射
     */
    @Data
    public static class Metadata {

        @JsonProperty("FileName")
        private String fileName;

        @JsonProperty("FileType")
        private String fileType;

        /**
         * 单位 MB 的字符串，早期记录可能只有该字段而没有 FileSizeBytes
         */
        @JsonProperty("FileSize")
        private String fileSize;

        @JsonProperty("FileSizeBytes")
        private Long fileSizeBytes;

        @JsonProperty("Width")
        private Integer width;

        @JsonProperty("Height")
        private Integer height;

        @JsonProperty("TimeStamp")
        private Long timeStamp;

        @JsonProperty("Directory")
        private String directory;
    }
}
