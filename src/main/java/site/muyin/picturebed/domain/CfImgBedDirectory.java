package site.muyin.picturebed.domain;

import lombok.Data;

/**
 * @author: lywq
 * @date: 2026/08/05 10:00
 * @version: v1.0.0
 * @description: CloudFlare ImgBed 目录树展平后的目录项
 **/
@Data
public class CfImgBedDirectory {

    /**
     * 完整目录路径，以 / 结尾，如 2024/travel/
     */
    private String path;

    private String name;
}
