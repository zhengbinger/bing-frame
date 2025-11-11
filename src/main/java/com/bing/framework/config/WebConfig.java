package com.bing.framework.config;

import com.bing.framework.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 用于配置Spring MVC的各种设置，包括拦截器、视图解析器等
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册JWT拦截器
//        registry.addInterceptor(jwtInterceptor)
                // 设置需要拦截的路径
//                .addPathPatterns("/api/**")
                // 设置不需要拦截的路径
//                 .excludePathPatterns("/static/**", "/swagger-resources/**", "/webjars/**",
//                        "/v2/api-docs", "/doc.html", "/knife4j/**", "/swagger-ui/**",
//                        "/v3/api-docs/**", "/swagger-ui.html","/favicon.ico","/error");
    }
}