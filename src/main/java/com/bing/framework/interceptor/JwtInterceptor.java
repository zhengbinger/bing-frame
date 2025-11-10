package com.bing.framework.interceptor;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.service.WhiteListService;
import com.bing.framework.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT拦截器
 * 用于验证请求中的JWT令牌
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private WhiteListService whiteListService;
    
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    
    // 日志记录器
    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);

    /**
     * 拦截请求，验证JWT令牌
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求路径
        String requestPath = request.getRequestURI();
        log.debug("拦截请求: {}", requestPath);
        
        // 检查是否在白名单中
        if (whiteListService.isInWhiteList(requestPath)) {
            log.debug("请求路径 '{}' 在白名单中，直接通过", requestPath);
            return true;
        }
        log.debug("请求路径 '{}' 不在白名单中，需要验证JWT令牌", requestPath);
        
        // 获取Authorization请求头
        String authorization = request.getHeader("Authorization");
        
        // 检查Authorization请求头是否存在
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        // 提取JWT令牌
        String token = authorization.substring(7);
        
        // 检查令牌是否在黑名单中
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        if (redisTemplate.hasKey(blacklistKey)) {
            throw new BusinessException(ErrorCode.TOKEN_BLACKLISTED);
        }
        
        // 验证JWT令牌
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        
        // 将用户信息存储到请求属性中
        request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
        request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // 不需要实现
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 不需要实现
    }
}