package com.zhishilu.controller;

import com.zhishilu.common.PageResult;
import com.zhishilu.common.Result;
import com.zhishilu.dto.ArticleCreateDTO;
import com.zhishilu.dto.ArticleQueryDTO;
import com.zhishilu.dto.ArticleUpdateDTO;
import com.zhishilu.entity.Article;
import com.zhishilu.entity.User;
import com.zhishilu.service.ArticleService;
import com.zhishilu.util.UserContext;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public Result<Article> create(@Valid @RequestBody ArticleCreateDTO dto) {
        User currentUser = UserContext.getCurrentUser();
        Article article = articleService.create(dto, currentUser);
        return Result.success("创建成功", article);
    }
    
    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public Result<Article> update(@PathVariable String id, @Valid @RequestBody ArticleUpdateDTO dto) {
        User currentUser = UserContext.getCurrentUser();
        Article article = articleService.update(id, dto, currentUser);
        return Result.success("更新成功", article);
    }
    
    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        User currentUser = UserContext.getCurrentUser();
        articleService.delete(id, currentUser);
        return Result.success("删除成功", null);
    }
    
    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public Result<Article> getById(@PathVariable String id) {
        Article article = articleService.getById(id);
        return Result.success(article);
    }
    
    /**
     * 分页查询文章
     */
    @GetMapping("/list")
    public Result<PageResult<Article>> list(ArticleQueryDTO queryDTO) {
        PageResult<Article> result = articleService.search(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 获取用户最常用的类别（用于推荐）
     */
    @GetMapping("/categories/top")
    public Result<List<Map<String, Object>>> getTopCategories(
            @RequestParam(defaultValue = "10") int limit) {
        User currentUser = UserContext.getCurrentUser();
        List<Map<String, Object>> categories = articleService.getTopCategories(currentUser.getId(), limit);
        return Result.success(categories);
    }
}
