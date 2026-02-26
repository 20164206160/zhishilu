package com.zhishilu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.suggest.Completion;

/**
 * 搜索补全实体类
 * 独立索引，存储各字段的补全建议
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "zhishilu_suggestion")
@Setting(shards = 1, replicas = 0, settingPath = "suggestion-settings.json")
@Mapping(mappingPath = "suggestion-mapping.json")
public class SearchSuggestion {
    
    @Id
    private String id;
    
    /**
     * 标题补全字段（拼音+中文）
     */
    @CompletionField(analyzer = "my_pinyin_analyzer", searchAnalyzer = "my_pinyin_analyzer")
    private Completion titleSuggest;
    
    /**
     * 类别补全字段（拼音+中文）
     */
    @CompletionField(analyzer = "my_pinyin_analyzer", searchAnalyzer = "my_pinyin_analyzer")
    private Completion categorySuggest;
    
    /**
     * 内容补全字段（拼音+中文）
     */
    @CompletionField(analyzer = "my_pinyin_analyzer", searchAnalyzer = "my_pinyin_analyzer")
    private Completion contentSuggest;
    
    /**
     * 用户名补全字段（拼音+中文）
     */
    @CompletionField(analyzer = "my_pinyin_analyzer", searchAnalyzer = "my_pinyin_analyzer")
    private Completion usernameSuggest;
    
    /**
     * 地点补全字段（拼音+中文）
     */
    @CompletionField(analyzer = "my_pinyin_analyzer", searchAnalyzer = "my_pinyin_analyzer")
    private Completion locationSuggest;

    /**
     * 地点原始文本（用于显示）
     */
    private String locationText;
}
