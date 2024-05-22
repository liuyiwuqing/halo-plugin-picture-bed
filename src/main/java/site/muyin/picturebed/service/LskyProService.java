package site.muyin.picturebed.service;

import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import site.muyin.picturebed.domain.LskyProAlbum;
import site.muyin.picturebed.domain.LskyProImage;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.ResultsVO;

import java.util.List;

/**
 * @author: lywq
 * @date: 2024/04/29 10:45
 * @version: v1.0.0
 * @description: 兰空图床服务接口
 **/
public interface LskyProService {

    /**
     * 上传图片
     *
     * @param multipartData:
     * @return: reactor.core.publisher.Mono<site.muyin.picturebed.vo.ResultsVO>
     * @author: lywq
     * @date: 2024/05/22 21:31
     **/
    Mono<ResultsVO> uploadImage(MultiValueMap<String, ?> multipartData);

    /**
     * 获取图库列表
     *
     * @param query:
     * @return: reactor.core.publisher.Mono<java.util.List < site.muyin.picturebed.domain.LskyProAlbum>>
     * @author: lywq
     * @date: 2024/05/22 21:31
     **/
    Mono<List<LskyProAlbum>> getAlbumList(CommonQuery query);

    /**
     * 获取图片列表
     *
     * @param query:
     * @return: reactor.core.publisher.Mono<site.muyin.picturebed.vo.PageResult < site.muyin.picturebed.domain.LskyProImage>>
     * @author: lywq
     * @date: 2024/05/22 21:31
     **/
    Mono<PageResult<LskyProImage>> getImageList(CommonQuery query);

    /**
     * 删除图片
     *
     * @param query:
     * @return: reactor.core.publisher.Mono<java.lang.Boolean>
     * @author: lywq
     * @date: 2024/05/22 21:31
     **/
    Mono<Boolean> deleteImage(CommonQuery query);
}
