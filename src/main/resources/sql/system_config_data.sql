-- 系统配置表初始化数据脚本
-- 插入常用的系统配置项

-- 清空现有数据（仅用于开发环境，生产环境请谨慎使用）
-- DELETE FROM system_config;

-- 重置自增ID（仅用于开发环境）
-- ALTER TABLE system_config AUTO_INCREMENT = 1000;

-- =============================================
-- 系统基本信息配置
-- =============================================

-- 系统名称
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`) VALUES
('system.name', 'Bing Framework', 'string', '系统名称', 'system', 1, 1),
('system.version', '1.0.0', 'string', '系统版本号', 'system', 1, 2),
('system.description', '基于Spring Boot的企业级开发框架', 'string', '系统描述', 'system', 1, 3),
('system.company', 'Bing科技', 'string', '公司名称', 'system', 1, 4),
('system.copyright', 'Copyright © 2025 Bing科技. All rights reserved.', 'string', '版权信息', 'system', 1, 5);

-- =============================================
-- 安全配置
-- =============================================

-- 密码策略
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`) VALUES
('security.password.minLength', '6', 'int', '密码最小长度', 'security', 1, 10),
('security.password.maxLength', '32', 'int', '密码最大长度', 'security', 1, 11),
('security.password.requireUppercase', 'true', 'boolean', '是否需要大写字母', 'security', 1, 12),
('security.password.requireLowercase', 'true', 'boolean', '是否需要小写字母', 'security', 1, 13),
('security.password.requireNumbers', 'true', 'boolean', '是否需要数字', 'security', 1, 14),
('security.password.requireSpecialChars', 'false', 'boolean', '是否需要特殊字符', 'security', 1, 15);

-- 会话配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`) VALUES
('session.timeout', '30', 'int', '会话超时时间（分钟）', 'security', 1, 20),
('session.maxConcurrent', '1000', 'int', '最大并发会话数', 'security', 1, 21),
('session.refreshInterval', '5', 'int', '会话刷新间隔（分钟）', 'security', 1, 22);

-- API安全配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`) VALUES
('api.rate.limit.enabled', 'true', 'boolean', '启用API限流', 'security', 1, 30),
('api.rate.limit.requests', '1000', 'int', '每分钟最大请求数', 'security', 1, 31),
('api.rate.limit.window', '60', 'int', '限流时间窗口（秒）', 'security', 1, 32),
('api.signature.required', 'true', 'boolean', 'API签名验证必需', 'security', 1, 33);

-- =============================================
-- 文件上传配置
-- =============================================

INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`) VALUES
('upload.maxSize', '10485760', 'int', '文件最大上传大小（字节）', 'upload', 1, 40),
('upload.allowedTypes', 'jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx', 'string', '允许的文件类型', 'upload', 1, 41),
('upload.path', '/uploads/', 'string', '文件上传路径', 'upload', 1, 42),
('upload.autoDelete', 'false', 'boolean', '是否自动删除过期文件', 'upload', 1, 43);

-- =============================================
-- 缓存配置
-- =============================================

INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`) VALUES
('cache.enabled', 'true', 'boolean', '启用缓存', 'cache', 1, 50),
('cache.defaultExpiry', '3600', 'int', '默认缓存过期时间（秒）', 'cache', 1, 51),
('cache.systemConfigExpiry', '300', 'int', '系统配置缓存过期时间（秒）', 'cache', 1, 52),
('cache.maxSize', '10000', 'int', '缓存最大条目数', 'cache', 1, 53);

-- =============================================
-- 数据库配置
-- =============================================

INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`) VALUES
('database.pageSize.default', '20', 'int', '默认分页大小', 'database', 1, 60),
('database.pageSize.max', '100', 'int', '最大分页大小', 'database', 1, 61),
('database.batchSize', '1000', 'int', '批量操作大小', 'database', 1, 62);

-- =============================================
-- 日志配置
-- =============================================

INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`) VALUES
('log.level.root', 'INFO', 'string', '根日志级别', 'log', 1, 70),
('log.level.com.bing.framework', 'DEBUG', 'string', '框架日志级别', 'log', 1, 71),
('log.audit.enabled', 'true', 'boolean', '启用审计日志', 'log', 1, 72),
('log.audit.retentionDays', '90', 'int', '审计日志保留天数', 'log', 1, 73);

-- =============================================
-- 邮件配置
-- =============================================

INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`, `is_sensitive`) VALUES
('mail.smtp.host', 'smtp.example.com', 'string', 'SMTP服务器地址', 'mail', 1, 80, 0),
('mail.smtp.port', '587', 'int', 'SMTP端口', 'mail', 1, 81, 0),
('mail.smtp.username', '', 'string', 'SMTP用户名', 'mail', 1, 82, 1),
('mail.smtp.password', '', 'string', 'SMTP密码', 'mail', 1, 83, 1),
('mail.smtp.tls', 'true', 'boolean', '启用TLS', 'mail', 1, 84, 0),
('mail.from.address', 'noreply@example.com', 'email', '发件人邮箱', 'mail', 1, 85, 0);

-- =============================================
-- Redis配置
-- =============================================

INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`, `is_sensitive`) VALUES
('redis.host', 'localhost', 'string', 'Redis服务器地址', 'redis', 1, 90, 0),
('redis.port', '6379', 'int', 'Redis端口', 'redis', 1, 91, 0),
('redis.password', '', 'string', 'Redis密码', 'redis', 1, 92, 1),
('redis.database', '0', 'int', 'Redis数据库', 'redis', 1, 93, 0),
('redis.timeout', '3000', 'int', '连接超时时间（毫秒）', 'redis', 1, 94, 0);

-- =============================================
-- 监控配置
-- =============================================

INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`) VALUES
('monitor.enabled', 'true', 'boolean', '启用系统监控', 'monitor', 1, 100),
('monitor.healthCheckInterval', '30', 'int', '健康检查间隔（秒）', 'monitor', 1, 101),
('monitor.metricsEnabled', 'true', 'boolean', '启用指标收集', 'monitor', 1, 102);

-- =============================================
-- 业务配置
-- =============================================

INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`) VALUES
('business.maintenanceMode', 'false', 'boolean', '维护模式', 'business', 1, 110),
('business.registration.enabled', 'true', 'boolean', '允许用户注册', 'business', 1, 111),
('business.emailVerification', 'false', 'boolean', '需要邮箱验证', 'business', 1, 112),
('business.defaultRole', 'USER', 'string', '新用户默认角色', 'business', 1, 113);

-- =============================================
-- 前端配置
-- =============================================

INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `config_category`, `enabled`, `sort_order`) VALUES
('frontend.theme', 'default', 'string', '系统主题', 'frontend', 1, 120),
('frontend.language', 'zh-CN', 'string', '默认语言', 'frontend', 1, 121),
('frontend.pageSize.table', '10', 'int', '表格默认页大小', 'frontend', 1, 122),
('frontend.autoRefresh', 'false', 'boolean', '自动刷新页面', 'frontend', 1, 123);