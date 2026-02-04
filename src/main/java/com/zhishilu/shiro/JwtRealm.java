package com.zhishilu.shiro;

import com.zhishilu.entity.User;
import com.zhishilu.service.UserService;
import com.zhishilu.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

/**
 * JWT Realm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRealm extends AuthorizingRealm {
    
    private final JwtUtil jwtUtil;
    private final UserService userService;
    
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }
    
    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 目前不做细粒度权限控制，只做登录认证
        return new SimpleAuthorizationInfo();
    }
    
    /**
     * 认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String token = (String) authenticationToken.getPrincipal();
        
        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            throw new AuthenticationException("Token无效或已过期");
        }
        
        // 获取用户ID
        String userId = jwtUtil.getUserIdFromToken(token);
        
        // 查询用户
        User user;
        try {
            user = userService.getById(userId);
        } catch (Exception e) {
            throw new AuthenticationException("用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new AuthenticationException("账号已被禁用");
        }
        
        return new SimpleAuthenticationInfo(user, token, getName());
    }
}
