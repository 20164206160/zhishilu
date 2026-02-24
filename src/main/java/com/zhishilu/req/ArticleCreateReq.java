package com.zhishilu.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 创建文章请求对象
 */
@Data
public class ArticleCreateReq {
    
    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    private String title;
    
    /**
     * 文章类别列表
     */
    @NotEmpty(message = "文章类别不能为空")
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
