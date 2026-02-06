package com.zhishilu.shiro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhishilu.common.Result;
import com.zhishilu.dto.UserDTO;
import com.zhishilu.util.UserContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

/**
 * JWT过滤器
 */
@Slf4j
public class JwtFilter extends AuthenticatingFilter {
    
    @Value("${jwt.header:Authorization}")
    private String header;
    
    @Value("${jwt.prefix:Bearer }")
    private String prefix;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        String token = getToken((HttpServletRequest) request);
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return new JwtToken(token);
    }
    
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 对于OPTIONS请求直接放行（CORS预检）
        if (((HttpServletRequest) request).getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }
        return false;
    }
    
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        String token = getToken((HttpServletRequest) request);
        
        if (StringUtils.isBlank(token)) {
            responseError(response, "请先登录");
            return false;
        }
        
        return executeLogin(request, response);
    }
    
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) {
        // 登录成功后，将用户信息存入上下文
        UserDTO user = (UserDTO) subject.getPrincipal();
        UserContext.setCurrentUser(user);
        return true;
    }
    
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        try {
            responseError(response, e.getMessage());
        } catch (Exception ex) {
            log.error("响应错误信息失败", ex);
        }
        return false;
    }
    
    /**
     * 获取Token
     */
    private String getToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(header);
        if (StringUtils.isNotBlank(bearerToken) && bearerToken.startsWith(prefix)) {
            return bearerToken.substring(prefix.length());
        }
        // 也支持从参数中获取
        return request.getParameter("token");
    }
    
    /**
     * 响应错误信息
     */
    private void responseError(ServletResponse response, String message) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.getWriter().write(objectMapper.writeValueAsString(Result.error(401, message)));
    }
}
