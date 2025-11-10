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

import org.springframework.beans.factory.annotation.Autowired;
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
 * 处理用户登录、注册等认证相关功能
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求数据
     * @return 登录响应数据，包含JWT令牌和用户信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest loginRequest) {
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
    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterRequest registerRequest) {
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
    @GetMapping("/current")
    public Result<User> getCurrentUser(HttpServletRequest request) {
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
}