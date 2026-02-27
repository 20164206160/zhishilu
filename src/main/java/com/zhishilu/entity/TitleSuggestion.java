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
 * 标题搜索补全实体类
 * 每条记录存储一个标题原文词语及其拼音分词结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "zhishilu_title_suggestion")
@Setting(shards = 1, replicas = 0, settingPath = "suggestion-settings.json")
@Mapping(mappingPath = "title-suggestion-mapping.json")
public class TitleSuggestion {
    
    @Id
    private String id;
    
    /**
     * 标题原文词语（用于显示）
     * 例如："广东省"
     */
    @Field(type = FieldType.Keyword)
    private String text;
    
    /**
     * 搜索频率统计
     */
    @Field(type = FieldType.Long)
    private Long searchCount;
    
    /**
     * 标题拼音补全字段
     * 存储该原文词语的拼音分词结果
     * 例如：["广东省", "gds", "guangdongsheng"]
     */
    @CompletionField(analyzer = "my_pinyin_analyzer", searchAnalyzer = "my_pinyin_analyzer")
    private Completion suggest;
}
