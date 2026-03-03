package com.zhishilu.repository;

import com.zhishilu.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 评论数据访问层
 */
public interface CommentRepository extends ElasticsearchRepository<Comment, String> {

    /**
     * 根据文章ID查询所有评论
     */
    List<Comment> findByArticleId(String articleId);

    /**
     * 根据文章ID分页查询根评论（parentId 为 null）
     */
    Page<Comment> findByArticleIdAndParentIdIsNull(String articleId, Pageable pageable);

    /**
     * 根据父评论ID查询回复列表
     */
    List<Comment> findByParentId(String parentId);

    /**
     * 根据父评论ID分页查询回复列表
     */
    Page<Comment> findByParentId(String parentId, Pageable pageable);

    /**
     * 统计文章的根评论数量
     */
    long countByArticleIdAndParentIdIsNull(String articleId);

    /**
     * 统计文章的总评论数量（含回复）
     */
    long countByArticleId(String articleId);
}
