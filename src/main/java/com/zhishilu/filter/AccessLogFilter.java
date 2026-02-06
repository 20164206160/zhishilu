package com.zhishilu.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * 全局访问日志过滤器
 * 负责生成追踪号、记录接口调用路径、IP、参数及响应结果
 */
@Slf4j
@Component
@WebFilter(urlPatterns = "/*")
@Order(Integer.MIN_VALUE) // 确保在最外层，最先执行
public class AccessLogFilter implements Filter {

    private static final String TRACE_ID = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 生成跟踪号
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(TRACE_ID, traceId);

        // 包装请求和响应以支持读取多次（记录日志使用）
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();
        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logAccess(requestWrapper, responseWrapper, duration);
            // 将响应内容复制回原始响应流，否则客户端收不到数据
            responseWrapper.copyBodyToResponse();
            MDC.remove(TRACE_ID);
        }
    }

    private void logAccess(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String ip = getIpAddress(request);
        String queryParams = request.getQueryString();
        String requestBody = getPayload(request);
        String responseBody = getPayload(response);

        log.info("接口调用: [{} {}] IP: [{}], 耗时: {}ms, 参数: [{}], 请求体: [{}], 响应结果: [{}]", 
                method, path, ip, duration, queryParams != null ? queryParams : "", requestBody, responseBody);
    }

    private String getPayload(ContentCachingRequestWrapper wrapper) {
        byte[] buf = wrapper.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                return new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                return "[Unsupported Encoding]";
            }
        }
        return "";
    }

    private String getPayload(ContentCachingResponseWrapper wrapper) {
        byte[] buf = wrapper.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                return new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                return "[Unsupported Encoding]";
            }
        }
        return "";
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
