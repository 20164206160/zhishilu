package com.zhishilu.repository;

import com.zhishilu.entity.SearchSuggestion;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 搜索补全Repository
 */
public interface SearchSuggestionRepository extends ElasticsearchRepository<SearchSuggestion, String> {
}
