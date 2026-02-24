package com.zhishilu.controller;

import com.zhishilu.common.Result;
import com.zhishilu.dto.UserDTO;
import com.zhishilu.resp.CategoryStatResp;
import com.zhishilu.service.CategoryStatsService;
import com.zhishilu.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 类别控制器
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryStatsService categoryStatsService;
    
    /**
     * 获取首页类别导航栏数据
     * 规则：
     * 1. 首先展示用户最常用的类别（按文章数从大到小排序）
     * 2. 不足20个时，补充其他类别（排除用户已使用的类别，按文章数从大到小排序）
     * 3. 最多展示20个
     */
    @GetMapping("/navigation")
    public Result<List<CategoryStatResp>> getCategoryNavigation(
            @RequestParam(defaultValue = "20") int maxCount) {
        UserDTO currentUser = UserContext.getCurrentUser();
        List<CategoryStatResp> categories = categoryStatsService.getCategoryNavigation(currentUser.getId(), maxCount);
        return Result.success(categories);
    }
    
    /**
     * 获取所有类别统计（按文章数从大到小排序）
     */
    @GetMapping("/stats")
    public Result<List<CategoryStatResp>> getAllCategoryStats() {
        List<CategoryStatResp> categories = categoryStatsService.getAllCategoryStats();
        return Result.success(categories);
    }
    
    /**
     * 获取用户最常用的类别
     */
    @GetMapping("/user/top")
    public Result<List<CategoryStatResp>> getUserTopCategories(
            @RequestParam(defaultValue = "10") int limit) {
        UserDTO currentUser = UserContext.getCurrentUser();
        List<CategoryStatResp> categories = categoryStatsService.getUserTopCategories(currentUser.getId(), limit);
        return Result.success(categories);
    }
    
    /**
     * 手动刷新类别统计（管理员功能）
     */
    @PostMapping("/refresh")
    public Result<Void> refreshCategoryStats() {
        categoryStatsService.refreshCategoryStats();
        return Result.success("类别统计刷新成功", null);
    }
}
