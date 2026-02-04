package com.zhishilu.config;

import com.zhishilu.shiro.JwtFilter;
import com.zhishilu.shiro.JwtRealm;
import javax.servlet.Filter;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
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
        
        // 过滤规则
        Map<String, String> filterRuleMap = new LinkedHashMap<>();
        // 公开接口
        filterRuleMap.put("/auth/**", "anon");
        filterRuleMap.put("/file/download/**", "anon");
        filterRuleMap.put("/swagger-ui/**", "anon");
        filterRuleMap.put("/v3/api-docs/**", "anon");
        filterRuleMap.put("/error", "anon");
        // 其他接口需要JWT认证
        filterRuleMap.put("/**", "jwt");
        
        factoryBean.setFilterChainDefinitionMap(filterRuleMap);
        factoryBean.setUnauthorizedUrl("/auth/unauthorized");
        
        return factoryBean;
    }
}
