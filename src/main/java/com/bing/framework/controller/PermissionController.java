package com.bing.framework.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.bing.framework.dto.PermissionDTO;
import com.bing.framework.entity.Permission;
import com.bing.framework.service.PermissionService;

/**
 * 权限控制器
 * 基于MyBatis-Plus实现的RESTful API接口
 * 提供权限管理相关的API接口，支持权限的增删改查、树形结构展示及角色权限分配等功能
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Api(tags = "权限管理")
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 创建权限
     * 
     * @param permissionDTO 权限数据传输对象
     * @return 创建的权限
     */
    @ApiOperation(value = "创建权限", notes = "创建新的权限节点，支持创建父子权限关系")
    @ApiResponses({
        @ApiResponse(code = 200, message = "创建成功"),
        @ApiResponse(code = 400, message = "参数错误或权限代码已存在"),
        @ApiResponse(code = 500, message = "创建失败")
    })
    @PostMapping
    public ResponseEntity<Permission> createPermission(@ApiParam(name = "permissionDTO", value = "权限数据", required = true) @RequestBody PermissionDTO permissionDTO) {
        Permission permission = permissionService.createPermission(permissionDTO);
        return ResponseEntity.ok(permission);
    }

    /**
     * 更新权限信息
     * 
     * @param id 权限ID
     * @param permissionDTO 权限数据传输对象
     * @return 更新后的权限
     */
    @ApiOperation(value = "更新权限", notes = "更新指定ID权限的信息，包括名称、代码、图标等")
    @ApiResponses({
        @ApiResponse(code = 200, message = "更新成功"),
        @ApiResponse(code = 400, message = "参数错误或权限代码已存在"),
        @ApiResponse(code = 404, message = "权限不存在"),
        @ApiResponse(code = 500, message = "更新失败")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Permission> updatePermission(@ApiParam(name = "id", value = "权限ID", required = true) @PathVariable Long id, 
                                                     @ApiParam(name = "permissionDTO", value = "权限数据", required = true) @RequestBody PermissionDTO permissionDTO) {
        permissionDTO.setId(id);
        Permission permission = permissionService.updatePermission(permissionDTO);
        return ResponseEntity.ok(permission);
    }

    /**
     * 删除权限
     * 
     * @param id 权限ID
     * @return 响应状态
     */
    @ApiOperation(value = "删除权限", notes = "根据ID删除指定的权限，删除前请确保该权限未被角色使用")
    @ApiResponses({
        @ApiResponse(code = 200, message = "删除成功"),
        @ApiResponse(code = 404, message = "权限不存在"),
        @ApiResponse(code = 500, message = "删除失败")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@ApiParam(name = "id", value = "权限ID", required = true) @PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据ID获取权限
     * 
     * @param id 权限ID
     * @return 权限信息
     */
    @ApiOperation(value = "根据ID获取权限", notes = "通过权限ID获取权限的详细信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "权限不存在"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PermissionDTO> getPermissionById(@ApiParam(name = "id", value = "权限ID", required = true) @PathVariable Long id) {
        PermissionDTO permissionDTO = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permissionDTO);
    }

    /**
     * 获取所有权限列表
     * 
     * @return 权限列表
     */
    @ApiOperation(value = "获取所有权限", notes = "返回系统中所有的权限列表，平铺结构")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping
    public ResponseEntity<List<Permission>> listAllPermissions() {
        List<Permission> permissions = permissionService.listAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    /**
     * 获取权限树（树形结构）
     * 
     * @return 权限树列表
     */
    @ApiOperation(value = "获取权限树", notes = "返回权限的树形结构，用于前端权限选择和展示")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/tree")
    public ResponseEntity<List<PermissionDTO>> getPermissionTree() {
        List<PermissionDTO> permissionTree = permissionService.getPermissionTree();
        return ResponseEntity.ok(permissionTree);
    }

    /**
     * 根据角色ID获取权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @ApiOperation(value = "获取角色权限", notes = "根据角色ID查询该角色拥有的所有权限")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "角色不存在"),
        @ApiResponse(code = 500, message = "查询失败")
    })
    @GetMapping("/by-role/{roleId}")
    public ResponseEntity<List<Permission>> getPermissionsByRoleId(@ApiParam(name = "roleId", value = "角色ID", required = true) @PathVariable Long roleId) {
        List<Permission> permissions = permissionService.getPermissionsByRoleId(roleId);
        return ResponseEntity.ok(permissions);
    }

    /**
     * 为角色分配权限
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 响应状态
     */
    @ApiOperation(value = "分配权限给角色", notes = "为指定角色分配多个权限，会覆盖原有权限配置")
    @ApiResponses({
        @ApiResponse(code = 200, message = "分配成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 404, message = "角色或权限不存在"),
        @ApiResponse(code = 500, message = "分配失败")
    })
    @PostMapping("/assign-to-role/{roleId}")
    public ResponseEntity<Void> assignPermissionsToRole(@ApiParam(name = "roleId", value = "角色ID", required = true) @PathVariable Long roleId, 
                                                      @ApiParam(name = "permissionIds", value = "权限ID列表", required = true) @RequestBody List<Long> permissionIds) {
        permissionService.assignPermissionsToRole(roleId, permissionIds);
        return ResponseEntity.ok().build();
    }

    /**
     * 检查权限代码是否存在
     * 
     * @param code 权限代码
     * @param id 权限ID（用于更新时排除当前权限）
     * @return 是否存在
     */
    @ApiOperation(value = "检查权限代码", notes = "检查指定的权限代码是否已存在，用于新增和更新权限时的校验")
    @ApiResponses({
        @ApiResponse(code = 200, message = "检查成功"),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 500, message = "检查失败")
    })
    @GetMapping("/check-code")
    public ResponseEntity<Boolean> checkCodeExists(@ApiParam(name = "code", value = "权限代码", required = true) @RequestParam String code, 
                                                 @ApiParam(name = "id", value = "权限ID（可选，更新时排除当前权限）", required = false) @RequestParam(required = false) Long id) {
        boolean exists = permissionService.isCodeExists(code, id);
        return ResponseEntity.ok(exists);
    }
}