-- 审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(100) COMMENT '操作用户名',
    ip_address VARCHAR(50) COMMENT '操作IP地址',
    operation_time DATETIME COMMENT '操作时间',
    module VARCHAR(100) COMMENT '操作模块',
    operation_type VARCHAR(50) COMMENT '操作类型',
    description VARCHAR(255) COMMENT '操作描述',
    request_params TEXT COMMENT '请求参数',
    result VARCHAR(20) COMMENT '操作结果',
    error_message TEXT COMMENT '错误信息',
    execution_time BIGINT COMMENT '操作耗时(毫秒)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation_time (operation_time),
    INDEX idx_module (module),
    INDEX idx_operation_type (operation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';