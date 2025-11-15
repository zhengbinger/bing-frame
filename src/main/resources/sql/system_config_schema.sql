-- 系统配置表结构创建脚本
-- SystemConfig表用于存储系统级别的配置信息
-- 对应实体类：com.bing.framework.entity.SystemConfig
-- 避免使用MySQL保留字，使用created_time和updated_time替代create_time和update_time

CREATE TABLE `system_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(255) NOT NULL COMMENT '配置键',
  `config_value` text NOT NULL COMMENT '配置值',
  `config_type` varchar(50) NOT NULL DEFAULT 'string' COMMENT '配置类型：string, int, boolean, json, email, url等',
  `description` varchar(500) DEFAULT NULL COMMENT '配置描述',
  `config_category` varchar(100) DEFAULT NULL COMMENT '配置分类',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序权重',
  `is_sensitive` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否敏感配置：0-非敏感，1-敏感',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_config_type` (`config_type`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_config_category` (`config_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 创建索引优化查询性能
CREATE INDEX idx_system_config_enabled ON `system_config` (`enabled`);
CREATE INDEX idx_system_config_type_category ON `system_config` (`config_type`, `config_category`);