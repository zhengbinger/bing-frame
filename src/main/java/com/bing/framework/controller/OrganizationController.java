package com.bing.framework.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.bing.framework.common.ErrorCode;
import com.bing.framework.common.Result;
import com.bing.framework.entity.Organization;
import com.bing.framework.service.OrganizationService;

/**
 * 组织管理控制器
 * 基于MyBatis-Plus实现的RESTful API接口
 * 提供组织相关的RESTful API接口，支持树形结构管理、循环引用检测等功能
 * 
 * @author zhengbing
 * @date 2025-11-12
 */
@Api(tags = "组织管理")
@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    /**
     * 根据ID获取组织信息
     *
     * @param id 组织ID
     * @return 组织信息
     */
    @ApiOperation(value = "根据ID获取组织信息", notes = "通过组织ID查询组织的详细信息")
    @GetMapping("/{id}")
    public Result<Organization> getById(@PathVariable Long id) {
        Organization organization = organizationService.getOrganizationById(id);
        if (organization == null) {
            return Result.error(ErrorCode.BUSINESS_ERROR, "组织不存在");
        }
        return Result.success(organization);
    }

    /**
     * 根据编码获取组织信息
     *
     * @param code 组织编码
     * @return 组织信息
     */
    @ApiOperation(value = "根据编码获取组织信息", notes = "通过组织编码查询组织的详细信息")
    @GetMapping("/code/{code}")
    public Result<Organization> getByCode(@PathVariable String code) {
        Organization organization = organizationService.getOrganizationByCode(code);
        if (organization == null) {
            return Result.error(ErrorCode.BUSINESS_ERROR, "组织不存在");
        }
        return Result.success(organization);
    }

    /**
     * 分页查询组织列表
     *
     * @param page 页码
     * @param size 每页数量
     * @param name 组织名称
     * @param code 组织编码
     * @return 分页数据
     */
    @ApiOperation(value = "分页查询组织列表", notes = "支持分页和条件查询组织列表")
    @GetMapping("/page")
    public Result<IPage<Organization>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code) {
        Organization organization = new Organization();
        organization.setName(name);
        organization.setCode(code);
        Page<Organization> pageObj = new Page<>(page, size);
        IPage<Organization> result = organizationService.getOrganizationPage(pageObj, organization);
        return Result.success(result);
    }

    /**
     * 查询所有组织列表
     *
     * @return 组织列表
     */
    @ApiOperation(value = "查询所有组织列表", notes = "获取系统中所有的组织列表")
    @GetMapping("/list")
    public Result<List<Organization>> list() {
        List<Organization> organizationList = organizationService.list();
        return Result.success(organizationList);
    }

    /**
     * 查询指定父组织下的子组织列表
     *
     * @param parentId 父组织ID
     * @return 子组织列表
     */
    @ApiOperation(value = "查询子组织列表", notes = "根据父组织ID查询其下的子组织列表")
    @GetMapping("/children/{parentId}")
    public Result<List<Organization>> getChildren(@PathVariable Long parentId) {
        List<Organization> children = organizationService.getOrganizationsByParentId(parentId);
        return Result.success(children);
    }

    /**
     * 获取组织树形结构
     *
     * @return 组织树形结构
     */
    @ApiOperation(value = "获取组织树形结构", notes = "获取完整的组织树形结构数据")
    @GetMapping("/tree")
    public Result<List<Organization>> getOrganizationTree() {
        List<Organization> organizationTree = organizationService.getOrganizationTree();
        return Result.success(organizationTree);
    }

    /**
     * 获取指定组织的树形结构
     *
     * @param id 组织ID
     * @return 组织树形结构
     */
    @ApiOperation(value = "获取指定组织的树形结构", notes = "获取以指定组织为根的树形结构数据")
    @GetMapping("/tree/{id}")
    public Result<List<Organization>> getOrganizationTree(@PathVariable Long id) {
        // 获取所有组织树，然后过滤出指定组织的子树
        List<Organization> allTree = organizationService.getOrganizationTree();
        // 简单实现，实际可能需要更复杂的过滤逻辑
        List<Organization> organizationTree = allTree.stream()
                .filter(org -> org.getId().equals(id))
                .collect(java.util.stream.Collectors.toList());
        return Result.success(organizationTree);
    }

    /**
     * 新增组织
     *
     * @param organization 组织信息
     * @return 操作结果
     */
    @ApiOperation(value = "新增组织", notes = "创建新的组织信息")
    @PostMapping
    public Result<Boolean> create(@RequestBody Organization organization) {
        // 检查编码是否重复
          Organization existing = organizationService.getOrganizationByCode(organization.getCode());
          if (existing != null && (organization.getId() == null || !existing.getId().equals(organization.getId()))) {
              return Result.error(ErrorCode.BUSINESS_ERROR, "组织编码已存在");
          }
        // 检查是否存在循环引用
        if (organization.getParentId() != null && organization.getId() != null && 
                organization.getId().equals(organization.getParentId())) {
            return Result.error(ErrorCode.BUSINESS_ERROR, "组织不能作为自己的父组织");
        }
        // 检查父子关系中是否存在循环引用
          if (organization.getParentId() != null && !organization.getId().equals(organization.getParentId())) {
              boolean hasCycle = organizationService.checkCircularReference(organization.getId(), organization.getParentId());
              if (hasCycle) {
                  return Result.error(ErrorCode.BUSINESS_ERROR, "设置的父组织会导致循环引用");
              }
          }
        boolean result = organizationService.createOrganization(organization);
        if (result) {
            return Result.success(true);
        } else {
            return Result.error(ErrorCode.BUSINESS_ERROR, "创建失败");
        }
    }

    /**
     * 更新组织信息
     *
     * @param id           组织ID
     * @param organization 组织信息
     * @return 操作结果
     */
    @ApiOperation(value = "更新组织信息", notes = "更新指定组织的信息")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Organization organization) {
        // 确保ID一致
        organization.setId(id);
        // 检查是否存在循环引用
        if (organization.getParentId() != null && organization.getId().equals(organization.getParentId())) {
            return Result.error(ErrorCode.BUSINESS_ERROR, "组织不能作为自己的父组织");
        }
        // 检查父子关系中是否存在循环引用
        if (organization.getParentId() != null && !organization.getId().equals(organization.getParentId())) {
            boolean hasCycle = organizationService.checkCircularReference(organization.getId(), organization.getParentId());
            if (hasCycle) {
                return Result.error(ErrorCode.BUSINESS_ERROR, "设置的父组织会导致循环引用");
            }
        }
        boolean result = organizationService.updateOrganization(organization);
        if (result) {
            return Result.success(true);
        } else {
            return Result.error(ErrorCode.BUSINESS_ERROR, "更新失败");
        }
    }

    /**
     * 删除组织
     *
     * @param id 组织ID
     * @return 操作结果
     */
    @ApiOperation(value = "删除组织", notes = "删除指定的组织")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        // 检查是否存在子组织
        if (organizationService.hasChildren(id)) {
            return Result.error(ErrorCode.BUSINESS_ERROR, "该组织下存在子组织，无法删除");
        }
        boolean result = organizationService.deleteOrganization(id);
        if (result) {
            return Result.success(true);
        } else {
            return Result.error(ErrorCode.BUSINESS_ERROR);
        }
    }

    /**
     * 批量删除组织
     *
     * @param ids 组织ID列表
     * @return 操作结果
     */
    @ApiOperation(value = "批量删除组织", notes = "批量删除多个组织")
    @DeleteMapping("/batch")
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(ErrorCode.BUSINESS_ERROR, "请选择要删除的组织");
        }
        // 检查是否存在子组织
        for (Long id : ids) {
            if (organizationService.hasChildren(id)) {
                return Result.error(ErrorCode.BUSINESS_ERROR, "存在包含子组织的组织，无法删除");
            }
        }
        boolean result = organizationService.removeByIds(ids);
        if (result) {
            return Result.success(true);
        } else {
            return Result.error(ErrorCode.BUSINESS_ERROR, "删除失败");
        }
    }

    /**
     * 验证组织编码是否唯一
     *
     * @param code 组织编码
     * @param id   组织ID（更新时使用）
     * @return 是否唯一
     */
    @ApiOperation(value = "验证组织编码是否唯一", notes = "用于表单验证组织编码的唯一性")
    @GetMapping("/validate/code")
    public Result<Boolean> validateCode(@RequestParam String code, @RequestParam(required = false) Long id) {
        Organization organization = organizationService.getOrganizationByCode(code);
        boolean exists = organization != null && (id == null || !organization.getId().equals(id));
        return Result.success(!exists);
    }
}