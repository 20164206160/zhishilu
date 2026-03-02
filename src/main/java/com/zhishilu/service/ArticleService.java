package com.zhishilu.service;

import com.zhishilu.common.PageResult;
import com.zhishilu.dto.ArticleDTO;
import com.zhishilu.dto.UserDTO;
import com.zhishilu.req.ArticleCreateReq;
import com.zhishilu.req.ArticleQueryReq;
import com.zhishilu.req.ArticleUpdateReq;
import com.zhishilu.req.DraftSaveReq;
import com.zhishilu.resp.ArticleResp;
import com.zhishilu.resp.CategoryStatResp;
import com.zhishilu.resp.DraftResp;
import com.zhishilu.resp.HotKeywordResp;
import com.zhishilu.resp.SearchSuggestionResp;
import com.zhishilu.entity.Article;
import com.zhishilu.entity.CategorySuggestion;
import com.zhishilu.entity.ContentSuggestion;
import com.zhishilu.entity.LocationSuggestion;
import com.zhishilu.entity.TitleSuggestion;
import com.zhishilu.entity.UsernameSuggestion;
import com.zhishilu.exception.BusinessException;
import com.zhishilu.repository.ArticleRepository;
import com.zhishilu.util.EsCompletionSuggestUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 文章服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {
    
    private final ArticleRepository articleRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final RestHighLevelClient restHighLevelClient;
    private final CategoryStatsService categoryStatsService;
    private final UserService userService;
    private final EsCompletionSuggestUtil esCompletionSuggestUtil;
    
    /**
     * 创建文章
     */
    public ArticleResp create(ArticleCreateReq req, UserDTO currentUser) {
        Article article = new Article();
        article.setTitle(req.getTitle());
        article.setCategories(req.getCategories());
        article.setContent(req.getContent());
        article.setUrl(req.getUrl());
        article.setImages(req.getImages());
        article.setLocation(req.getLocation());
        article.setCreatedBy(currentUser.getUsername());
        article.setCreatorId(currentUser.getId());
        article.setCreatedTime(LocalDateTime.now());
        article.setUpdatedTime(LocalDateTime.now());
        article.setStatus("PUBLISHED");
        
        article = articleRepository.save(article);
        
        // 更新类别统计计数
        categoryStatsService.incrementCategoryCount(req.getCategories());
        log.info("文章创建成功，类别统计已更新: {}", req.getCategories());
        
        // 异步同步补全建议
        final Article finalArticle = article;
        CompletableFuture.runAsync(() -> syncSuggestions(finalArticle));
        
        return convertToResp(article);
    }
    
    /**
     * 保存草稿（新建或更新）
     */
    public DraftResp saveDraft(DraftSaveReq req, UserDTO currentUser) {
        Article article;
        
        if (StringUtils.isNotBlank(req.getId())) {
            // 更新现有草稿
            article = articleRepository.findById(req.getId())
                    .orElseThrow(() -> new BusinessException("草稿不存在"));
            
            // 权限检查
            if (!article.getCreatorId().equals(currentUser.getId())) {
                throw new BusinessException(403, "没有权限修改此草稿");
            }
            
            // 只能更新草稿状态的文章
            if (!"DRAFT".equals(article.getStatus())) {
                throw new BusinessException("只能编辑草稿状态的文章");
            }
        } else {
            // 创建新草稿
            article = new Article();
            article.setCreatedBy(currentUser.getUsername());
            article.setCreatorId(currentUser.getId());
            article.setCreatedTime(LocalDateTime.now());
            article.setStatus("DRAFT");
        }
        
        // 更新字段
        article.setTitle(req.getTitle());
        article.setCategories(req.getCategories());
        article.setContent(req.getContent());
        article.setUrl(req.getUrl());
        article.setImages(req.getImages());
        article.setLocation(req.getLocation());
        article.setUpdatedTime(LocalDateTime.now());
        
        article = articleRepository.save(article);
        log.info("草稿保存成功，ID: {}", article.getId());
        
        return convertToDraftResp(article);
    }
    
    /**
     * 获取用户的草稿列表
     */
    public List<DraftResp> getUserDrafts(String userId) {
        List<Article> drafts = articleRepository.findByCreatorIdAndStatus(userId, "DRAFT");
        return drafts.stream()
                .sorted((a, b) -> b.getUpdatedTime().compareTo(a.getUpdatedTime()))
                .map(this::convertToDraftResp)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取草稿详情
     */
    public DraftResp getDraftById(String id, UserDTO currentUser) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("草稿不存在"));
        
        // 权限检查
        if (!article.getCreatorId().equals(currentUser.getId())) {
            throw new BusinessException(403, "没有权限查看此草稿");
        }
        
        if (!"DRAFT".equals(article.getStatus())) {
            throw new BusinessException("该文章不是草稿状态");
        }
        
        return convertToDraftResp(article);
    }
    
    /**
     * 删除草稿
     */
    public void deleteDraft(String id, UserDTO currentUser) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("草稿不存在"));
        
        // 权限检查
        if (!article.getCreatorId().equals(currentUser.getId())) {
            throw new BusinessException(403, "没有权限删除此草稿");
        }
        
        if (!"DRAFT".equals(article.getStatus())) {
            throw new BusinessException("只能删除草稿状态的文章");
        }
        
        articleRepository.deleteById(id);
        log.info("草稿删除成功，ID: {}", id);
    }
    
    /**
     * 将草稿发布为正式文章
     */
    public ArticleResp publishDraft(String id, ArticleCreateReq req, UserDTO currentUser) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("草稿不存在"));
        
        // 权限检查
        if (!article.getCreatorId().equals(currentUser.getId())) {
            throw new BusinessException(403, "没有权限发布此草稿");
        }
        
        if (!"DRAFT".equals(article.getStatus())) {
            throw new BusinessException("该文章不是草稿状态");
        }
        
        // 更新为发布状态
        article.setTitle(req.getTitle());
        article.setCategories(req.getCategories());
        article.setContent(req.getContent());
        article.setUrl(req.getUrl());
        article.setImages(req.getImages());
        article.setLocation(req.getLocation());
        article.setStatus("PUBLISHED");
        article.setUpdatedTime(LocalDateTime.now());
        
        article = articleRepository.save(article);
        
        // 更新类别统计计数
        categoryStatsService.incrementCategoryCount(req.getCategories());
        log.info("草稿发布成功，ID: {}, 类别统计已更新: {}", id, req.getCategories());
        
        // 异步同步补全建议
        final Article finalArticle = article;
        CompletableFuture.runAsync(() -> syncSuggestions(finalArticle));
        
        return convertToResp(article);
    }
    
    /**
     * 更新文章
     */
    public ArticleResp update(String id, ArticleUpdateReq req, UserDTO currentUser) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        
        // 权限检查：只有创建者可以修改
        if (!article.getCreatorId().equals(currentUser.getId())) {
            throw new BusinessException(403, "没有权限修改此文章");
        }
        
        // 保存旧类别信息用于后续更新统计
        List<String> oldCategories = article.getCategories();
        
        if (StringUtils.isNotBlank(req.getTitle())) {
            article.setTitle(req.getTitle());
        }
        if (req.getContent() != null) {
            article.setContent(req.getContent());
        }
        if (req.getCategories() != null && !req.getCategories().isEmpty()) {
            article.setCategories(req.getCategories());
        }
        if (req.getLocation() != null) {
            article.setLocation(req.getLocation());
        }
        if (req.getUrl() != null) {
            article.setUrl(req.getUrl());
        }
        if (req.getImages() != null) {
            article.setImages(req.getImages());
        }
        article.setUpdatedTime(LocalDateTime.now());
        
        article = articleRepository.save(article);
        
        // 如果类别发生变化，更新类别统计计数
        if (req.getCategories() != null && !req.getCategories().isEmpty()) {
            List<String> newCategories = req.getCategories();
            boolean categoriesChanged = !oldCategories.equals(newCategories);
            
            if (categoriesChanged) {
                categoryStatsService.updateCategoryCount(oldCategories, newCategories);
                log.info("文章更新成功，类别统计已更新，旧类别: {}, 新类别: {}", oldCategories, newCategories);
            }
        }
        
        // 异步同步补全建议
        final Article finalArticle = article;
        CompletableFuture.runAsync(() -> syncSuggestions(finalArticle));
        
        return convertToResp(article);
    }
    
    /**
     * 删除文章
     */
    public void delete(String id, UserDTO currentUser) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        
        // 权限检查：只有创建者可以删除
        if (!article.getCreatorId().equals(currentUser.getId())) {
            throw new BusinessException(403, "没有权限删除此文章");
        }
        
        // 保存类别信息用于后续更新统计
        List<String> categories = article.getCategories();
        
        articleRepository.deleteById(id);
        
        // 更新类别统计计数
        categoryStatsService.decrementCategoryCount(categories);
        log.info("文章删除成功，类别统计已更新: {}", categories);
    }
    
    /**
     * 获取文章详情
     */
    public ArticleResp getById(String id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        return convertToResp(article);
    }
    
    /**
     * 分页查询文章
     */
    public PageResult<ArticleResp> search(ArticleQueryReq req) {
        log.info("搜索文章，参数: {}", req);
        
        // 异步更新搜索频率统计
        CompletableFuture.runAsync(() ->  updateSearchFrequency(req));
        
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        
        // 只查询已发布的文章
        boolQuery.must(QueryBuilders.termQuery("status", "PUBLISHED"));
        
        // 关键词全部字段模糊查询（优先级最高）
        if (StringUtils.isNotBlank(req.getKeyword())) {
            BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
            String keyword = req.getKeyword();
            keywordQuery.should(QueryBuilders.matchQuery("title", keyword));
            keywordQuery.should(QueryBuilders.matchQuery("categories", keyword));
            keywordQuery.should(QueryBuilders.matchQuery("content", keyword));
            keywordQuery.should(QueryBuilders.matchQuery("createdBy", keyword));
            keywordQuery.should(QueryBuilders.matchQuery("location", keyword));
            boolQuery.must(keywordQuery);
        } else {
            // 标题模糊查询
            if (StringUtils.isNotBlank(req.getTitle())) {
                boolQuery.must(QueryBuilders.matchQuery("title", req.getTitle()));
            }
            
            // 类别精确查询（满足任一类别）
            if (req.getCategories() != null && !req.getCategories().isEmpty()) {
                BoolQueryBuilder categoryQuery = QueryBuilders.boolQuery();
                for (String category : req.getCategories()) {
                    categoryQuery.should(QueryBuilders.termQuery("categories", category));
                }
                boolQuery.must(categoryQuery);
            }
            
            // 内容模糊查询
            if (StringUtils.isNotBlank(req.getContent())) {
                boolQuery.must(QueryBuilders.matchQuery("content", req.getContent()));
            }
            
            // 用户名精确查询
            if (StringUtils.isNotBlank(req.getUsername())) {
                boolQuery.must(QueryBuilders.termQuery("createdBy", req.getUsername()));
            }
            
            // 地点模糊查询
            if (StringUtils.isNotBlank(req.getLocation())) {
                boolQuery.must(QueryBuilders.matchQuery("location", req.getLocation()));
            }
        }
        
        // 构建高亮字段
        HighlightBuilder.Field titleHighlightField = new HighlightBuilder.Field("title")
                .preTags("<mark class=\"search-highlight\">")
                .postTags("</mark>")
                .fragmentSize(20)
                .numOfFragments(1);
        
        HighlightBuilder.Field contentHighlightField = new HighlightBuilder.Field("content")
                .preTags("<mark class=\"search-highlight\">")
                .postTags("</mark>")
                .fragmentSize(50)
                .numOfFragments(1);

        HighlightBuilder.Field locationHighlightField = new HighlightBuilder.Field("location")
                .preTags("<mark class=\"search-highlight\">")
                .postTags("</mark>")
                .fragmentSize(20)
                .numOfFragments(1);
        
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(req.getPage(), req.getSize(), 
                        Sort.by(Sort.Direction.DESC, "createdTime")))
                .withHighlightFields(titleHighlightField, contentHighlightField, locationHighlightField)
                .build();
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class);
        
        log.info("ES查询结果总数: {}", searchHits.getTotalHits());
        
        List<ArticleResp> articles = searchHits.getSearchHits().stream()
                .map(hit -> {
                    ArticleResp resp = convertToResp(hit.getContent());
                    // 提取高亮片段
                    if (hit.getHighlightFields() != null) {
                        List<String> titleHighlights = hit.getHighlightFields().get("title");
                        if (titleHighlights != null && !titleHighlights.isEmpty()) {
                            resp.setHighlightTitle(titleHighlights.get(0));
                        }
                        List<String> contentHighlights = hit.getHighlightFields().get("content");
                        if (contentHighlights != null && !contentHighlights.isEmpty()) {
                            resp.setHighlightContent(contentHighlights.get(0));
                        }

                        List<String> locationHighlights = hit.getHighlightFields().get("location");
                        if (locationHighlights != null && !locationHighlights.isEmpty()) {
                            resp.setHighlightLocation(locationHighlights.get(0));
                        }
                    }
                    return resp;
                })
                .toList();
        
        log.info("返回文章列表数量: {}", articles.size());
        
        return PageResult.of(articles, req.getPage(), req.getSize(), 
                searchHits.getTotalHits());
    }
    
    /**
     * 搜索补全（自动完成）
     * 根据搜索字段返回对应的补全建议
     * 对于全部字段，按 用户名/地点/类别/标题/内容 排序返回
     */
    public SearchSuggestionResp getSearchSuggestions(String keyword, String field) {
        SearchSuggestionResp resp = new SearchSuggestionResp();
        
        if (StringUtils.isBlank(keyword)) {
            resp.setUsernames(new ArrayList<>());
            resp.setLocations(new ArrayList<>());
            resp.setCategories(new ArrayList<>());
            resp.setTitles(new ArrayList<>());
            resp.setContents(new ArrayList<>());
            return resp;
        }
        
        // 如果指定了特定字段，只查询该字段
        if (StringUtils.isNotBlank(field) && !"all".equals(field)) {
            switch (field) {
                case "title":
                    resp.setTitles(getTitleSuggestionsFromIndex(keyword));
                    resp.setUsernames(new ArrayList<>());
                    resp.setLocations(new ArrayList<>());
                    resp.setCategories(new ArrayList<>());
                    resp.setContents(new ArrayList<>());
                    break;
                case "category":
                    resp.setCategories(getCategorySuggestionsFromIndex(keyword));
                    resp.setUsernames(new ArrayList<>());
                    resp.setLocations(new ArrayList<>());
                    resp.setTitles(new ArrayList<>());
                    resp.setContents(new ArrayList<>());
                    break;
                case "content":
                    resp.setContents(getContentSuggestionsFromIndex(keyword));
                    resp.setUsernames(new ArrayList<>());
                    resp.setLocations(new ArrayList<>());
                    resp.setCategories(new ArrayList<>());
                    resp.setTitles(new ArrayList<>());
                    break;
                case "username":
                    resp.setUsernames(getUsernameSuggestionsFromIndex(keyword));
                    resp.setLocations(new ArrayList<>());
                    resp.setCategories(new ArrayList<>());
                    resp.setTitles(new ArrayList<>());
                    resp.setContents(new ArrayList<>());
                    break;
                case "location":
                    resp.setLocations(getLocationSuggestionsFromIndex(keyword));
                    resp.setUsernames(new ArrayList<>());
                    resp.setCategories(new ArrayList<>());
                    resp.setTitles(new ArrayList<>());
                    resp.setContents(new ArrayList<>());
                    break;
                default:
                    // 全部字段
                    fillAllSuggestionsFromIndex(resp, keyword);
                    break;
            }
        } else {
            // 全部字段，按指定顺序：用户名/地点/类别/标题/内容
            fillAllSuggestionsFromIndex(resp, keyword);
        }
        
        return resp;
    }
    
    /**
     * 填充所有字段的补全建议
     * 按 用户名/地点/类别/标题/内容 排序
     */
    private void fillAllSuggestionsFromIndex(SearchSuggestionResp resp, String keyword) {
        resp.setUsernames(getUsernameSuggestionsFromIndex(keyword));
        resp.setLocations(getLocationSuggestionsFromIndex(keyword));
        resp.setCategories(getCategorySuggestionsFromIndex(keyword));
        resp.setTitles(getTitleSuggestionsFromIndex(keyword));
        resp.setContents(getContentSuggestionsFromIndex(keyword));
    }
    
    /**
     * 从补全索引获取用户名补全建议
     */
    private List<SearchSuggestionResp.SuggestionItem> getUsernameSuggestionsFromIndex(String keyword) {
        return getCompletionSuggestionsFromIndex(keyword, "suggest", "username", "text", UsernameSuggestion.class);
    }
    
    /**
     * 从补全索引获取地点补全建议
     */
    private List<SearchSuggestionResp.SuggestionItem> getLocationSuggestionsFromIndex(String keyword) {


        return getCompletionSuggestionsFromIndex(keyword, "suggest", "location", "text", LocationSuggestion.class);
    }
    
    /**
     * 从补全索引获取类别补全建议
     */
    private List<SearchSuggestionResp.SuggestionItem> getCategorySuggestionsFromIndex(String keyword) {
        return getCompletionSuggestionsFromIndex(keyword, "suggest", "category", "text", CategorySuggestion.class);
    }
    
    /**
     * 从补全索引获取标题补全建议
     */
    private List<SearchSuggestionResp.SuggestionItem> getTitleSuggestionsFromIndex(String keyword) {
        return getCompletionSuggestionsFromIndex(keyword, "suggest", "title", "text", TitleSuggestion.class);
    }
    
    /**
     * 从补全索引获取内容补全建议
     */
    private List<SearchSuggestionResp.SuggestionItem> getContentSuggestionsFromIndex(String keyword) {
        return getCompletionSuggestionsFromIndex(keyword, "suggest", "content", "text", ContentSuggestion.class);
    }

    /**
     * 从补全索引使用 Completion Suggester 获取补全建议
     * 支持中文/拼音混合输入补全，返回中文原文
     */
    private <T> List<SearchSuggestionResp.SuggestionItem> getCompletionSuggestionsFromIndex(
            String keyword, String suggestField, String fieldType, String columName, Class<T> suggestionClass) {

        if (StringUtils.isBlank(keyword)) {
            return Collections.emptyList();
        }

        // 通过实体类获取 index 名称
        String indexName = elasticsearchOperations.getIndexCoordinatesFor(suggestionClass).getIndexName();

        // 调用通用 Suggest 工具类
        List<EsCompletionSuggestUtil.SuggestItem> suggestItems =
                esCompletionSuggestUtil.completionSuggest(
                        new String[]{indexName},        // index
                        keyword,                        // prefix
                        List.of(suggestField),          // suggest 字段
                        20,                             // ES fetch size
                        5,                              // 最终返回数量
                        false                           // 不跳过重复（便于统计频次）
                );

        // 转换成你自己的返回结构
        return suggestItems.stream()
                .map(item -> new SearchSuggestionResp.SuggestionItem(
                        item.getSourceAsMap().get(columName).toString(),     // 补全文本
                        fieldType          // 类型
                ))
                .sorted(Comparator.comparing((SearchSuggestionResp.SuggestionItem item) -> item.getText().length()))
                .toList();
    }
    
    /**
     * 获取用户最常用的类别（前N个）
     */
    public List<CategoryStatResp> getTopCategories(String userId, int limit) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("creatorId", userId))
                .addAggregation(AggregationBuilders.terms("top_categories").field("categories").size(limit))
                .withMaxResults(0)
                .build();
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class);
        
        // 从聚合结果中提取类别统计
        if (searchHits.hasAggregations()) {
            Aggregations aggregations = (Aggregations) searchHits.getAggregations().aggregations();
            Terms terms = aggregations.get("top_categories");
            if (terms != null) {
                return terms.getBuckets().stream()
                        .map(bucket -> {
                            CategoryStatResp resp = new CategoryStatResp();
                            resp.setCategory(bucket.getKeyAsString());
                            resp.setCount(bucket.getDocCount());
                            return resp;
                        })
                        .toList();
            }
        }
        return List.of();
    }
    
    /**
     * 将Entity转换为Resp（用于Controller响应）
     */
    private ArticleResp convertToResp(Article article) {
        ArticleResp resp = new ArticleResp();
        BeanUtils.copyProperties(article, resp);
        
        // 获取创建者头像
        try {
            if (article.getCreatorId() != null) {
                var user = userService.getById(article.getCreatorId());
                if (user != null) {
                    resp.setCreatorAvatar(user.getAvatar());
                }
            }
        } catch (Exception e) {
            // 用户不存在时不影响文章展示
            log.debug("获取用户头像失败, userId: {}", article.getCreatorId());
        }
        
        return resp;
    }
    
    /**
     * 将Entity转换为DTO（用于Service层传递）
     */
    private ArticleDTO convertToDTO(Article article) {
        ArticleDTO dto = new ArticleDTO();
        BeanUtils.copyProperties(article, dto);
        return dto;
    }
    
    /**
     * 将Entity转换为DraftResp
     */
    private DraftResp convertToDraftResp(Article article) {
        DraftResp resp = new DraftResp();
        BeanUtils.copyProperties(article, resp);
        return resp;
    }
    
    /**
     * 同步文章的补全建议到搜索补全索引
     * 对每个字段：
     * 1. 先使用 ik_max_word 分词得到原文词语，过滤长度为1的词
     * 2. 对每个原文词语再使用 my_pinyin_analyzer 分词，存储到 Completion 字段
     * 3. 为每个原文词语创建一条独立的索引记录，如果该词语已存在则不重复添加
     */
    @Async
    public void syncSuggestions(Article article) {
        if (!"PUBLISHED".equals(article.getStatus())) {
            return;
        }
        
        try {
            List<Object> allSuggestions = new ArrayList<>();
            
            // 处理标题字段
            if (StringUtils.isNotBlank(article.getTitle())) {
                List<String> titleTexts = analyzeTextWithIkMaxWord(article.getTitle());
                for (String titleText : titleTexts) {
                    // 查重：检查该原文词语是否已经存在
                    String suggestionId = "title_" + titleText;
                    if (!elasticsearchOperations.exists(suggestionId, TitleSuggestion.class)) {
                        // 对每个原文词语进行拼音分词
                        List<String> pinyinTerms = analyzeText(titleText, "zhishilu_title_suggestion");
                        if (!pinyinTerms.isEmpty()) {
                            TitleSuggestion suggestion = new TitleSuggestion();
                            suggestion.setId(suggestionId);
                            suggestion.setText(titleText);
                            suggestion.setSearchCount(0L);
                            suggestion.setSuggest(new Completion(pinyinTerms.toArray(new String[0])));
                            allSuggestions.add(suggestion);
                        }
                    }
                }
            }
            
            // 处理类别字段
            if (article.getCategories() != null && !article.getCategories().isEmpty()) {
                for (String category : article.getCategories()) {
                    if (StringUtils.isNotBlank(category)) {
                        List<String> categoryTexts = analyzeTextWithIkMaxWord(category);
                        for (String categoryText : categoryTexts) {
                            // 查重
                            String suggestionId = "category_" + categoryText;
                            if (!elasticsearchOperations.exists(suggestionId, CategorySuggestion.class)) {
                                List<String> pinyinTerms = analyzeText(categoryText, "zhishilu_category_suggestion");
                                if (!pinyinTerms.isEmpty()) {
                                    CategorySuggestion suggestion = new CategorySuggestion();
                                    suggestion.setId(suggestionId);
                                    suggestion.setText(categoryText);
                                    suggestion.setSearchCount(0L);
                                    suggestion.setSuggest(new Completion(pinyinTerms.toArray(new String[0])));
                                    allSuggestions.add(suggestion);
                                }
                            }
                        }
                    }
                }
            }

            if (StringUtils.isNotBlank(article.getContent())) {
                List<String> contentTexts = analyzeTextWithIkMaxWord(article.getContent());
                for (String contentText : contentTexts) {
                    // 查重
                    String suggestionId = "content_" + contentText;
                    if (!elasticsearchOperations.exists(suggestionId, ContentSuggestion.class)) {
                        List<String> pinyinTerms = analyzeText(contentText, "zhishilu_content_suggestion");
                        if (!pinyinTerms.isEmpty()) {
                            ContentSuggestion suggestion = new ContentSuggestion();
                            suggestion.setId(suggestionId);
                            suggestion.setText(contentText);
                            suggestion.setSearchCount(0L);
                            suggestion.setSuggest(new Completion(pinyinTerms.toArray(new String[0])));
                            allSuggestions.add(suggestion);
                        }
                    }
                }
            }
            
            // 处理用户名字段
            if (StringUtils.isNotBlank(article.getCreatedBy())) {
                List<String> usernameTexts = analyzeTextWithIkMaxWord(article.getCreatedBy());
                for (String usernameText : usernameTexts) {
                    // 查重
                    String suggestionId = "username_" + usernameText;
                    if (!elasticsearchOperations.exists(suggestionId, UsernameSuggestion.class)) {
                        List<String> pinyinTerms = analyzeText(usernameText, "zhishilu_username_suggestion");
                        if (!pinyinTerms.isEmpty()) {
                            UsernameSuggestion suggestion = new UsernameSuggestion();
                            suggestion.setId(suggestionId);
                            suggestion.setText(usernameText);
                            suggestion.setSearchCount(0L);
                            suggestion.setSuggest(new Completion(pinyinTerms.toArray(new String[0])));
                            allSuggestions.add(suggestion);
                        }
                    }
                }
            }
            
            // 处理地点字段
            if (StringUtils.isNotBlank(article.getLocation())) {
                List<String> locationTexts = analyzeTextWithIkMaxWord(article.getLocation());
                for (String locationText : locationTexts) {
                    // 查重
                    String suggestionId = "location_" + locationText;
                    if (!elasticsearchOperations.exists(suggestionId, LocationSuggestion.class)) {
                        List<String> pinyinTerms = analyzeText(locationText, "zhishilu_location_suggestion");
                        if (!pinyinTerms.isEmpty()) {
                            LocationSuggestion suggestion = new LocationSuggestion();
                            suggestion.setId(suggestionId);
                            suggestion.setText(locationText);
                            suggestion.setSearchCount(0L);
                            suggestion.setSuggest(new Completion(pinyinTerms.toArray(new String[0])));
                            allSuggestions.add(suggestion);
                        }
                    }
                }
            }
            
            // 批量保存所有补全建议
            if (!allSuggestions.isEmpty()) {
                for (Object suggestion : allSuggestions) {
                    elasticsearchOperations.save(suggestion);
                }
                log.info("补全建议同步成功，文章ID: {}, 共创建 {} 条记录", article.getId(), allSuggestions.size());
            }
        } catch (Exception e) {
            log.error("补全建议同步失败，文章ID: {}", article.getId(), e);
        }
    }
    
    /**
     * 对文本进行分词，过滤掉长度小于2的词项，并去重
     * 使用 Elasticsearch 的 my_pinyin_analyzer 分词器，支持拼音索引
     */
    private List<String> analyzeText(String text, String index) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }

        try {
            // 使用 my_pinyin_analyzer 分词器进行分词，生成拼音和中文混合的索引项
            AnalyzeRequest request = AnalyzeRequest.withIndexAnalyzer(
                index,  // 指定索引名，因为自定义分词器定义在索引中
                "my_pinyin_analyzer",   // 使用拼音分词器，生成首字母、全拼、原文等多种索引
                text
            );

            AnalyzeResponse response = restHighLevelClient.indices().analyze(request, RequestOptions.DEFAULT);
            List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();

            // 提取 token 并过滤、去重
            // 过滤单字
            // 去重
            return tokens.stream()
                .map(AnalyzeResponse.AnalyzeToken::getTerm)
                // 过滤单字
                .filter(term -> term.length() >= 2)
                .distinct()  // 去重
                .toList();
        } catch (Exception e) {
            log.error("分词失败: {}", text, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 使用 ik_max_word 分词器对文本进行分词，过滤掉长度为1的词项，并去重
     * 返回原文词语列表
     */
    private List<String> analyzeTextWithIkMaxWord(String text) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }

        try {
            // 使用 ik_max_word 分词器进行分词
            AnalyzeRequest request = AnalyzeRequest.withGlobalAnalyzer(
                "ik_max_word",
                text
            );

            AnalyzeResponse response = restHighLevelClient.indices().analyze(request, RequestOptions.DEFAULT);
            List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();

            // 提取 token 并过滤、去重
            // 过滤长度为1的词
            // 去重

            return tokens.stream()
                .map(AnalyzeResponse.AnalyzeToken::getTerm)
                .filter(term -> term.length() > 1)  // 过滤长度为1的词
                .distinct()  // 去重
                .toList();
        } catch (Exception e) {
            log.error("ik_max_word 分词失败: {}", text, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 更新搜索频率统计
     * 当用户点击搜索时，将用户搜索条件与原文词语匹配，频率加1
     */
    public void updateSearchFrequency(ArticleQueryReq req) {
        try {
            // 处理关键词搜索（全字段搜索）
            if (StringUtils.isNotBlank(req.getKeyword())) {
                updateFrequencyForAllFields(req.getKeyword());
            } else {
                // 处理单个字段搜索
                if (StringUtils.isNotBlank(req.getTitle())) {
                    updateSuggestionFrequency(req.getTitle(), TitleSuggestion.class);
                }
                if (req.getCategories() != null && !req.getCategories().isEmpty()) {
                    for (String category : req.getCategories()) {
                        updateSuggestionFrequency(category, CategorySuggestion.class);
                    }
                }
                if (StringUtils.isNotBlank(req.getContent())) {
                    updateSuggestionFrequency(req.getContent(), ContentSuggestion.class);
                }
                if (StringUtils.isNotBlank(req.getUsername())) {
                    updateSuggestionFrequency(req.getUsername(), UsernameSuggestion.class);
                }
                if (StringUtils.isNotBlank(req.getLocation())) {
                    updateSuggestionFrequency(req.getLocation(), LocationSuggestion.class);
                }
            }
        } catch (Exception e) {
            log.error("更新搜索频率失败", e);
        }
    }
    
    /**
     * 全字段搜索时更新频率
     */
    private void updateFrequencyForAllFields(String keyword) {
        updateSuggestionFrequency(keyword, TitleSuggestion.class);
        updateSuggestionFrequency(keyword, CategorySuggestion.class);
        updateSuggestionFrequency(keyword, ContentSuggestion.class);
        updateSuggestionFrequency(keyword, UsernameSuggestion.class);
        updateSuggestionFrequency(keyword, LocationSuggestion.class);
    }
    
    /**
     * 更新指定类型suggestion的搜索频率
     * 将搜索关键词与原文词语匹配，如果匹配成功则searchCount+1
     */
    private <T> void updateSuggestionFrequency(String searchKeyword, Class<T> suggestionClass) {
        if (StringUtils.isBlank(searchKeyword)) {
            return;
        }
        
        try {
            String fieldType = getFieldTypeFromClass(suggestionClass);
            String suggestionId = fieldType + "_" + searchKeyword;

            // 检查该suggestion是否存在
            if (elasticsearchOperations.exists(suggestionId, suggestionClass)) {
                // 读取当前记录
                Object suggestion = elasticsearchOperations.get(suggestionId, suggestionClass);
                if (suggestion != null) {
                    try {
                        // 使用反射获取和更新searchCount字段
                        Long currentCount = (Long) suggestion.getClass().getMethod("getSearchCount").invoke(suggestion);
                        if (currentCount == null) {
                            currentCount = 0L;
                        }
                        suggestion.getClass().getMethod("setSearchCount", Long.class).invoke(suggestion, currentCount + 1);

                        // 保存更新后的记录
                        elasticsearchOperations.save(suggestion);
                        log.debug("更新搜索频率: {} -> {}, count: {}", fieldType, searchKeyword, currentCount + 1);
                    } catch (Exception e) {
                        log.error("更新searchCount字段失败", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("更新搜索频率失败", e);
        }
    }
    
    /**
     * 根据实体类型获取字段类型名称
     */
    private <T> String getFieldTypeFromClass(Class<T> clazz) {
        if (clazz == TitleSuggestion.class) {
            return "title";
        } else if (clazz == CategorySuggestion.class) {
            return "category";
        } else if (clazz == ContentSuggestion.class) {
            return "content";
        } else if (clazz == UsernameSuggestion.class) {
            return "username";
        } else if (clazz == LocationSuggestion.class) {
            return "location";
        }
        return "unknown";
    }
    
    /**
     * 获取热门搜索关键词（按搜索频率排序）
     * 从所有 suggestion 索引中汇总 searchCount 最高的关键词
     * @param limit 返回数量限制，默认15
     * @return 热门关键词列表
     */
    public List<HotKeywordResp> getHotKeywords(int limit) {
        List<HotKeywordResp> allKeywords = new ArrayList<>();
        
        // 从各个 suggestion 索引中查询有搜索记录的关键词
        allKeywords.addAll(getKeywordsFromIndex(TitleSuggestion.class));
        allKeywords.addAll(getKeywordsFromIndex(CategorySuggestion.class));
        allKeywords.addAll(getKeywordsFromIndex(ContentSuggestion.class));
        allKeywords.addAll(getKeywordsFromIndex(UsernameSuggestion.class));
        allKeywords.addAll(getKeywordsFromIndex(LocationSuggestion.class));
        
        // 按搜索次数降序排序，取前 limit 个
        return allKeywords.stream()
                .sorted(Comparator.comparing(HotKeywordResp::getSearchCount).reversed())
                .limit(limit)
                .toList();
    }
    
    /**
     * 从指定 suggestion 索引中获取有搜索记录的关键词
     */
    private <T> List<HotKeywordResp> getKeywordsFromIndex(Class<T> suggestionClass) {
        List<HotKeywordResp> keywords = new ArrayList<>();
        
        try {
            // 构建查询：searchCount > 0
            NativeSearchQuery query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.rangeQuery("searchCount").gt(0))
                    .withPageable(PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "searchCount")))
                    .build();
            
            SearchHits<T> searchHits = elasticsearchOperations.search(query, suggestionClass);
            
            for (var hit : searchHits.getSearchHits()) {
                T suggestion = hit.getContent();
                try {
                    String text = (String) suggestion.getClass().getMethod("getText").invoke(suggestion);
                    Long count = (Long) suggestion.getClass().getMethod("getSearchCount").invoke(suggestion);
                    if (StringUtils.isNotBlank(text) && count != null && count > 0) {
                        keywords.add(new HotKeywordResp(text, count));
                    }
                } catch (Exception e) {
                    log.error("获取 suggestion 字段失败", e);
                }
            }
        } catch (Exception e) {
            log.error("查询热门关键词失败: {}", suggestionClass.getSimpleName(), e);
        }
        
        return keywords;
    }
}
