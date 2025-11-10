// package com.bing.framework.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// import org.springframework.web.filter.CorsFilter;

// /**
//  * CORS配置类
//  * 使用@Configuration注解定义配置类，配置跨域资源共享规则
//  * 通过CorsFilter实现跨域请求的处理，允许来自不同源的HTTP请求
//  * 
//  * @author zhengbing
//  * @date 2025-11-01
//  */
// @Configuration
// public class CorsConfig {

//     /**
//      * 创建CORS过滤器
//      * @return CORS过滤器实例
//      */
//     @Bean
//     public CorsFilter corsFilter() {
//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         CorsConfiguration config = new CorsConfiguration();
        
//         // 允许所有来源
//         config.addAllowedOrigin("*");
//         // 允许所有方法
//         config.addAllowedMethod("*");
//         // 允许所有请求头
//         config.addAllowedHeader("*");
//         // 允许凭证
//         config.setAllowCredentials(true);
//         // 预检请求的有效期，单位为秒
//         config.setMaxAge(1800L);
        
//         // 配置所有路径
//         source.registerCorsConfiguration("/**", config);
        
//         return new CorsFilter(source);
//     }
// }