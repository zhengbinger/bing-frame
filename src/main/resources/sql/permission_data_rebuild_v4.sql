-- ===========================================
-- 权限数据重新生成脚本（V4版本-外键约束修复版）
-- 基于项目现有功能生成的权限数据
-- 创建时间: 2025-11-17
-- 作者: zhengbing
-- 修复内容: 彻底解决SQL错误[1093]和[1452]问题
-- 修复策略: 
-- 1. 分步骤插入，避免在同一INSERT中引用目标表
-- 2. 使用子查询获取实际的角色ID，避免外键约束失败
-- ===========================================

-- 清理现有权限数据（可选）
DELETE FROM role_permission;
DELETE FROM user_role;
DELETE FROM permission;
DELETE FROM role;

-- 开启事务
START TRANSACTION;

-- ===========================================
-- 第一步：插入一级权限（模块权限）
-- ===========================================
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('认证管理', 'auth', '用户认证相关操作权限', NULL, NULL, 0, 1, 0, 'auth-icon', 1, NOW()),
('用户管理', 'user', '用户信息相关操作权限', NULL, NULL, 0, 2, 0, 'user-icon', 1, NOW()),
('角色管理', 'role', '角色信息相关操作权限', NULL, NULL, 0, 3, 0, 'role-icon', 1, NOW()),
('权限管理', 'permission', '权限信息相关操作权限', NULL, NULL, 0, 4, 0, 'permission-icon', 1, NOW()),
('组织管理', 'organization', '组织架构相关操作权限', NULL, NULL, 0, 5, 0, 'org-icon', 1, NOW()),
('用户组织关联', 'user-organization', '用户与组织关联相关操作权限', NULL, NULL, 0, 6, 0, 'user-org-icon', 1, NOW()),
('数据字典', 'data-dict', '数据字典相关操作权限', NULL, NULL, 0, 7, 0, 'dict-icon', 1, NOW()),
('字典项管理', 'data-dict-item', '数据字典项相关操作权限', NULL, NULL, 0, 8, 0, 'dict-item-icon', 1, NOW()),
('系统配置', 'system-config', '系统配置相关操作权限', NULL, NULL, 0, 9, 0, 'config-icon', 1, NOW()),
('登录记录', 'login-record', '登录记录相关操作权限', NULL, NULL, 0, 10, 0, 'login-record-icon', 1, NOW()),
('白名单管理', 'white-list', '白名单相关操作权限', NULL, NULL, 0, 11, 0, 'white-list-icon', 1, NOW()),
('验证码管理', 'captcha', '验证码相关操作权限', NULL, NULL, 0, 12, 0, 'captcha-icon', 1, NOW()),
('国际化测试', 'i18n', '国际化相关操作权限', NULL, NULL, 0, 13, 0, 'i18n-icon', 1, NOW());

-- ===========================================
-- 第二步：分批插入二级权限（避免SQL1093错误）
-- ===========================================

-- 2.1 插入认证管理权限
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('登录', 'auth:login', '用户登录操作', '/api/auth/login', 'POST', 1, 1, 2, NULL, 1, NOW()),
('注册', 'auth:register', '用户注册操作', '/api/auth/register', 'POST', 1, 2, 2, NULL, 1, NOW()),
('注销', 'auth:logout', '用户注销操作', '/api/auth/logout', 'POST', 1, 3, 2, NULL, 1, NOW()),
('获取当前用户信息', 'auth:current', '获取当前登录用户信息', '/api/auth/current', 'GET', 1, 4, 2, NULL, 1, NOW()),
('刷新令牌', 'auth:refresh', '刷新用户访问令牌', '/api/auth/refresh', 'POST', 1, 5, 2, NULL, 1, NOW());

-- 2.2 插入用户管理权限（基本信息）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('用户列表', 'user:list', '获取用户列表信息', '/api/user', 'GET', 2, 1, 2, NULL, 1, NOW()),
('用户详情', 'user:detail', '获取用户详细信息', '/api/user/{id}', 'GET', 2, 2, 2, NULL, 1, NOW()),
('创建用户', 'user:create', '创建新用户', '/api/user', 'POST', 2, 3, 2, NULL, 1, NOW()),
('更新用户', 'user:update', '更新用户信息', '/api/user', 'PUT', 2, 4, 2, NULL, 1, NOW()),
('删除用户', 'user:delete', '删除用户', '/api/user/{id}', 'DELETE', 2, 5, 2, NULL, 1, NOW()),
('批量删除用户', 'user:batchDelete', '批量删除用户', '/api/user/batch', 'DELETE', 2, 6, 2, NULL, 1, NOW());

-- 2.3 插入用户管理权限（密码管理）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('重置密码', 'user:resetPassword', '重置用户密码', '/api/user/reset-password', 'PUT', 2, 7, 2, NULL, 1, NOW()),
('批量重置密码', 'user:batchResetPassword', '批量重置用户密码', '/api/user/batch-reset-password', 'PUT', 2, 8, 2, NULL, 1, NOW()),
('修改密码', 'user:changePassword', '修改当前用户密码', '/api/user/change-password', 'PUT', 2, 9, 2, NULL, 1, NOW());

-- 2.4 插入用户管理权限（状态管理）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('启用用户', 'user:enable', '启用用户账号', '/api/user/enable', 'PUT', 2, 10, 2, NULL, 1, NOW()),
('禁用用户', 'user:disable', '禁用用户账号', '/api/user/disable', 'PUT', 2, 11, 2, NULL, 1, NOW()),
('批量启用用户', 'user:batchEnable', '批量启用用户账号', '/api/user/batch-enable', 'PUT', 2, 12, 2, NULL, 1, NOW()),
('批量禁用用户', 'user:batchDisable', '批量禁用用户账号', '/api/user/batch-disable', 'PUT', 2, 13, 2, NULL, 1, NOW());

-- 2.5 插入用户管理权限（角色管理）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('分配用户角色', 'user:assignRole', '为用户分配角色', '/api/user/assign-role', 'POST', 2, 14, 2, NULL, 1, NOW()),
('撤销用户角色', 'user:revokeRole', '撤销用户角色', '/api/user/revoke-role', 'DELETE', 2, 15, 2, NULL, 1, NOW());

-- 2.6 插入角色管理权限（基本信息）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('角色列表', 'role:list', '获取角色列表信息', '/api/roles', 'GET', 3, 1, 2, NULL, 1, NOW()),
('角色详情', 'role:detail', '获取角色详细信息', '/api/roles/{id}', 'GET', 3, 2, 2, NULL, 1, NOW()),
('创建角色', 'role:create', '创建新角色', '/api/roles', 'POST', 3, 3, 2, NULL, 1, NOW()),
('更新角色', 'role:update', '更新角色信息', '/api/roles', 'PUT', 3, 4, 2, NULL, 1, NOW()),
('删除角色', 'role:delete', '删除角色', '/api/roles/{id}', 'DELETE', 3, 5, 2, NULL, 1, NOW()),
('批量删除角色', 'role:batchDelete', '批量删除角色', '/api/roles/batch', 'DELETE', 3, 6, 2, NULL, 1, NOW());

-- 2.7 插入角色管理权限（状态和权限管理）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('启用角色', 'role:enable', '启用角色', '/api/roles/enable', 'PUT', 3, 7, 2, NULL, 1, NOW()),
('禁用角色', 'role:disable', '禁用角色', '/api/roles/disable', 'PUT', 3, 8, 2, NULL, 1, NOW()),
('分配角色权限', 'role:assignPermission', '为角色分配权限', '/api/roles/assign-permission', 'POST', 3, 9, 2, NULL, 1, NOW()),
('撤销角色权限', 'role:revokePermission', '撤销角色权限', '/api/roles/revoke-permission', 'DELETE', 3, 10, 2, NULL, 1, NOW());

-- 2.8 插入角色管理权限（用户管理）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('分配角色用户', 'role:assignUser', '为角色分配用户', '/api/roles/assign-user', 'POST', 3, 11, 2, NULL, 1, NOW()),
('撤销角色用户', 'role:revokeUser', '撤销角色用户', '/api/roles/revoke-user', 'DELETE', 3, 12, 2, NULL, 1, NOW());

-- 2.9 插入权限管理权限（基本信息）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('权限列表', 'permission:list', '获取权限列表信息', '/api/permissions', 'GET', 4, 1, 2, NULL, 1, NOW()),
('权限树', 'permission:tree', '获取权限树形结构', '/api/permissions/tree', 'GET', 4, 2, 2, NULL, 1, NOW()),
('权限详情', 'permission:detail', '获取权限详细信息', '/api/permissions/{id}', 'GET', 4, 3, 2, NULL, 1, NOW()),
('创建权限', 'permission:create', '创建新权限', '/api/permissions', 'POST', 4, 4, 2, NULL, 1, NOW()),
('更新权限', 'permission:update', '更新权限信息', '/api/permissions', 'PUT', 4, 5, 2, NULL, 1, NOW()),
('删除权限', 'permission:delete', '删除权限', '/api/permissions/{id}', 'DELETE', 4, 6, 2, NULL, 1, NOW()),
('批量删除权限', 'permission:batchDelete', '批量删除权限', '/api/permissions/batch', 'DELETE', 4, 7, 2, NULL, 1, NOW());

-- 2.10 插入权限管理权限（状态管理）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('启用权限', 'permission:enable', '启用权限', '/api/permissions/enable', 'PUT', 4, 8, 2, NULL, 1, NOW()),
('禁用权限', 'permission:disable', '禁用权限', '/api/permissions/disable', 'PUT', 4, 9, 2, NULL, 1, NOW());

-- 2.11 插入组织管理权限（基本信息）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('组织列表', 'organization:list', '获取组织列表信息', '/api/organization/list', 'GET', 5, 1, 2, NULL, 1, NOW()),
('组织树', 'organization:tree', '获取组织树形结构', '/api/organization/tree', 'GET', 5, 2, 2, NULL, 1, NOW()),
('组织详情', 'organization:detail', '获取组织详细信息', '/api/organization/{id}', 'GET', 5, 3, 2, NULL, 1, NOW()),
('创建组织', 'organization:create', '创建新组织', '/api/organization', 'POST', 5, 4, 2, NULL, 1, NOW()),
('更新组织', 'organization:update', '更新组织信息', '/api/organization', 'PUT', 5, 5, 2, NULL, 1, NOW()),
('删除组织', 'organization:delete', '删除组织', '/api/organization/{id}', 'DELETE', 5, 6, 2, NULL, 1, NOW());

-- 2.12 插入组织管理权限（子级管理）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('子组织列表', 'organization:children', '获取子组织列表', '/api/organization/children/{parentId}', 'GET', 5, 7, 2, NULL, 1, NOW()),
('检查组织关联', 'organization:checkRelation', '检查组织关联关系', '/api/organization/check-relation', 'GET', 5, 8, 2, NULL, 1, NOW());

-- 2.13 插入用户组织关联权限
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('用户组织列表', 'user-organization:list', '获取用户组织关联列表', '/api/user-organization/list', 'GET', 6, 1, 2, NULL, 1, NOW()),
('用户组织详情', 'user-organization:detail', '获取用户组织关联详情', '/api/user-organization/{id}', 'GET', 6, 2, 2, NULL, 1, NOW()),
('关联用户组织', 'user-organization:create', '关联用户和组织', '/api/user-organization', 'POST', 6, 3, 2, NULL, 1, NOW()),
('更新用户组织', 'user-organization:update', '更新用户组织关联', '/api/user-organization', 'PUT', 6, 4, 2, NULL, 1, NOW()),
('删除用户组织', 'user-organization:delete', '删除用户组织关联', '/api/user-organization/{id}', 'DELETE', 6, 5, 2, NULL, 1, NOW()),
('批量关联用户组织', 'user-organization:batchCreate', '批量关联用户和组织', '/api/user-organization/batch', 'POST', 6, 6, 2, NULL, 1, NOW());

-- 2.14 插入数据字典管理权限
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('字典列表', 'data-dict:list', '获取数据字典列表', '/api/data-dict/list', 'GET', 7, 1, 2, NULL, 1, NOW()),
('字典详情', 'data-dict:detail', '获取数据字典详情', '/api/data-dict/{id}', 'GET', 7, 2, 2, NULL, 1, NOW()),
('创建字典', 'data-dict:create', '创建新数据字典', '/api/data-dict', 'POST', 7, 3, 2, NULL, 1, NOW()),
('更新字典', 'data-dict:update', '更新数据字典', '/api/data-dict', 'PUT', 7, 4, 2, NULL, 1, NOW()),
('删除字典', 'data-dict:delete', '删除数据字典', '/api/data-dict/{id}', 'DELETE', 7, 5, 2, NULL, 1, NOW()),
('批量删除字典', 'data-dict:batchDelete', '批量删除数据字典', '/api/data-dict/batch', 'DELETE', 7, 6, 2, NULL, 1, NOW()),
('启用字典', 'data-dict:enable', '启用数据字典', '/api/data-dict/status', 'PUT', 7, 7, 2, NULL, 1, NOW()),
('禁用字典', 'data-dict:disable', '禁用数据字典', '/api/data-dict/status', 'PUT', 7, 8, 2, NULL, 1, NOW());

-- 2.15 插入数据字典项管理权限
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('字典项列表', 'data-dict-item:list', '获取数据字典项列表', '/api/data-dict-item/list-by-dict/{dictId}', 'GET', 8, 1, 2, NULL, 1, NOW()),
('字典项详情', 'data-dict-item:detail', '获取数据字典项详情', '/api/data-dict-item/{id}', 'GET', 8, 2, 2, NULL, 1, NOW()),
('创建字典项', 'data-dict-item:create', '创建新数据字典项', '/api/data-dict-item', 'POST', 8, 3, 2, NULL, 1, NOW()),
('更新字典项', 'data-dict-item:update', '更新数据字典项', '/api/data-dict-item', 'PUT', 8, 4, 2, NULL, 1, NOW()),
('删除字典项', 'data-dict-item:delete', '删除数据字典项', '/api/data-dict-item/{id}', 'DELETE', 8, 5, 2, NULL, 1, NOW()),
('批量删除字典项', 'data-dict-item:batchDelete', '批量删除数据字典项', '/api/data-dict-item/batch', 'DELETE', 8, 6, 2, NULL, 1, NOW()),
('启用字典项', 'data-dict-item:enable', '启用数据字典项', '/api/data-dict-item/status', 'PUT', 8, 7, 2, NULL, 1, NOW()),
('禁用字典项', 'data-dict-item:disable', '禁用数据字典项', '/api/data-dict-item/status', 'PUT', 8, 8, 2, NULL, 1, NOW());

-- 2.16 插入系统配置管理权限
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('配置列表', 'system-config:list', '获取系统配置列表', '/api/system-config/list', 'GET', 9, 1, 2, NULL, 1, NOW()),
('配置详情', 'system-config:detail', '获取系统配置详情', '/api/system-config/{configKey}', 'GET', 9, 2, 2, NULL, 1, NOW()),
('创建配置', 'system-config:create', '创建新系统配置', '/api/system-config', 'POST', 9, 3, 2, NULL, 1, NOW()),
('更新配置', 'system-config:update', '更新系统配置', '/api/system-config', 'PUT', 9, 4, 2, NULL, 1, NOW()),
('删除配置', 'system-config:delete', '删除系统配置', '/api/system-config/{configKey}', 'DELETE', 9, 5, 2, NULL, 1, NOW()),
('批量更新配置', 'system-config:batchUpdate', '批量更新系统配置', '/api/system-config/batch', 'PUT', 9, 6, 2, NULL, 1, NOW());

-- 2.17 插入登录记录管理权限
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('登录记录列表', 'login-record:list', '获取登录记录列表', '/api/loginRecords', 'GET', 10, 1, 2, NULL, 1, NOW()),
('用户登录记录', 'login-record:userList', '获取指定用户的登录记录', '/api/loginRecords/user/{userId}', 'GET', 10, 2, 2, NULL, 1, NOW()),
('最近登录记录', 'login-record:recent', '获取最近的登录记录', '/api/loginRecords/recent', 'GET', 10, 3, 2, NULL, 1, NOW()),
('失败登录记录', 'login-record:failed', '获取失败的登录记录', '/api/loginRecords/failed', 'GET', 10, 4, 2, NULL, 1, NOW()),
('清理登录记录', 'login-record:clean', '清理过期的登录记录', '/api/loginRecords/clean', 'DELETE', 10, 5, 2, NULL, 1, NOW());

-- 2.18 插入白名单管理权限
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('白名单列表', 'white-list:list', '获取白名单列表', '/api/white-list/list', 'GET', 11, 1, 2, NULL, 1, NOW()),
('白名单详情', 'white-list:detail', '获取白名单详情', '/api/white-list/{id}', 'GET', 11, 2, 2, NULL, 1, NOW()),
('创建白名单', 'white-list:create', '创建新白名单', '/api/white-list', 'POST', 11, 3, 2, NULL, 1, NOW()),
('更新白名单', 'white-list:update', '更新白名单', '/api/white-list', 'PUT', 11, 4, 2, NULL, 1, NOW()),
('删除白名单', 'white-list:delete', '删除白名单', '/api/white-list/{id}', 'DELETE', 11, 5, 2, NULL, 1, NOW()),
('批量删除白名单', 'white-list:batchDelete', '批量删除白名单', '/api/white-list/batch', 'DELETE', 11, 6, 2, NULL, 1, NOW());

-- 2.19 插入验证码管理权限
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('生成验证码', 'captcha:generate', '生成验证码', '/api/captcha/generate', 'GET', 12, 1, 2, NULL, 1, NOW()),
('刷新验证码', 'captcha:refresh', '刷新验证码', '/api/captcha/refresh/{captchaId}', 'GET', 12, 2, 2, NULL, 1, NOW()),
('验证验证码', 'captcha:verify', '验证验证码', '/api/captcha/verify', 'POST', 12, 3, 2, NULL, 1, NOW());

-- 2.20 插入国际化测试权限
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES
('国际化测试', 'i18n:test', '国际化功能测试', '/api/i18n/test', 'GET', 13, 1, 2, NULL, 1, NOW());

-- ===========================================
-- 第三步：插入角色数据
-- ===========================================
INSERT INTO role (`name`, `code`, `description`, `status`, `create_time`) VALUES
-- 系统内置角色
('系统管理员', 'ADMIN', '系统管理员，拥有系统所有权限', 1, NOW()),
('普通用户', 'USER', '普通用户，拥有基本的系统使用权限', 1, NOW()),
('部门管理员', 'DEPT_ADMIN', '部门管理员，拥有部门内用户管理权限', 1, NOW()),
('财务管理员', 'FINANCE_ADMIN', '财务管理员，拥有财务相关权限', 1, NOW()),
('人事管理员', 'HR_ADMIN', '人事管理员，拥有人事相关权限', 1, NOW()),
('审计员', 'AUDITOR', '审计员，拥有查看系统操作日志权限', 1, NOW()),
('访客', 'GUEST', '访客，只能查看基本信息，无修改权限', 1, NOW());

-- ===========================================
-- 第四步：分批分配权限给角色（使用子查询获取实际ID）
-- ===========================================

-- 4.1 系统管理员拥有所有权限
INSERT INTO role_permission (role_id, permission_id)
SELECT (SELECT id FROM role WHERE code = 'ADMIN'), id FROM permission WHERE status = 1;

-- 4.2 普通用户权限
INSERT INTO role_permission (role_id, permission_id)
SELECT (SELECT id FROM role WHERE code = 'USER'), id FROM permission 
WHERE code IN (
    -- 认证相关
    'auth:login', 'auth:logout', 'auth:current', 
    -- 用户个人相关（只能查看和更新自己的信息）
    'user:detail', 'user:update', 'user:changePassword',
    -- 字典查看权限
    'data-dict:list', 'data-dict-item:list',
    -- 国际化测试
    'i18n:test'
);

-- 4.3 部门管理员权限
INSERT INTO role_permission (role_id, permission_id)
SELECT (SELECT id FROM role WHERE code = 'DEPT_ADMIN'), id FROM permission 
WHERE code IN (
    -- 基本认证权限
    'auth:login', 'auth:logout', 'auth:current',
    -- 用户管理（除密码重置外的所有操作）
    'user:list', 'user:detail', 'user:create', 'user:update', 
    'user:enable', 'user:disable', 'user:assignRole', 'user:revokeRole',
    -- 角色管理（查看权限）
    'role:list', 'role:detail',
    -- 组织管理
    'organization:list', 'organization:tree', 'organization:detail',
    'organization:children', 'organization:checkRelation',
    -- 用户组织关联
    'user-organization:list', 'user-organization:detail', 'user-organization:create',
    'user-organization:update', 'user-organization:delete', 'user-organization:batchCreate',
    -- 数据字典查看
    'data-dict:list', 'data-dict-item:list',
    -- 国际化测试
    'i18n:test'
);

-- 4.4 财务管理员权限
INSERT INTO role_permission (role_id, permission_id)
SELECT (SELECT id FROM role WHERE code = 'FINANCE_ADMIN'), id FROM permission 
WHERE code IN (
    -- 基本认证权限
    'auth:login', 'auth:logout', 'auth:current',
    -- 用户管理（查看和更新）
    'user:list', 'user:detail', 'user:update',
    -- 角色管理（查看权限）
    'role:list', 'role:detail',
    -- 组织管理
    'organization:list', 'organization:tree', 'organization:detail',
    -- 数据字典查看
    'data-dict:list', 'data-dict-item:list',
    -- 登录记录查看
    'login-record:list', 'login-record:userList', 'login-record:recent', 'login-record:failed',
    -- 国际化测试
    'i18n:test'
);

-- 4.5 人事管理员权限
INSERT INTO role_permission (role_id, permission_id)
SELECT (SELECT id FROM role WHERE code = 'HR_ADMIN'), id FROM permission 
WHERE code IN (
    -- 基本认证权限
    'auth:login', 'auth:logout', 'auth:current',
    -- 用户管理
    'user:list', 'user:detail', 'user:create', 'user:update', 
    'user:enable', 'user:disable', 'user:assignRole', 'user:revokeRole',
    -- 角色管理（查看权限）
    'role:list', 'role:detail',
    -- 组织管理
    'organization:list', 'organization:tree', 'organization:detail',
    'organization:children', 'organization:checkRelation',
    -- 用户组织关联
    'user-organization:list', 'user-organization:detail', 'user-organization:create',
    'user-organization:update', 'user-organization:delete', 'user-organization:batchCreate',
    -- 数据字典查看
    'data-dict:list', 'data-dict-item:list',
    -- 登录记录查看
    'login-record:list', 'login-record:userList', 'login-record:recent', 'login-record:failed',
    -- 国际化测试
    'i18n:test'
);

-- 4.6 审计员权限
INSERT INTO role_permission (role_id, permission_id)
SELECT (SELECT id FROM role WHERE code = 'AUDITOR'), id FROM permission 
WHERE code IN (
    -- 基本认证权限
    'auth:login', 'auth:logout', 'auth:current',
    -- 查看所有用户信息
    'user:list', 'user:detail',
    -- 查看所有角色
    'role:list', 'role:detail',
    -- 查看权限
    'permission:list', 'permission:tree', 'permission:detail',
    -- 组织查看
    'organization:list', 'organization:tree', 'organization:detail',
    -- 用户组织关联查看
    'user-organization:list', 'user-organization:detail',
    -- 数据字典查看
    'data-dict:list', 'data-dict-item:list',
    -- 登录记录管理
    'login-record:list', 'login-record:userList', 'login-record:recent', 'login-record:failed', 'login-record:clean',
    -- 白名单查看
    'white-list:list', 'white-list:detail',
    -- 验证码管理（验证权限）
    'captcha:generate', 'captcha:verify',
    -- 国际化测试
    'i18n:test'
);

-- 4.7 访客权限
INSERT INTO role_permission (role_id, permission_id)
SELECT (SELECT id FROM role WHERE code = 'GUEST'), id FROM permission 
WHERE code IN (
    -- 基本认证权限
    'auth:login', 'auth:logout', 'auth:current',
    -- 用户个人相关
    'user:detail',
    -- 字典查看权限
    'data-dict:list', 'data-dict-item:list',
    -- 验证码管理（生成权限）
    'captcha:generate',
    -- 国际化测试
    'i18n:test'
);

-- 提交事务
COMMIT;

-- 验证结果
SELECT '权限数据重建完成!' AS message;
SELECT COUNT(*) AS total_permissions FROM permission;
SELECT COUNT(*) AS total_roles FROM role;
SELECT COUNT(*) AS total_role_permissions FROM role_permission;