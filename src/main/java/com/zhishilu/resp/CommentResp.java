package com.zhishilu.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应对象
 */
@Data
public class CommentResp {

    /**
     * 评论ID
     */
    private String id;

    /**
     * 所属文章ID
     */
    private String articleId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论者用户名
     */
    private String createdBy;

    /**
     * 评论者ID
     */
    private String creatorId;

    /**
     * 评论者头像
     */
    private String creatorAvatar;

    /**
     * 父评论ID（null 为根评论）
     */
    private String parentId;

    /**
     * 被回复的评论ID
     */
    private String replyToId;

    /**
     * 被回复的用户名
     */
    private String replyToUser;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 回复列表（仅根评论有此字段）
     */
    private List<CommentResp> replies;

    /**
     * 该根评论下的总回复数（用于展示"查看全部X条回复"）
     */
    private Long totalReplyCount;
}
