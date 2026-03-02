package com.zhishilu.resp;

import lombok.Data;

/**
 * 热门关键词响应
 */
@Data
public class HotKeywordResp {
    
    /**
     * 关键词文本
     */
    private String keyword;
    
    /**
     * 搜索次数
     */
    private Long searchCount;
    
    public HotKeywordResp(String keyword, Long searchCount) {
        this.keyword = keyword;
        this.searchCount = searchCount;
    }
}
