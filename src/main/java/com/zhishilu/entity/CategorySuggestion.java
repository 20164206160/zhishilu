package com.zhishilu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.suggest.Completion;

/**
 * 类别搜索补全实体类
 * 每条记录存储一个类别原文词语及其拼音分词结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "zhishilu_category_suggestion")
@Setting(shards = 1, replicas = 0, settingPath = "suggestion-settings.json")
@Mapping(mappingPath = "category-suggestion-mapping.json")
public class CategorySuggestion {
    
    @Id
    private String id;
    
    /**
     * 类别原文词语（用于显示）
     */
    @Field(type = FieldType.Keyword)
    private String text;
    
    /**
     * 搜索频率统计
     */
    @Field(type = FieldType.Long)
    private Long searchCount;
    
    /**
     * 类别拼音补全字段
     * 存储该原文词语的拼音分词结果
     */
    @CompletionField(analyzer = "my_pinyin_analyzer", searchAnalyzer = "my_pinyin_analyzer")
    private Completion suggest;
}
