package com.zhishilu.controller;

import com.zhishilu.common.PageResult;
import com.zhishilu.common.Result;
import com.zhishilu.req.ArticleCreateReq;
import com.zhishilu.req.ArticleQueryReq;
import com.zhishilu.req.ArticleUpdateReq;
import com.zhishilu.resp.ArticleResp;
import com.zhishilu.resp.CategoryStatResp;
import com.zhishilu.dto.UserDTO;
import com.zhishilu.service.ArticleService;
import com.zhishilu.util.UserContext;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {
    
    private final ArticleService articleService;
    
    /**
     * 创建文章
     */
    @PostMapping
    public Result<ArticleResp> create(@Valid @RequestBody ArticleCreateReq req) {
        UserDTO currentUser = UserContext.getCurrentUser();
        ArticleResp resp = articleService.create(req, currentUser);
        return Result.success("创建成功", resp);
    }
    
    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public Result<ArticleResp> update(@PathVariable String id, @Valid @RequestBody ArticleUpdateReq req) {
        UserDTO currentUser = UserContext.getCurrentUser();
        ArticleResp resp = articleService.update(id, req, currentUser);
        return Result.success("更新成功", resp);
    }
    
    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        UserDTO currentUser = UserContext.getCurrentUser();
        articleService.delete(id, currentUser);
        return Result.success("删除成功", null);
    }
    
    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public Result<ArticleResp> getById(@PathVariable String id) {
        ArticleResp resp = articleService.getById(id);
        return Result.success(resp);
    }
    
    /**
     * 分页查询文章
     */
    @GetMapping("/list")
    public Result<PageResult<ArticleResp>> list(ArticleQueryReq req) {
        PageResult<ArticleResp> result = articleService.search(req);
        return Result.success(result);
    }
    
    /**
     * 获取用户最常用的类别（用于推荐）
     */
    @GetMapping("/categories/top")
    public Result<List<CategoryStatResp>> getTopCategories(
            @RequestParam(defaultValue = "10") int limit) {
        UserDTO currentUser = UserContext.getCurrentUser();
        List<CategoryStatResp> categories = articleService.getTopCategories(currentUser.getId(), limit);
        return Result.success(categories);
    }
}
