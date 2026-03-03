package com.zhishilu.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 发布评论请求
 */
@Data
public class CommentCreateReq {

    /**
     * 文章ID（必填）
     */
    @NotBlank(message = "文章ID不能为空")
    private String articleId;

    /**
     * 评论内容（必填，最大500字）
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;

    /**
     * 父评论ID（选填，回复时传入根评论的ID）
     */
    private String parentId;

    /**
     * 被回复的评论ID（选填，楼中楼时使用）
     */
    private String replyToId;

    /**
     * 被回复的用户名（选填）
     */
    private String replyToUser;
}
