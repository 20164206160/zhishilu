package com.zhishilu.req;

import lombok.Data;

import java.util.List;

/**
 * 文章查询请求对象
 */
@Data
public class ArticleQueryReq {
    
    /**
     * 关键词（全部字段模糊查询）
     */
    private String keyword;
    
    /**
     * 标题（模糊查询）
     */
    private String title;
    
    /**
     * 类别列表（精确查询，满足任一类别）
     */
    private List<String> categories;
    
    /**
     * 内容（模糊查询）
     */
    private String content;
    
    /**
     * 用户名（精确查询）
     */
    private String username;
    
    /**
     * 地点（模糊查询）
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
