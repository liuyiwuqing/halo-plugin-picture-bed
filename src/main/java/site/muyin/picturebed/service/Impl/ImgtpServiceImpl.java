package site.muyin.picturebed.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import site.muyin.picturebed.config.PictureBedConfig;
import site.muyin.picturebed.domain.ImgtpImage;
import site.muyin.picturebed.query.CommonQuery;
import site.muyin.picturebed.service.ImgtpService;
import site.muyin.picturebed.utils.PluginCacheManager;
import site.muyin.picturebed.vo.PageResult;
import site.muyin.picturebed.vo.ResultsVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static site.muyin.picturebed.constant.CommonConstant.PictureBedType.IMGTP;

/**
 * @author: lywq
 * @date: 2024/05/29 09:52
 * @version: v1.0.0
 * @description: Imgtp图床服务接口
 **/
@Service
@RequiredArgsConstructor
public class ImgtpServiceImpl implements ImgtpService {

    private final PluginCacheManager pluginCacheManager;

    @Override
    public Mono<ResultsVO> uploadImage(CommonQuery query, MultiValueMap<String, ?> multipartData) {
        Map<String, Object> paramMap = new HashMap(1);
        paramMap.put("file", multipartData);
        return req(query.getPictureBedId(), "upload", paramMap)
                .map(response -> {
                    if (response.code == 200) {
                        return ResultsVO.success(response.msg, response.data);
                    }
                    return ResultsVO.failure(response.msg);
                });
    }

    @Override
    public Mono<PageResult<ImgtpImage>> getImageList(CommonQuery query) {
        Map<String, Object> paramMap = new HashMap(1);
        paramMap.put("page", query.getPage());
        paramMap.put("rows", query.getSize());
        String params = HttpUtil.toParams(paramMap);

        return req(query.getPictureBedId(), "images" + "?" + params, null)
                .map(response -> {
                    if (response.code == 200) {
                        ImgtpImagePageRes imgtpImagePageRes = JSONUtil.toBean(JSONUtil.toJsonStr(response.data), ImgtpImagePageRes.class);
                        return new PageResult<>(imgtpImagePageRes.current_page, imgtpImagePageRes.per_page, imgtpImagePageRes.total, imgtpImagePageRes.last_page, imgtpImagePageRes.data);
                    }
                    return null;
                });
    }

    @Override
    public Mono<Boolean> deleteImage(CommonQuery query) {
        if (ObjectUtil.isEmpty(query.getImageId())) {
            return Mono.just(false);
        }
        Map<String, Object> paramMap = new HashMap(1);
        paramMap.put("id", query.getImageId());
        String params = HttpUtil.toParams(paramMap);
        return req(query.getPictureBedId(), "delete" + "?" + params, null)
                .map(response -> {
                    return response.code == 200;
                });
    }

    private Mono<ImgtpResponseRecord> req(String pictureBedId, String path, Map<String, Object> paramMap) {
        PictureBedConfig pictureBedConfig = pluginCacheManager.getConfig(PictureBedConfig.class);
        PictureBedConfig.PictureBed config = pictureBedConfig.getPictureBeds().stream().filter(p -> p.getPictureBedType().equals(IMGTP) && p.getPictureBedId().equals(pictureBedId)).findFirst().orElseThrow();
        String url = config.getPictureBedUrl();
        String authorization = config.getPictureBedToken();

        WebClient WEB_CLIENT =
                WebClient.builder()
                        .defaultHeader("token", authorization).build();

        if (StrUtil.startWithAny(path, "images", "delete")) {
            return WEB_CLIENT.post()
                    .uri(url + path)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<>() {
                    });
        } else if (StrUtil.equals(path, "upload")) {
            MultiValueMap<String, ?> multiValueMap = (MultiValueMap<String, ?>) paramMap.get("file");
            MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
            multipartData.add("image", multiValueMap.getFirst("file"));

            return WEB_CLIENT.post()
                    .uri(url + path)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipartData))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<>() {
                    });

        } else {
            throw new IllegalArgumentException("Unsupported path: " + path);
        }
    }

    @Data
    class ImgtpImagePageRes {
        Integer total;
        Integer per_page;
        Integer current_page;
        Integer last_page;
        List<ImgtpImage> data;
    }

    public record ImgtpResponseRecord(Integer code, String msg, Object data, Integer time) {
    }
}
