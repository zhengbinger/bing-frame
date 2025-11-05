package com.bing.framework.config;

import com.bing.framework.util.AuditLogAppender;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * 日志配置类
 * 用于配置和注册日志相关组件，采用延迟初始化以提升启动性能
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Configuration
public class LogConfig {
    
    /**
     * 创建并注册AuditLogAppender
     * 使用@Lazy注解实现延迟初始化，仅在首次使用时创建
     * 
     * @return AuditLogAppender实例
     */
    @Bean
    @Lazy
    public AuditLogAppender auditLogAppender() {
        AuditLogAppender appender = new AuditLogAppender();
        appender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        appender.setName("AUDIT_LOG_APPENDER");
        appender.start();
        
        // 注册到审计日志记录器
        Logger auditLogger = (Logger) LoggerFactory.getLogger("AUDIT_LOG");
        auditLogger.addAppender(appender);
        
        return appender;
    }
}