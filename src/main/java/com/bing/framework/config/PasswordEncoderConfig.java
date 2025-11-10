package com.bing.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码编码器配置类
 * 单独配置BCryptPasswordEncoder，避免与SecurityConfig的循环依赖问题
 * 
 * @author zhengbing
 * @date 2025-11-10
 */
@Configuration
public class PasswordEncoderConfig {
    
    /**
     * 配置BCryptPasswordEncoder用于密码加密和验证
     * 
     * @return BCryptPasswordEncoder实例
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}