package com.zhishilu.controller;

import com.zhishilu.common.Result;
import com.zhishilu.req.LoginReq;
import com.zhishilu.req.RegisterReq;
import com.zhishilu.resp.LoginResp;
import com.zhishilu.resp.UserResp;
import com.zhishilu.service.UserService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public Result<UserResp> register(@Valid @RequestBody RegisterReq req) {
        UserResp resp = userService.register(req);
        return Result.success("注册成功", resp);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResp> login(@Valid @RequestBody LoginReq req) {
        LoginResp resp = userService.login(req);
        return Result.success("登录成功", resp);
    }
    
    /**
     * 未授权提示
     */
    @GetMapping("/unauthorized")
    public Result<Void> unauthorized() {
        return Result.unauthorized();
    }
}
