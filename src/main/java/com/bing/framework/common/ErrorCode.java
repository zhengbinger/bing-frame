package com.bing.framework.common;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Locale;

/**
 * 错误码枚举类
 * 定义系统中所有的错误码和对应的消息键，支持多语言
 * 
 * @author zhengbing
 */
public enum ErrorCode {
    
    // 系统级错误码 1000-1999
    SUCCESS(200, "error.success"),
    SYSTEM_ERROR(1000, "error.system"),
    PARAM_ERROR(1001, "error.param"),
    NULL_POINTER_ERROR(1002, "error.null"),
    
    // 业务级错误码 2000-2999
    BUSINESS_ERROR(2000, "error.business"),
    
    // 用户相关错误码 3000-3999
    USER_NOT_FOUND(3001, "error.user.not_found"),
    USER_EXIST(3002, "error.user.exist"),
    USER_LOGIN_FAILED(3003, "error.user.login_failed"),
    USER_DISABLED(3004, "error.user.disabled"),
    INCORRECT_PASSWORD(3005, "error.user.incorrect_password"),
    REGISTER_FAILED(3006, "error.user.register_failed"),
    INVALID_CAPTCHA(3007, "error.captcha.invalid"),
    CAPTCHA_EXPIRED(3008, "error.captcha.expired"),
    CAPTCHA_REQUIRED(3009, "error.captcha.required"),
    CAPTCHA_FREQUENCY_LIMIT(3010, "error.captcha.frequency_limit"),
    SMS_SEND_FAILED(3011, "error.sms.send_failed"),
    
    // 数据库相关错误码 4000-4999
    DATABASE_ERROR(4000, "error.database"),
    
    // 权限相关错误码 5000-5999
    UNAUTHORIZED(5000, "error.unauthorized"),
    FORBIDDEN(5001, "error.forbidden"),
    INVALID_TOKEN(5002, "error.token.invalid"),
    TOKEN_EXPIRED(5004, "error.token.expired"),
    TOKEN_BLACKLISTED(5005, "error.token.blacklisted"),
    DEVICE_MISMATCH(5006, "error.device.mismatch"),
    INVALID_REFRESH_TOKEN(5007, "error.refresh_token.invalid"),
    REFRESH_TOKEN_FAILED(5008, "error.refresh_token.failed"),
    LOGOUT_FAILED(5009, "error.logout.failed"),
    
    // 未知错误
    UNKNOWN_ERROR(9999, "error.unknown");
    
    /** 错误码 */
    private final Integer code;
    
    /** 错误消息键 */
    private final String messageKey;
    
    // MessageSource实例，使用静态变量确保全局可访问
    private static MessageSource messageSource;
    
  private ErrorCode(final Integer code, final String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessageKey() {
        return messageKey;
    }
    
    /**
     * 获取错误消息。
     * 
     * @return 错误消息
     */
    public String getMessage() {
        if (messageSource != null) {
            return messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        }
        // 如果messageSource还未初始化，返回消息键作为默认值
        return messageKey;
    }
    
    /**
     * 根据语言环境获取错误消息。
     * 
     * @param locale 语言环境
     * @return 本地化的错误消息
     */
    public String getMessage(final Locale locale) {
        if (messageSource != null) {
            return messageSource.getMessage(messageKey, null, locale);
        }
        return messageKey;
    }
    
    /**
     * 根据错误码获取错误码枚举。
     * 
     * @param code 错误码
     * @return ErrorCode枚举
     */
    public static ErrorCode getByCode(final Integer code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR;
    }
    
    /**
     * 设置MessageSource实例
     * 这个方法需要在Spring容器初始化后调用
     * @param source MessageSource实例
     */
    public static void setMessageSource(MessageSource source) {
        messageSource = source;
    }
    
    /**
     * MessageSource配置类，用于注入MessageSource到ErrorCode枚举
     */
    @Component
    public static class ErrorCodeMessageSourceConfig {
        
        @Resource
        private MessageSource messageSource;
        
        @PostConstruct
        public void init() {
            ErrorCode.setMessageSource(messageSource);
        }
    }
}