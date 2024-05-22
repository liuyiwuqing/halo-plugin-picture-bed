package site.muyin.picturebed.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * @author: lywq
 * @date: 2023/12/27 18:54
 * @version: v1.0.0
 * @description:
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultsVO<T> {

    private Integer code;

    private T data;

    private String msg;

    public static ResultsVO success(String msg) {
        return create(HttpStatus.OK.value(), msg, null);
    }

    public static <T> ResultsVO<T> success(String msg, T data) {
        return create(HttpStatus.OK.value(), msg, data);
    }

    public static ResultsVO failure(String msg) {
        return create(HttpStatus.BAD_REQUEST.value(), msg, null);
    }

    public static <T> ResultsVO<T> failure(String msg, T data) {
        return create(HttpStatus.BAD_REQUEST.value(), msg, data);
    }

    private static ResultsVO create(Integer code, String msg, Object data) {
        return ResultsVO.builder()
            .data(data)
            .msg(msg)
            .code(code)
            .build();
    }

}
