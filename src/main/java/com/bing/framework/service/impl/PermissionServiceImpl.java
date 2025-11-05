package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.dto.PermissionDTO;
import com.bing.framework.entity.Permission;
import com.bing.framework.entity.RolePermission;
import com.bing.framework.mapper.PermissionMapper;
import com.bing.framework.mapper.RolePermissionMapper;
import com.bing.framework.service.PermissionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 * 实现权限管理的具体业务逻辑
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    @Transactional
    public Permission createPermission(PermissionDTO permissionDTO) {
        // 检查权限编码是否已存在
        if (isCodeExists(permissionDTO.getCode(), null)) {
            throw new RuntimeException("权限编码已存在");
        }

        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionDTO, permission);
        permission.setCreateTime(new Date());
        permission.setUpdateTime(new Date());
        
        this.save(permission);
        return permission;
    }

    @Override
    @Transactional
    public Permission updatePermission(PermissionDTO permissionDTO) {
        // 检查权限编码是否已存在
        if (isCodeExists(permissionDTO.getCode(), permissionDTO.getId())) {
            throw new RuntimeException("权限编码已存在");
        }

        Permission permission = this.getById(permissionDTO.getId());
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }

        BeanUtils.copyProperties(permissionDTO, permission);
        permission.setUpdateTime(new Date());
        
        this.updateById(permission);
        return permission;
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        // 检查是否有子权限
        List<Permission> children = permissionMapper.findByParentId(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("该权限下有子权限，无法删除");
        }

        // 删除角色权限关联关系
        rolePermissionMapper.deleteByPermissionId(id);
        
        // 删除权限
        this.removeById(id);
    }

    @Override
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = this.getById(id);
        if (permission == null) {
            return null;
        }

        PermissionDTO permissionDTO = new PermissionDTO();
        BeanUtils.copyProperties(permission, permissionDTO);
        return permissionDTO;
    }

    @Override
    public List<Permission> listAllPermissions() {
        return this.list();
    }

    @Override
    public List<PermissionDTO> getPermissionTree() {
        List<Permission> permissions = this.list();
        List<PermissionDTO> permissionDTOs = new ArrayList<>();
        
        // 将权限实体转换为DTO
        for (Permission permission : permissions) {
            PermissionDTO dto = new PermissionDTO();
            BeanUtils.copyProperties(permission, dto);
            permissionDTOs.add(dto);
        }
        
        // 构建树形结构
        return buildPermissionTree(permissionDTOs);
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        return permissionMapper.findPermissionsByRoleId(roleId);
    }

    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        return permissionMapper.findPermissionsByUserId(userId);
    }

    @Override
    @Transactional
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 删除原有权限关联
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 批量插入新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<RolePermission> rolePermissions = new ArrayList<>();
            for (Long permissionId : permissionIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissions.add(rolePermission);
            }
            rolePermissionMapper.insertBatch(rolePermissions);
        }
    }

    @Override
    public boolean isCodeExists(String code, Long id) {
        return permissionMapper.checkCodeExist(code, id) > 0;
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        List<Permission> permissions = permissionMapper.findPermissionsByUserId(userId);
        return permissions.stream().anyMatch(p -> Objects.equals(p.getCode(), permissionCode));
    }

    /**
     * 构建权限树
     * 
     * @param permissionDTOs 权限DTO列表
     * @return 权限树列表
     */
    private List<PermissionDTO> buildPermissionTree(List<PermissionDTO> permissionDTOs) {
        List<PermissionDTO> result = new ArrayList<>();
        
        // 创建权限ID和DTO的映射
        Map<Long, PermissionDTO> permissionMap = permissionDTOs.stream()
                .collect(Collectors.toMap(PermissionDTO::getId, p -> p));
        
        // 构建树形结构
        for (PermissionDTO permissionDTO : permissionDTOs) {
            if (permissionDTO.getParentId() == null || permissionDTO.getParentId() == 0) {
                // 顶级权限
                result.add(permissionDTO);
            } else {
                // 子权限
                PermissionDTO parent = permissionMap.get(permissionDTO.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(permissionDTO);
                    // 对子权限按排序字段排序
                    parent.getChildren().sort(Comparator.comparingInt(PermissionDTO::getSort));
                }
            }
        }
        
        // 对顶级权限按排序字段排序
        result.sort(Comparator.comparingInt(PermissionDTO::getSort));
        
        return result;
    }
}