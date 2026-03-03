package com.zhishilu.util;

import com.zhishilu.config.AdminConfig;
import com.zhishilu.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 管理员工具类
 * 用于判断用户是否为管理员
 */
@Component
@RequiredArgsConstructor
public class AdminUtil {
    
    private final AdminConfig adminConfig;
    
    private static AdminConfig staticAdminConfig;
    
    @PostConstruct
    public void init() {
        staticAdminConfig = adminConfig;
    }
    
    /**
     * 判断指定用户名是否为管理员
     * @param username 用户名
     * @return true-是管理员，false-不是管理员
     */
    public static boolean isAdmin(String username) {
        if (staticAdminConfig == null) {
            return false;
        }
        return staticAdminConfig.isAdmin(username);
    }
    
    /**
     * 判断指定用户是否为管理员
     * @param userDTO 用户DTO
     * @return true-是管理员，false-不是管理员
     */
    public static boolean isAdmin(UserDTO userDTO) {
        if (userDTO == null || userDTO.getUsername() == null) {
            return false;
        }
        return isAdmin(userDTO.getUsername());
    }
    
    /**
     * 判断当前登录用户是否为管理员
     * @return true-是管理员，false-不是管理员
     */
    public static boolean isCurrentUserAdmin() {
        UserDTO currentUser = UserContext.getCurrentUser();
        return isAdmin(currentUser);
    }
}
