package com.zhishilu.resp;

import lombok.Data;

import java.util.List;

/**
 * 搜索补全响应对象
 */
@Data
public class SearchSuggestionResp {
    
    /**
     * 用户名补全列表
     */
    private List<SuggestionItem> usernames;
    
    /**
     * 地点补全列表
     */
    private List<SuggestionItem> locations;
    
    /**
     * 类别补全列表
     */
    private List<SuggestionItem> categories;
    
    /**
     * 标题补全列表
     */
    private List<SuggestionItem> titles;
    
    /**
     * 内容补全列表
     */
    private List<SuggestionItem> contents;
    
    /**
     * 补全项
     */
    @Data
    public static class SuggestionItem {
        /**
         * 补全文本
         */
        private String text;
        
        /**
         * 匹配字段类型
         */
        private String field;
        
        public SuggestionItem(String text, String field) {
            this.text = text;
            this.field = field;
        }
    }
}
