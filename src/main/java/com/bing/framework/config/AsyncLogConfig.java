package com.bing.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步日志配置类
 * 提供专用的线程池用于审计日志记录，避免阻塞主业务线程
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
// 添加@Lazy注解实现延迟初始化，提升启动性能
@Configuration
@Lazy
public class AsyncLogConfig {

    /**
     * 配置审计日志专用线程池
     * 核心线程数：5
     * 最大线程数：10
     * 队列容量：500
     * 线程名称前缀：audit-log-
     */
    @Bean("auditLogExecutor")
    public Executor auditLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(5);
        // 设置最大线程数
        executor.setMaxPoolSize(10);
        // 设置队列容量
        executor.setQueueCapacity(500);
        // 设置线程名称前缀
        executor.setThreadNamePrefix("audit-log-");
        // 设置线程存活时间（秒）
        executor.setKeepAliveSeconds(60);
        // 设置拒绝策略：当线程池和队列都满时，使用调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化线程池
        executor.initialize();
        return executor;
    }
}