package com.bing.framework.exception;

import com.bing.framework.common.ErrorCode;

/**
 * 业务异常类
 * 用于封装业务逻辑处理过程中的自定义异常
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String customMessage; // 用于存储自定义消息，可空

    public BusinessException(Integer code, String message) {
        this.customMessage = message;
        this.errorCode = ErrorCode.getByCode(code);
    }
    
    public BusinessException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.customMessage = null; // 不使用自定义消息，使用ErrorCode的多语言消息
    }
    
    public BusinessException(ErrorCode errorCode, String customMessage) {
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    public Integer getCode() {
        return errorCode != null ? errorCode.getCode() : null;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        // 如果提供了自定义消息，使用自定义消息
        // 否则使用ErrorCode的多语言消息
        return customMessage != null ? customMessage : (errorCode != null ? errorCode.getMessage() : null);
    }
}