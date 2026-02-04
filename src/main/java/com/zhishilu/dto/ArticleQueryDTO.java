package com.zhishilu.dto;

import lombok.Data;

/**
 * 文章查询请求DTO
 */
@Data
public class ArticleQueryDTO {
    
    /**
     * 标题（模糊查询）
     */
    private String title;
    
    /**
     * 类别
     */
    private String category;
    
    /**
     * 内容（模糊查询）
     */
    private String content;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 地点
     */
    private String location;
    
    /**
     * 页码
     */
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
}
