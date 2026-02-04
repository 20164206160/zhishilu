package com.zhishilu.service;

import com.zhishilu.common.PageResult;
import com.zhishilu.dto.ArticleCreateDTO;
import com.zhishilu.dto.ArticleQueryDTO;
import com.zhishilu.dto.ArticleUpdateDTO;
import com.zhishilu.entity.Article;
import com.zhishilu.entity.User;
import com.zhishilu.exception.BusinessException;
import com.zhishilu.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    
    /**
     * 创建文章
     */
    public Article create(ArticleCreateDTO dto, User currentUser) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setCategory(dto.getCategory());
        article.setContent(dto.getContent());
        article.setUrl(dto.getUrl());
        article.setImages(dto.getImages());
        article.setLocation(dto.getLocation());
        article.setCreatedBy(currentUser.getUsername());
        article.setCreatorId(currentUser.getId());
        article.setCreatedTime(LocalDateTime.now());
        article.setUpdatedTime(LocalDateTime.now());
        
        return articleRepository.save(article);
    }
    
    /**
     * 更新文章
     */
    public Article update(String id, ArticleUpdateDTO dto, User currentUser) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        
        // 权限检查：只有创建者可以修改
        if (!article.getCreatorId().equals(currentUser.getId())) {
            throw new BusinessException(403, "没有权限修改此文章");
        }
        
        if (StringUtils.isNotBlank(dto.getTitle())) {
            article.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            article.setContent(dto.getContent());
        }
        if (dto.getUrl() != null) {
            article.setUrl(dto.getUrl());
        }
        if (dto.getImages() != null) {
            article.setImages(dto.getImages());
        }
        article.setUpdatedTime(LocalDateTime.now());
        
        return articleRepository.save(article);
    }
    
    /**
     * 删除文章
     */
    public void delete(String id, User currentUser) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        
        // 权限检查：只有创建者可以删除
        if (!article.getCreatorId().equals(currentUser.getId())) {
            throw new BusinessException(403, "没有权限删除此文章");
        }
        
        articleRepository.deleteById(id);
    }
    
    /**
     * 获取文章详情
     */
    public Article getById(String id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
    }
    
    /**
     * 分页查询文章
     */
    public PageResult<Article> search(ArticleQueryDTO queryDTO) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        
        // 标题模糊查询
        if (StringUtils.isNotBlank(queryDTO.getTitle())) {
            boolQuery.must(QueryBuilders.matchQuery("title", queryDTO.getTitle()));
        }
        
        // 类别精确查询
        if (StringUtils.isNotBlank(queryDTO.getCategory())) {
            boolQuery.must(QueryBuilders.termQuery("category", queryDTO.getCategory()));
        }
        
        // 内容模糊查询
        if (StringUtils.isNotBlank(queryDTO.getContent())) {
            boolQuery.must(QueryBuilders.matchQuery("content", queryDTO.getContent()));
        }
        
        // 用户名精确查询
        if (StringUtils.isNotBlank(queryDTO.getUsername())) {
            boolQuery.must(QueryBuilders.termQuery("createdBy", queryDTO.getUsername()));
        }
        
        // 地点精确查询
        if (StringUtils.isNotBlank(queryDTO.getLocation())) {
            boolQuery.must(QueryBuilders.termQuery("location", queryDTO.getLocation()));
        }
        
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(queryDTO.getPage(), queryDTO.getSize(), 
                        Sort.by(Sort.Direction.DESC, "createdTime")))
                .build();
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class);
        
        List<Article> articles = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        
        return PageResult.of(articles, queryDTO.getPage(), queryDTO.getSize(), 
                searchHits.getTotalHits());
    }
    
    /**
     * 获取用户最常用的类别（前10个）
     */
    public List<Map<String, Object>> getTopCategories(String userId, int limit) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("creatorId", userId))
                .addAggregation(AggregationBuilders.terms("top_categories").field("category").size(limit))
                .withMaxResults(0)
                .build();
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class);
        
        // 从聚合结果中提取类别统计
        if (searchHits.hasAggregations()) {
            Aggregations aggregations = (Aggregations) searchHits.getAggregations().aggregations();
            Terms terms = aggregations.get("top_categories");
            if (terms != null) {
                return terms.getBuckets().stream()
                        .map(bucket -> Map.<String, Object>of(
                                "category", bucket.getKeyAsString(),
                                "count", bucket.getDocCount()))
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}
