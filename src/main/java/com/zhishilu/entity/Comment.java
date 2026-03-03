package com.zhishilu.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@Document(indexName = "zhishilu_comment")
@Mapping(mappingPath = "comment-mapping.json")
@Setting(shards = 1, replicas = 0)
public class Comment {

    @Id
    private String id;

    /**
     * 所属文章ID
     */
    @Field(type = FieldType.Keyword)
    private String articleId;

    /**
     * 评论内容
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 评论者用户名
     */
    @Field(type = FieldType.Keyword)
    private String createdBy;

    /**
     * 评论者ID
     */
    @Field(type = FieldType.Keyword)
    private String creatorId;

    /**
     * 评论者头像
     */
    @Field(type = FieldType.Keyword, index = false)
    private String creatorAvatar;

    /**
     * 父评论ID（null 表示根评论，非 null 表示是某条根评论的回复）
     */
    @Field(type = FieldType.Keyword)
    private String parentId;

    /**
     * 被回复的评论ID（回复楼中楼时使用）
     */
    @Field(type = FieldType.Keyword)
    private String replyToId;

    /**
     * 被回复的用户名
     */
    @Field(type = FieldType.Keyword)
    private String replyToUser;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdTime;

    /**
     * 点赞数
     */
    @Field(type = FieldType.Long)
    private Long likeCount;
}
