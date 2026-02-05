package com.zhishilu.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求对象
 */
@Data
public class LoginReq {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
