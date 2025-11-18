package com.bing.framework.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应结果类
 * 使用Lombok的@Data注解简化开发，实现Serializable接口支持序列化
 * 封装API返回的标准格式，包含状态码、消息和数据
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码，200表示成功，其他表示失败
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 成功响应。
     * 
     * @param data 返回数据
     * @return Result<T>
     */
    public static <T> Result<T> success(final T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMessage(ErrorCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（无数据）。
     * 
     * @return Result<Void>
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 错误响应。
     * 
     * @param code 错误码
     * @param message 错误消息
     * @return Result<T>
     */
    public static <T> Result<T> error(final Integer code, final String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }
    
    /**
     * 错误响应（使用错误码枚举）。
     * 
     * @param errorCode 错误码枚举
     * @return Result<T>
     */
    public static <T> Result<T> error(final ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }
    
    /**
     * 错误响应（使用错误码枚举，自定义错误消息）。
     * 
     * @param errorCode 错误码枚举
     * @param customMessage 自定义错误消息
     * @return Result<T>
     */
    public static <T> Result<T> error(final ErrorCode errorCode, final String customMessage) {
        return error(errorCode.getCode(), customMessage);
    }
}