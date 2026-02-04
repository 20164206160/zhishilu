package com.zhishilu.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文章创建请求DTO
 */
@Data
public class ArticleCreateDTO {
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 64, message = "标题长度不能超过64个字符")
    private String title;
    
    @NotBlank(message = "类别不能为空")
    @Size(max = 32, message = "类别长度不能超过32个字符")
    private String category;
    
    private String content;
    
    @Size(max = 64, message = "网址长度不能超过64个字符")
    private String url;
    
    private List<String> images;
    
    private String location;
}
