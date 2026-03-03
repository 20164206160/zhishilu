package com.zhishilu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis Token服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenService {
    
    private final StringRedisTemplate redisTemplate;
    
    // Token有效期：7天
    private static final long TOKEN_EXPIRE_DAYS = 7;
    
    /**
     * 存储Token
     */
    public void saveToken(String token, String userId) {
        redisTemplate.opsForValue().set(token, userId, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        log.info("Token已存储: {} -> {}", token, userId);
    }
    
    /**
     * 验证并刷新Token
     */
    public String validateAndRefreshToken(String token) {
        String userId = redisTemplate.opsForValue().get(token);
        if (userId != null) {
            // Token有效，刷新过期时间
            redisTemplate.expire(token, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
            log.info("Token已刷新: {}", token);
        }
        return userId;
    }
    
    /**
     * 删除Token（登出）
     */
    public void removeToken(String token) {
        redisTemplate.delete(token);
        log.info("Token已删除: {}", token);
    }
    
    /**
     * 检查Token是否存在
     */
    public boolean exists(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
    
    /**
     * 根据用户ID删除Token（用于强制用户登出）
     * 注意：此方法会遍历所有Token，性能较低，仅在必要时使用
     */
    public void removeTokenByUserId(String userId) {
        // 使用keys命令查找所有Token（生产环境建议使用Scan）
        var keys = redisTemplate.keys("*");
        if (keys != null) {
            for (String key : keys) {
                String storedUserId = redisTemplate.opsForValue().get(key);
                if (userId.equals(storedUserId)) {
                    redisTemplate.delete(key);
                    log.info("已删除用户 {} 的Token: {}", userId, key);
                }
            }
        }
    }
}

