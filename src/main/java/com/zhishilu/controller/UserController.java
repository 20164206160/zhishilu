package com.zhishilu.controller;

import com.zhishilu.common.PageResult;
import com.zhishilu.common.Result;
import com.zhishilu.dto.UserDTO;
import com.zhishilu.resp.UserResp;
import com.zhishilu.config.AdminConfig;
import com.zhishilu.service.FileService;
import com.zhishilu.service.UserService;
import com.zhishilu.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileService fileService;
    private final AdminConfig adminConfig;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<UserResp> getCurrentUser() {
        UserDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.unauthorized();
        }
        UserResp resp = new UserResp();
        resp.setId(currentUser.getId());
        resp.setUsername(currentUser.getUsername());
        resp.setNickname(currentUser.getNickname());
        resp.setEmail(currentUser.getEmail());
        resp.setAvatar(currentUser.getAvatar());
        resp.setStatus(currentUser.getStatus());
        resp.setCreatedTime(currentUser.getCreatedTime());
        resp.setLastLoginTime(currentUser.getLastLoginTime());
        return Result.success(resp);
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        UserDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.unauthorized();
        }

        try {
            // 删除旧头像
            if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                fileService.deleteAvatar(currentUser.getAvatar());
            }

            // 上传新头像
            String avatarFileName = fileService.uploadAvatar(file, currentUser.getId());

            // 更新用户头像信息
            userService.updateAvatar(currentUser.getId(), avatarFileName);

            log.info("用户 {} 更新头像成功: {}", currentUser.getUsername(), avatarFileName);
            return Result.success("头像上传成功", Map.of("avatar", avatarFileName));
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 检查当前用户是否为管理员
     */
    @GetMapping("/is-admin")
    public Result<Boolean> isAdmin() {
        UserDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.unauthorized();
        }
        boolean isAdmin = adminConfig.isAdmin(currentUser.getUsername());
        return Result.success(isAdmin);
    }

    /**
     * 获取管理员用户名列表
     */
    @GetMapping("/admin/list")
    public Result<java.util.Set<String>> getAdminUsernames() {
        UserDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.unauthorized();
        }
        // 只有管理员可以查看管理员列表
        if (!adminConfig.isAdmin(currentUser.getUsername())) {
            return Result.error(403, "没有权限查看管理员列表");
        }
        return Result.success(adminConfig.getAdminUsernameSet());
    }
    
    // ==================== 用户管理接口（管理员专用） ====================
    
    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<UserResp>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UserDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.unauthorized();
        }
        // 只有管理员可以查看用户列表
        if (!adminConfig.isAdmin(currentUser.getUsername())) {
            return Result.error(403, "没有权限查看用户列表");
        }
        Pageable pageable = PageRequest.of(page, size);
        var userPage = userService.getUserList(pageable);
        return Result.success(PageResult.of(
            userPage.getContent(),
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements()
        ));
    }
    
    /**
     * 根据授权状态获取用户列表
     */
    @GetMapping("/list/by-authorized")
    public Result<List<UserResp>> getUsersByAuthorized(@RequestParam Integer authorized) {
        UserDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.unauthorized();
        }
        // 只有管理员可以查看用户列表
        if (!adminConfig.isAdmin(currentUser.getUsername())) {
            return Result.error(403, "没有权限查看用户列表");
        }
        List<UserResp> users = userService.getUsersByAuthorized(authorized);
        return Result.success(users);
    }
    
    /**
     * 授权用户
     */
    @PostMapping("/{userId}/authorize")
    public Result<Void> authorizeUser(@PathVariable String userId) {
        UserDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.unauthorized();
        }
        // 只有管理员可以授权用户
        if (!adminConfig.isAdmin(currentUser.getUsername())) {
            return Result.error(403, "没有权限授权用户");
        }
        userService.authorizeUser(userId);
        return Result.success("授权成功", null);
    }
    
    /**
     * 取消授权用户
     */
    @PostMapping("/{userId}/unauthorize")
    public Result<Void> unauthorizeUser(@PathVariable String userId) {
        UserDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.unauthorized();
        }
        // 只有管理员可以取消授权用户
        if (!adminConfig.isAdmin(currentUser.getUsername())) {
            return Result.error(403, "没有权限取消授权用户");
        }
        // 不能取消管理员自己的授权
        if (currentUser.getId().equals(userId)) {
            return Result.error(403, "不能取消自己的授权");
        }
        userService.unauthorizeUser(userId);
        return Result.success("取消授权成功，用户已被强制登出", null);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable String userId) {
        UserDTO currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            return Result.unauthorized();
        }
        // 只有管理员可以删除用户
        if (!adminConfig.isAdmin(currentUser.getUsername())) {
            return Result.error(403, "没有权限删除用户");
        }
        // 不能删除自己
        if (currentUser.getId().equals(userId)) {
            return Result.error(403, "不能删除自己");
        }
        // 不能删除其他管理员
        UserDTO targetUser = userService.getById(userId);
        if (targetUser.getAdmin() != null && targetUser.getAdmin()) {
            return Result.error(403, "不能删除管理员用户");
        }
        userService.deleteUser(userId);
        return Result.success("删除用户成功，用户已被强制登出", null);
    }
}
