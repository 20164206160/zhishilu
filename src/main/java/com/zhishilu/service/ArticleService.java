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
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
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
    private final CategoryStatsService categoryStatsService;
    private final UserService userService;
    
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
}
