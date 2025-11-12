-- 组织表设计
CREATE TABLE `organization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `name` varchar(255) NOT NULL COMMENT '组织名称',
  `code` varchar(50) NOT NULL COMMENT '组织编码',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父组织ID，0表示顶级组织',
  `path` varchar(1000) DEFAULT '' COMMENT '组织路径，用于快速查找层级关系',
  `sort` int(11) DEFAULT '0' COMMENT '排序字段',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用，1启用，0禁用',
  `description` varchar(1000) DEFAULT NULL COMMENT '组织描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_path` (`path`),
  KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织信息表';

-- 用户组织关联表
CREATE TABLE `user_organization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `organization_id` bigint(20) NOT NULL COMMENT '组织ID',
  `is_main` tinyint(1) DEFAULT '0' COMMENT '是否为主组织，1是，0否',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_org` (`user_id`,`organization_id`),
  UNIQUE KEY `idx_user_main` (`user_id`,`is_main`) USING BTREE,
  KEY `idx_user_id` (`user_id`),
  KEY `idx_organization_id` (`organization_id`),
  CONSTRAINT `fk_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_organization_id` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户组织关联表';