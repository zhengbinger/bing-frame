-- 白名单表结构创建脚本
CREATE TABLE IF NOT EXISTS `sys_white_list` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pattern` varchar(255) NOT NULL COMMENT '请求路径模式，支持Ant风格路径模式',
  `type` varchar(50) DEFAULT 'URL' COMMENT '白名单类型，如：URL, IP, USER_AGENT等',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用：1启用，0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_pattern` (`pattern`) COMMENT '请求路径模式唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请求白名单表';

-- 插入初始数据
INSERT INTO `sys_white_list` (`pattern`, `type`, `description`, `enabled`) VALUES
('/api/auth/login', 'URL', '用户登录接口', 1),
('/api/auth/register', 'URL', '用户注册接口', 1),
('/api/auth/logout', 'URL', '用户注销接口', 1),
('/api/white-list/**', 'URL', '白名单管理接口', 1),
('/doc.html', 'URL', 'Swagger文档页面', 1),
('/swagger-resources/**', 'URL', 'Swagger资源', 1),
('/v2/api-docs', 'URL', 'Swagger API文档', 1),
('/webjars/**', 'URL', 'Swagger依赖资源', 1),
('/knife4j/**', 'URL', 'Knife4j资源', 1),
('/swagger-ui/**', 'URL', 'Swagger UI资源', 1);