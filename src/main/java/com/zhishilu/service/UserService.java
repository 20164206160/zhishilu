package com.zhishilu.service;

import com.zhishilu.dto.UserDTO;
import com.zhishilu.req.LoginReq;
import com.zhishilu.req.RegisterReq;
import com.zhishilu.resp.LoginResp;
import com.zhishilu.resp.UserResp;
import com.zhishilu.entity.User;
import com.zhishilu.exception.BusinessException;
import com.zhishilu.config.AdminConfig;
import com.zhishilu.repository.UserRepository;
import com.zhishilu.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;
    private final AdminConfig adminConfig;
    
    private static final String SALT = "zhishilu";
    
    /**
     * 用户注册
     */
    public UserResp register(RegisterReq req) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (req.getEmail() != null && userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }
        
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(req.getUsername());
        user.setPassword(encryptPassword(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        user.setEmail(req.getEmail());
        user.setStatus(1);
        user.setAuthorized(0); // 新用户默认未授权
        user.setCreatedTime(LocalDateTime.now());
        
        user = userRepository.save(user);
        return convertToUserResp(user);
    }
    
    /**
     * 用户登录
     */
    public LoginResp login(LoginReq req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        
        // 验证密码
        if (!user.getPassword().equals(encryptPassword(req.getPassword()))) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        
        // 检查用户是否已授权
        if (user.getAuthorized() == null || user.getAuthorized() != 1) {
            throw new BusinessException("用户未授权，请联系网站人员");
        }
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 存储Token到Redis（有效期7天）
        redisTokenService.saveToken(token, user.getId());
        log.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());
        
        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUser(convertToUserResp(user));
        
        return resp;
    }
    
    /**
     * 用户登出
     */
    public void logout(String token) {
        redisTokenService.removeToken(token);
        log.info("用户已登出");
    }
    
    /**
     * 根据ID获取用户
     */
    public UserDTO getById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return convertToUserDTO(user);
    }
    
    /**
     * 根据用户名获取用户
     */
    public UserDTO getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return convertToUserDTO(user);
    }
    
    /**
     * 更新用户头像
     */
    public void updateAvatar(String userId, String avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setAvatar(avatar);
        userRepository.save(user);
        log.info("用户 {} 头像已更新: {}", user.getUsername(), avatar);
    }
    
    /**
     * 密码加密
     */
    private String encryptPassword(String password) {
        return new Sha256Hash(password, SALT, 1024).toHex();
    }
    
    /**
     * 将Entity转换为UserResp（用于Controller响应）
     */
    private UserResp convertToUserResp(User user) {
        UserResp resp = new UserResp();
        BeanUtils.copyProperties(user, resp);
        // 设置管理员标识
        resp.setAdmin(adminConfig.isAdmin(user.getUsername()));
        return resp;
    }
    
    /**
     * 将Entity转换为UserDTO（用于Service层传递）
     */
    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        // 设置管理员标识
        dto.setAdmin(adminConfig.isAdmin(user.getUsername()));
        return dto;
    }
    
    // ==================== 用户管理接口（管理员专用） ====================
    
    /**
     * 获取所有用户列表（分页）
     */
    public Page<UserResp> getUserList(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(this::convertToUserResp);
    }
    
    /**
     * 根据授权状态查询用户列表
     */
    public List<UserResp> getUsersByAuthorized(Integer authorized) {
        List<User> users = userRepository.findByAuthorized(authorized);
        return users.stream()
                .map(this::convertToUserResp)
                .collect(Collectors.toList());
    }
    
    /**
     * 授权用户
     */
    public void authorizeUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setAuthorized(1);
        userRepository.save(user);
        log.info("用户 {} 已被授权", user.getUsername());
    }
    
    /**
     * 取消授权用户
     */
    public void unauthorizeUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setAuthorized(0);
        userRepository.save(user);
        // 删除用户的Token，强制登出
        redisTokenService.removeTokenByUserId(userId);
        log.info("用户 {} 授权已被取消，Token已清除", user.getUsername());
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        // 删除用户的Token，强制登出
        redisTokenService.removeTokenByUserId(userId);
        userRepository.delete(user);
        log.info("用户 {} 已被删除", user.getUsername());
    }
    
    /**
     * 管理员重置用户密码
     */
    public void resetUserPassword(String userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        // 加密新密码
        String encryptedPassword = encryptPassword(newPassword);
        user.setPassword(encryptedPassword);
        userRepository.save(user);
        // 删除用户的Token，强制重新登录
        redisTokenService.removeTokenByUserId(userId);
        log.info("用户 {} 的密码已被管理员重置", user.getUsername());
    }
}
