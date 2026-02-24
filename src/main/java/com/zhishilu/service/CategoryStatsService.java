package com.zhishilu.service;

import com.zhishilu.entity.Article;
import com.zhishilu.entity.CategoryStats;
import com.zhishilu.repository.CategoryStatsRepository;
import com.zhishilu.resp.CategoryStatResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 类别统计服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryStatsService {
    
    private final ElasticsearchOperations elasticsearchOperations;
    private final CategoryStatsRepository categoryStatsRepository;
    
    /**
     * 刷新所有类别统计
     * 统计每个类别的文章数量并更新到CategoryStats索引
     */
    public void refreshCategoryStats() {
        log.info("开始刷新类别统计...");
        
        // 1. 从文章索引中聚合统计所有类别的文章数量
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .addAggregation(AggregationBuilders.terms("all_categories").field("categories").size(100))
                .withMaxResults(0)
                .build();
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class);
        
        if (searchHits.hasAggregations()) {
            Aggregations aggregations = (Aggregations) searchHits.getAggregations().aggregations();
            Terms terms = aggregations.get("all_categories");
            
            if (terms != null) {
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                
                // 2. 更新或创建类别统计记录
                for (Terms.Bucket bucket : buckets) {
                    String category = bucket.getKeyAsString();
                    long count = bucket.getDocCount();
                    
                    Optional<CategoryStats> existingStats = categoryStatsRepository.findByCategory(category);
                    
                    CategoryStats stats;
                    if (existingStats.isPresent()) {
                        stats = existingStats.get();
                    } else {
                        stats = new CategoryStats();
                        stats.setCategory(category);
                    }
                    
                    stats.setCount(count);
                    stats.setStatsTime(LocalDateTime.now());
                    categoryStatsRepository.save(stats);
                }
                
                log.info("类别统计刷新完成，共 {} 个类别", buckets.size());
            }
        }
    }
    
    /**
     * 获取所有类别统计（按文章数从大到小排序）
     */
    public List<CategoryStatResp> getAllCategoryStats() {
        // 从文章索引中聚合统计所有类别
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .addAggregation(AggregationBuilders.terms("all_categories").field("categories").size(100))
                .withMaxResults(0)
                .build();
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class);
        
        List<CategoryStatResp> result = new ArrayList<>();
        
        if (searchHits.hasAggregations()) {
            Aggregations aggregations = (Aggregations) searchHits.getAggregations().aggregations();
            Terms terms = aggregations.get("all_categories");
            
            if (terms != null) {
                result = terms.getBuckets().stream()
                        .map(bucket -> {
                            CategoryStatResp resp = new CategoryStatResp();
                            resp.setCategory(bucket.getKeyAsString());
                            resp.setCount(bucket.getDocCount());
                            return resp;
                        })
                        .sorted((a, b) -> Long.compare(b.getCount(), a.getCount())) // 按数量从大到小排序
                        .collect(Collectors.toList());
            }
        }
        
        return result;
    }
    
    /**
     * 获取用户最常用的类别（按文章数从大到小排序）
     */
    public List<CategoryStatResp> getUserTopCategories(String userId, int limit) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("creatorId", userId))
                .addAggregation(AggregationBuilders.terms("user_categories").field("categories").size(limit))
                .withMaxResults(0)
                .build();
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class);
        
        List<CategoryStatResp> result = new ArrayList<>();
        
        if (searchHits.hasAggregations()) {
            Aggregations aggregations = (Aggregations) searchHits.getAggregations().aggregations();
            Terms terms = aggregations.get("user_categories");
            
            if (terms != null) {
                result = terms.getBuckets().stream()
                        .map(bucket -> {
                            CategoryStatResp resp = new CategoryStatResp();
                            resp.setCategory(bucket.getKeyAsString());
                            resp.setCount(bucket.getDocCount());
                            return resp;
                        })
                        .sorted((a, b) -> Long.compare(b.getCount(), a.getCount())) // 按数量从大到小排序
                        .collect(Collectors.toList());
            }
        }
        
        return result;
    }
    
    /**
     * 获取首页类别导航栏数据
     * 规则：
     * 1. 首先展示用户最常用的类别（按文章数从大到小排序）
     * 2. 不足20个时，补充其他类别（排除用户已使用的类别，按文章数从大到小排序）
     * 3. 最多展示20个
     * 
     * @param userId 用户ID
     * @param maxCount 最大展示数量（默认20）
     * @return 类别导航栏列表
     */
    public List<CategoryStatResp> getCategoryNavigation(String userId, int maxCount) {
        // 1. 获取用户最常用的类别
        List<CategoryStatResp> userCategories = getUserTopCategories(userId, maxCount);
        
        // 2. 获取所有类别统计
        List<CategoryStatResp> allCategories = getAllCategoryStats();
        
        // 3. 提取用户已使用的类别名称集合
        Set<String> userCategoryNames = userCategories.stream()
                .map(CategoryStatResp::getCategory)
                .collect(Collectors.toSet());
        
        // 4. 过滤出用户未使用的其他类别
        List<CategoryStatResp> otherCategories = allCategories.stream()
                .filter(cat -> !userCategoryNames.contains(cat.getCategory()))
                .collect(Collectors.toList());
        
        // 5. 合并结果：用户类别在前，其他类别在后
        List<CategoryStatResp> result = new ArrayList<>();
        result.addAll(userCategories);
        result.addAll(otherCategories);
        
        // 6. 限制最大数量
        if (result.size() > maxCount) {
            result = result.subList(0, maxCount);
        }
        
        log.info("获取类别导航栏数据，用户类别数: {}, 其他类别数: {}, 总计: {}", 
                userCategories.size(), otherCategories.size(), result.size());
        
        return result;
    }
    
    /**
     * 增加指定类别的文章计数
     * @param categories 类别列表
     */
    public void incrementCategoryCount(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        
        for (String category : categories) {
            if (category == null || category.trim().isEmpty()) {
                continue;
            }
            
            Optional<CategoryStats> existingStats = categoryStatsRepository.findByCategory(category);
            CategoryStats stats;
            
            if (existingStats.isPresent()) {
                stats = existingStats.get();
                stats.setCount(stats.getCount() + 1);
            } else {
                stats = new CategoryStats();
                stats.setCategory(category);
                stats.setCount(1L);
            }
            
            stats.setStatsTime(LocalDateTime.now());
            categoryStatsRepository.save(stats);
            log.debug("类别 '{}' 计数增加到 {}", category, stats.getCount());
        }
    }
    
    /**
     * 减少指定类别的文章计数
     * @param categories 类别列表
     */
    public void decrementCategoryCount(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        
        for (String category : categories) {
            if (category == null || category.trim().isEmpty()) {
                continue;
            }
            
            Optional<CategoryStats> existingStats = categoryStatsRepository.findByCategory(category);
            
            if (existingStats.isPresent()) {
                CategoryStats stats = existingStats.get();
                long newCount = stats.getCount() - 1;
                
                if (newCount <= 0) {
                    // 如果计数为0或负数，删除该类别统计记录
                    categoryStatsRepository.delete(stats);
                    log.debug("类别 '{}' 计数为0，已删除统计记录", category);
                } else {
                    stats.setCount(newCount);
                    stats.setStatsTime(LocalDateTime.now());
                    categoryStatsRepository.save(stats);
                    log.debug("类别 '{}' 计数减少到 {}", category, newCount);
                }
            }
        }
    }
    
    /**
     * 更新类别计数（用于文章类别变更时）
     * @param oldCategories 旧类别列表
     * @param newCategories 新类别列表
     */
    public void updateCategoryCount(List<String> oldCategories, List<String> newCategories) {
        // 1. 减少旧类别的计数
        decrementCategoryCount(oldCategories);
        
        // 2. 增加新类别的计数
        incrementCategoryCount(newCategories);
        
        log.info("类别计数已更新，旧类别: {}, 新类别: {}", oldCategories, newCategories);
    }
    
    /**
     * 获取指定类别的文章数量（实时从ES统计）
     * @param category 类别名称
     * @return 文章数量
     */
    public long getCategoryCount(String category) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("categories", category))
                .withMaxResults(0)
                .build();
        
        SearchHits<Article> searchHits = elasticsearchOperations.search(query, Article.class);
        return searchHits.getTotalHits();
    }
}
