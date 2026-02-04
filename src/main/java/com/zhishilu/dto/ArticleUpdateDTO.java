package com.zhishilu.dto;

import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文章更新请求DTO
 */
@Data
public class ArticleUpdateDTO {
    
    @Size(max = 64, message = "标题长度不能超过64个字符")
    private String title;
    
    private String content;
    
    @Size(max = 64, message = "网址长度不能超过64个字符")
    private String url;
    
    private List<String> images;
}
