package com.bing.framework.context;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求上下文类
 * 使用ThreadLocal存储HTTP请求相关的上下文信息，提供统一的访问接口
 * 用于替换在普通接口中直接使用HttpServletRequest对象的现象
 * 
 * @author zhengbing
 * @date 2025-11-11
 */
@Slf4j
public class RequestContext {

    /**
     * 使用ThreadLocal存储请求对象
     */
    private static final ThreadLocal<HttpServletRequest> REQUEST_HOLDER = new ThreadLocal<>();
    
    /**
     * 使用ThreadLocal存储响应对象
     */
    private static final ThreadLocal<HttpServletResponse> RESPONSE_HOLDER = new ThreadLocal<>();
    
    /**
     * 使用ThreadLocal存储请求头信息
     */
    private static final ThreadLocal<Map<String, String>> HEADER_HOLDER = ThreadLocal.withInitial(HashMap::new);
    
    /**
     * 使用ThreadLocal存储客户端IP地址
     */
    private static final ThreadLocal<String> CLIENT_IP_HOLDER = new ThreadLocal<>();
    
    /**
     * 使用ThreadLocal存储User-Agent信息
     */
    private static final ThreadLocal<String> USER_AGENT_HOLDER = new ThreadLocal<>();

    /**
     * 设置请求上下文信息
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     */
    public static void setContext(HttpServletRequest request, HttpServletResponse response) {
        REQUEST_HOLDER.set(request);
        RESPONSE_HOLDER.set(response);
        
        // 提取请求头信息
        Map<String, String> headers = new HashMap<>();
        for (String headerName : request.getHeaderNames()) {
            headers.put(headerName, request.getHeader(headerName));
        }
        HEADER_HOLDER.set(headers);
        
        // 设置客户端IP地址
        String clientIp = getClientIpFromRequest(request);
        CLIENT_IP_HOLDER.set(clientIp);
        
        // 设置User-Agent
        USER_AGENT_HOLDER.set(request.getHeader("User-Agent"));
        
        log.debug("设置请求上下文完成，客户端IP: {}", clientIp);
    }

    /**
     * 获取当前请求对象
     * 
     * @return HTTP请求对象，如果不存在则返回null
     */
    public static HttpServletRequest getRequest() {
        return REQUEST_HOLDER.get();
    }

    /**
     * 获取当前响应对象
     * 
     * @return HTTP响应对象，如果不存在则返回null
     */
    public static HttpServletResponse getResponse() {
        return RESPONSE_HOLDER.get();
    }

    /**
     * 获取指定的请求头值
     * 
     * @param headerName 请求头名称
     * @return 请求头值，如果不存在则返回null
     */
    public static String getHeader(String headerName) {
        Map<String, String> headers = HEADER_HOLDER.get();
        if (headers != null) {
            return headers.get(headerName);
        }
        
        // 如果头信息不存在，尝试从请求对象中获取
        HttpServletRequest request = getRequest();
        if (request != null) {
            return request.getHeader(headerName);
        }
        
        return null;
    }

    /**
     * 获取所有请求头信息
     * 
     * @return 请求头信息映射
     */
    public static Map<String, String> getHeaders() {
        return new HashMap<>(HEADER_HOLDER.get());
    }

    /**
     * 获取客户端IP地址
     * 
     * @return 客户端IP地址
     */
    public static String getClientIp() {
        String ip = CLIENT_IP_HOLDER.get();
        if (ip == null) {
            HttpServletRequest request = getRequest();
            if (request != null) {
                ip = getClientIpFromRequest(request);
                CLIENT_IP_HOLDER.set(ip);
            }
        }
        return ip;
    }

    /**
     * 获取User-Agent信息
     * 
     * @return User-Agent字符串
     */
    public static String getUserAgent() {
        return USER_AGENT_HOLDER.get();
    }

    /**
     * 获取请求URI
     * 
     * @return 请求URI
     */
    public static String getRequestUri() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return request.getRequestURI();
        }
        return null;
    }

    /**
     * 获取请求方法
     * 
     * @return HTTP请求方法（GET, POST等）
     */
    public static String getRequestMethod() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return request.getMethod();
        }
        return null;
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

    /**
     * 清理请求上下文信息，防止内存泄漏
     */
    public static void clear() {
        try {
            REQUEST_HOLDER.remove();
            RESPONSE_HOLDER.remove();
            HEADER_HOLDER.remove();
            CLIENT_IP_HOLDER.remove();
            USER_AGENT_HOLDER.remove();
            log.debug("请求上下文已清理");
        } catch (Exception e) {
            log.error("清理请求上下文时发生异常: {}", e.getMessage());
        }
    }
}