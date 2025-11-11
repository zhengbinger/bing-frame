package com.bing.framework.controller;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.common.Result;
import com.bing.framework.dto.LoginRequest;
import com.bing.framework.dto.LoginResponse;
import com.bing.framework.dto.RegisterRequest;
import com.bing.framework.entity.LoginRecord;
import com.bing.framework.entity.Role;
import com.bing.framework.entity.User;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.service.LoginRecordService;
import com.bing.framework.service.RoleService;
import com.bing.framework.service.UserService;
import com.bing.framework.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;


import java.util.Date;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证控制器
 * 处理用户登录、注册、注销等认证相关功能
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Api(tags = "认证管理", description = "提供用户登录、注册、注销和获取当前用户信息等认证相关功能")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    

    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_TOKEN_PREFIX = "user:token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:token:";

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private LoginRecordService loginRecordService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${jwt.expiration:24}")
    private Integer jwtExpiration;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求数据
     * @return 登录响应数据，包含JWT令牌和用户信息
     */
    @ApiOperation(value = "用户登录", notes = "用户登录接口，验证用户名和密码，返回JWT令牌和用户信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "登录成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 401, message = "用户名或密码错误"),
        @ApiResponse(code = 403, message = "用户已禁用"),
        @ApiResponse(code = 500, message = "登录失败")
    })
    @PostMapping("/login")
    public Result<LoginResponse> login(@ApiParam(name = "loginRequest", value = "登录请求数据", required = true) @Validated @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        // 根据用户名查询用户
        User user = userService.getUserByUsername(loginRequest.getUsername());
        
        // 检查用户是否存在
        if (user == null) {
            log.info("Login failed: user not found, username={}", loginRequest.getUsername());
            // 记录失败的登录日志
            recordLoginRecord(request, loginRequest.getUsername(), null, 0, "用户不存在");
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            log.info("Login failed: user disabled, username={}", loginRequest.getUsername());
            // 记录失败的登录日志
            recordLoginRecord(request, loginRequest.getUsername(), user.getId(), 0, "用户已禁用");
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        
        // 详细日志记录
        log.info("========== Password Validation Diagnostics ==========");
        log.info("Username: {}", loginRequest.getUsername());
        log.info("Input password length: {}", loginRequest.getPassword().length());
        log.info("Database password length: {}", user.getPassword().length());
        
        // 获取输入密码和数据库密码
        String inputPassword = loginRequest.getPassword();
        String dbPassword = user.getPassword();
        
        // 检查是否为BCrypt格式
        boolean isBCryptFormat = dbPassword.startsWith("$2a$") || 
                               dbPassword.startsWith("$2b$") || 
                               dbPassword.startsWith("$2y$");
        log.info("Database password format: isBCrypt={}", isBCryptFormat);
        
        // 验证密码的多种策略
        boolean passwordMatch = false;
        
        try {
            // 优先尝试BCrypt验证（如果数据库密码是BCrypt格式）
            if (isBCryptFormat) {
                try {
                    passwordMatch = passwordEncoder.matches(inputPassword, dbPassword);
                    log.info("Strategy 1 - BCrypt validation result: {}", passwordMatch);
                } catch (Exception e) {
                    log.warn("BCrypt validation exception: {}", e.getMessage());
                }
            }
            
            // 如果BCrypt验证失败或者不是BCrypt格式，尝试明文密码比较
            // if (!passwordMatch) {
            //     passwordMatch = inputPassword.equals(dbPassword);
            //     log.info("Strategy 2 - Plain text comparison result: {}", passwordMatch);
                
            //     if (passwordMatch) {
            //         log.warn("Plain text password detected for user: {}", user.getUsername());
            //         // 自动更新为BCrypt格式
            //         try {
            //             user.setPassword(passwordEncoder.encode(inputPassword));
            //             userService.updateUser(user);
            //             log.info("Successfully updated password format to BCrypt for user: {}", user.getUsername());
            //         } catch (Exception e) {
            //             log.error("Failed to update password format for user: {}", user.getUsername(), e);
            //         }
            //     }
            // }
            
            // 检查是否有前后空白字符问题
            // if (!passwordMatch) {
            //     String trimmedPassword = inputPassword.trim();
            //     // 尝试修剪后的密码BCrypt验证
            //     if (isBCryptFormat) {
            //         boolean trimmedMatch = passwordEncoder.matches(trimmedPassword, dbPassword);
            //         log.info("Strategy 3a - Trimmed password BCrypt result: {}", trimmedMatch);
            //         if (trimmedMatch) {
            //             passwordMatch = true;
            //             log.warn("Password contains leading/trailing whitespace: {}", user.getUsername());
            //         }
            //     }
            //     // 尝试修剪后的密码明文比较
            //     if (!passwordMatch) {
            //         boolean trimmedPlainMatch = trimmedPassword.equals(dbPassword);
            //         log.info("Strategy 3b - Trimmed password plain text result: {}", trimmedPlainMatch);
            //         if (trimmedPlainMatch) {
            //             passwordMatch = true;
            //             log.warn("Password contains leading/trailing whitespace: {}", user.getUsername());
            //         }
            //     }
            // }
            
            // 特殊处理已知的非BCrypt格式用户（根据启动日志发现）
            // if (!passwordMatch && ("admin".equals(user.getUsername()) || 
            //                        "user01".equals(user.getUsername()) || 
            //                        "readonly".equals(user.getUsername()))) {
            //     log.info("Strategy 4 - Special handling for known non-BCrypt user: {}", user.getUsername());
            //     // 对于这些用户，直接使用明文密码比较
            //     passwordMatch = inputPassword.equals(dbPassword);
                
            //     // 如果匹配成功，自动更新为BCrypt格式
            //     if (passwordMatch) {
            //         log.warn("Auto-updating password to BCrypt format for user: {}", user.getUsername());
            //         try {
            //             user.setPassword(passwordEncoder.encode(inputPassword));
            //             userService.updateUser(user);
            //             log.info("Successfully updated password format for user: {}", user.getUsername());
            //         } catch (Exception e) {
            //             log.error("Failed to update password format for user: {}", user.getUsername(), e);
            //         }
            //     }
            // }
            
            // 开发环境特殊处理：允许测试密码"123456"
            // if (!passwordMatch && "123456".equals(inputPassword)) {
            //     log.warn("DEBUG MODE: Allowing login with test password for user: {}", user.getUsername());
            //     passwordMatch = true;
            // }
            
        } catch (Exception e) {
            log.error("Error during password validation: {}", e.getMessage(), e);
        }
        
        if (!passwordMatch) {
            log.info("Password validation failed for user: {}", user.getUsername());
            // 记录失败的登录日志
            recordLoginRecord(request, loginRequest.getUsername(), user.getId(), 0, "密码错误");
            throw new BusinessException(ErrorCode.INCORRECT_PASSWORD);
        }
        
        log.info("Password validation successful for user: {}", user.getUsername());
        
        // 生成JWT访问令牌和刷新令牌
        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
        
        // 将访问令牌保存到Redis，设置过期时间
        String userTokenKey = USER_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(userTokenKey, accessToken, jwtExpiration, TimeUnit.HOURS);
        
        // 将刷新令牌保存到Redis，设置过期时间
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(refreshTokenKey, user.getId(), jwtUtil.getRefreshExpiration(), TimeUnit.HOURS);
        
        // 获取用户角色列表
        List<Role> roles = roleService.getRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
        
        // 构建登录响应
        LoginResponse response = new LoginResponse();
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(jwtExpiration)));
        response.setRefreshExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(jwtUtil.getRefreshExpiration())));
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRoles(roleCodes);
        
        // 记录成功的登录日志
        recordLoginRecord(request, user.getUsername(), user.getId(), 1, "登录成功");
        
        return Result.success(response);
    }

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求数据
     * @return 注册结果
     */
    @ApiOperation(value = "用户注册", notes = "用户注册接口，创建新用户账户，默认分配普通用户角色")
    @ApiResponses({
        @ApiResponse(code = 200, message = "注册成功"),
        @ApiResponse(code = 400, message = "参数错误或用户名已存在"),
        @ApiResponse(code = 500, message = "注册失败")
    })
    @PostMapping("/register")
    public Result<?> register(@ApiParam(name = "registerRequest", value = "注册请求数据", required = true) @Validated @RequestBody RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        User existingUser = userService.getUserByUsername(registerRequest.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USER_EXIST);
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setNickname(registerRequest.getNickname() != null ? registerRequest.getNickname() : registerRequest.getUsername());
        user.setStatus(1); // 默认启用
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        
        // 保存用户
        boolean saved = userService.saveUser(user);
        if (!saved) {
            throw new BusinessException(ErrorCode.REGISTER_FAILED);
        }
        
        // 为新用户分配默认角色（普通用户）
        // 这里假设角色ID为3的是普通用户角色
        roleService.assignRolesToUser(user.getId(), Arrays.asList(3L));
        
        return Result.success();
    }

    /**
     * 获取当前登录用户信息
     * 
     * @param request HTTP请求
     * @return 当前用户信息
     */
    @ApiOperation(value = "获取当前用户", notes = "获取当前登录用户的详细信息，需要在请求头中携带有效的JWT令牌")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未登录或令牌失效"),
        @ApiResponse(code = 403, message = "无权限访问"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/current")
    public Result<User> getCurrentUser(@ApiParam(hidden = true) HttpServletRequest request) {
        // 从请求属性中获取用户ID
        Long userId = (Long) request.getAttribute("userId");
        
        // 查询用户信息
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 清空密码等敏感信息
        user.setPassword(null);
        
        return Result.success(user);
    }
    
    /**
     * 用户注销
     * 将用户的JWT令牌加入黑名单，实现立即失效
     * 
     * @param request HTTP请求，包含用户的JWT令牌
     * @return 注销结果
     */
    @ApiOperation(value = "用户注销", notes = "用户注销接口，将当前用户的JWT令牌加入黑名单，使令牌立即失效")
    @ApiResponses({
        @ApiResponse(code = 200, message = "注销成功"),
        @ApiResponse(code = 401, message = "未登录或令牌失效"),
        @ApiResponse(code = 500, message = "注销失败")
    })
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        try {
            // 从请求头获取Authorization
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            
            // 提取令牌
            String token = authorization.substring(7);
            
            // 获取用户信息，用于日志记录
            Long userId = (Long) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");
            
            // 将令牌加入黑名单，设置与原令牌相同的过期时间
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, userId, jwtExpiration, TimeUnit.HOURS);
            
            // 删除用户的访问令牌缓存
            String userTokenKey = USER_TOKEN_PREFIX + userId;
            redisTemplate.delete(userTokenKey);
            
            log.info("用户注销成功，用户ID: {}, 用户名: {}", userId, username);
            return Result.success("注销成功");
        } catch (Exception e) {
            log.error("用户注销失败", e);
            throw new BusinessException(ErrorCode.LOGOUT_FAILED);
        }
    }
    
    /**
     * 刷新访问令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌和刷新令牌
     */
    @ApiOperation(value = "刷新访问令牌", notes = "使用刷新令牌获取新的访问令牌")
    @ApiResponses({
        @ApiResponse(code = 200, message = "刷新成功"),
        @ApiResponse(code = 401, message = "刷新令牌无效或已过期"),
        @ApiResponse(code = 500, message = "刷新失败")
    })
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@ApiParam(name = "refreshToken", value = "刷新令牌", required = true) @RequestParam String refreshToken) {
        try {
            // 验证刷新令牌是否有效
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                log.info("Invalid refresh token");
                throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
            
            // 检查刷新令牌是否在Redis中存在
            String refreshTokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            if (!redisTemplate.hasKey(refreshTokenKey)) {
                log.info("Refresh token not found in Redis");
                throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
            
            // 从刷新令牌中获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            
            // 验证用户是否存在
            User user = userService.getUserById(userId);
            if (user == null || user.getStatus() == 0) {
                log.info("User not found or disabled");
                // 删除无效的刷新令牌
                redisTemplate.delete(refreshTokenKey);
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }
            
            // 生成新的访问令牌
            String newAccessToken = jwtUtil.generateToken(userId, username);
            
            // 更新Redis中的访问令牌
            String userTokenKey = USER_TOKEN_PREFIX + userId;
            redisTemplate.opsForValue().set(userTokenKey, newAccessToken, jwtExpiration, TimeUnit.HOURS);
            
            // 获取用户角色列表
            List<Role> roles = roleService.getRolesByUserId(userId);
            List<String> roleCodes = roles.stream()
                    .map(Role::getCode)
                    .collect(Collectors.toList());
            
            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setToken(newAccessToken);
            response.setRefreshToken(refreshToken); // 保留原有的刷新令牌
            response.setExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(jwtExpiration)));
            response.setRefreshExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(jwtUtil.getRefreshExpiration())));
            response.setUserId(userId);
            response.setUsername(username);
            response.setNickname(user.getNickname());
            response.setRoles(roleCodes);
            
            log.info("Token refreshed successfully for user: {}", username);
            return Result.success(response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_FAILED);
        }
    }
    
    /**
     * 记录登录日志
     * 
     * @param request HTTP请求
     * @param username 用户名
     * @param userId 用户ID
     * @param status 登录状态：0-失败，1-成功
     * @param message 登录结果描述
     */
    private void recordLoginRecord(HttpServletRequest request, String username, Long userId, Integer status, String message) {
        try {
            LoginRecord loginRecord = new LoginRecord();
            loginRecord.setUsername(username);
            if (userId != null) {
                loginRecord.setUserId(userId);
            }
            loginRecord.setStatus(status);
            loginRecord.setMessage(message);
            loginRecord.setIpAddress(getClientIp(request));
            loginRecord.setUserAgent(request.getHeader("User-Agent"));
            loginRecord.setLoginTime(new Date());
            
            // 异步记录登录记录
            loginRecordService.saveLoginRecord(loginRecord);
        } catch (Exception e) {
            // 记录登录日志失败不应影响主流程
            log.error("Failed to record login log", e);
        }
    }
    
    /**
     * 获取客户端真实IP地址
     * 
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
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