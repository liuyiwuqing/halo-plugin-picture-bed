package site.muyin.picturebed.service;

import reactor.core.publisher.Mono;
import site.muyin.picturebed.domain.CfImgBedDirectory;
import site.muyin.picturebed.domain.CfImgBedImage;
import site.muyin.picturebed.query.CommonQuery;

import java.util.List;

/**
 * @author: lywq
 * @date: 2026/08/05 10:00
 * @version: v1.0.0
 * @description: CloudFlare ImgBed 图床服务接口
 **/
public interface CfImgBedService extends BaseImageService<CfImgBedImage> {

    /**
     * 获取目录列表，目录树会被展平成扁平的完整路径列表
     *
     * @param query:
     * @return: reactor.core.publisher.Mono<java.util.List < site.muyin.picturebed.domain.CfImgBedDirectory>>
     * @author: lywq
     * @date: 2026/08/05 10:00
     **/
    Mono<List<CfImgBedDirectory>> getAlbumList(CommonQuery query);
}
