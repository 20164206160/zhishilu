package com.zhishilu.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhishilu.dto.UserDTO;
import com.zhishilu.service.OperationLogService;
import com.zhishilu.util.UserContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogInterceptor implements HandlerInterceptor {
    
    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String START_TIME = "REQUEST_START_TIME";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                 Object handler, Exception ex) {
        try {
            Long startTime = (Long) request.getAttribute(START_TIME);
            Long executionTime = System.currentTimeMillis() - startTime;
            
            UserDTO currentUser = UserContext.getCurrentUser();
            String userId = currentUser != null ? currentUser.getId() : null;
            String username = currentUser != null ? currentUser.getUsername() : "anonymous";
            
            String path = request.getRequestURI();
            String method = request.getMethod();
            String params = getRequestParams(request);
            String ip = getClientIp(request);
            Integer statusCode = response.getStatus();
            
            // 异步保存日志
            operationLogService.saveLog(userId, username, path, method, params, ip, statusCode, executionTime);
            
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        } finally {
            // 清除用户上下文
            UserContext.clear();
        }
    }
    
    /**
     * 获取请求参数
     */
    private String getRequestParams(HttpServletRequest request) {
        try {
            Map<String, Object> params = new HashMap<>();
            
            // 获取URL参数
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                params.put(name, request.getParameter(name));
            }
            
            // 如果是POST/PUT请求，尝试获取请求体（已被ContentCachingRequestWrapper缓存）
            if (request instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    String body = new String(content, request.getCharacterEncoding());
                    // 限制长度避免日志过大
                    if (body.length() > 2000) {
                        body = body.substring(0, 2000) + "...(truncated)";
                    }
                    params.put("_body", body);
                }
            }
            
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            log.warn("获取请求参数失败", e);
            return "{}";
        }
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
