package com.bing.framework.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import com.bing.framework.entity.AuditLog;
import com.bing.framework.service.AuditLogService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;

/**
 * 自定义审计日志Appender
 * 继承logback的AppenderBase，实现ApplicationContextAware接口获取Spring容器中的Bean
 * 用于捕获日志系统中的审计日志并异步写入数据库，支持日志解析和异常处理
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
// 移除@Component注解，改为通过配置类进行延迟注册
public class AuditLogAppender extends AppenderBase<ILoggingEvent> implements ApplicationContextAware {
    
    // 静态的ApplicationContext引用
    private static ApplicationContext applicationContext;
    private static Executor auditLogExecutor;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 静态方法，用于直接设置ApplicationContext
     * 这是一个备用机制，确保ApplicationContext可以从Spring容器中正确注入
     */
    public static synchronized void setStaticApplicationContext(ApplicationContext context) {
        if (context != null) {
            applicationContext = context;
            System.out.println("[AUDIT_LOG] ApplicationContext set via static method");
            // 如果设置了ApplicationContext，尝试初始化线程池
            try {
                if (auditLogExecutor == null) {
                    auditLogExecutor = context.getBean("auditLogExecutor", Executor.class);
                    System.out.println("[AUDIT_LOG] auditLogExecutor initialized via static method");
                }
            } catch (Exception e) {
                System.err.println("[AUDIT_LOG] Failed to initialize auditLogExecutor: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取当前的ApplicationContext实例
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    /**
     * 检查ApplicationContext是否已设置
     */
    public static boolean isApplicationContextAvailable() {
        return applicationContext != null;
    }
    
    @Override
    protected void append(ILoggingEvent event) {
        // 检查是否是审计日志标记
        if (event.getLoggerName().startsWith("AUDIT_LOG")) {
            // 异步处理，避免影响主业务
            getAuditLogExecutor().execute(() -> {
                try {
                    processAuditLog(event);
                } catch (Exception e) {
                    System.err.println("处理审计日志失败: " + e.getMessage());
                }
            });
        }
    }
    
    /**
     * 获取审计日志线程池
     * 如果线程池未初始化，则从Spring容器中获取
     */
    private Executor getAuditLogExecutor() {
        if (auditLogExecutor == null) {
            synchronized (AuditLogAppender.class) {
                if (auditLogExecutor == null) {
                    auditLogExecutor = applicationContext.getBean("auditLogExecutor", Executor.class);
                }
            }
        }
        return auditLogExecutor;
    }
    private void processAuditLog(ILoggingEvent event) {
        try {
            // 获取AuditLogService实例
            AuditLogService auditLogService = applicationContext.getBean(AuditLogService.class);
            
            // 解析日志消息，提取审计信息
            AuditLog auditLog = parseAuditLog(event);
            
            // 调用服务记录审计日志
            auditLogService.recordAuditLog(auditLog);
        } catch (Exception e) {
            System.err.println("记录审计日志到数据库失败: " + e.getMessage());
        }
    }
    
    private AuditLog parseAuditLog(ILoggingEvent event) {
        AuditLog auditLog = new AuditLog();
        
        // 设置操作时间
        auditLog.setOperationTime(new Date(event.getTimeStamp()));
        
        // 解析消息内容（格式：key1:value1,key2:value2,...）
        String message = event.getFormattedMessage();
        String[] pairs = message.split(",");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                
                switch (key) {
                    case "userId":
                        auditLog.setUserId(value.isEmpty() ? null : Long.valueOf(value));
                        break;
                    case "username":
                        auditLog.setUsername(value);
                        break;
                    case "ipAddress":
                        auditLog.setIpAddress(value);
                        break;
                    case "module":
                        auditLog.setModule(value);
                        break;
                    case "operationType":
                        auditLog.setOperationType(value);
                        break;
                    case "description":
                        auditLog.setDescription(value);
                        break;
                    case "requestParams":
                        auditLog.setRequestParams(value);
                        break;
                    case "result":
                        auditLog.setResult(value);
                        break;
                    case "executionTime":
                        auditLog.setExecutionTime(value.isEmpty() ? null : Long.valueOf(value));
                        break;
                }
            }
        }
        
        // 处理异常信息
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            auditLog.setResult("失败");
            auditLog.setErrorMessage(ThrowableProxyUtil.asString(throwableProxy));
        } else if (auditLog.getResult() == null) {
            auditLog.setResult("成功");
        }
        
        return auditLog;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AuditLogAppender.applicationContext = applicationContext;
        System.out.println("[AUDIT_LOG] ApplicationContext set via ApplicationContextAware interface");
        
        // 初始化线程池
        try {
            this.auditLogExecutor = applicationContext.getBean("auditLogExecutor", Executor.class);
            System.out.println("[AUDIT_LOG] auditLogExecutor initialized");
        } catch (Exception e) {
            System.err.println("[AUDIT_LOG] Failed to initialize auditLogExecutor: " + e.getMessage());
        }
    }
}