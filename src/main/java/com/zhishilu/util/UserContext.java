package com.zhishilu.util;

import com.zhishilu.entity.User;

/**
 * 用户上下文工具类
 */
public class UserContext {
    
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置当前用户
     */
    public static void setCurrentUser(User user) {
        USER_HOLDER.set(user);
    }
    
    /**
     * 获取当前用户
     */
    public static User getCurrentUser() {
        return USER_HOLDER.get();
    }
    
    /**
     * 清除当前用户
     */
    public static void clear() {
        USER_HOLDER.remove();
    }
}
