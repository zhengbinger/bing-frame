package com.bing.framework.controller;

import com.bing.framework.entity.User;
import com.bing.framework.service.UserService;
import com.bing.framework.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bing.framework.common.ErrorCode;
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
@Api(tags = "用户管理", description = "提供用户信息的查询、新增、更新、删除及密码管理等功能")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 根据ID查询用户。
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @ApiOperation(value = "根据ID获取用户", notes = "通过用户ID获取用户的详细信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/{id}")
    public Result<?> getUserById(@ApiParam(name = "id", value = "用户ID", required = true) @PathVariable final Long id) {
        User user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 根据用户名查询用户。
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @ApiOperation(value = "根据用户名获取用户", notes = "通过用户名获取用户的详细信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/username/{username}")
    public Result<?> getUserByUsername(@ApiParam(name = "username", value = "用户名", required = true) @PathVariable final String username) {
        User user = userService.getUserByUsername(username);
        return Result.success(user);
    }

    /**
     * 查询所有用户。
     * 
     * @return 用户列表
     */
    @ApiOperation(value = "获取所有用户", notes = "返回系统中所有的用户列表")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 500, message = "查询失败")
    })
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
    @ApiOperation(value = "新增用户", notes = "创建新的用户账户，包含基本用户信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "创建成功"),
        @ApiResponse(code = 400, message = "参数错误或用户名已存在"),
        @ApiResponse(code = 500, message = "创建失败")
    })
    @PostMapping("/")
    public Result<?> saveUser(@ApiParam(name = "user", value = "用户信息", required = true) @RequestBody final User user) {
        userService.saveUser(user);
        return Result.success();
    }

    /**
     * 更新用户。
     * 
     * @param user 用户信息
     * @return 操作结果
     */
    @ApiOperation(value = "更新用户", notes = "更新用户的基本信息，不包括密码")
    @ApiResponses({
        @ApiResponse(code = 200, message = "更新成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 500, message = "更新失败")
    })
    @PutMapping("/")
    public Result<?> updateUser(@ApiParam(name = "user", value = "用户信息", required = true) @RequestBody final User user) {
        userService.updateUser(user);
        return Result.success();
    }

    /**
     * 删除用户。
     * 
     * @param id 用户ID
     * @return 操作结果
     */
    @ApiOperation(value = "删除用户", notes = "根据ID删除指定的用户")
    @ApiResponses({
        @ApiResponse(code = 200, message = "删除成功"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 500, message = "删除失败")
    })
    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@ApiParam(name = "id", value = "用户ID", required = true) @PathVariable final Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 批量删除用户。
     * 
     * @param ids 用户ID列表
     * @return 操作结果
     */
    @ApiOperation(value = "批量删除用户", notes = "一次性删除多个用户")
    @ApiResponses({
        @ApiResponse(code = 200, message = "删除成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 500, message = "删除失败")
    })
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@ApiParam(name = "ids", value = "用户ID列表", required = true) @RequestBody final List<Long> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }
    
    /**
     * 重置用户密码。
     * 
     * @param id 用户ID
     * @param request 包含新密码的请求体
     * @return 操作结果
     */
    @ApiOperation(value = "重置用户密码", notes = "重置指定用户的密码")
    @ApiResponses({
        @ApiResponse(code = 200, message = "密码重置成功"),
        @ApiResponse(code = 400, message = "参数错误")
    })
    @PutMapping("/{id}/password")
    public Result<?> resetPassword(@ApiParam(name = "id", value = "用户ID", required = true) @PathVariable final Long id, 
                                 @ApiParam(name = "request", value = "包含新密码的请求体", required = true) @RequestBody final Map<String, String> request) {
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error(ErrorCode.PARAM_ERROR.getCode(), "新密码不能为空");
        }
        userService.resetPassword(id, newPassword);
        return Result.success();
    }
    
    /**
     * 生成随机密码。
     * 
     * @param id 用户ID
     * @return 随机生成的密码
     */
    @ApiOperation(value = "生成随机密码", notes = "为指定用户生成一个随机密码")
    @ApiResponses({
        @ApiResponse(code = 200, message = "密码生成成功"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 500, message = "密码生成失败")
    })
    @PostMapping("/{id}/random-password")
    public Result<?> generateRandomPassword(@ApiParam(name = "id", value = "用户ID", required = true) @PathVariable final Long id) {
        String randomPassword = userService.generateAndResetPassword(id);
        Map<String, String> result = new HashMap<>();
        result.put("randomPassword", randomPassword);
        return Result.success(result);
    }
}