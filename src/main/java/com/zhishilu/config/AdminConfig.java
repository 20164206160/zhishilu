package com.zhishilu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 管理员配置类
 * 从配置文件中读取管理员用户名列表
 */
@Data
@Component
@ConfigurationProperties(prefix = "admin")
public class AdminConfig {
    
    /**
     * 管理员用户名列表，多个用户名用逗号分隔
     */
    private String usernames;
    
    /**
     * 获取管理员用户名集合
     * @return 管理员用户名Set集合
     */
    public Set<String> getAdminUsernameSet() {
        if (usernames == null || usernames.trim().isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(usernames.split(",")));
    }
    
    /**
     * 判断指定用户名是否为管理员
     * @param username 用户名
     * @return true-是管理员，false-不是管理员
     */
    public boolean isAdmin(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return getAdminUsernameSet().contains(username.trim());
    }
}
