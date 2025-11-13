package com.bing.framework.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 请求上下文类
 * 使用Spring的RequestContextHolder获取HTTP请求相关的上下文信息，提供统一的访问接口
 * 用于替换在普通接口中直接使用HttpServletRequest对象的现象
 * 
 * @author zhengbing
 * @date 2025-11-11
 */
@Slf4j
public class RequestContext {

    /**
     * @return 获取当前请求
     */
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }
    
    /**
     * @return 获取当前响应
     */
    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }
    
    /**
     * 获取请求属性
     * 
     * @return ServletRequestAttributes实例
     */
    private static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = Optional.ofNullable(RequestContextHolder.getRequestAttributes()).orElseThrow(() -> {
            log.error("非web上下文无法获取请求属性, 异步操作请在同步操作内获取所需信息");
            return new RuntimeException("请求异常");
        });
        return ((ServletRequestAttributes) attributes);
    }

    /**
     * 获取指定的请求头值
     * 
     * @param headerName 请求头名称
     * @return 请求头值，如果不存在则返回null
     */
    public static String getHeader(String headerName) {
        try {
            return getRequest().getHeader(headerName);
        } catch (Exception e) {
            log.error("获取请求头失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取所有请求头信息
     * 
     * @return 请求头信息映射
     */
    public static Map<String, String> getHeaders() {
        try {
            HttpServletRequest request = getRequest();
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
            return headers;
        } catch (Exception e) {
            log.error("获取请求头信息失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 获取客户端IP地址
     * 
     * @return 客户端IP地址
     */
    public static String getClientIp() {
        try {
            return getClientIpFromRequest(getRequest());
        } catch (Exception e) {
            log.error("获取客户端IP失败: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * 获取User-Agent信息
     * 
     * @return User-Agent字符串
     */
    public static String getUserAgent() {
        return getHeader("User-Agent");
    }

    /**
     * 获取请求URI
     * 
     * @return 请求URI
     */
    public static String getRequestUri() {
        try {
            return getRequest().getRequestURI();
        } catch (Exception e) {
            log.error("获取请求URI失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取请求方法
     * 
     * @return HTTP请求方法（GET, POST等）
     */
    public static String getRequestMethod() {
        try {
            return getRequest().getMethod();
        } catch (Exception e) {
            log.error("获取请求方法失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从请求对象中获取客户端真实IP地址
     * 
     * @param request HTTP请求对象
     * @return 客户端真实IP地址
     */
    private static String getClientIpFromRequest(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}