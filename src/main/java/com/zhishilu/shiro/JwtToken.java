package com.zhishilu.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * JWT Token
 */
public class JwtToken implements AuthenticationToken {
    
    private final String token;
    
    public JwtToken(String token) {
        this.token = token;
    }
    
    @Override
    public Object getPrincipal() {
        return token;
    }
    
    @Override
    public Object getCredentials() {
        return token;
    }
}
