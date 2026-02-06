package com.zhishilu.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@Document(indexName = "zhishilu_log")
@Setting(shards = 1, replicas = 0)
public class OperationLog {
    
    @Id
    private String id;
    
    /**
     * 用户名
     */
    @Field(type = FieldType.Keyword)
    private String username;
    
    /**
     * 用户ID
     */
    @Field(type = FieldType.Keyword)
    private String userId;
    
    /**
     * 请求路径
     */
    @Field(type = FieldType.Keyword)
    private String path;
    
    /**
     * 请求方法
     */
    @Field(type = FieldType.Keyword)
    private String method;
    
    /**
     * 请求参数
     */
    @Field(type = FieldType.Text, index = false)
    private String params;
    
    /**
     * IP地址
     */
    @Field(type = FieldType.Keyword)
    private String ip;
    
    /**
     * 响应状态码
     */
    @Field(type = FieldType.Integer)
    private Integer statusCode;
    
    /**
     * 执行时间（毫秒）
     */
    @Field(type = FieldType.Long)
    private Long executionTime;
    
    /**
     * 操作时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime operationTime;
}
