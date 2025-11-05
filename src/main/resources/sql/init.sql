-- 数据库创建脚本
CREATE DATABASE bing CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 用户表创建脚本
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户表初始化数据
-- 密码使用加密后的格式，默认密码为：admin123
INSERT INTO `user` (`id`, `username`, `password`, `email`, `phone`, `nickname`, `status`) VALUES
(1, 'admin', '$2a$10$eLbXj3wG78G2G8e7Nk4uS.y0qJ5z1Kj1Z1X1V1N1B1A1Q1L1O1P1', 'admin@example.com', '13800138000', '超级管理员', 1),
(2, 'user01', '$2a$10$eLbXj3wG78G2G8e7Nk4uS.y0qJ5z1Kj1Z1X1V1N1B1A1Q1L1O1P1', 'user01@example.com', '13800138001', '普通用户', 1),
(3, 'readonly', '$2a$10$eLbXj3wG78G2G8e7Nk4uS.y0qJ5z1Kj1Z1X1V1N1B1A1Q1L1O1P1', 'readonly@example.com', '13800138002', '只读用户', 1);

-- 重置自增ID
ALTER TABLE `user` AUTO_INCREMENT = 1000;