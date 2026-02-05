package com.zhishilu.config;

import com.zhishilu.shiro.JwtFilter;
import com.zhishilu.shiro.JwtRealm;
import javax.servlet.Filter;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro配置
 */
@Configuration
public class ShiroConfig {
    
    /**
     * 安全管理器
     */
    @Bean
    public DefaultWebSecurityManager securityManager(JwtRealm jwtRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(jwtRealm);
        
        // 关闭Shiro自带的Session
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator sessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(sessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        
        return securityManager;
    }

    /**
     * 手动注入JwtFilter，确保其@Value生效
     */
    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter();
    }

    /**
     * 禁用JwtFilter的自动注册
     * 否则它会被Spring注册为全局过滤器，导致anon失效
     */
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter filter) {
        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
    
    /**
     * Shiro过滤器
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager, 
                                                          JwtFilter jwtFilter) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);
        
        // 自定义过滤器
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("jwt", jwtFilter);
        factoryBean.setFilters(filterMap);
        
        // 过滤规则（注意：Shiro过滤器匹配的是去除context-path后的路径）
        Map<String, String> filterRuleMap = new LinkedHashMap<>();
        
        // 公开接口（无需登录）
        filterRuleMap.put("/auth/**", "anon");              // 登录注册
        filterRuleMap.put("/article/list", "anon");         // 首页列表
        filterRuleMap.put("/article/search", "anon");       // 搜索
        filterRuleMap.put("/file/img/**", "anon");          // 图片访问
        filterRuleMap.put("/file/download/**", "anon");     // 文件下载
        filterRuleMap.put("/swagger-ui/**", "anon");        // Swagger
        filterRuleMap.put("/v3/api-docs/**", "anon");
        filterRuleMap.put("/error", "anon");
        
        // 需要认证的接口
        filterRuleMap.put("/article", "jwt");               // 发布内容
        filterRuleMap.put("/article/*", "jwt");             // 内容详情
        filterRuleMap.put("/article/*/edit", "jwt");        // 编辑内容
        filterRuleMap.put("/profile/**", "jwt");            // 个人中心
        filterRuleMap.put("/user/**", "jwt");               // 用户相关
        filterRuleMap.put("/**", "jwt");                    // 其他接口默认需要认证
        
        factoryBean.setFilterChainDefinitionMap(filterRuleMap);
        factoryBean.setUnauthorizedUrl("/auth/unauthorized");
        
        return factoryBean;
    }
}
