-- 角色表创建脚本
DROP TABLE IF EXISTS `user_role`;
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(50) NOT NULL COMMENT '角色名称',
  `code` varchar(50) NOT NULL COMMENT '角色标识',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表创建脚本
CREATE TABLE `user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色表初始化数据
INSERT INTO `role` (`id`, `name`, `code`, `description`, `status`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '系统最高权限管理员', 1),
(2, '普通管理员', 'ADMIN', '系统普通管理员', 1),
(3, '普通用户', 'USER', '普通用户权限', 1),
(4, '只读用户', 'READONLY', '仅具有数据查看权限', 1);

-- 用户角色关联表初始化数据
-- 超级管理员用户关联超级管理员角色
-- 普通用户关联普通用户角色
-- 只读用户关联只读用户角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 3),
(3, 4);

-- 重置自增ID
ALTER TABLE `role` AUTO_INCREMENT = 1000;
ALTER TABLE `user_role` AUTO_INCREMENT = 1000;