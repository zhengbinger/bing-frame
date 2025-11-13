-- 数据字典管理表结构创建脚本

-- 创建数据字典表
CREATE TABLE `data_dict` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '字典名称',
  `code` varchar(100) NOT NULL COMMENT '字典编码',
  `description` varchar(255) DEFAULT NULL COMMENT '字典描述',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- 创建数据字典项表
CREATE TABLE `data_dict_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_id` bigint(20) NOT NULL COMMENT '所属字典ID',
  `label` varchar(100) NOT NULL COMMENT '显示文本',
  `value` varchar(100) NOT NULL COMMENT '值',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_value` (`dict_id`,`value`),
  KEY `idx_dict_id` (`dict_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_data_dict_item_dict_id` FOREIGN KEY (`dict_id`) REFERENCES `data_dict` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典项表';

-- 插入初始数据字典
INSERT INTO `data_dict` (`id`, `name`, `code`, `description`, `sort`, `status`) VALUES
(1, '用户状态', 'USER_STATUS', '系统用户状态类型', 1, 1),
(2, '性别类型', 'GENDER_TYPE', '性别分类', 2, 1),
(3, '操作类型', 'OPERATION_TYPE', '操作日志类型', 3, 1),
(4, '数据级别', 'DATA_LEVEL', '数据访问权限级别', 4, 1);

-- 插入初始数据字典项
INSERT INTO `data_dict_item` (`dict_id`, `label`, `value`, `description`, `sort`, `status`) VALUES
-- 用户状态
(1, '启用', '1', '用户账户处于启用状态', 1, 1),
(1, '禁用', '0', '用户账户处于禁用状态', 2, 1),
(1, '锁定', '2', '用户账户处于锁定状态', 3, 1),
-- 性别类型
(2, '男', 'MALE', '男性', 1, 1),
(2, '女', 'FEMALE', '女性', 2, 1),
(2, '保密', 'SECRET', '保密性别', 3, 1),
-- 操作类型
(3, '新增', 'CREATE', '新增操作', 1, 1),
(3, '修改', 'UPDATE', '修改操作', 2, 1),
(3, '删除', 'DELETE', '删除操作', 3, 1),
(3, '查询', 'QUERY', '查询操作', 4, 1),
(3, '登录', 'LOGIN', '登录操作', 5, 1),
(3, '登出', 'LOGOUT', '登出操作', 6, 1),
-- 数据级别
(4, '公开', 'PUBLIC', '公开数据', 1, 1),
(4, '内部', 'INTERNAL', '内部数据', 2, 1),
(4, '机密', 'CONFIDENTIAL', '机密数据', 3, 1),
(4, '绝密', 'TOP_SECRET', '绝密数据', 4, 1);