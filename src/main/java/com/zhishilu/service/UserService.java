package com.zhishilu.service;

import com.zhishilu.dto.LoginDTO;
import com.zhishilu.dto.RegisterDTO;
import com.zhishilu.entity.User;
import com.zhishilu.exception.BusinessException;
import com.zhishilu.repository.UserRepository;
import com.zhishilu.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    private static final String SALT = "zhishilu";
    
    /**
     * 用户注册
     */
    public User register(RegisterDTO dto) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (dto.getEmail() != null && userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }
        
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(dto.getUsername());
        user.setPassword(encryptPassword(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        user.setCreatedTime(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(LoginDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        
        // 验证密码
        if (!user.getPassword().equals(encryptPassword(dto.getPassword()))) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", sanitizeUser(user));
        
        return result;
    }
    
    /**
     * 根据ID获取用户
     */
    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    /**
     * 根据用户名获取用户
     */
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    /**
     * 密码加密
     */
    private String encryptPassword(String password) {
        return new Sha256Hash(password, SALT, 1024).toHex();
    }
    
    /**
     * 清理用户敏感信息
     */
    private User sanitizeUser(User user) {
        User sanitized = new User();
        sanitized.setId(user.getId());
        sanitized.setUsername(user.getUsername());
        sanitized.setNickname(user.getNickname());
        sanitized.setEmail(user.getEmail());
        sanitized.setAvatar(user.getAvatar());
        sanitized.setStatus(user.getStatus());
        sanitized.setCreatedTime(user.getCreatedTime());
        sanitized.setLastLoginTime(user.getLastLoginTime());
        return sanitized;
    }
}
