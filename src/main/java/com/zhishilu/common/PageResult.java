package com.zhishilu.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    public static <T> PageResult<T> of(List<T> list, int page, int size, long total) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        result.setTotalPages((int) Math.ceil((double) total / size));
        return result;
    }
}
