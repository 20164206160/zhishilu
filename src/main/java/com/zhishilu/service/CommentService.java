package com.zhishilu.service;

import com.zhishilu.common.PageResult;
import com.zhishilu.dto.UserDTO;
import com.zhishilu.entity.Comment;
import com.zhishilu.exception.BusinessException;
import com.zhishilu.repository.CommentRepository;
import com.zhishilu.req.CommentCreateReq;
import com.zhishilu.resp.CommentResp;
import com.zhishilu.util.AdminUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;

    /**
     * 发布评论或回复
     */
    public CommentResp createComment(CommentCreateReq req, UserDTO currentUser) {
        Comment comment = new Comment();
        comment.setArticleId(req.getArticleId());
        comment.setContent(req.getContent());
        comment.setCreatedBy(currentUser.getUsername());
        comment.setCreatorId(currentUser.getId());
        comment.setParentId(req.getParentId());
        comment.setReplyToId(req.getReplyToId());
        comment.setReplyToUser(req.getReplyToUser());
        comment.setCreatedTime(LocalDateTime.now());
        comment.setLikeCount(0L);

        // 获取发布者头像
        try {
            var user = userService.getById(currentUser.getId());
            if (user != null) {
                comment.setCreatorAvatar(user.getAvatar());
            }
        } catch (Exception e) {
            log.debug("获取用户头像失败, userId: {}", currentUser.getId());
        }

        comment = commentRepository.save(comment);
        log.info("评论创建成功，ID: {}, 文章ID: {}", comment.getId(), comment.getArticleId());

        return convertToResp(comment);
    }

    /**
     * 分页获取文章的根评论（每条根评论附带最多3条回复预览）
     */
    public PageResult<CommentResp> getCommentsByArticleId(String articleId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        Page<Comment> commentPage = commentRepository.findByArticleIdAndParentIdIsNull(articleId, pageable);

        List<CommentResp> list = commentPage.getContent().stream()
                .map(comment -> {
                    CommentResp resp = convertToResp(comment);

                    // 查询该根评论的所有回复
                    List<Comment> allReplies = commentRepository.findByParentId(comment.getId());
                    long totalReplyCount = allReplies.size();
                    resp.setTotalReplyCount(totalReplyCount);

                    // 默认只附带最多3条回复预览（按时间升序）
                    List<CommentResp> replyPreviews = allReplies.stream()
                            .sorted((a, b) -> a.getCreatedTime().compareTo(b.getCreatedTime()))
                            .limit(3)
                            .map(this::convertToResp)
                            .collect(Collectors.toList());
                    resp.setReplies(replyPreviews);

                    return resp;
                })
                .collect(Collectors.toList());

        return PageResult.of(list, page, size, commentPage.getTotalElements());
    }

    /**
     * 分页获取某条根评论的所有回复
     */
    public PageResult<CommentResp> getRepliesByParentId(String parentId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdTime"));
        Page<Comment> replyPage = commentRepository.findByParentId(parentId, pageable);

        List<CommentResp> list = replyPage.getContent().stream()
                .map(this::convertToResp)
                .collect(Collectors.toList());

        return PageResult.of(list, page, size, replyPage.getTotalElements());
    }

    /**
     * 删除评论（本人或管理员可操作）
     */
    public void deleteComment(String id, UserDTO currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        if (!comment.getCreatorId().equals(currentUser.getId()) && !AdminUtil.isAdmin(currentUser)) {
            throw new BusinessException(403, "没有权限删除此评论");
        }

        // 如果是根评论，同时删除其所有回复
        if (comment.getParentId() == null) {
            List<Comment> replies = commentRepository.findByParentId(id);
            if (!replies.isEmpty()) {
                commentRepository.deleteAll(replies);
                log.info("删除根评论的回复，共 {} 条，parentId: {}", replies.size(), id);
            }
        }

        commentRepository.deleteById(id);
        log.info("评论删除成功，ID: {}", id);
    }

    /**
     * 点赞评论
     */
    public CommentResp likeComment(String id, UserDTO currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        long current = comment.getLikeCount() == null ? 0L : comment.getLikeCount();
        comment.setLikeCount(current + 1);
        comment = commentRepository.save(comment);
        log.info("评论点赞，ID: {}, 当前点赞数: {}", id, comment.getLikeCount());

        return convertToResp(comment);
    }

    /**
     * 获取文章总评论数（含回复）
     */
    public long countByArticleId(String articleId) {
        return commentRepository.countByArticleId(articleId);
    }

    /**
     * 将实体转换为响应对象
     */
    private CommentResp convertToResp(Comment comment) {
        CommentResp resp = new CommentResp();
        BeanUtils.copyProperties(comment, resp);
        return resp;
    }
}
