package com.bing.framework.controller;

import com.bing.framework.annotation.AuditLogLevel;
import com.bing.framework.dto.RoleDTO;
import com.bing.framework.entity.Role;
import com.bing.framework.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 * 提供角色管理相关的RESTful API接口
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Api(tags = "角色管理", description = "提供角色的增删改查及用户角色分配等功能")
@RestController
@RequestMapping("/api/roles")
@AuditLogLevel(module = "角色管理", value = AuditLogLevel.Level.BASIC)
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取所有角色列表
     * 
     * @return 角色列表
     */
    @ApiOperation(value = "获取所有角色", notes = "返回系统中所有可用的角色列表")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping
    public List<Role> listRoles() {
        return roleService.listAllRoles();
    }

    /**
     * 根据ID获取角色详情
     * 
     * @param id 角色ID
     * @return 角色详情
     */
    @ApiOperation(value = "根据ID获取角色", notes = "通过角色ID获取角色的详细信息，包括权限配置")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "角色不存在"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/{id}")
    public RoleDTO getRole(@ApiParam(name = "id", value = "角色ID", required = true) @PathVariable Long id) {
        return roleService.getRoleById(id);
    }

    /**
     * 创建角色
     * 
     * @param roleDTO 角色数据
     * @return 创建的角色
     */
    @ApiOperation(value = "创建角色", notes = "创建新的角色，包括基本信息和权限配置")
    @ApiResponses({
        @ApiResponse(code = 200, message = "创建成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 500, message = "创建失败")
    })
    @PostMapping
    @AuditLogLevel(value = AuditLogLevel.Level.FULL, description = "创建新角色")
    public Role createRole(@ApiParam(name = "roleDTO", value = "角色数据", required = true) @RequestBody RoleDTO roleDTO) {
        return roleService.createRole(roleDTO);
    }

    /**
     * 更新角色
     * 
     * @param id 角色ID
     * @param roleDTO 角色数据
     * @return 更新后的角色
     */
    @ApiOperation(value = "更新角色", notes = "更新指定ID角色的信息，包括基本信息和权限配置")
    @ApiResponses({
        @ApiResponse(code = 200, message = "更新成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 404, message = "角色不存在"),
        @ApiResponse(code = 500, message = "更新失败")
    })
    @PutMapping("/{id}")
    @AuditLogLevel(value = AuditLogLevel.Level.FULL, description = "更新角色信息")
    public Role updateRole(@ApiParam(name = "id", value = "角色ID", required = true) @PathVariable Long id, 
                          @ApiParam(name = "roleDTO", value = "角色数据", required = true) @RequestBody RoleDTO roleDTO) {
        roleDTO.setId(id);
        return roleService.updateRole(roleDTO);
    }

    /**
     * 删除角色
     * 
     * @param id 角色ID
     */
    @ApiOperation(value = "删除角色", notes = "根据ID删除指定的角色，删除前请确保该角色未被用户使用")
    @ApiResponses({
        @ApiResponse(code = 200, message = "删除成功"),
        @ApiResponse(code = 404, message = "角色不存在"),
        @ApiResponse(code = 500, message = "删除失败")
    })
    @DeleteMapping("/{id}")
    @AuditLogLevel(value = AuditLogLevel.Level.FULL, description = "删除角色")
    public void deleteRole(@ApiParam(name = "id", value = "角色ID", required = true) @PathVariable Long id) {
        roleService.deleteRole(id);
    }

    /**
     * 根据用户ID获取用户拥有的角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @ApiOperation(value = "获取用户角色", notes = "根据用户ID查询该用户拥有的所有角色")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/user/{userId}")
    public List<Role> getRolesByUserId(@ApiParam(name = "userId", value = "用户ID", required = true) @PathVariable Long userId) {
        return roleService.getRolesByUserId(userId);
    }

    /**
     * 为用户分配角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    @ApiOperation(value = "分配角色给用户", notes = "为指定用户分配多个角色，会覆盖原有角色配置")
    @ApiResponses({
        @ApiResponse(code = 200, message = "分配成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 404, message = "用户或角色不存在"),
        @ApiResponse(code = 500, message = "分配失败")
    })
    @PostMapping("/assign/{userId}")
    @AuditLogLevel(value = AuditLogLevel.Level.FULL, description = "为用户分配角色")
    public void assignRolesToUser(@ApiParam(name = "userId", value = "用户ID", required = true) @PathVariable Long userId, 
                                 @ApiParam(name = "roleIds", value = "角色ID列表", required = true) @RequestBody List<Long> roleIds) {
        roleService.assignRolesToUser(userId, roleIds);
    }
}