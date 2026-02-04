package com.zhishilu.controller;

import com.zhishilu.common.Result;
import com.zhishilu.dto.LoginDTO;
import com.zhishilu.dto.RegisterDTO;
import com.zhishilu.entity.User;
import com.zhishilu.service.UserService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterDTO dto) {
        User user = userService.register(dto);
        return Result.success("注册成功", user);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        Map<String, Object> result = userService.login(dto);
        return Result.success("登录成功", result);
    }
    
    /**
     * 未授权提示
     */
    @GetMapping("/unauthorized")
    public Result<Void> unauthorized() {
        return Result.unauthorized();
    }
}
