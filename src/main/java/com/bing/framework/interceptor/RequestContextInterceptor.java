package com.bing.framework.interceptor;

import com.bing.framework.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求上下文拦截器
 * 在请求处理前设置请求上下文信息，在请求处理后清理上下文信息
 * 确保RequestContext在整个请求生命周期内可用
 * 
 * @author zhengbing
 * @date 2025-11-11
 */
@Slf4j
@Component
public class RequestContextInterceptor implements HandlerInterceptor {

    /**
     * 请求处理前执行
     * 设置请求上下文信息，包括请求对象、响应对象、请求头信息等
     * 
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @return 是否继续处理请求
     * @throws Exception 处理异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 设置请求上下文信息
        RequestContext.setContext(request, response);
        log.debug("请求上下文已设置，请求URI: {}", request.getRequestURI());
        return true;
    }

    /**
     * 请求处理后执行，视图渲染前
     * 
     * @param request      HTTP请求
     * @param response     HTTP响应
     * @param handler      处理器
     * @param modelAndView 模型和视图
     * @throws Exception 处理异常
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 不需要实现特定逻辑
    }

    /**
     * 请求处理完成后执行
     * 清理请求上下文信息，防止内存泄漏
     * 
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @param ex       异常（如果有）
     * @throws Exception 处理异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 清理请求上下文信息
            RequestContext.clear();
            log.debug("请求处理完成，自动清理请求上下文，请求URI: {}", request.getRequestURI());
        } catch (Exception e) {
            // 记录清理异常但不抛出，避免影响正常的请求处理流程
            log.error("清理请求上下文时发生异常: {}", e.getMessage());
        }
    }
}