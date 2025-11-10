package com.bing.framework.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.framework.dto.RoleDTO;
import com.bing.framework.entity.Permission;
import com.bing.framework.entity.Role;
import com.bing.framework.entity.RolePermission;
import com.bing.framework.entity.UserRole;
import com.bing.framework.mapper.RoleMapper;
import com.bing.framework.mapper.RolePermissionMapper;
import com.bing.framework.mapper.UserRoleMapper;
import com.bing.framework.service.RoleService;
import com.bing.framework.util.AuditLogUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 角色服务实现类
 * 实现角色管理相关的业务逻辑
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    
    @Autowired
    private AuditLogUtil auditLogUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role createRole(RoleDTO roleDTO) {
        // 检查角色编码是否已存在
        if (isCodeExists(roleDTO.getCode(), null)) {
            throw new RuntimeException("角色编码已存在");
        }

        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        role.setCreateTime(new Date());
        role.setUpdateTime(new Date());

        // 保存角色
        this.save(role);

        // 如果有分配用户，则保存用户角色关系
        if (roleDTO.getUserIds() != null && !roleDTO.getUserIds().isEmpty()) {
            List<UserRole> userRoles = new ArrayList<>();
            for (Long userId : roleDTO.getUserIds()) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(role.getId());
                userRoles.add(userRole);
            }
            userRoleMapper.insertBatch(userRoles);
        }
        
        // 为角色分配权限
        if (roleDTO.getPermissionIds() != null && !roleDTO.getPermissionIds().isEmpty()) {
            assignPermissions(role.getId(), roleDTO.getPermissionIds());
        }

        // 手动记录创建角色的审计日志
        auditLogUtil.logSuccess("角色管理", "创建角色", "成功创建角色: " + role.getName(), roleDTO.toString());
        
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role updateRole(RoleDTO roleDTO) {
        // 检查角色编码是否已存在
        if (isCodeExists(roleDTO.getCode(), roleDTO.getId())) {
            throw new RuntimeException("角色编码已存在");
        }

        Role role = this.getById(roleDTO.getId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        BeanUtils.copyProperties(roleDTO, role);
        role.setUpdateTime(new Date());

        // 更新角色信息
        this.updateById(role);
        
        // 更新角色权限关联
        if (roleDTO.getPermissionIds() != null) {
            assignPermissions(role.getId(), roleDTO.getPermissionIds());
        }

        // 手动记录更新角色的审计日志
        auditLogUtil.logSuccess("角色管理", "更新角色", "成功更新角色: " + role.getName(), roleDTO.toString());
        
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        // 检查角色是否存在
        Role role = this.getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 删除用户角色关联关系
        userRoleMapper.deleteByRoleId(id);
        
        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(id);

        // 删除角色
        this.removeById(id);
        
        // 手动记录删除角色的审计日志
        auditLogUtil.log("角色管理", "删除角色", "成功删除角色: " + role.getName(), null, "成功");
    }

    @Override
    public RoleDTO getRoleById(Long id) {
        Role role = this.getById(id);
        if (role == null) {
            return null;
        }

        RoleDTO roleDTO = new RoleDTO();
        BeanUtils.copyProperties(role, roleDTO);

        // 获取关联的用户ID列表
        List<UserRole> userRoles = userRoleMapper.findByRoleId(id);
        List<Long> userIds = new ArrayList<>();
        for (UserRole userRole : userRoles) {
            userIds.add(userRole.getUserId());
        }
        roleDTO.setUserIds(userIds);
        
        // 查询角色拥有的权限ID列表
        List<RolePermission> rolePermissions = rolePermissionMapper.findByRoleId(id);
        List<Long> permissionIds = new ArrayList<>();
        for (RolePermission rp : rolePermissions) {
            permissionIds.add(rp.getPermissionId());
        }
        roleDTO.setPermissionIds(permissionIds);

        return roleDTO;
    }

    @Override
    public List<Role> listAllRoles() {
        return this.list();
    }

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        return roleMapper.findRolesByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        // 先删除用户原有的所有角色关联
        userRoleMapper.deleteByUserId(userId);

        // 如果角色列表不为空，则添加新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            List<UserRole> userRoles = new ArrayList<>();
            for (Long roleId : roleIds) {
                // 检查角色是否存在
                if (this.getById(roleId) == null) {
                    throw new RuntimeException("角色ID " + roleId + " 不存在");
                }

                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoles.add(userRole);
            }
            userRoleMapper.insertBatch(userRoles);
        }
        
        // 手动记录分配角色的审计日志
        auditLogUtil.logSuccess("角色管理", "分配角色", "成功为用户ID: " + userId + " 分配角色", roleIds.toString());
    }

    @Override
    public boolean isCodeExists(String code, Long id) {
        return roleMapper.checkCodeExist(code, id) > 0;
    }
    
    /**
     * 为角色分配权限
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    private void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 删除原有权限关联
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 批量插入新的权限关联
        if (!permissionIds.isEmpty()) {
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
}