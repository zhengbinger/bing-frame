package com.bing.framework.controller;

import com.bing.framework.entity.User;
import com.bing.framework.service.UserService;
import com.bing.framework.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 * 通过RESTful API提供用户管理功能，集成Swagger文档和统一响应格式
 * 支持用户信息的查询、新增、更新、删除等核心操作
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 根据ID查询用户。
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<?> getUserById(@PathVariable final Long id) {
        User user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 根据用户名查询用户。
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Operation(summary = "根据用户名查询用户")
    @GetMapping("/username/{username}")
    public Result<?> getUserByUsername(@PathVariable final String username) {
        User user = userService.getUserByUsername(username);
        return Result.success(user);
    }

    /**
     * 查询所有用户。
     * 
     * @return 用户列表
     */
    @Operation(summary = "查询所有用户")
    @GetMapping("/")
    public Result<?> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return Result.success(users);
    }

    /**
     * 新增用户。
     * 
     * @param user 用户信息
     * @return 操作结果
     */
    @Operation(summary = "新增用户")
    @PostMapping("/")
    public Result<?> saveUser(@RequestBody final User user) {
        userService.saveUser(user);
        return Result.success();
    }

    /**
     * 更新用户。
     * 
     * @param user 用户信息
     * @return 操作结果
     */
    @Operation(summary = "更新用户")
    @PutMapping("/")
    public Result<?> updateUser(@RequestBody final User user) {
        userService.updateUser(user);
        return Result.success();
    }

    /**
     * 删除用户。
     * 
     * @param id 用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable final Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 批量删除用户。
     * 
     * @param ids 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除用户")
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestBody final List<Long> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }
    
    /**
     * 重置用户密码。
     * 
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 操作结果
     */
    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/password")
    public Result<?> resetPassword(@PathVariable final Long id, @RequestBody final Map<String, String> request) {
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.fail("新密码不能为空");
        }
        userService.resetPassword(id, newPassword);
        return Result.success();
    }
    
    /**
     * 生成随机密码并重置。
     * 
     * @param id 用户ID
     * @return 生成的随机密码
     */
    @Operation(summary = "生成随机密码并重置")
    @PostMapping("/{id}/random-password")
    public Result<?> generateRandomPassword(@PathVariable final Long id) {
        String randomPassword = userService.generateAndResetPassword(id);
        Map<String, String> result = new HashMap<>();
        result.put("randomPassword", randomPassword);
        return Result.success(result);
    }
}