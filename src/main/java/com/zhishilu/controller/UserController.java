package com.zhishilu.controller;

import com.zhishilu.common.Result;
import com.zhishilu.dto.UserDTO;
import com.zhishilu.resp.UserResp;
import com.zhishilu.service.FileService;
import com.zhishilu.service.UserService;
import com.zhishilu.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
}
