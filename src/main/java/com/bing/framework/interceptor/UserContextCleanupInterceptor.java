package com.bing.framework.interceptor;

import com.bing.framework.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户上下文清理拦截器
 * 确保在请求处理完成后自动清理ThreadLocal中的用户上下文信息，防止内存泄漏
 * 适用于所有HTTP请求，在请求结束时执行清理操作
 *
 * @author zhengbing
 * @date 2025-11-11
 */
@Slf4j
public class UserContextCleanupInterceptor implements HandlerInterceptor {

    /**
     * 请求处理完成后执行，无论成功或失败
     * 确保用户上下文被正确清理，防止ThreadLocal内存泄漏
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @param ex       异常（如果有）
     * @throws Exception 处理异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) throws Exception {
        try {
            // 清理用户上下文信息
            UserContext.clear();
            log.debug("请求处理完成，自动清理用户上下文");
        } catch (Exception e) {
            // 记录清理异常但不抛出，避免影响正常的请求处理流程
            log.error("清理用户上下文时发生异常: {}", e.getMessage());
        }
    }
}