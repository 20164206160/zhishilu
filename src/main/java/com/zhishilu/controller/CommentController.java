package com.zhishilu.controller;

import com.zhishilu.common.PageResult;
import com.zhishilu.common.Result;
import com.zhishilu.dto.UserDTO;
import com.zhishilu.req.CommentCreateReq;
import com.zhishilu.resp.CommentResp;
import com.zhishilu.service.CommentService;
import com.zhishilu.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 发布评论或回复
     */
    @PostMapping("/create")
    public Result<CommentResp> create(@Valid @RequestBody CommentCreateReq req) {
        UserDTO currentUser = UserContext.getCurrentUser();
        CommentResp resp = commentService.createComment(req, currentUser);
        return Result.success("评论成功", resp);
    }

    /**
     * 获取文章评论列表（含每条根评论的3条回复预览）
     */
    @GetMapping("/list")
    public Result<PageResult<CommentResp>> list(
            @RequestParam String articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<CommentResp> result = commentService.getCommentsByArticleId(articleId, page, size);
        return Result.success(result);
    }

    /**
     * 获取某条根评论的所有回复（分页）
     */
    @GetMapping("/replies")
    public Result<PageResult<CommentResp>> replies(
            @RequestParam String parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<CommentResp> result = commentService.getRepliesByParentId(parentId, page, size);
        return Result.success(result);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable String id) {
        UserDTO currentUser = UserContext.getCurrentUser();
        commentService.deleteComment(id, currentUser);
        return Result.success("删除成功", null);
    }

    /**
     * 点赞评论
     */
    @PostMapping("/like/{id}")
    public Result<CommentResp> like(@PathVariable String id) {
        UserDTO currentUser = UserContext.getCurrentUser();
        CommentResp resp = commentService.likeComment(id, currentUser);
        return Result.success(resp);
    }

    /**
     * 获取文章总评论数
     */
    @GetMapping("/count")
    public Result<Long> count(@RequestParam String articleId) {
        long total = commentService.countByArticleId(articleId);
        return Result.success(total);
    }
}
