package com.bing.framework.interceptor;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.service.WhiteListService;
import com.bing.framework.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
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
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private WhiteListService whiteListService;
    
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_TOKEN_PREFIX = "user:token:";
    
    @Value("${jwt.expiration:24}")
    private Integer jwtExpiration;
    
    // 日志记录器


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
            log.warn("请求缺少有效的Authorization头");
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        // 提取JWT令牌
        String token = authorization.substring(7);
        
        // 检查令牌是否在黑名单中
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            log.warn("Token已被加入黑名单");
            throw new BusinessException(ErrorCode.TOKEN_BLACKLISTED);
        }
        
        try {
            // 验证JWT令牌是否有效且为access类型
            if (!jwtUtil.validateToken(token)) {
                log.warn("Token验证失败或不是有效的访问令牌");
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
            
            // 从token中获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            
            // 检查Redis中是否存在该用户的有效token
            String userTokenKey = USER_TOKEN_PREFIX + userId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(userTokenKey))) {
                String redisToken = (String) redisTemplate.opsForValue().get(userTokenKey);
                // 验证请求中的token是否与Redis中保存的token一致
                if (!token.equals(redisToken)) {
                    log.warn("Token已失效或被替换");
                    throw new BusinessException(ErrorCode.INVALID_TOKEN);
                }
            } else {
                log.warn("未找到用户对应的有效Token");
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
            
            // 将用户信息存储到请求属性中
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            
            // 设置用户上下文信息
            com.bing.framework.context.UserContext.setUserInfo(userId, username);
            
            log.debug("Token验证通过，用户ID: {}, 用户名: {}", userId, username);
            
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token处理异常: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
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