package com.bing.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Spring Security配置类
 * 继承WebSecurityConfigurerAdapter，配置Spring Security安全规则
 * 关闭CSRF保护，配置跨域支持，设置URL访问权限控制
 * 与白名单请求拦截器协同工作
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
// 添加@Lazy注解实现延迟初始化，提升启动性能
@Configuration
@Lazy
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 关闭CSRF
            .csrf().disable()
            // 允许跨域
            .cors().and()
            // 配置授权规则
            .authorizeRequests()
            // 允许访问所有资源，具体的访问控制由RequestInterceptor实现
            // 这里保持宽松配置，让拦截器进行精确的白名单控制
            .anyRequest().permitAll()
            .and()
            // 禁用表单登录和HTTP基本认证
            .formLogin().disable()
            .httpBasic().disable();
    }
    
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