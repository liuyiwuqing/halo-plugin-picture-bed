package site.muyin.picturebed.service;

import jakarta.activation.MimetypesFileTypeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import site.muyin.picturebed.domain.ImgtpImage;
import site.muyin.picturebed.domain.LskyProAlbum;
import site.muyin.picturebed.domain.LskyProImage;
import site.muyin.picturebed.domain.Pan123Image;
import site.muyin.picturebed.domain.SmmsImage;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.vo.AlbumVO;
import site.muyin.picturebed.vo.ImageVO;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.ResultsVO;

import java.io.File;
import java.util.List;

import static site.muyin.picturebed.constant.CommonConstant.PictureBedType.IMGTP;
import static site.muyin.picturebed.constant.CommonConstant.PictureBedType.LSKY;
import static site.muyin.picturebed.constant.CommonConstant.PictureBedType.SMMS;
import static site.muyin.picturebed.constant.CommonConstant.PictureBedType.Pan123;

/**
 * @author: lywq
 * @date: 2024/05/20 16:02
 * @version: v1.0.0
 * @description:
 **/
@Component
@RequiredArgsConstructor
public class PictureBedService {

    private final LskyProService lskyProService;
    private final SmmsService smmsService;
    private final ImgtpService imgtpService;
    private final Pan123Service pan123Service;

    public Mono<ResultsVO> uploadImage(CommonQuery query, MultiValueMap<String, Part> parts) {
        String type = query.getType();
        switch (type) {
            case LSKY:
                return lskyProService.uploadImage(query, parts);
            case SMMS:
                return smmsService.uploadImage(query, parts);
            case IMGTP:
                return imgtpService.uploadImage(query, parts);
            case Pan123:
                return pan123Service.uploadImage(query, parts);
            default:
                // TODO: get album list from other picture bed service
                throw new IllegalArgumentException("暂不支持该图片托管服务");
        }
    }

    public Mono<List<AlbumVO>> getAlbumList(CommonQuery query) {
        String type = query.getType();
        switch (type) {
            case LSKY:
                return lskyProService.getAlbumList(query).flatMap(albumList -> {
                    List<AlbumVO> albumVOList = convertLskyProAlbumListToAlbumVOList(albumList);
                    return Mono.just(albumVOList);
                });
            case SMMS:
                // TODO: get album list from smms
            default:
                // TODO: get album list from other picture bed service
                break;
        }

        return null;
    }

    public Mono<PageResult<ImageVO>> getImageList(CommonQuery query) {
        String type = query.getType();
        switch (type) {
            case LSKY:
                return lskyProService.getImageList(query).flatMap(lskyProImages -> {
                    PageResult<ImageVO> imageVOPageResult = convertLskyProImageListToImageVOList(lskyProImages);
                    return Mono.just(imageVOPageResult);
                });
            case SMMS:
                return smmsService.getImageList(query).flatMap(smmsImages -> {
                    PageResult<ImageVO> imageVOPageResult = convertSmmsImageListToImageVOList(smmsImages);
                    return Mono.just(imageVOPageResult);
                });
            case IMGTP:
                return imgtpService.getImageList(query).flatMap(imgtpImages -> {
                    PageResult<ImageVO> imageVOPageResult = convertImgtpImageListToImageVOList(imgtpImages);
                    return Mono.just(imageVOPageResult);
                });
            case Pan123:
                return pan123Service.getImageList(query).flatMap(pan123Images -> {
                    PageResult<ImageVO> imageVOPageResult = convertPan123ImageListToImageVOList(pan123Images);
                    return Mono.just(imageVOPageResult);
                });
            default:
                // TODO: get image list from other picture bed service
                throw new IllegalArgumentException("暂不支持该图片托管服务");
        }
    }

    public Mono<Boolean> deleteImage(CommonQuery query) {
        String type = query.getType();
        switch (type) {
            case LSKY:
                return lskyProService.deleteImage(query);
            case SMMS:
                return smmsService.deleteImage(query);
            case IMGTP:
                return imgtpService.deleteImage(query);
            case Pan123:
                return pan123Service.deleteImage(query);
            default:
                // TODO: delete image from other picture bed service
                throw new IllegalArgumentException("暂不支持该图片托管服务");
        }
    }

    static List<AlbumVO> convertLskyProAlbumListToAlbumVOList(List<LskyProAlbum> albumList) {
        return albumList.stream().map(album -> {
            AlbumVO albumVO = new AlbumVO();
            albumVO.setId(String.valueOf(album.getId())).setName(album.getName()).setDescription(album.getIntro());
            return albumVO;
        }).toList();
    }

    static PageResult<ImageVO> convertLskyProImageListToImageVOList(PageResult<LskyProImage> page) {
        List<LskyProImage> imageList = page.getList();
        List<ImageVO> imageVOList = imageList.stream().map(image -> {
            ImageVO imageVO = new ImageVO();
            imageVO.setId(String.valueOf(image.getKey()))
                    .setName(image.getOrigin_name())
                    .setUrl(image.getLinks().getUrl())
                    .setMediaType(image.getMimetype())
                    .setSize(image.getSize())
                    .setWidth(image.getWidth())
                    .setHeight(image.getHeight());
            return imageVO;
        }).toList();
        return new PageResult<>(page.getPage(), page.getSize(), page.getTotalCount(), page.getTotalPages(), imageVOList);
    }

    private PageResult<ImageVO> convertSmmsImageListToImageVOList(PageResult<SmmsImage> page) {
        List<SmmsImage> smmsImages = page.getList();
        List<ImageVO> imageVOList = smmsImages.stream().map(image -> {
            ImageVO imageVO = new ImageVO();
            imageVO.setId(image.getHash())
                    .setName(image.getFilename())
                    .setUrl(image.getUrl())
                    .setMediaType(getMediaType(image.getFilename()))
                    .setSize(Float.valueOf(image.getSize()))
                    .setWidth(image.getWidth())
                    .setHeight(image.getHeight());
            return imageVO;
        }).toList();
        return new PageResult<>(page.getPage(), page.getSize(), page.getTotalCount(), page.getTotalPages(), imageVOList);
    }

    private PageResult<ImageVO> convertImgtpImageListToImageVOList(PageResult<ImgtpImage> imgtpImages) {
        List<ImgtpImage> imageList = imgtpImages.getList();
        List<ImageVO> imageVOList = imageList.stream().map(image -> {
            ImageVO imageVO = new ImageVO();
            imageVO.setId(image.getId())
                    .setName(image.getName())
                    .setUrl(image.getUrl())
                    .setMediaType(image.getMime())
                    .setSize(image.getSize());
            return imageVO;
        }).toList();
        return new PageResult<>(imgtpImages.getPage(), imgtpImages.getSize(), imgtpImages.getTotalCount(), imgtpImages.getTotalPages(), imageVOList);
    }

    private PageResult<ImageVO> convertPan123ImageListToImageVOList(PageResult<Pan123Image> pan123Images) {
        List<Pan123Image> imageList = pan123Images.getList();
        List<ImageVO> imageVOList = imageList.stream().map(image -> {
            ImageVO imageVO = new ImageVO();
            imageVO.setId(image.getFileId())
                   .setName(image.getFilename())
                   .setUrl(image.getDownloadURL())
                   .setMediaType(image.getType() == 0 ? getMediaType(image.getFilename()):"folder")
                   .setSize(image.getSize());
            return imageVO;
        }).toList();
        return new PageResult<>(pan123Images.getPage(), pan123Images.getSize(), pan123Images.getTotalCount(), pan123Images.getTotalPages(), imageVOList);
    }

    public static String getMediaType(String fileName) {
        MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
        File file = new File(fileName);
        return fileTypeMap.getContentType(file);
    }
}
