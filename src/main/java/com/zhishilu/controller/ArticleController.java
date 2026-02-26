package com.zhishilu.controller;

import com.zhishilu.common.PageResult;
import com.zhishilu.common.Result;
import com.zhishilu.req.ArticleCreateReq;
import com.zhishilu.req.ArticleQueryReq;
import com.zhishilu.req.ArticleUpdateReq;
import com.zhishilu.req.DraftSaveReq;
import com.zhishilu.resp.ArticleResp;
import com.zhishilu.resp.CategoryStatResp;
import com.zhishilu.resp.DraftResp;
import com.zhishilu.resp.SearchSuggestionResp;
import com.zhishilu.dto.UserDTO;
import com.zhishilu.service.ArticleService;
import com.zhishilu.service.IpLocationService;
import com.zhishilu.util.IpUtil;
import com.zhishilu.util.UserContext;
import javax.servlet.http.HttpServletRequest;
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
    private final IpLocationService ipLocationService;
    
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

    /**
     * 根据用户 IP 获取地理位置
     * 用于发布内容时自动填充位置信息
     */
    @GetMapping("/location")
    public Result<String> getLocationByIp(HttpServletRequest request) {
        String ip = IpUtil.getClientIp(request);
        String location = ipLocationService.getLocation(ip);
        if (location == null || location.isEmpty()) {
            return Result.success("无法获取位置信息");
        }
        return Result.success(location);
    }
    
    /**
     * 保存草稿（新建或更新）
     */
    @PostMapping("/draft")
    public Result<DraftResp> saveDraft(@RequestBody DraftSaveReq req) {
        UserDTO currentUser = UserContext.getCurrentUser();
        DraftResp resp = articleService.saveDraft(req, currentUser);
        return Result.success("保存成功", resp);
    }
    
    /**
     * 获取当前用户的草稿列表
     */
    @GetMapping("/draft/list")
    public Result<List<DraftResp>> getUserDrafts() {
        UserDTO currentUser = UserContext.getCurrentUser();
        List<DraftResp> drafts = articleService.getUserDrafts(currentUser.getId());
        return Result.success(drafts);
    }
    
    /**
     * 获取草稿详情
     */
    @GetMapping("/draft/{id}")
    public Result<DraftResp> getDraftById(@PathVariable String id) {
        UserDTO currentUser = UserContext.getCurrentUser();
        DraftResp resp = articleService.getDraftById(id, currentUser);
        return Result.success(resp);
    }
    
    /**
     * 删除草稿
     */
    @DeleteMapping("/draft/{id}")
    public Result<Void> deleteDraft(@PathVariable String id) {
        UserDTO currentUser = UserContext.getCurrentUser();
        articleService.deleteDraft(id, currentUser);
        return Result.success("删除成功", null);
    }
    
    /**
     * 将草稿发布为正式文章
     */
    @PostMapping("/draft/{id}/publish")
    public Result<ArticleResp> publishDraft(@PathVariable String id, @Valid @RequestBody ArticleCreateReq req) {
        UserDTO currentUser = UserContext.getCurrentUser();
        ArticleResp resp = articleService.publishDraft(id, req, currentUser);
        return Result.success("发布成功", resp);
    }
    
    /**
     * 搜索补全（自动完成）
     * @param keyword 搜索关键词
     * @param field 搜索字段（all/title/category/content/username/location）
     * @return 各字段的补全建议
     */
    @GetMapping("/suggestions")
    public Result<SearchSuggestionResp> getSearchSuggestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "all") String field) {
        SearchSuggestionResp resp = articleService.getSearchSuggestions(keyword, field);
        return Result.success(resp);
    }
}
