# 审计日志系统设计与使用指南

## 1. 系统概述

本项目实现了一套完整的高性能审计日志系统，支持将重要的业务操作和API访问记录自动或手动记录到数据库中，便于后续审计和查询。

主要特点：
- 支持自动拦截Controller方法并记录API访问日志
- 支持手动记录关键业务操作日志
- 高性能异步处理，专用线程池管理，不影响主业务流程
- 缓冲池和批量写入策略，大幅减少数据库I/O操作
- 支持多级审计日志级别控制，可以针对不同接口灵活配置
- 双重保障机制（直接写入数据库 + Logback Appender）
- 包含完整的审计信息（用户、IP、操作类型、时间、结果等）

## 2. 系统架构

![审计日志系统架构](https://example.com/audit-log-architecture.png)

### 2.1 核心组件

1. **AuditLog实体类**：定义审计日志的数据结构
2. **AuditLogMapper**：数据库访问层，提供单个和批量写入审计日志的方法
3. **AuditLogService**：业务逻辑层，提供同步和异步记录日志的方法
4. **AuditLogAppender**：自定义Logback Appender，用于将日志写入数据库
5. **AuditLogAspect**：AOP切面，自动拦截Controller方法并记录审计日志
6. **AuditLogUtil**：工具类，提供便捷的手动记录审计日志方法
7. **AuditLogBufferManager**：缓冲池管理器，实现批量写入和定时刷新功能
8. **AsyncLogConfig**：异步日志配置类，提供专用的线程池配置
9. **AuditLogLevel注解**：用于控制不同接口的审计日志级别

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

系统会自动拦截所有Controller方法的调用，并记录访问信息。无需额外配置，只需确保：

1. `AuditLogAspect` 类已被Spring容器扫描并加载
2. 数据库中已创建 `audit_log` 表

### 4.1.1 审计日志级别控制

通过`@AuditLogLevel`注解，可以灵活控制不同接口的审计日志记录策略：

## 5. 登录记录功能集成

### 5.1 登录记录与审计日志的关系

登录记录功能是审计日志系统的一个重要补充，专门用于记录用户的登录行为，提供更详细的登录信息追踪和安全审计能力。

### 5.2 主要区别

| 特性 | 审计日志 | 登录记录 |
| :--- | :--- | :--- |
| **记录范围** | 所有API操作 | 仅登录行为 |
| **存储方式** | 单一`audit_log`表 | 专用`login_record`表 |
| **详细程度** | 中等 | 高（包含IP、UserAgent等） |
| **查询功能** | 通用查询 | 专用查询接口，支持多条件筛选 |
| **保留策略** | 默认永久保留 | 支持定期清理（默认90天） |
| **安全审计** | 全面操作审计 | 专注登录安全分析 |

### 5.3 登录记录表结构

```sql
CREATE TABLE IF NOT EXISTS login_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    ip_address VARCHAR(50) COMMENT '登录IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理信息',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    status INT DEFAULT 1 COMMENT '登录状态：0-失败，1-成功',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_login_time (login_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录记录表';
```

### 5.4 集成方式

#### 5.4.1 自动登录记录

系统在`AuthController`中集成了登录记录功能，当用户执行登录操作时，会自动记录登录信息：

1. 成功登录时记录成功状态和用户ID
2. 登录失败时记录失败状态和尝试的用户名
3. 同时记录IP地址、UserAgent等信息

#### 5.4.2 手动记录登录信息

在需要记录登录信息的地方，可以使用以下方式：

```java
// 创建登录记录
LoginRecord loginRecord = new LoginRecord();
loginRecord.setUserId(userId);
loginRecord.setUsername(username);
loginRecord.setIpAddress(ipAddress);
loginRecord.setUserAgent(userAgent);
loginRecord.setLoginTime(new Date());
loginRecord.setStatus(status); // 0-失败，1-成功

// 保存登录记录
loginRecordService.saveLoginRecord(loginRecord);
```

### 5.5 安全功能

登录记录功能增强了系统的安全审计能力：

1. **异常登录检测**：通过分析登录记录，可以检测到异常的登录行为（如频繁失败、异常IP等）
2. **登录统计分析**：可以统计登录成功率、活跃用户等信息
3. **安全事件溯源**：当发生安全事件时，可以通过登录记录进行溯源分析
4. **自动清理机制**：支持定期清理过期的登录记录，避免数据量过大

### 5.6 配置与使用

#### 5.6.1 清理策略配置

在`LoginRecordController`中提供了清理过期记录的API接口，可以配置默认清理天数：

```java
// 默认清理90天前的记录
@DeleteMapping("/clean")
public Result<Map<String, Integer>> cleanExpiredRecords(@RequestParam(defaultValue = "90") Integer days) {
    // 实现逻辑
}
```

#### 5.6.2 与审计日志的协同工作

登录操作会同时生成：
1. 审计日志记录（记录操作过程）
2. 登录记录（记录登录详情）

这样可以从不同角度完整记录用户的登录行为，提供全方位的安全审计支持。

```java
// 忽略某个方法的审计日志记录
@AuditLogLevel(ignore = true)
@RequestMapping("/public/health")
public String healthCheck() {
    return "ok";
}

// 使用基本级别记录（不记录详细请求参数）
@AuditLogLevel(AuditLogLevel.Level.BASIC)
@RequestMapping("/api/user/list")
public List<User> getUserList() {
    return userService.list();
}

// 使用最小级别记录（仅记录基本信息）
@AuditLogLevel(AuditLogLevel.Level.MINIMAL)
@RequestMapping("/api/log/list")
public List<Log> getLogList() {
    return logService.list();
}

// 自定义模块名称和描述
@AuditLogLevel(module = "用户管理", description = "获取用户信息详情")
@RequestMapping("/api/user/{id}")
public User getUserInfo(@PathVariable Long id) {
    return userService.getById(id);
}

// 在类级别设置默认级别
@RestController
@RequestMapping("/api/user")
@AuditLogLevel(AuditLogLevel.Level.BASIC)
public class UserController {
    // 类方法...
}

// 忽略整个控制器的审计日志
@RestController
@RequestMapping("/api/public")
@AuditLogLevel(ignore = true)
public class PublicController {
    // 这个类中的所有方法默认都不会记录审计日志
}
```

审计日志级别说明：
- **FULL**（默认）：记录所有信息，包括请求参数、返回结果、耗时等
- **BASIC**：记录基本信息，不包括详细的请求参数
- **MINIMAL**：仅记录操作时间、用户、模块等最基本信息
- **ignore = true**：完全忽略该方法或类的审计日志记录

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

#### 4.3.1 线程池配置

系统使用专用的线程池处理审计日志，配置位于`AsyncLogConfig`类中：

```java
@Configuration
public class AsyncLogConfig {
    @Bean("auditLogExecutor")
    public Executor auditLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(5);
        // 设置最大线程数
        executor.setMaxPoolSize(10);
        // 设置队列容量
        executor.setQueueCapacity(500);
        // 设置线程名称前缀
        executor.setThreadNamePrefix("audit-log-");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

可以根据系统负载和并发情况调整线程池参数。

#### 4.3.2 缓冲池配置

系统使用内存缓冲池批量写入审计日志，配置位于`AuditLogBufferManager`类中：

```java
@Component
public class AuditLogBufferManager {
    // 缓冲队列容量
    private final BlockingQueue<AuditLog> bufferQueue = new LinkedBlockingQueue<>(10000);
    
    // 批量写入的阈值
    private final int batchSize = 50;
    
    // 定时写入的时间间隔（毫秒）
    private final long flushInterval = 10000;
    
    // ... 其他代码
}
```

可以根据系统日志量和性能需求调整缓冲池参数。

#### 4.3.3 日志配置

在 `logback-spring.xml` 中已配置了审计日志的处理方式。可以根据需要调整：

- 审计日志会同时写入数据库和文件日志

## 5. 性能优化说明

本审计日志系统采用了多种性能优化策略，确保在高并发场景下也能高效运行，同时兼顾应用启动性能：

### 5.1 异步处理

- 使用专用的线程池处理审计日志，避免阻塞主业务线程
- 合理配置线程池参数，确保足够的处理能力

### 5.2 缓冲池和批量写入

- 使用内存缓冲池存储审计日志，减少数据库I/O次数
- 当缓冲池达到阈值时批量写入数据库
- 定时任务定期刷新缓冲池，确保日志及时持久化
- 系统关闭时自动刷新所有缓冲日志

### 5.3 审计级别控制

- 支持多级审计日志级别，可根据接口重要性灵活配置
- 对于高频低价值接口，可以使用较低级别或完全忽略审计
- 对于重要业务接口，可以使用完整级别记录详细信息

### 5.4 异常处理

- 审计日志处理异常不影响主业务流程
- 当缓冲池满时，自动降级为直接写入数据库
- 批量写入失败时，自动重试机制确保日志不丢失

### 5.5 启动性能优化

- 采用延迟初始化（@Lazy）策略，审计日志相关组件仅在首次使用时初始化
- 优化Bean依赖关系，避免启动时不必要的初始化
- 将AuditLogAppender从自动组件改为通过配置类注册的懒加载方式
- 审计日志缓冲管理器实现延迟初始化
- 与Spring Boot全局懒加载配置协同工作，提升整体应用启动速度

## 6. 最佳实践

### 6.1 接口级别控制

- 对于公共接口（如健康检查、状态查询等），建议使用`@AuditLogLevel(ignore = true)`完全忽略
- 对于高频查询接口，建议使用`@AuditLogLevel(AuditLogLevel.Level.BASIC)`减少存储开销
- 对于业务操作接口（如增删改），建议使用默认的`FULL`级别
- 对于敏感操作接口，可以通过自定义`module`和`description`提供更清晰的审计信息

### 6.2 性能监控

- 定期监控审计日志表的增长情况，及时清理过期数据
- 监控缓冲池状态，调整`batchSize`和`flushInterval`参数
- 根据系统负载情况，适时调整线程池大小

### 6.3 系统调优

- 对于日志量特别大的系统，考虑使用分表分库策略
- 定期归档历史审计日志，保持活跃表的性能
- 为常用查询字段创建合适的索引
- 与JVM参数优化配合，如使用G1GC垃圾收集器、启用字符串去重等
- 在应用启动配置中启用Spring Boot的并行Bean初始化机制

## 7. 注意事项

- 审计日志记录可能会对系统性能产生一定影响，建议根据实际需求合理配置
- 对于包含敏感信息的接口，需要确保审计日志不会泄露敏感数据
- 在分布式系统中，需要确保用户身份信息在各服务间正确传递
- 当系统重启时，缓冲池中的日志会在关闭前自动刷新到数据库
- 可以修改日志级别和输出格式
- 延迟初始化策略可能导致首次访问审计日志功能时略有延迟
- 确保必要的依赖关系正确配置，避免延迟初始化导致的潜在问题
- 对于高可用性要求极高的场景，需要评估延迟初始化对系统恢复时间的影响

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
      shutdown:
        await-termination: true
        await-termination-period: 60s
```

可以根据系统负载情况调整这些参数。其中：
- `shutdown.await-termination: true` 启用线程池优雅关闭
- `shutdown.await-termination-period: 60s` 设置关闭等待时间，确保在应用关闭时能完成剩余的审计日志任务

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