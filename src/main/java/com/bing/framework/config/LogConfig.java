package com.bing.framework.config;

import com.bing.framework.util.AuditLogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import javax.annotation.PostConstruct;

import ch.qos.logback.classic.LoggerContext;

/**
 * 日志配置类
 * 用于配置和注册日志相关组件，确保ApplicationContext正确注入
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Configuration
public class LogConfig implements InitializingBean {
    
    private static final Logger logger = LoggerFactory.getLogger(LogConfig.class);
    private final ApplicationContext applicationContext;
    private AuditLogAppender auditLogAppender;
    
    /**
     * 构造函数注入ApplicationContext
     */
    public LogConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        logger.info("[LOG_CONFIG] ApplicationContext injected via constructor");
    }
    
    @PostConstruct
    public void init() {
        logger.info("[LOG_CONFIG] @PostConstruct called, attempting to set ApplicationContext to AuditLogAppender");
        // 使用静态方法设置ApplicationContext
        AuditLogAppender.setStaticApplicationContext(applicationContext);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("[LOG_CONFIG] afterPropertiesSet called");
        // 确保ApplicationContext已设置
        if (!AuditLogAppender.isApplicationContextAvailable()) {
            logger.warn("[LOG_CONFIG] ApplicationContext not available in afterPropertiesSet, retrying...");
            AuditLogAppender.setStaticApplicationContext(applicationContext);
        }
        
        // 确保AuditLogAppender已创建和配置
        getOrCreateAuditLogAppender();
    }
    
    /**
     * 创建并注册AuditLogAppender
     * 
     * @return AuditLogAppender实例
     */
    @Bean
    public AuditLogAppender auditLogAppender() {
        return getOrCreateAuditLogAppender();
    }
    
    /**
     * 获取或创建AuditLogAppender实例
     */
    private AuditLogAppender getOrCreateAuditLogAppender() {
        if (auditLogAppender == null) {
            synchronized (this) {
                if (auditLogAppender == null) {
                    logger.info("[LOG_CONFIG] Creating new AuditLogAppender instance");
                    auditLogAppender = new AuditLogAppender();
                    auditLogAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
                    auditLogAppender.setName("AUDIT_LOG_APPENDER");
                    
                    // 使用静态方法确保ApplicationContext已设置
                    if (!AuditLogAppender.isApplicationContextAvailable()) {
                        logger.warn("[LOG_CONFIG] ApplicationContext not available, setting it now");
                        AuditLogAppender.setStaticApplicationContext(applicationContext);
                    }
                    
                    auditLogAppender.start();
                    logger.info("[LOG_CONFIG] AuditLogAppender started");
                    
                    // 注册到审计日志记录器
                    ch.qos.logback.classic.Logger auditLogger = 
                        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("AUDIT_LOG");
                    auditLogger.addAppender(auditLogAppender);
                    logger.info("[LOG_CONFIG] AuditLogAppender registered to AUDIT_LOG logger");
                }
            }
        }
        return auditLogAppender;
    }
    
    /**
     * 在Spring容器完全初始化后再次确保ApplicationContext已注入
     * 这是一个额外的保障措施
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed(ContextRefreshedEvent event) {
        try {
            ApplicationContext context = event.getApplicationContext();
            logger.info("[LOG_CONFIG] ContextRefreshedEvent received, ensuring ApplicationContext is set");
            
            // 使用静态方法再次确保ApplicationContext已设置
            AuditLogAppender.setStaticApplicationContext(context);
            
            // 验证ApplicationContext是否已成功注入
            boolean isAvailable = AuditLogAppender.isApplicationContextAvailable();
            logger.info("[LOG_CONFIG] ContextRefreshedEvent: ApplicationContext available: {}", isAvailable);
            
            if (!isAvailable) {
                logger.error("[LOG_CONFIG] ERROR: ApplicationContext still not available after ContextRefreshedEvent");
            }
            
        } catch (Exception e) {
            logger.error("[LOG_CONFIG] Failed during ContextRefreshedEvent processing: {}", e.getMessage(), e);
        }
    }
}