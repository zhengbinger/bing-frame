package com.bing.framework.exception;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

/**
 * 全局异常处理类
 * 使用@ControllerAdvice注解实现全局异常捕获，统一异常响应格式
 * 处理业务异常、参数异常、空指针异常等各类异常情况
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {



    /**
     * 处理业务异常。
     * 
     * @param e 业务异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result<?> handleBusinessException(final BusinessException e, final HttpServletRequest request) {
        log.error("业务异常: {}", e.getMessage(), e);
        // 直接使用ErrorCode获取多语言消息，而不是使用异常的message属性
        return Result.error(e.getErrorCode());
    }

    /**
     * 处理空指针异常。
     * 
     * @param e 空指针异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    public Result<?> handleNullPointerException(final NullPointerException e, final HttpServletRequest request) {
        log.error("空指针异常: {}", e.getMessage(), e);
        return Result.error(ErrorCode.NULL_POINTER_ERROR);
    }

    /**
     * 处理SQL异常。
     * 
     * @param e SQL异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(SQLException.class)
    @ResponseBody
    public Result<?> handleSQLException(final SQLException e, final HttpServletRequest request) {
        log.error("数据库异常: {}", e.getMessage(), e);
        return Result.error(ErrorCode.DATABASE_ERROR);
    }

    /**
     * 处理参数异常。
     * 
     * @param e 参数异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public Result<?> handleIllegalArgumentException(final IllegalArgumentException e, final HttpServletRequest request) {
        log.error("参数异常: {}", e.getMessage(), e);
        return Result.error(ErrorCode.PARAM_ERROR, e.getMessage());
    }

    /**
     * 处理系统异常。
     * 
     * @param e 系统异常
     * @param request HTTP请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<?> handleException(final Exception e, final HttpServletRequest request) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error(ErrorCode.SYSTEM_ERROR);
    }
}