package com.bing.framework.config;

import com.bing.framework.interceptor.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置类。
 * 用于注册和配置Spring MVC的拦截器。
 *
 * @author zhengbing
 * @date 2024-11-03
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private RequestInterceptor requestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册请求拦截器
        registry.addInterceptor(requestInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除静态资源和Swagger相关路径（可根据实际需求调整）
                .excludePathPatterns("/static/**", "/swagger-resources/**", "/webjars/**", 
                        "/v2/api-docs", "/doc.html", "/knife4j/**", "/swagger-ui/**");
    }
}