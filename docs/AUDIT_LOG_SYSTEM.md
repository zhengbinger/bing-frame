# 审计日志系统设计与使用指南

## 1. 系统概述

本项目实现了一套完整的审计日志系统，支持将重要的业务操作和API访问记录自动或手动记录到数据库中，便于后续审计和查询。

主要特点：
- 支持自动拦截Controller方法并记录API访问日志
- 支持手动记录关键业务操作日志
- 异步处理，不影响主业务流程
- 双重保障机制（直接写入数据库 + Logback Appender）
- 包含完整的审计信息（用户、IP、操作类型、时间、结果等）

## 2. 系统架构

![审计日志系统架构](https://example.com/audit-log-architecture.png)

### 2.1 核心组件

1. **AuditLog实体类**：定义审计日志的数据结构
2. **AuditLogMapper**：数据库访问层，负责将日志写入数据库
3. **AuditLogService**：业务逻辑层，提供同步和异步记录日志的方法
4. **AuditLogAppender**：自定义Logback Appender，用于将日志写入数据库
5. **AuditLogAspect**：AOP切面，自动拦截Controller方法并记录审计日志
6. **AuditLogUtil**：工具类，提供便捷的手动记录审计日志方法

## 3. 数据库设计

审计日志表结构如下：

```sql
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
```

## 4. 使用方法

### 4.1 自动记录API访问日志

系统会自动拦截所有Controller方法的调用，并记录详细的访问信息。无需额外配置，只需确保：

1. `AuditLogAspect` 类已被Spring容器扫描并加载
2. 数据库中已创建 `audit_log` 表

### 4.2 手动记录业务操作日志

对于关键业务操作，可以使用 `AuditLogUtil` 工具类手动记录审计日志：

```java
// 记录成功操作
AuditLogUtil.logSuccess(
    "用户管理",           // 模块名称
    "修改密码",           // 操作类型
    "用户修改登录密码",    // 操作描述
    "{userId:123, newPassword:***}" // 请求参数
);

// 记录失败操作
AuditLogUtil.logFailure(
    "权限管理",           // 模块名称
    "分配角色",           // 操作类型
    "为用户分配角色权限",   // 操作描述
    "{userId:123, roleIds:[1,2,3]}", // 请求参数
    "用户不存在"           // 错误信息
);

// 记录自定义结果的操作
AuditLogUtil.log(
    "订单管理",           // 模块名称
    "创建订单",           // 操作类型
    "用户创建新订单",      // 操作描述
    "{productId:123, quantity:2}", // 请求参数
    "待支付"               // 自定义结果
);
```

### 4.3 配置说明

#### 4.3.1 日志配置

在 `logback-spring.xml` 中已配置了审计日志的处理方式。可以根据需要调整：

- 审计日志会同时写入数据库和文件日志
- 可以修改日志级别和输出格式

#### 4.3.2 异步任务配置

在 `application.yml` 中配置了异步任务线程池：

```yaml
spring:
  task:
    execution:
      pool:
        core-size: 10
        max-size: 50
        queue-capacity: 1000
        keep-alive: 60s
        thread-name-prefix: audit-log-executor-
```

可以根据系统负载情况调整这些参数。

## 5. 注意事项

1. **性能考虑**：审计日志采用异步处理，不会阻塞主业务流程，但在高并发场景下仍需监控数据库写入性能
2. **存储优化**：建议定期清理或归档历史审计日志，避免表数据过大影响查询性能
3. **用户信息获取**：当前实现从请求头获取用户名，实际项目中应结合认证机制修改 `getCurrentUsername()` 方法
4. **异常处理**：记录审计日志失败时不会影响主业务流程，会降级记录到文件日志
5. **敏感信息**：记录请求参数时注意过滤敏感信息（如密码、身份证号等）

## 6. 扩展建议

1. **添加查询接口**：实现审计日志的查询、统计和导出功能
2. **实时监控**：结合ELK或其他日志系统，实现审计日志的实时监控和告警
3. **分布式追踪**：集成Zipkin或SkyWalking，将审计日志与分布式追踪信息关联
4. **数据加密**：对敏感的审计信息进行加密存储
5. **定时清理**：实现定时任务，定期清理或归档过期的审计日志