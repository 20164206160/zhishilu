package com.zhishilu.req;

import lombok.Data;

import java.util.List;

/**
 * 保存草稿请求对象
 */
@Data
public class DraftSaveReq {
    
    /**
     * 草稿ID（更新时使用）
     */
    private String id;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章类别列表
     */
    private List<String> categories;
    
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
}
