package com.bing.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 时间配置类
 * 提供Clock的Bean定义，用于依赖注入
 * 提高代码的可测试性和时间处理的一致性
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Configuration
public class TimeConfig {

    /**
     * 配置Clock Bean
     * 使用系统默认时区的Clock实例
     * 
     * @return Clock实例
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}