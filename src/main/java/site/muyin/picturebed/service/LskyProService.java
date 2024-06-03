package site.muyin.picturebed.service;

import reactor.core.publisher.Mono;
import site.muyin.picturebed.domain.LskyProAlbum;
import site.muyin.picturebed.domain.LskyProImage;
import site.muyin.picturebed.query.CommonQuery;

import java.util.List;

/**
 * @author: lywq
 * @date: 2024/04/29 10:45
 * @version: v1.0.0
 * @description: 兰空图床服务接口
 **/
public interface LskyProService extends BaseImageService<LskyProImage> {

    /**
     * 获取图库列表
     *
     * @param query:
     * @return: reactor.core.publisher.Mono<java.util.List < site.muyin.picturebed.domain.LskyProAlbum>>
     * @author: lywq
     * @date: 2024/05/22 21:31
     **/
    Mono<List<LskyProAlbum>> getAlbumList(CommonQuery query);
}
