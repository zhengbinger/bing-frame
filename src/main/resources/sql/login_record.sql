-- 创建登录记录表
CREATE TABLE IF NOT EXISTS `login_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `ip_address` varchar(50) DEFAULT NULL COMMENT '登录IP地址',
  `user_agent` text COMMENT '用户代理信息',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '登录状态(1:成功, 0:失败)',
  `message` varchar(200) DEFAULT NULL COMMENT '登录结果描述',
  `login_time` datetime NOT NULL COMMENT '登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_status_time` (`status`, `login_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录记录表';