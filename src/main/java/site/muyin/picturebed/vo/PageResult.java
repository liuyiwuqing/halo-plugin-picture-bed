package site.muyin.picturebed.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: lywq
 * @date: 2024/05/21 22:03
 * @version: v1.0.0
 * @description:
 **/
@Data
public class PageResult<T> {
    private Integer page; // 当前页码
    private Integer size; // 每页显示的记录数
    private Integer totalCount; // 总记录数
    private Integer totalPages; // 总页数
    private List<T> list; // 数据列表

    public PageResult(Integer page, Integer size, Integer totalCount, Integer totalPages, List<T> list) {
        this.page = page;
        this.size = size;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
        this.list = list;
    }
}
