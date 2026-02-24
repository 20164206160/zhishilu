package com.zhishilu.repository;

import com.zhishilu.entity.CategoryStats;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 类别统计Repository
 */
@Repository
public interface CategoryStatsRepository extends ElasticsearchRepository<CategoryStats, String> {
    
    /**
     * 根据类别名称查找统计记录
     */
    Optional<CategoryStats> findByCategory(String category);
}
