package com.bing.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志级别注解
 * 用于标记Controller方法的审计日志记录级别
 * 可以根据业务需求灵活控制不同接口的审计日志记录策略
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLogLevel {
    
    /**
     * 审计日志级别
     */
    Level value() default Level.FULL;
    
    /**
     * 是否忽略此方法的审计日志记录
     */
    boolean ignore() default false;
    
    /**
     * 自定义模块名称
     */
    String module() default "";
    
    /**
     * 自定义操作描述
     */
    String description() default "";
    
    /**
     * 审计日志级别枚举
     */
    enum Level {
        /**
         * 完整级别：记录所有信息，包括请求参数、返回结果、耗时等
         */
        FULL,
        
        /**
         * 基本级别：记录基本信息，不包括详细的请求参数和返回结果
         */
        BASIC,
        
        /**
         * 最小级别：仅记录操作时间、用户、模块等最基本信息
         */
        MINIMAL
    }
}