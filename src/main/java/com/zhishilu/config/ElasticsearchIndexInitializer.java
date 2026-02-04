package com.zhishilu.config;

import com.zhishilu.entity.Article;
import com.zhishilu.entity.OperationLog;
import com.zhishilu.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * ES索引初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer implements CommandLineRunner {
    
    private final ElasticsearchOperations elasticsearchOperations;
    
    @Override
    public void run(String... args) {
        createIndexIfNotExists(Article.class);
        createIndexIfNotExists(User.class);
        createIndexIfNotExists(OperationLog.class);
        log.info("Elasticsearch索引初始化完成");
    }
    
    private void createIndexIfNotExists(Class<?> clazz) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(clazz);
        if (!indexOps.exists()) {
            indexOps.create();
            indexOps.putMapping();
            log.info("创建索引: {}", clazz.getSimpleName());
        }
    }
}
