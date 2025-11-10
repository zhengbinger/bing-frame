package com.bing.framework.controller;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.common.Result;
import com.bing.framework.dto.LoginRequest;
import com.bing.framework.dto.LoginResponse;
import com.bing.framework.dto.RegisterRequest;
import com.bing.framework.entity.Role;
import com.bing.framework.entity.User;
import com.bing.framework.exception.BusinessException;
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

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${jwt.expiration:24}")
    private Integer jwtExpiration;

    // 直接创建BCryptPasswordEncoder实例，避免依赖注入问题
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
    public Result<LoginResponse> login(@ApiParam(name = "loginRequest", value = "登录请求数据", required = true) @Validated @RequestBody LoginRequest loginRequest) {
        // 根据用户名查询用户
        User user = userService.getUserByUsername(loginRequest.getUsername());
        
        // 检查用户是否存在
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        
        // 检查密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INCORRECT_PASSWORD);
        }
        
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 获取用户角色列表
        List<Role> roles = roleService.getRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
        
        // 构建登录响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)));
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRoles(roleCodes);
        
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
            
            log.info("用户注销成功，用户ID: {}, 用户名: {}", userId, username);
            return Result.success("注销成功");
        } catch (Exception e) {
            log.error("用户注销失败", e);
            throw new BusinessException(ErrorCode.LOGOUT_FAILED);
        }
    }
}