package com.zhishilu.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Document(indexName = "zhishilu_user")
@Setting(shards = 1, replicas = 0)
public class User {
    
    @Id
    private String id;
    
    /**
     * 用户名
     */
    @Field(type = FieldType.Keyword)
    private String username;
    
    /**
     * 密码（加密存储）
     */
    @Field(type = FieldType.Keyword, index = false)
    private String password;
    
    /**
     * 昵称
     */
    @Field(type = FieldType.Keyword)
    private String nickname;
    
    /**
     * 邮箱
     */
    @Field(type = FieldType.Keyword)
    private String email;
    
    /**
     * 头像路径
     */
    @Field(type = FieldType.Keyword)
    private String avatar;
    
    /**
     * 状态：1-正常，0-禁用
     */
    @Field(type = FieldType.Integer)
    private Integer status;
    
    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdTime;
    
    /**
     * 最后登录时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime lastLoginTime;
}
