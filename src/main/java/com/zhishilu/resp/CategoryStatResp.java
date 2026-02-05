package com.zhishilu.resp;

import lombok.Data;

/**
 * 类别统计响应对象
 */
@Data
public class CategoryStatResp {
    
    /**
     * 类别名称
     */
    private String category;
    
    /**
     * 文章数量
     */
    private Long count;
}
