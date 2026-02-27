package com.zhishilu.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EsCompletionSuggestUtil {

    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    public EsCompletionSuggestUtil(ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    /**
     * 通用 Completion Suggest 方法
     *
     * @param indices          索引（支持多个）
     * @param prefix           用户输入前缀
     * @param suggestFields    suggest 字段（支持多个）
     * @param fetchSize        ES 返回数量
     * @param resultLimit      最终返回数量
     * @param skipDuplicates   是否去重
     */
    public List<SuggestItem> completionSuggest(
            String[] indices,
            String prefix,
            List<String> suggestFields,
            int fetchSize,
            int resultLimit,
            boolean skipDuplicates) {

        try {
            SearchRequest searchRequest = new SearchRequest(indices);

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.size(0); // 不返回文档

            SuggestBuilder suggestBuilder = new SuggestBuilder();

            for (String field : suggestFields) {

                CompletionSuggestionBuilder completionSuggestion =
                        new CompletionSuggestionBuilder(field)
                                .prefix(prefix)
                                .size(fetchSize)
                                .skipDuplicates(skipDuplicates);

                suggestBuilder.addSuggestion(field + "_suggest", completionSuggestion);
            }

            sourceBuilder.suggest(suggestBuilder);
            searchRequest.source(sourceBuilder);

            SearchResponse response = elasticsearchRestTemplate.execute(client ->
                    client.search(searchRequest, RequestOptions.DEFAULT)
            );

            return parseSuggestResult(response, suggestFields, resultLimit);

        } catch (Exception e) {
            log.error("Completion suggest 查询失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析 Suggest 结果
     */
    private List<SuggestItem> parseSuggestResult(
            SearchResponse response,
            List<String> suggestFields,
            int resultLimit) {

        Map<String, SuggestItem> resultMap = new HashMap<>();

        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            return Collections.emptyList();
        }

        final List<SuggestItem> suggestItems = new ArrayList<>();
        for (String field : suggestFields) {

            CompletionSuggestion completionSuggestion =
                    suggest.getSuggestion(field + "_suggest");

            if (completionSuggestion == null) {
                continue;
            }

            for (CompletionSuggestion.Entry entry : completionSuggestion.getEntries()) {

                for (CompletionSuggestion.Entry.Option option : entry.getOptions()) {
                    String text = option.getText().string();
                    float score = option.getScore();
                    final Map<String, Object> sourceAsMap = option.getHit().getSourceAsMap();
                    suggestItems.add(new SuggestItem(text, field, score, sourceAsMap));
                }
            }
        }
        return suggestItems.stream()
                .sorted(Comparator
                        .comparing(SuggestItem::getScore).reversed())
                .limit(resultLimit)
                .toList();
    }

    @Data
    @AllArgsConstructor
    public static class SuggestItem {

        private String text;

        private String field;

        private Float score;

        private Map<String, Object> sourceAsMap;
    }
}
