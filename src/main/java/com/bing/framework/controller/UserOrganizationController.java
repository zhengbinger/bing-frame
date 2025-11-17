package com.bing.framework.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.common.Result;
import com.bing.framework.entity.Organization;
import com.bing.framework.entity.UserOrganization;
import com.bing.framework.service.UserOrganizationService;

/**
 * 用户组织关联管理Controller
 * 基于MyBatis-Plus实现的RESTful API接口
 * 提供用户与组织关联关系的CRUD操作，支持用户与组织的关联、解绑、查询、设置主组织等操作
 * 
 * @author zhengbing
 * @date 2025-11-12
 */
@RestController
@RequestMapping("/api/user-organization")
@Api(tags = "用户组织关联管理")
public class UserOrganizationController {

    @Autowired
    private UserOrganizationService userOrganizationService;

    /**
     * 根据用户ID查询关联的所有组织
     * @param userId 用户ID
     * @return ApiResult<List<Organization>>
     */
    @GetMapping("/user/{userId}")
    @ApiOperation(value = "根据用户ID查询关联的所有组织", notes = "获取用户关联的所有组织信息")
    public Result<List<Organization>> getUserOrganizationsByUserId(
            @ApiParam(name = "userId", value = "用户ID", required = true) @PathVariable Long userId) {
        List<Organization> organizations = userOrganizationService.getUserOrganizationsByUserId(userId);
        return Result.success(organizations);
    }

    /**
     * 根据组织ID查询关联的所有用户
     * @param organizationId 组织ID
     * @return ApiResult<List<UserOrganization>>
     */
    @GetMapping("/organization/{organizationId}")
    @ApiOperation(value = "根据组织ID查询关联的所有用户", notes = "获取组织关联的所有用户信息")
    public Result<List<UserOrganization>> getUserOrganizationsByOrganizationId(
            @ApiParam(name = "organizationId", value = "组织ID", required = true) @PathVariable Long organizationId) {
        List<UserOrganization> userOrganizations = userOrganizationService.getOrganizationUsersByOrganizationId(organizationId);
        return Result.success(userOrganizations);
    }

    /**
     * 查询用户的主组织
     * @param userId 用户ID
     * @return ApiResult<Organization>
     */
    @GetMapping("/main/{userId}")
    @ApiOperation(value = "查询用户的主组织", notes = "获取用户的主组织信息")
    public Result<Organization> getUserMainOrganization(
            @ApiParam(name = "userId", value = "用户ID", required = true) @PathVariable Long userId) {
        Organization mainOrganization = userOrganizationService.getUserMainOrganization(userId);
        return Result.success(mainOrganization);
    }

    /**
     * 关联用户和组织
     * @param userId 用户ID
     * @param organizationId 组织ID
     * @param isMain 是否为主组织
     * @return ApiResult<Boolean>
     */
    @PostMapping("/bind")
    @ApiOperation(value = "关联用户和组织", notes = "建立用户与组织的关联关系")
    public Result<Boolean> bindUserOrganization(
            @ApiParam(name = "userId", value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(name = "organizationId", value = "组织ID", required = true) @RequestParam Long organizationId,
            @ApiParam(name = "isMain", value = "是否为主组织", defaultValue = "false") @RequestParam(defaultValue = "false") boolean isMain) {
        try {
            boolean result = userOrganizationService.bindUserOrganization(userId, organizationId, isMain);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(ErrorCode.BUSINESS_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 批量关联用户和组织
     * @param userId 用户ID
     * @param organizationIds 组织ID列表
     * @param mainOrganizationId 主组织ID
     * @return ApiResult<Boolean>
     */
    @PostMapping("/batch-bind")
    @ApiOperation(value = "批量关联用户和组织", notes = "批量建立用户与多个组织的关联关系")
    public Result<Boolean> batchBindUserOrganizations(
            @ApiParam(name = "userId", value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(name = "organizationIds", value = "组织ID列表", required = true) @RequestParam List<Long> organizationIds,
            @ApiParam(name = "mainOrganizationId", value = "主组织ID") @RequestParam(required = false) Long mainOrganizationId) {
        try {
            boolean result = userOrganizationService.batchBindUserOrganizations(userId, organizationIds);
            // 如果指定了主组织，额外设置主组织
            if (mainOrganizationId != null) {
                userOrganizationService.setMainOrganization(userId, mainOrganizationId);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(ErrorCode.BUSINESS_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 取消用户和组织的关联
     * @param userId 用户ID
     * @param organizationId 组织ID
     * @return ApiResult<Boolean>
     */
    @DeleteMapping("/unbind")
    @ApiOperation(value = "取消用户和组织的关联", notes = "解除用户与组织的关联关系")
    public Result<Boolean> unbindUserOrganization(
            @ApiParam(name = "userId", value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(name = "organizationId", value = "组织ID", required = true) @RequestParam Long organizationId) {
        try {
            boolean result = userOrganizationService.unbindUserOrganization(userId, organizationId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(ErrorCode.BUSINESS_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 批量取消用户和组织的关联
     * @param userId 用户ID
     * @param organizationIds 组织ID列表
     * @return ApiResult<Boolean>
     */
    @DeleteMapping("/batch-unbind")
    @ApiOperation(value = "批量取消用户和组织的关联", notes = "批量解除用户与多个组织的关联关系")
    public Result<Boolean> batchUnbindUserOrganizations(
            @ApiParam(name = "userId", value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(name = "organizationIds", value = "组织ID列表", required = true) @RequestParam List<Long> organizationIds) {
        try {
            boolean result = userOrganizationService.batchUnbindUserOrganizations(userId, organizationIds);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(ErrorCode.BUSINESS_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 设置用户的主组织
     * @param userId 用户ID
     * @param organizationId 组织ID
     * @return ApiResult<Boolean>
     */
    @PutMapping("/set-main")
    @ApiOperation(value = "设置用户的主组织", notes = "设置用户的主组织")
    public Result<Boolean> setMainOrganization(
            @ApiParam(name = "userId", value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(name = "organizationId", value = "组织ID", required = true) @RequestParam Long organizationId) {
        try {
            boolean result = userOrganizationService.setMainOrganization(userId, organizationId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(ErrorCode.BUSINESS_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 检查用户是否已关联到指定组织
     * @param userId 用户ID
     * @param organizationId 组织ID
     * @return ApiResult<Boolean>
     */
    @GetMapping("/check")
    @ApiOperation(value = "检查用户是否已关联到指定组织", notes = "验证用户是否已属于特定组织")
    public Result<Boolean> checkUserInOrganization(
            @ApiParam(name = "userId", value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(name = "organizationId", value = "组织ID", required = true) @RequestParam Long organizationId) {
        boolean isInOrganization = userOrganizationService.isUserInOrganization(userId, organizationId);
        return Result.success(isInOrganization);
    }
}