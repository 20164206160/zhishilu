package com.zhishilu.repository;

import com.zhishilu.entity.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文章Repository
 */
@Repository
public interface ArticleRepository extends ElasticsearchRepository<Article, String> {
    
    /**
     * 根据创建者ID查询
     */
    List<Article> findByCreatorId(String creatorId);
    
    /**
     * 根据类别查询
     */
    List<Article> findByCategory(String category);
    
    /**
     * 根据创建者ID和类别查询
     */
    List<Article> findByCreatorIdAndCategory(String creatorId, String category);
}
