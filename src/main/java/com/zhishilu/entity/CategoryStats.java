package com.zhishilu.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

/**
 * 类别统计实体类
 * 用于存储每个类别的文章数量统计
 */
@Data
@Document(indexName = "zhishilu_category_stats")
@Setting(shards = 1, replicas = 0)
@Mapping(mappingPath = "category-stats-mapping.json")
public class CategoryStats {
    
    @Id
    private String id;
    
    /**
     * 类别名称
     */
    @Field(type = FieldType.Keyword)
    private String category;
    
    /**
     * 文章数量
     */
    @Field(type = FieldType.Long)
    private Long count;
    
    /**
     * 统计时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime statsTime;
}
