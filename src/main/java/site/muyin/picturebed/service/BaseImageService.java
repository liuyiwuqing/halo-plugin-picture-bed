package site.muyin.picturebed.service;

import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.ResultsVO;

/**
 * @author: lywq
 * @date: 2024/05/29 09:51
 * @version: v1.0.0
 * @description:
 **/
public interface BaseImageService<T> {
    /**
     * 上传图片
     *
     * @param multipartData:
     * @return: reactor.core.publisher.Mono<site.muyin.picturebed.vo.ResultsVO>
     * @author: lywq
     * @date: 2024/05/22 21:33
     **/
    Mono<ResultsVO> uploadImage(MultiValueMap<String, ?> multipartData);

    /**
     * 获取图片列表
     *
     * @param query:
     * @return: reactor.core.publisher.Mono<site.muyin.picturebed.vo.PageResult < site.muyin.picturebed.domain.SmmsImage>>
     * @author: lywq
     * @date: 2024/05/22 21:33
     **/
    Mono<PageResult<T>> getImageList(CommonQuery query);

    /**
     * 删除图片
     *
     * @param query:
     * @return: reactor.core.publisher.Mono<java.lang.Boolean>
     * @author: lywq
     * @date: 2024/05/22 21:33
     **/
    Mono<Boolean> deleteImage(CommonQuery query);
}
