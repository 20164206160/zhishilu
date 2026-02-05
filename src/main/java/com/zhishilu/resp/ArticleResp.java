package com.zhishilu.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章响应对象
 */
@Data
public class ArticleResp {
    
    /**
     * 文章ID
     */
    private String id;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章类别
     */
    private String category;
    
    /**
     * 文章内容
     */
    private String content;
    
    /**
     * 相关URL
     */
    private String url;
    
    /**
     * 图片列表
     */
    private List<String> images;
    
    /**
     * 地点
     */
    private String location;
    
    /**
     * 创建者用户名
     */
    private String createdBy;
    
    /**
     * 创建者ID
     */
    private String creatorId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
