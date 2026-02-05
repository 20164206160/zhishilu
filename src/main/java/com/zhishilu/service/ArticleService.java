package com.zhishilu.service;

import com.zhishilu.common.PageResult;
import com.zhishilu.dto.ArticleDTO;
import com.zhishilu.dto.UserDTO;
import com.zhishilu.req.ArticleCreateReq;
import com.zhishilu.req.ArticleQueryReq;
import com.zhishilu.req.ArticleUpdateReq;
import com.zhishilu.resp.ArticleResp;
import com.zhishilu.resp.CategoryStatResp;
import com.zhishilu.entity.Article;
import com.zhishilu.exception.BusinessException;
import com.zhishilu.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.time.LocalDateTime;
import java.util.List;
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
    public ArticleResp create(ArticleCreateReq req, UserDTO currentUser) {
        Article article = new Article();
        article.setTitle(req.getTitle());
        article.setCategory(req.getCategory());
        article.setContent(req.getContent());
        article.setUrl(req.getUrl());
        article.setImages(req.getImages());
        article.setLocation(req.getLocation());
        article.setCreatedBy(currentUser.getUsername());
        article.setCreatorId(currentUser.getId());
        article.setCreatedTime(LocalDateTime.now());
        article.setUpdatedTime(LocalDateTime.now());
        
        article = articleRepository.save(article);
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
        
        if (StringUtils.isNotBlank(req.getTitle())) {
            article.setTitle(req.getTitle());
        }
        if (req.getContent() != null) {
            article.setContent(req.getContent());
        }
        if (req.getUrl() != null) {
            article.setUrl(req.getUrl());
        }
        if (req.getImages() != null) {
            article.setImages(req.getImages());
        }
        article.setUpdatedTime(LocalDateTime.now());
        
        article = articleRepository.save(article);
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
        
        articleRepository.deleteById(id);
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
        
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        
        // 标题模糊查询
        if (StringUtils.isNotBlank(req.getTitle())) {
            boolQuery.must(QueryBuilders.matchQuery("title", req.getTitle()));
        }
        
        // 类别精确查询
        if (StringUtils.isNotBlank(req.getCategory())) {
            boolQuery.must(QueryBuilders.termQuery("category", req.getCategory()));
        }
        
        // 内容模糊查询
        if (StringUtils.isNotBlank(req.getContent())) {
            boolQuery.must(QueryBuilders.matchQuery("content", req.getContent()));
        }
        
        // 用户名精确查询
        if (StringUtils.isNotBlank(req.getUsername())) {
            boolQuery.must(QueryBuilders.termQuery("createdBy", req.getUsername()));
        }
        
        // 地点精确查询
        if (StringUtils.isNotBlank(req.getLocation())) {
            boolQuery.must(QueryBuilders.termQuery("location", req.getLocation()));
        }
        
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(req.getPage(), req.getSize(), 
                        Sort.by(Sort.Direction.DESC, "createdTime")))
                .build();
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class);
        
        log.info("ES查询结果总数: {}", searchHits.getTotalHits());
        
        List<ArticleResp> articles = searchHits.getSearchHits().stream()
                .map(hit -> convertToResp(hit.getContent()))
                .collect(Collectors.toList());
        
        log.info("返回文章列表数量: {}", articles.size());
        
        return PageResult.of(articles, req.getPage(), req.getSize(), 
                searchHits.getTotalHits());
    }
    
    /**
     * 获取用户最常用的类别（前N个）
     */
    public List<CategoryStatResp> getTopCategories(String userId, int limit) {
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
                        .map(bucket -> {
                            CategoryStatResp resp = new CategoryStatResp();
                            resp.setCategory(bucket.getKeyAsString());
                            resp.setCount(bucket.getDocCount());
                            return resp;
                        })
                        .collect(Collectors.toList());
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
}
