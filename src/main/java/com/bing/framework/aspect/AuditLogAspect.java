package com.bing.framework.aspect;

import com.bing.framework.annotation.AuditLogLevel;
import com.bing.framework.entity.AuditLog;
import com.bing.framework.service.AuditLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 审计日志切面，用于自动记录API操作日志
 */
@Aspect
@Component
public class AuditLogAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogAspect.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_LOG");
    
    @Autowired
    private AuditLogService auditLogService;
    
    // 用于存储操作开始时间，避免并发问题
    private ConcurrentHashMap<String, Long> startTimeMap = new ConcurrentHashMap<>();
    
    /**
     * 定义切点，拦截所有Controller方法
     */
    @Pointcut("execution(* com.bing.framework.controller.*.*(..))")
    public void auditLogPointcut() {
    }
    
    /**
     * 操作前记录开始时间
     */
    @Before("auditLogPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        // 生成唯一标识
        String key = joinPoint.getSignature().toLongString() + Thread.currentThread().getId();
        startTimeMap.put(key, System.currentTimeMillis());
    }
    
    /**
     * 操作成功后记录审计日志
     */
    @AfterReturning(returning = "result", pointcut = "auditLogPointcut()")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        recordAuditLog(joinPoint, null, "成功");
    }
    
    /**
     * 操作异常时记录审计日志
     */
    @AfterThrowing(throwing = "exception", pointcut = "auditLogPointcut()")
    public void doAfterThrowing(JoinPoint joinPoint, Exception exception) {
        recordAuditLog(joinPoint, exception, "失败");
    }
    
    /**
     * 记录审计日志
     */
    private void recordAuditLog(JoinPoint joinPoint, Exception exception, String result) {
        try {
            // 获取方法信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 检查是否需要忽略审计日志记录
            if (shouldIgnoreAudit(method)) {
                return;
            }
            
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // 获取审计日志级别
            AuditLogLevel.Level level = getAuditLogLevel(method);
            
            // 计算执行时间
            String key = joinPoint.getSignature().toLongString() + Thread.currentThread().getId();
            Long startTime = startTimeMap.remove(key);
            Long executionTime = startTime != null ? System.currentTimeMillis() - startTime : null;
            
            // 创建审计日志对象
            AuditLog auditLog = new AuditLog();
            auditLog.setOperationTime(new Date());
            auditLog.setIpAddress(getClientIp(request));
            auditLog.setModule(getModuleName(method));
            auditLog.setOperationType(getOperationType(request.getMethod()));
            auditLog.setDescription(getDescription(method, joinPoint));
            auditLog.setResult(result);
            
            // 根据级别设置不同的字段
            switch (level) {
                case FULL:
                    // 完整级别：记录所有信息
                    auditLog.setRequestParams(getRequestParams(joinPoint));
                    auditLog.setExecutionTime(executionTime);
                    if (exception != null) {
                        auditLog.setErrorMessage(exception.getMessage());
                    }
                    break;
                case BASIC:
                    // 基本级别：不记录详细请求参数
                    auditLog.setExecutionTime(executionTime);
                    if (exception != null) {
                        auditLog.setErrorMessage("操作异常：" + exception.getClass().getSimpleName());
                    }
                    break;
                case MINIMAL:
                    // 最小级别：仅记录最基本信息
                    break;
            }
            
            // 获取用户信息（这里需要根据实际情况修改，比如从Session或Token中获取）
            // 这里假设从请求头中获取用户信息
            auditLog.setUserId(null); // 实际应用中需要从认证信息中获取
            auditLog.setUsername(request.getHeader("X-User-Name") != null ? request.getHeader("X-User-Name") : "anonymous");
            
            // 异步记录审计日志到数据库
            auditLogService.recordAuditLogAsync(auditLog);
            
            // 同时记录到自定义Appender（作为双重保障）
            auditLogger.info(buildAuditLogMessage(auditLog));
            
        } catch (Exception e) {
            // 记录审计日志失败不影响主业务
            logger.error("记录审计日志失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 获取模块名称
     */
    /**
     * 检查是否需要忽略审计日志记录
     */
    private boolean shouldIgnoreAudit(Method method) {
        // 检查方法上的注解
        AuditLogLevel methodAnnotation = method.getAnnotation(AuditLogLevel.class);
        if (methodAnnotation != null && methodAnnotation.ignore()) {
            return true;
        }
        
        // 检查类上的注解
        AuditLogLevel classAnnotation = method.getDeclaringClass().getAnnotation(AuditLogLevel.class);
        return classAnnotation != null && classAnnotation.ignore();
    }
    
    /**
     * 获取审计日志级别
     */
    private AuditLogLevel.Level getAuditLogLevel(Method method) {
        // 方法注解优先
        AuditLogLevel methodAnnotation = method.getAnnotation(AuditLogLevel.class);
        if (methodAnnotation != null) {
            return methodAnnotation.value();
        }
        
        // 类注解次之
        AuditLogLevel classAnnotation = method.getDeclaringClass().getAnnotation(AuditLogLevel.class);
        if (classAnnotation != null) {
            return classAnnotation.value();
        }
        
        // 默认级别
        return AuditLogLevel.Level.FULL;
    }
    
    private String getModuleName(Method method) {
        // 先检查注解中的自定义模块名
        AuditLogLevel methodAnnotation = method.getAnnotation(AuditLogLevel.class);
        if (methodAnnotation != null && !methodAnnotation.module().isEmpty()) {
            return methodAnnotation.module();
        }
        
        AuditLogLevel classAnnotation = method.getDeclaringClass().getAnnotation(AuditLogLevel.class);
        if (classAnnotation != null && !classAnnotation.module().isEmpty()) {
            return classAnnotation.module();
        }
        
        // 默认从类名获取
        // 从类名中提取模块信息
        String className = method.getDeclaringClass().getSimpleName();
        if (className.endsWith("Controller")) {
            return className.substring(0, className.length() - 10);
        }
        return className;
    }
    
    /**
     * 获取操作类型
     */
    private String getOperationType(String httpMethod) {
        switch (httpMethod.toUpperCase()) {
            case "GET":
                return "查询";
            case "POST":
                return "新增";
            case "PUT":
                return "修改";
            case "DELETE":
                return "删除";
            default:
                return "其他";
        }
    }
    
    /**
     * 获取操作描述
     */
    private String getDescription(Method method, JoinPoint joinPoint) {
        // 先检查注解中的自定义描述
        AuditLogLevel methodAnnotation = method.getAnnotation(AuditLogLevel.class);
        if (methodAnnotation != null && !methodAnnotation.description().isEmpty()) {
            return methodAnnotation.description();
        }
        
        AuditLogLevel classAnnotation = method.getDeclaringClass().getAnnotation(AuditLogLevel.class);
        if (classAnnotation != null && !classAnnotation.description().isEmpty()) {
            return classAnnotation.description();
        }
        
        // 默认返回方法名
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }
    
    /**
     * 获取请求参数
     */
    private String getRequestParams(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.toString(args);
    }
    
    /**
     * 构建审计日志消息格式
     */
    private String buildAuditLogMessage(AuditLog auditLog) {
        StringBuilder sb = new StringBuilder();
        sb.append("userId:").append(auditLog.getUserId() == null ? "" : auditLog.getUserId()).append(",");
        sb.append("username:").append(auditLog.getUsername()).append(",");
        sb.append("ipAddress:").append(auditLog.getIpAddress()).append(",");
        sb.append("module:").append(auditLog.getModule()).append(",");
        sb.append("operationType:").append(auditLog.getOperationType()).append(",");
        sb.append("description:").append(auditLog.getDescription()).append(",");
        sb.append("requestParams:").append(auditLog.getRequestParams()).append(",");
        sb.append("result:").append(auditLog.getResult()).append(",");
        sb.append("executionTime:").append(auditLog.getExecutionTime() == null ? "" : auditLog.getExecutionTime());
        
        if (auditLog.getErrorMessage() != null) {
            sb.append(",errorMessage:").append(auditLog.getErrorMessage());
        }
        
        return sb.toString();
    }
}