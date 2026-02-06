package com.zhishilu.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章/内容实体类
 */
@Data
@Document(indexName = "zhishilu_article")
@Setting(shards = 1, replicas = 0)
public class Article {
    
    @Id
    private String id;
    
    /**
     * 标题 - 最大64字符
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    
    /**
     * 类别 - 最大32字符
     */
    @Field(type = FieldType.Keyword)
    private String category;
    
    /**
     * 正文内容 - 不限长度
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    
    /**
     * 来源网址 - 最大64字符
     */
    @Field(type = FieldType.Keyword)
    private String url;
    
    /**
     * 图片路径列表
     */
    @Field(type = FieldType.Keyword)
    private List<String> images;
    
    /**
     * 创建者用户名
     */
    @Field(type = FieldType.Keyword)
    private String createdBy;
    
    /**
     * 创建者ID
     */
    @Field(type = FieldType.Keyword)
    private String creatorId;
    
    /**
     * 创建地点
     */
    @Field(type = FieldType.Keyword)
    private String location;
    
    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime updatedTime;
}
