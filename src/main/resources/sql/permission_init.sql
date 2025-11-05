-- 权限表和角色权限关联表的初始化脚本

-- 创建权限表
CREATE TABLE `permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '权限名称',
  `code` varchar(100) NOT NULL COMMENT '权限标识',
  `description` varchar(255) DEFAULT NULL COMMENT '权限描述',
  `url` varchar(255) DEFAULT NULL COMMENT '权限URL',
  `method` varchar(20) DEFAULT NULL COMMENT '请求方法',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父级权限ID',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `type` tinyint(4) NOT NULL COMMENT '类型：0-菜单，1-按钮，2-API',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 创建角色权限关联表
CREATE TABLE `role_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`),
  CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 插入系统管理菜单权限
INSERT INTO `permission` (`name`, `code`, `description`, `url`, `method`, `parent_id`, `sort`, `type`, `icon`, `status`) VALUES
('系统管理', 'sys:manage', '系统管理菜单', '/api/system', NULL, NULL, 1, 0, 'system', 1),
('用户管理', 'sys:user:manage', '用户管理菜单', '/api/users', NULL, 1, 1, 0, 'user', 1),
('用户查询', 'sys:user:query', '查询用户权限', '/api/users', 'GET', 2, 1, 1, NULL, 1),
('用户创建', 'sys:user:create', '创建用户权限', '/api/users', 'POST', 2, 2, 1, NULL, 1),
('用户更新', 'sys:user:update', '更新用户权限', '/api/users/{id}', 'PUT', 2, 3, 1, NULL, 1),
('用户删除', 'sys:user:delete', '删除用户权限', '/api/users/{id}', 'DELETE', 2, 4, 1, NULL, 1),
('角色管理', 'sys:role:manage', '角色管理菜单', '/api/roles', NULL, 1, 2, 0, 'role', 1),
('角色查询', 'sys:role:query', '查询角色权限', '/api/roles', 'GET', 7, 1, 1, NULL, 1),
('角色创建', 'sys:role:create', '创建角色权限', '/api/roles', 'POST', 7, 2, 1, NULL, 1),
('角色更新', 'sys:role:update', '更新角色权限', '/api/roles/{id}', 'PUT', 7, 3, 1, NULL, 1),
('角色删除', 'sys:role:delete', '删除角色权限', '/api/roles/{id}', 'DELETE', 7, 4, 1, NULL, 1),
('权限管理', 'sys:permission:manage', '权限管理菜单', '/api/permissions', NULL, 1, 3, 0, 'permission', 1),
('权限查询', 'sys:permission:query', '查询权限权限', '/api/permissions', 'GET', 12, 1, 1, NULL, 1),
('权限创建', 'sys:permission:create', '创建权限权限', '/api/permissions', 'POST', 12, 2, 1, NULL, 1),
('权限更新', 'sys:permission:update', '更新权限权限', '/api/permissions/{id}', 'PUT', 12, 3, 1, NULL, 1),
('权限删除', 'sys:permission:delete', '删除权限权限', '/api/permissions/{id}', 'DELETE', 12, 4, 1, NULL, 1);

-- 为管理员角色分配权限（假设管理员角色ID为1）
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
(1, 1),  -- 系统管理
(1, 2),  -- 用户管理
(1, 3),  -- 用户查询
(1, 4),  -- 用户创建
(1, 5),  -- 用户更新
(1, 6),  -- 用户删除
(1, 7),  -- 角色管理
(1, 8),  -- 角色查询
(1, 9),  -- 角色创建
(1, 10), -- 角色更新
(1, 11), -- 角色删除
(1, 12), -- 权限管理
(1, 13), -- 权限查询
(1, 14), -- 权限创建
(1, 15), -- 权限更新
(1, 16); -- 权限删除