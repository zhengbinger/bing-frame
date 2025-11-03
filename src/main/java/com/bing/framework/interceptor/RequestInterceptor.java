package com.bing.framework.interceptor;

import com.bing.framework.service.WhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 请求拦截器。
 * 根据数据库配置的白名单对请求进行拦截控制。
 *
 * @author zhengbing
 * @date 2024-11-03
 */
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired
    private WhiteListService whiteListService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestURI = request.getRequestURI();
        
        // 检查是否在白名单中
        if (whiteListService.isInWhiteList(requestURI)) {
            // 在白名单中，允许访问
            return true;
        } else {
            // 不在白名单中，拒绝访问
            return handleRejectedRequest(response);
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 处理请求后的操作，可以在这里添加日志记录等
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后的清理操作
    }

    /**
     * 处理被拒绝的请求。
     *
     * @param response 响应对象
     * @return false 表示拒绝请求继续处理
     * @throws IOException IO异常
     */
    private boolean handleRejectedRequest(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        String jsonResponse = "{\"code\":403,\"message\":\"Access Denied: Request path not in whitelist\"}";
        
        // 设置响应的字符编码
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(jsonResponse);
            writer.flush();
        }
        
        return false;
    }
}