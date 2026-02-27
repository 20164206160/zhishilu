package com.zhishilu.config;

import com.zhishilu.entity.Article;
import com.zhishilu.entity.CategoryStats;
import com.zhishilu.entity.CategorySuggestion;
import com.zhishilu.entity.ContentSuggestion;
import com.zhishilu.entity.LocationSuggestion;
import com.zhishilu.entity.OperationLog;
import com.zhishilu.entity.TitleSuggestion;
import com.zhishilu.entity.User;
import com.zhishilu.entity.UsernameSuggestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * ES索引初始化
 * 使用自定义 mapping JSON 方式创建索引，确保字段属性完整映射
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer implements CommandLineRunner {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void run(String... args) {
        createIndexWithCustomMapping(Article.class, "article-mapping.json");
        createIndexWithCustomMapping(User.class, "user-mapping.json");
        createIndexWithCustomMapping(OperationLog.class, "operation-log-mapping.json");
        createIndexWithCustomMapping(CategoryStats.class, "category-stats-mapping.json");
        
        // 创建5个独立的搜索补全索引
        createIndexWithCustomMapping(TitleSuggestion.class, "title-suggestion-mapping.json");
        createIndexWithCustomMapping(CategorySuggestion.class, "category-suggestion-mapping.json");
        createIndexWithCustomMapping(ContentSuggestion.class, "content-suggestion-mapping.json");
        createIndexWithCustomMapping(UsernameSuggestion.class, "username-suggestion-mapping.json");
        createIndexWithCustomMapping(LocationSuggestion.class, "location-suggestion-mapping.json");
        
        log.info("Elasticsearch索引初始化完成");
    }

    /**
     * 使用自定义 mapping JSON 创建索引
     * 解决 Spring Data ES 注解无法完整映射某些字段属性的问题
     *
     * @param clazz          实体类
     * @param mappingPath    mapping JSON 文件路径
     */
    private void createIndexWithCustomMapping(Class<?> clazz, String mappingPath) {
        IndexOperations indexOps = elasticsearchOperations.indexOps(clazz);
        if (!indexOps.exists()) {
            try {
                // 先创建索引（settings 会从 @Setting 注解加载）
                indexOps.create();

                // 从 classpath 读取自定义 mapping JSON
                ClassPathResource mappingResource = new ClassPathResource(mappingPath);
                String mappingJson = StreamUtils.copyToString(mappingResource.getInputStream(), StandardCharsets.UTF_8);

                // 应用自定义 mapping
                Document mapping = Document.parse(mappingJson);
                indexOps.putMapping(mapping);

                log.info("创建索引 [{}] 并应用自定义 mapping: {}",
                        indexOps.getIndexCoordinates().getIndexName(), mappingPath);
            } catch (IOException e) {
                log.error("创建索引 [{}] 失败: {}", clazz.getSimpleName(), e.getMessage(), e);
                throw new RuntimeException("创建索引失败: " + clazz.getSimpleName(), e);
            }
        }
    }
}
