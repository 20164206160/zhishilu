package com.zhishilu.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户DTO - Service层间传递使用
 */
@Data
public class UserDTO {
    
    /**
     * 用户ID
     */
    private String id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 用户状态 0-禁用 1-正常
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
}
