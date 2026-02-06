package com.zhishilu.resp;

import lombok.Data;

/**
 * 登录响应对象
 */
@Data
public class LoginResp {
    
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserResp user;
}
