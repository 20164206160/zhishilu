package com.zhishilu.repository;

import com.zhishilu.entity.OperationLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 操作日志Repository
 */
@Repository
public interface OperationLogRepository extends ElasticsearchRepository<OperationLog, String> {
}
