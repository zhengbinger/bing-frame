-- ===========================================
-- 权限数据重新生成脚本（修复版）
-- 基于项目现有功能生成的权限数据
-- 创建时间: 2025-11-16
-- 作者: zhengbing
-- 修复内容: 解决SQL错误[1093]问题
-- ===========================================

-- 清理现有权限数据（可选）
-- DELETE FROM role_permission;
-- DELETE FROM user_role;
-- DELETE FROM permission;
-- DELETE FROM role;

-- 开启事务
START TRANSACTION;

-- 第一步：插入一级权限（模块权限）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES

-- ========== 一级权限（模块） ==========
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

-- 第二步：插入二级权限（具体操作权限）
INSERT INTO permission (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`, `create_time`) VALUES

-- ========== 认证管理权限 ==========
('登录', 'auth:login', '用户登录操作', '/api/auth/login', 'POST', (SELECT id FROM permission WHERE code='auth'), 1, 2, NULL, 1, NOW()),
('注册', 'auth:register', '用户注册操作', '/api/auth/register', 'POST', (SELECT id FROM permission WHERE code='auth'), 2, 2, NULL, 1, NOW()),
('注销', 'auth:logout', '用户注销操作', '/api/auth/logout', 'POST', (SELECT id FROM permission WHERE code='auth'), 3, 2, NULL, 1, NOW()),
('获取当前用户信息', 'auth:current', '获取当前登录用户信息', '/api/auth/current', 'GET', (SELECT id FROM permission WHERE code='auth'), 4, 2, NULL, 1, NOW()),
('刷新令牌', 'auth:refresh', '刷新用户访问令牌', '/api/auth/refresh', 'POST', (SELECT id FROM permission WHERE code='auth'), 5, 2, NULL, 1, NOW()),

-- ========== 用户管理权限 ==========
-- 用户基本信息管理
('用户列表', 'user:list', '获取用户列表信息', '/api/user', 'GET', (SELECT id FROM permission WHERE code='user'), 1, 2, NULL, 1, NOW()),
('用户详情', 'user:detail', '获取用户详细信息', '/api/user/{id}', 'GET', (SELECT id FROM permission WHERE code='user'), 2, 2, NULL, 1, NOW()),
('创建用户', 'user:create', '创建新用户', '/api/user', 'POST', (SELECT id FROM permission WHERE code='user'), 3, 2, NULL, 1, NOW()),
('更新用户', 'user:update', '更新用户信息', '/api/user', 'PUT', (SELECT id FROM permission WHERE code='user'), 4, 2, NULL, 1, NOW()),
('删除用户', 'user:delete', '删除用户', '/api/user/{id}', 'DELETE', (SELECT id FROM permission WHERE code='user'), 5, 2, NULL, 1, NOW()),
('批量删除用户', 'user:batchDelete', '批量删除用户', '/api/user/batch', 'DELETE', (SELECT id FROM permission WHERE code='user'), 6, 2, NULL, 1, NOW()),

-- 用户密码管理
('重置密码', 'user:resetPassword', '重置用户密码', '/api/user/reset-password', 'PUT', (SELECT id FROM permission WHERE code='user'), 7, 2, NULL, 1, NOW()),
('批量重置密码', 'user:batchResetPassword', '批量重置用户密码', '/api/user/batch-reset-password', 'PUT', (SELECT id FROM permission WHERE code='user'), 8, 2, NULL, 1, NOW()),
('修改密码', 'user:changePassword', '修改当前用户密码', '/api/user/change-password', 'PUT', (SELECT id FROM permission WHERE code='user'), 9, 2, NULL, 1, NOW()),

-- 用户状态管理
('启用用户', 'user:enable', '启用用户账号', '/api/user/enable', 'PUT', (SELECT id FROM permission WHERE code='user'), 10, 2, NULL, 1, NOW()),
('禁用用户', 'user:disable', '禁用用户账号', '/api/user/disable', 'PUT', (SELECT id FROM permission WHERE code='user'), 11, 2, NULL, 1, NOW()),
('批量启用用户', 'user:batchEnable', '批量启用用户账号', '/api/user/batch-enable', 'PUT', (SELECT id FROM permission WHERE code='user'), 12, 2, NULL, 1, NOW()),
('批量禁用用户', 'user:batchDisable', '批量禁用用户账号', '/api/user/batch-disable', 'PUT', (SELECT id FROM permission WHERE code='user'), 13, 2, NULL, 1, NOW()),

-- 用户角色管理
('分配用户角色', 'user:assignRole', '为用户分配角色', '/api/user/assign-role', 'POST', (SELECT id FROM permission WHERE code='user'), 14, 2, NULL, 1, NOW()),
('撤销用户角色', 'user:revokeRole', '撤销用户角色', '/api/user/revoke-role', 'DELETE', (SELECT id FROM permission WHERE code='user'), 15, 2, NULL, 1, NOW()),

-- ========== 角色管理权限 ==========
-- 角色基本信息管理
('角色列表', 'role:list', '获取角色列表信息', '/api/roles', 'GET', (SELECT id FROM permission WHERE code='role'), 1, 2, NULL, 1, NOW()),
('角色详情', 'role:detail', '获取角色详细信息', '/api/roles/{id}', 'GET', (SELECT id FROM permission WHERE code='role'), 2, 2, NULL, 1, NOW()),
('创建角色', 'role:create', '创建新角色', '/api/roles', 'POST', (SELECT id FROM permission WHERE code='role'), 3, 2, NULL, 1, NOW()),
('更新角色', 'role:update', '更新角色信息', '/api/roles', 'PUT', (SELECT id FROM permission WHERE code='role'), 4, 2, NULL, 1, NOW()),
('删除角色', 'role:delete', '删除角色', '/api/roles/{id}', 'DELETE', (SELECT id FROM permission WHERE code='role'), 5, 2, NULL, 1, NOW()),
('批量删除角色', 'role:batchDelete', '批量删除角色', '/api/roles/batch', 'DELETE', (SELECT id FROM permission WHERE code='role'), 6, 2, NULL, 1, NOW()),

-- 角色状态管理
('启用角色', 'role:enable', '启用角色', '/api/roles/enable', 'PUT', (SELECT id FROM permission WHERE code='role'), 7, 2, NULL, 1, NOW()),
('禁用角色', 'role:disable', '禁用角色', '/api/roles/disable', 'PUT', (SELECT id FROM permission WHERE code='role'), 8, 2, NULL, 1, NOW()),

-- 角色权限管理
('分配角色权限', 'role:assignPermission', '为角色分配权限', '/api/roles/assign-permission', 'POST', (SELECT id FROM permission WHERE code='role'), 9, 2, NULL, 1, NOW()),
('撤销角色权限', 'role:revokePermission', '撤销角色权限', '/api/roles/revoke-permission', 'DELETE', (SELECT id FROM permission WHERE code='role'), 10, 2, NULL, 1, NOW()),

-- 角色用户管理
('分配角色用户', 'role:assignUser', '为角色分配用户', '/api/roles/assign-user', 'POST', (SELECT id FROM permission WHERE code='role'), 11, 2, NULL, 1, NOW()),
('撤销角色用户', 'role:revokeUser', '撤销角色用户', '/api/roles/revoke-user', 'DELETE', (SELECT id FROM permission WHERE code='role'), 12, 2, NULL, 1, NOW()),

-- ========== 权限管理权限 ==========
-- 权限基本信息管理
('权限列表', 'permission:list', '获取权限列表信息', '/api/permissions', 'GET', (SELECT id FROM permission WHERE code='permission'), 1, 2, NULL, 1, NOW()),
('权限树', 'permission:tree', '获取权限树形结构', '/api/permissions/tree', 'GET', (SELECT id FROM permission WHERE code='permission'), 2, 2, NULL, 1, NOW()),
('权限详情', 'permission:detail', '获取权限详细信息', '/api/permissions/{id}', 'GET', (SELECT id FROM permission WHERE code='permission'), 3, 2, NULL, 1, NOW()),
('创建权限', 'permission:create', '创建新权限', '/api/permissions', 'POST', (SELECT id FROM permission WHERE code='permission'), 4, 2, NULL, 1, NOW()),
('更新权限', 'permission:update', '更新权限信息', '/api/permissions', 'PUT', (SELECT id FROM permission WHERE code='permission'), 5, 2, NULL, 1, NOW()),
('删除权限', 'permission:delete', '删除权限', '/api/permissions/{id}', 'DELETE', (SELECT id FROM permission WHERE code='permission'), 6, 2, NULL, 1, NOW()),
('批量删除权限', 'permission:batchDelete', '批量删除权限', '/api/permissions/batch', 'DELETE', (SELECT id FROM permission WHERE code='permission'), 7, 2, NULL, 1, NOW()),

-- 权限状态管理
('启用权限', 'permission:enable', '启用权限', '/api/permissions/enable', 'PUT', (SELECT id FROM permission WHERE code='permission'), 8, 2, NULL, 1, NOW()),
('禁用权限', 'permission:disable', '禁用权限', '/api/permissions/disable', 'PUT', (SELECT id FROM permission WHERE code='permission'), 9, 2, NULL, 1, NOW()),

-- ========== 组织管理权限 ==========
-- 组织基本信息管理
('组织列表', 'organization:list', '获取组织列表信息', '/api/organization/list', 'GET', (SELECT id FROM permission WHERE code='organization'), 1, 2, NULL, 1, NOW()),
('组织树', 'organization:tree', '获取组织树形结构', '/api/organization/tree', 'GET', (SELECT id FROM permission WHERE code='organization'), 2, 2, NULL, 1, NOW()),
('组织详情', 'organization:detail', '获取组织详细信息', '/api/organization/{id}', 'GET', (SELECT id FROM permission WHERE code='organization'), 3, 2, NULL, 1, NOW()),
('创建组织', 'organization:create', '创建新组织', '/api/organization', 'POST', (SELECT id FROM permission WHERE code='organization'), 4, 2, NULL, 1, NOW()),
('更新组织', 'organization:update', '更新组织信息', '/api/organization', 'PUT', (SELECT id FROM permission WHERE code='organization'), 5, 2, NULL, 1, NOW()),
('删除组织', 'organization:delete', '删除组织', '/api/organization/{id}', 'DELETE', (SELECT id FROM permission WHERE code='organization'), 6, 2, NULL, 1, NOW()),

-- 组织子级管理
('子组织列表', 'organization:children', '获取子组织列表', '/api/organization/children/{parentId}', 'GET', (SELECT id FROM permission WHERE code='organization'), 7, 2, NULL, 1, NOW()),
('检查组织关联', 'organization:checkRelation', '检查组织关联关系', '/api/organization/check-relation', 'GET', (SELECT id FROM permission WHERE code='organization'), 8, 2, NULL, 1, NOW()),

-- ========== 用户组织关联权限 ==========
-- 用户组织关联管理
('用户组织列表', 'user-organization:list', '获取用户组织关联列表', '/api/user-organization/list', 'GET', (SELECT id FROM permission WHERE code='user-organization'), 1, 2, NULL, 1, NOW()),
('用户组织详情', 'user-organization:detail', '获取用户组织关联详情', '/api/user-organization/{id}', 'GET', (SELECT id FROM permission WHERE code='user-organization'), 2, 2, NULL, 1, NOW()),
('关联用户组织', 'user-organization:create', '关联用户和组织', '/api/user-organization', 'POST', (SELECT id FROM permission WHERE code='user-organization'), 3, 2, NULL, 1, NOW()),
('更新用户组织', 'user-organization:update', '更新用户组织关联', '/api/user-organization', 'PUT', (SELECT id FROM permission WHERE code='user-organization'), 4, 2, NULL, 1, NOW()),
('删除用户组织', 'user-organization:delete', '删除用户组织关联', '/api/user-organization/{id}', 'DELETE', (SELECT id FROM permission WHERE code='user-organization'), 5, 2, NULL, 1, NOW()),
('批量关联用户组织', 'user-organization:batchCreate', '批量关联用户和组织', '/api/user-organization/batch', 'POST', (SELECT id FROM permission WHERE code='user-organization'), 6, 2, NULL, 1, NOW()),

-- ========== 数据字典管理权限 ==========
-- 数据字典管理
('字典列表', 'data-dict:list', '获取数据字典列表', '/api/data-dict/list', 'GET', (SELECT id FROM permission WHERE code='data-dict'), 1, 2, NULL, 1, NOW()),
('字典详情', 'data-dict:detail', '获取数据字典详情', '/api/data-dict/{id}', 'GET', (SELECT id FROM permission WHERE code='data-dict'), 2, 2, NULL, 1, NOW()),
('创建字典', 'data-dict:create', '创建新数据字典', '/api/data-dict', 'POST', (SELECT id FROM permission WHERE code='data-dict'), 3, 2, NULL, 1, NOW()),
('更新字典', 'data-dict:update', '更新数据字典', '/api/data-dict', 'PUT', (SELECT id FROM permission WHERE code='data-dict'), 4, 2, NULL, 1, NOW()),
('删除字典', 'data-dict:delete', '删除数据字典', '/api/data-dict/{id}', 'DELETE', (SELECT id FROM permission WHERE code='data-dict'), 5, 2, NULL, 1, NOW()),
('批量删除字典', 'data-dict:batchDelete', '批量删除数据字典', '/api/data-dict/batch', 'DELETE', (SELECT id FROM permission WHERE code='data-dict'), 6, 2, NULL, 1, NOW()),

-- 字典状态管理
('启用字典', 'data-dict:enable', '启用数据字典', '/api/data-dict/status', 'PUT', (SELECT id FROM permission WHERE code='data-dict'), 7, 2, NULL, 1, NOW()),
('禁用字典', 'data-dict:disable', '禁用数据字典', '/api/data-dict/status', 'PUT', (SELECT id FROM permission WHERE code='data-dict'), 8, 2, NULL, 1, NOW()),

-- ========== 数据字典项管理权限 ==========
-- 字典项管理
('字典项列表', 'data-dict-item:list', '获取数据字典项列表', '/api/data-dict-item/list-by-dict/{dictId}', 'GET', (SELECT id FROM permission WHERE code='data-dict-item'), 1, 2, NULL, 1, NOW()),
('字典项详情', 'data-dict-item:detail', '获取数据字典项详情', '/api/data-dict-item/{id}', 'GET', (SELECT id FROM permission WHERE code='data-dict-item'), 2, 2, NULL, 1, NOW()),
('创建字典项', 'data-dict-item:create', '创建新数据字典项', '/api/data-dict-item', 'POST', (SELECT id FROM permission WHERE code='data-dict-item'), 3, 2, NULL, 1, NOW()),
('更新字典项', 'data-dict-item:update', '更新数据字典项', '/api/data-dict-item', 'PUT', (SELECT id FROM permission WHERE code='data-dict-item'), 4, 2, NULL, 1, NOW()),
('删除字典项', 'data-dict-item:delete', '删除数据字典项', '/api/data-dict-item/{id}', 'DELETE', (SELECT id FROM permission WHERE code='data-dict-item'), 5, 2, NULL, 1, NOW()),
('批量删除字典项', 'data-dict-item:batchDelete', '批量删除数据字典项', '/api/data-dict-item/batch', 'DELETE', (SELECT id FROM permission WHERE code='data-dict-item'), 6, 2, NULL, 1, NOW()),

-- 字典项状态管理
('启用字典项', 'data-dict-item:enable', '启用数据字典项', '/api/data-dict-item/status', 'PUT', (SELECT id FROM permission WHERE code='data-dict-item'), 7, 2, NULL, 1, NOW()),
('禁用字典项', 'data-dict-item:disable', '禁用数据字典项', '/api/data-dict-item/status', 'PUT', (SELECT id FROM permission WHERE code='data-dict-item'), 8, 2, NULL, 1, NOW()),

-- ========== 系统配置管理权限 ==========
-- 系统配置管理
('配置列表', 'system-config:list', '获取系统配置列表', '/api/system-config/list', 'GET', (SELECT id FROM permission WHERE code='system-config'), 1, 2, NULL, 1, NOW()),
('配置详情', 'system-config:detail', '获取系统配置详情', '/api/system-config/{configKey}', 'GET', (SELECT id FROM permission WHERE code='system-config'), 2, 2, NULL, 1, NOW()),
('创建配置', 'system-config:create', '创建新系统配置', '/api/system-config', 'POST', (SELECT id FROM permission WHERE code='system-config'), 3, 2, NULL, 1, NOW()),
('更新配置', 'system-config:update', '更新系统配置', '/api/system-config', 'PUT', (SELECT id FROM permission WHERE code='system-config'), 4, 2, NULL, 1, NOW()),
('删除配置', 'system-config:delete', '删除系统配置', '/api/system-config/{configKey}', 'DELETE', (SELECT id FROM permission WHERE code='system-config'), 5, 2, NULL, 1, NOW()),
('批量更新配置', 'system-config:batchUpdate', '批量更新系统配置', '/api/system-config/batch', 'PUT', (SELECT id FROM permission WHERE code='system-config'), 6, 2, NULL, 1, NOW()),

-- ========== 登录记录管理权限 ==========
-- 登录记录管理
('登录记录列表', 'login-record:list', '获取登录记录列表', '/api/loginRecords', 'GET', (SELECT id FROM permission WHERE code='login-record'), 1, 2, NULL, 1, NOW()),
('用户登录记录', 'login-record:userList', '获取指定用户的登录记录', '/api/loginRecords/user/{userId}', 'GET', (SELECT id FROM permission WHERE code='login-record'), 2, 2, NULL, 1, NOW()),
('最近登录记录', 'login-record:recent', '获取最近的登录记录', '/api/loginRecords/recent', 'GET', (SELECT id FROM permission WHERE code='login-record'), 3, 2, NULL, 1, NOW()),
('失败登录记录', 'login-record:failed', '获取失败的登录记录', '/api/loginRecords/failed', 'GET', (SELECT id FROM permission WHERE code='login-record'), 4, 2, NULL, 1, NOW()),
('清理登录记录', 'login-record:clean', '清理过期的登录记录', '/api/loginRecords/clean', 'DELETE', (SELECT id FROM permission WHERE code='login-record'), 5, 2, NULL, 1, NOW()),

-- ========== 白名单管理权限 ==========
-- 白名单管理
('白名单列表', 'white-list:list', '获取白名单列表', '/api/white-list/list', 'GET', (SELECT id FROM permission WHERE code='white-list'), 1, 2, NULL, 1, NOW()),
('白名单详情', 'white-list:detail', '获取白名单详情', '/api/white-list/{id}', 'GET', (SELECT id FROM permission WHERE code='white-list'), 2, 2, NULL, 1, NOW()),
('创建白名单', 'white-list:create', '创建新白名单', '/api/white-list', 'POST', (SELECT id FROM permission WHERE code='white-list'), 3, 2, NULL, 1, NOW()),
('更新白名单', 'white-list:update', '更新白名单', '/api/white-list', 'PUT', (SELECT id FROM permission WHERE code='white-list'), 4, 2, NULL, 1, NOW()),
('删除白名单', 'white-list:delete', '删除白名单', '/api/white-list/{id}', 'DELETE', (SELECT id FROM permission WHERE code='white-list'), 5, 2, NULL, 1, NOW()),
('批量删除白名单', 'white-list:batchDelete', '批量删除白名单', '/api/white-list/batch', 'DELETE', (SELECT id FROM permission WHERE code='white-list'), 6, 2, NULL, 1, NOW()),

-- ========== 验证码管理权限 ==========
-- 验证码管理
('生成验证码', 'captcha:generate', '生成验证码', '/api/captcha/generate', 'GET', (SELECT id FROM permission WHERE code='captcha'), 1, 2, NULL, 1, NOW()),
('刷新验证码', 'captcha:refresh', '刷新验证码', '/api/captcha/refresh/{captchaId}', 'GET', (SELECT id FROM permission WHERE code='captcha'), 2, 2, NULL, 1, NOW()),
('验证验证码', 'captcha:verify', '验证验证码', '/api/captcha/verify', 'POST', (SELECT id FROM permission WHERE code='captcha'), 3, 2, NULL, 1, NOW()),

-- ========== 国际化测试权限 ==========
-- 国际化管理
('国际化测试', 'i18n:test', '国际化功能测试', '/api/i18n/test', 'GET', (SELECT id FROM permission WHERE code='i18n'), 1, 2, NULL, 1, NOW());

-- 第三步：插入角色数据
INSERT INTO role (`name`, `code`, `description`, `status`, `create_time`) VALUES
-- 系统内置角色
('系统管理员', 'ADMIN', '系统管理员，拥有系统所有权限', 1, NOW()),
('普通用户', 'USER', '普通用户，拥有基本的系统使用权限', 1, NOW()),
('部门管理员', 'DEPT_ADMIN', '部门管理员，拥有部门内用户管理权限', 1, NOW()),
('财务管理员', 'FINANCE_ADMIN', '财务管理员，拥有财务相关权限', 1, NOW()),
('人事管理员', 'HR_ADMIN', '人事管理员，拥有人事相关权限', 1, NOW()),
('审计员', 'AUDITOR', '审计员，拥有查看系统操作日志权限', 1, NOW()),
('访客', 'GUEST', '访客，只能查看基本信息，无修改权限', 1, NOW());

-- 第四步：分配权限给角色
-- 1. 系统管理员拥有所有权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission WHERE status = 1;

-- 2. 普通用户权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 2, id FROM permission 
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

-- 3. 部门管理员权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 3, id FROM permission 
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

-- 4. 财务管理员权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 4, id FROM permission 
WHERE code IN (
    -- 基本认证权限
    'auth:login', 'auth:logout', 'auth:current',
    -- 用户查看权限
    'user:list', 'user:detail',
    -- 组织管理查看权限
    'organization:list', 'organization:tree',
    -- 登录记录查看权限
    'login-record:list', 'login-record:userList', 'login-record:recent', 'login-record:failed',
    -- 数据字典查看权限
    'data-dict:list', 'data-dict-item:list',
    -- 国际化测试
    'i18n:test'
);

-- 5. 人事管理员权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 5, id FROM permission 
WHERE code IN (
    -- 基本认证权限
    'auth:login', 'auth:logout', 'auth:current',
    -- 完整的用户管理权限
    'user:list', 'user:detail', 'user:create', 'user:update', 'user:delete',
    'user:enable', 'user:disable', 'user:assignRole', 'user:revokeRole',
    'user:resetPassword', 'user:batchResetPassword', 'user:batchEnable', 'user:batchDisable',
    -- 组织管理权限
    'organization:list', 'organization:tree', 'organization:detail', 
    'organization:children', 'organization:checkRelation',
    -- 用户组织关联权限
    'user-organization:list', 'user-organization:detail', 'user-organization:create',
    'user-organization:update', 'user-organization:delete', 'user-organization:batchCreate',
    -- 数据字典管理权限
    'data-dict:list', 'data-dict:detail', 'data-dict:create', 'data-dict:update', 
    'data-dict:enable', 'data-dict:disable',
    'data-dict-item:list', 'data-dict-item:detail', 'data-dict-item:create', 
    'data-dict-item:update', 'data-dict-item:enable', 'data-dict-item:disable',
    -- 国际化测试
    'i18n:test'
);

-- 6. 审计员权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 6, id FROM permission 
WHERE code IN (
    -- 基本认证权限
    'auth:login', 'auth:logout', 'auth:current',
    -- 只读权限
    'user:list', 'user:detail',
    'role:list', 'role:detail',
    'permission:list', 'permission:tree', 'permission:detail',
    'organization:list', 'organization:tree', 'organization:detail',
    'user-organization:list', 'user-organization:detail',
    'data-dict:list', 'data-dict:detail',
    'data-dict-item:list', 'data-dict-item:detail',
    'login-record:list', 'login-record:userList', 'login-record:recent', 'login-record:failed',
    'system-config:list', 'system-config:detail',
    'white-list:list', 'white-list:detail',
    -- 国际化测试
    'i18n:test'
);

-- 7. 访客权限（只有查看权限）
INSERT INTO role_permission (role_id, permission_id)
SELECT 7, id FROM permission 
WHERE code IN (
    -- 基本认证权限
    'auth:login', 'auth:current',
    -- 只读权限
    'user:list',
    'organization:list',
    'data-dict:list',
    'data-dict-item:list',
    -- 国际化测试
    'i18n:test'
);

-- 提交事务
COMMIT;

-- 查询权限统计信息
SELECT 
    (SELECT COUNT(*) FROM permission WHERE status = 1) as total_permissions,
    (SELECT COUNT(*) FROM role WHERE status = 1) as total_roles,
    (SELECT COUNT(*) FROM role_permission) as role_permission_assignments;