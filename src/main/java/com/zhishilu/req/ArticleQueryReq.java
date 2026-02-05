package com.zhishilu.req;

import lombok.Data;

/**
 * 文章查询请求对象
 */
@Data
public class ArticleQueryReq {
    
    /**
     * 标题（模糊查询）
     */
    private String title;
    
    /**
     * 类别（精确查询）
     */
    private String category;
    
    /**
     * 内容（模糊查询）
     */
    private String content;
    
    /**
     * 用户名（精确查询）
     */
    private String username;
    
    /**
     * 地点（精确查询）
     */
    private String location;
    
    /**
     * 当前页码（从0开始）
     */
    private int page = 0;
    
    /**
     * 每页大小
     */
    private int size = 10;
}
