package com.zhishilu.req;

import lombok.Data;

import java.util.List;

/**
 * 更新文章请求对象
 */
@Data
public class ArticleUpdateReq {
    
    /**
     * 文章标题
     */
    private String title;
    
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
}
