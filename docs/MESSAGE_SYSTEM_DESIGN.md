# 系统通知消息推送系统设计方案

## 项目概述

本文档详细设计了一个基于Spring Boot的企业级系统通知推送系统，专注于系统与用户之间的实时信息交互，支持系统通知、消息模板、用户偏好设置等功能，提供完善的消息管理、状态追踪和通知推送机制。

**核心特点**：
- 系统主动推送消息给用户
- 支持多种通知类型（系统公告、业务通知、紧急通知等）
- 用户消息中心统一管理
- 多种推送渠道支持

## 1. 系统架构设计

### 1.1 整体架构

![系统通知推送系统架构图](images/simple_architecture_diagram.svg)

**架构说明：**

- **前端层**：支持Web前端、移动端APP和管理后台多端访问
- **网关层**：API Gateway统一入口，提供路由转发、负载均衡、限流熔断功能  
- **服务层**：通知服务、消息服务和模板服务独立部署，职责清晰
- **业务层**：包含通知处理器、消息推送器和通知推送器等核心组件
- **数据层**：数据访问层、缓存层和消息队列协同工作，MySQL作为主数据库

### 1.2 技术栈选型

- **基础框架**: Spring Boot 2.7.x
- **消息队列**: RabbitMQ/Redis (用于异步消息处理)
- **缓存**: Redis (消息状态缓存、会话缓存)
- **数据库**: MySQL (主数据存储) + Elasticsearch (消息搜索)
- **实时通信**: WebSocket (在线消息推送)
- **文档存储**: MongoDB (富文本消息)
- **API文档**: Knife4j
- **监控**: Spring Boot Actuator

## 2. 数据库设计

### 2.1 核心表结构

#### 2.1.1 系统通知表 (system_notification)

```sql
CREATE TABLE system_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    notification_code VARCHAR(100) NOT NULL COMMENT '通知编码',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    content_json JSON DEFAULT NULL COMMENT '富文本内容(JSON格式)',
    notification_type TINYINT NOT NULL COMMENT '通知类型：1-系统公告，2-业务通知，3-紧急通知，4-个人消息，5-任务提醒',
    priority TINYINT DEFAULT 1 COMMENT '优先级：1-普通，2-重要，3-紧急',
    sender_type TINYINT DEFAULT 1 COMMENT '发送者类型：1-系统，2-管理员',
    sender_id BIGINT DEFAULT NULL COMMENT '发送者ID',
    target_type TINYINT NOT NULL COMMENT '目标类型：1-所有用户，2-指定用户，3-指定角色，4-指定部门',
    target_value JSON DEFAULT NULL COMMENT '目标值(JSON格式，包含用户ID列表、角色列表等)',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-待发送，2-已发送，3-已取消，4-已过期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_notification_type (notification_type),
    INDEX idx_priority (priority),
    INDEX idx_send_time (send_time),
    INDEX idx_status (status),
    INDEX idx_expire_time (expire_time),
    UNIQUE KEY uk_notification_code (notification_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';
```

#### 2.1.2 用户通知消息表 (user_notification_message)

```sql
CREATE TABLE user_notification_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    notification_id BIGINT NOT NULL COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    message_type TINYINT NOT NULL COMMENT '消息类型：1-文本，2-图片，3-文件，4-链接',
    content TEXT COMMENT '消息内容',
    content_json JSON DEFAULT NULL COMMENT '富文本内容(JSON格式)',
    media_url VARCHAR(500) DEFAULT NULL COMMENT '媒体文件URL',
    media_size BIGINT DEFAULT NULL COMMENT '媒体文件大小',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-未读，2-已读，3-已删除',
    read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
    click_count INT DEFAULT 0 COMMENT '点击次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_notification (notification_id),
    INDEX idx_user (user_id),
    INDEX idx_send_time (send_time),
    INDEX idx_status (status),
    FOREIGN KEY (notification_id) REFERENCES system_notification(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知消息表';
```

#### 2.1.3 通知推送日志表 (notification_push_log)

```sql
CREATE TABLE notification_push_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '推送日志ID',
    notification_id BIGINT NOT NULL COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    push_type TINYINT NOT NULL COMMENT '推送类型：1-站内信，2-邮件，3-SMS，4-推送通知',
    push_status TINYINT DEFAULT 1 COMMENT '推送状态：1-待发送，2-发送成功，3-发送失败',
    push_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '推送时间',
    result_message VARCHAR(500) DEFAULT NULL COMMENT '推送结果消息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_notification (notification_id),
    INDEX idx_user (user_id),
    INDEX idx_push_type (push_type),
    INDEX idx_push_status (push_status),
    INDEX idx_push_time (push_time),
    FOREIGN KEY (notification_id) REFERENCES system_notification(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知推送日志表';
```

#### 2.1.5 消息模板表 (message_template)

```sql
CREATE TABLE message_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_code VARCHAR(100) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_type TINYINT NOT NULL COMMENT '模板类型：1-系统通知，2-业务通知，3-营销消息',
    content TEXT NOT NULL COMMENT '模板内容',
    variables JSON DEFAULT NULL COMMENT '模板变量定义',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_template_code (template_code),
    INDEX idx_template_type (template_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';
```

#### 2.1.6 用户通知设置表 (user_notification_setting)

```sql
CREATE TABLE user_notification_setting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    notification_type TINYINT NOT NULL COMMENT '通知类型：1-系统通知，2-私信，3-群聊，4-邮件，5-SMS',
    is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    sound_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用声音',
    vibration_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用震动',
    quiet_hours_start TIME DEFAULT NULL COMMENT '免打扰开始时间',
    quiet_hours_end TIME DEFAULT NULL COMMENT '免打扰结束时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_type (user_id, notification_type),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知设置表';
```

### 2.2 索引优化策略

1. **复合索引**: 为常用查询场景创建复合索引
2. **分区表**: 对大表(消息表)按时间进行分区
3. **读写分离**: 主从复制，读写分离提升性能
4. **缓存策略**: Redis缓存热点数据

## 3. 功能模块设计

### 3.1 系统通知管理

#### 3.1.1 通知创建和管理
- 系统公告：面向所有用户的公共通知
- 业务通知：基于业务流程的专项通知
- 紧急通知：高优先级紧急消息推送
- 个人消息：针对特定用户的个性化通知
- 任务提醒：系统任务到期提醒

#### 3.1.2 通知发送策略
- **定时发送**: 支持定时发布通知
- **立即发送**: 紧急通知即时推送
- **分组发送**: 按用户角色、部门分组推送
- **条件发送**: 基于用户属性条件推送

### 3.2 消息推送机制

#### 3.2.1 推送类型支持
```java
public enum NotificationType {
    SYSTEM_ANNOUNCEMENT(1, "系统公告"),
    BUSINESS_NOTIFICATION(2, "业务通知"),
    URGENT_NOTICE(3, "紧急通知"),
    PERSONAL_MESSAGE(4, "个人消息"),
    TASK_REMINDER(5, "任务提醒");
}
```

#### 3.2.2 推送渠道
- **站内信**: 系统内消息推送
- **邮件通知**: 邮件渠道消息推送
- **短信通知**: 短信渠道重要通知
- **APP推送**: 移动端推送通知

### 3.3 用户消息管理

#### 3.3.1 消息状态管理
```
未读 → 已读 → 已删除
```

#### 3.3.2 消息中心功能
- 消息列表展示和筛选
- 批量操作（批量标记已读、删除）
- 消息搜索和历史记录
- 消息统计和分析

#### 3.3.3 未读消息统计
- 基于Redis实现高性能计数
- 按通知类型分类统计
- 实时更新和异步同步
- 支持批量查询优化

### 3.4 消息模板系统

#### 3.4.1 模板变量支持
```java
public class MessageTemplate {
    private String templateCode;
    private String content;
    private Map<String, TemplateVariable> variables;
    private List<NotificationChannel> channels;
}
```

#### 3.4.2 模板渲染引擎
- 支持条件渲染
- 多语言模板支持
- 模板版本管理

## 4. API接口设计

### 4.1 系统通知管理API

#### 4.1.1 创建系统通知
```http
POST /api/notifications
Content-Type: application/json

{
    "notificationCode": "SYS_001",
    "title": "系统维护通知",
    "content": "系统将在今晚23:00-01:00进行维护升级",
    "notificationType": 1,  // 1-系统公告
    "priority": 2,          // 1-普通，2-重要，3-紧急
    "targetType": 1,        // 1-所有用户，2-指定用户，3-指定角色，4-指定部门
    "targetValue": null,    // 目标值JSON
    "expireTime": "2025-11-25 23:59:59"
}
```

#### 4.1.2 获取通知列表
```http
GET /api/notifications?page=1&size=20&type=1&status=2
```

Response:
```json
{
    "code": 200,
    "data": {
        "notifications": [
            {
                "id": 1,
                "notificationCode": "SYS_001",
                "title": "系统维护通知",
                "notificationType": 1,
                "priority": 2,
                "status": 2,
                "sendTime": "2025-11-19 10:30:00",
                "expireTime": "2025-11-25 23:59:59",
                "readCount": 1200,
                "totalCount": 2000
            }
        ],
        "total": 100,
        "current": 1
    }
}
```

### 4.2 用户消息中心API

#### 4.2.1 获取用户消息列表
```http
GET /api/user-messages?page=1&size=20&status=1&type=1
```

#### 4.2.2 标记消息已读
```http
PUT /api/user-messages/{messageId}/read
Content-Type: application/json

{
    "messageId": 1001
}
```

#### 4.2.3 批量标记已读
```http
PUT /api/user-messages/batch-read
Content-Type: application/json

{
    "messageIds": [1001, 1002, 1003]
}
```

#### 4.2.4 删除消息
```http
DELETE /api/user-messages/{messageId}
```

#### 4.2.5 获取未读消息统计
```http
GET /api/user-messages/unread-count
```

Response:
```json
{
    "code": 200,
    "data": {
        "totalUnread": 25,
        "typeStats": {
            "1": 10,  // 系统公告未读数量
            "2": 8,   // 业务通知未读数量
            "3": 3,   // 紧急通知未读数量
            "4": 4    // 个人消息未读数量
        }
    }
}
```

### 4.3 消息推送API

#### 4.3.1 手动触发推送
```http
POST /api/notifications/{notificationId}/push
Content-Type: application/json

{
    "pushTypes": [1, 2, 3],  // 推送渠道：1-站内信，2-邮件，3-SMS
    "userIds": [1001, 1002, 1003]  // 指定用户ID列表
}
```

#### 4.3.2 获取推送日志
```http
GET /api/push-logs/{notificationId}?page=1&size=20&status=2
```

### 4.4 模板管理API

#### 4.4.1 获取模板列表
```http
GET /api/message-templates?type=1&page=1&size=20
```

#### 4.4.2 发送模板消息
```http
POST /api/message-templates/{templateId}/send
Content-Type: application/json

{
    "userIds": [1001, 1002, 1003],
    "variables": {
        "userName": "张三",
        "orderId": "ORD123456"
    }
}
```

## 5. 核心技术实现

### 5.1 实时消息推送

#### 5.1.1 WebSocket配置
```java
@Configuration
@EnableWebSocket
public class NotificationWebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationWebSocketHandler(), "/ws/notification")
                .setAllowedOrigins("*");
    }
    
    @Bean
    public TextWebSocketHandler notificationWebSocketHandler() {
        return new NotificationWebSocketHandler();
    }
}
```

#### 5.1.2 通知推送策略
- **在线用户**: WebSocket实时推送
- **离线用户**: 消息入队，多渠道通知(站内信、邮件、短信)
- **紧急通知**: 强制推送，确保用户及时收到

### 5.2 消息队列处理

#### 5.2.1 异步通知处理
```java
@Service
public class NotificationService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void sendNotificationAsync(SystemNotification notification) {
        // 发送到通知队列异步处理
        rabbitTemplate.convertAndSend("notification.exchange", "notification.send", notification);
    }
    
    @RabbitListener(queues = "notification.send.queue")
    public void handleNotificationSend(SystemNotification notification) {
        // 通知发送处理逻辑
        processNotificationDelivery(notification);
    }
}
```

### 5.3 缓存策略

#### 5.3.1 Redis缓存设计
```java
@Service
public class NotificationCacheService {
    
    // 未读通知计数缓存
    private static final String UNREAD_COUNT_KEY = "unread:notification:user:";
    
    // 用户通知列表缓存
    private static final String NOTIFICATION_LIST_KEY = "notifications:user:";
    
    // 通知详情缓存
    private static final String NOTIFICATION_DETAIL_KEY = "notification:detail:";
    
    public void incrementUnreadCount(Long userId) {
        redisTemplate.opsForValue().increment(UNREAD_COUNT_KEY + userId);
    }
    
    public void cacheUserNotifications(Long userId, List<UserNotificationMessage> messages) {
        redisTemplate.opsForValue().set(
            NOTIFICATION_LIST_KEY + userId, 
            messages, 
            Duration.ofMinutes(30)
        );
    }
}
```

### 5.4 权限控制

#### 5.4.1 通知访问权限
```java
@Component
public class NotificationSecurityService {
    
    public boolean canCreateNotification(Long userId, SystemNotification notification) {
        // 检查用户是否有创建通知的权限
        return userService.hasPermission(userId, "notification:create");
    }
    
    public boolean canReadNotification(Long userId, Long notificationId) {
        // 检查用户是否可以读取通知
        return notificationService.isTargetUser(notificationId, userId) ||
               userService.hasPermission(userId, "notification:view:all");
    }
}
```

## 6. 性能优化方案

### 6.1 数据库优化

1. **分库分表策略**
   - 按用户ID分表：user_messages_[hash]
   - 按时间分表：messages_2025_11
   - 历史数据归档

2. **查询优化**
   - 合理使用索引
   - 避免N+1查询
   - 分页查询优化

### 6.2 缓存优化

1. **多级缓存**
   - 本地缓存(Caffeine)：热点通知
   - Redis缓存：用户消息、未读计数
   - 数据库：持久化存储

2. **缓存策略**
   - 通知详情：LRU策略
   - 用户消息列表：TTL策略
   - 未读计数：实时更新

### 6.3 通知性能优化

1. **批量处理**
   - 通知批量发送
   - 状态批量更新
   - 数据库批量操作

2. **异步处理**
   - 通知入库异步
   - 多渠道推送异步
   - 统计计算异步

## 7. 安全和监控

### 7.1 消息安全

1. **内容安全**
   - 敏感词过滤
   - XSS攻击防护
   - 文件上传安全

2. **传输安全**
   - HTTPS加密传输
   - WebSocket安全认证
   - 消息签名验证

### 7.2 监控和告警

1. **性能监控**
   - 消息发送成功率
   - 消息延迟监控
   - 系统资源监控

2. **业务监控**
   - 用户活跃度统计
   - 消息量统计
   - 异常消息监控

## 8. 扩展性设计

### 8.1 多端同步

1. **通知同步机制**
   - 增量同步
   - 全量同步
   - 冲突解决

2. **离线通知支持**
   - 离线通知存储
   - 通知推送策略
   - 通知去重机制

### 8.2 第三方集成

1. **通知集成**
   - 邮件通知服务
   - 短信通知服务
   - 推送通知服务

2. **外部系统集成**
   - 单点登录(SSO)
   - 第三方登录
   - API开放平台

## 9. 部署和运维

### 9.1 部署架构

1. **微服务架构**
   - 消息服务独立部署
   - 通知服务独立部署
   - API网关统一入口

2. **高可用设计**
   - 集群部署
   - 负载均衡
   - 服务降级

### 9.2 运维管理

1. **配置管理**
   - 动态配置更新
   - 配置版本管理
   - 配置回滚机制

2. **日志管理**
   - 统一日志收集
   - 日志分级管理
   - 日志分析平台

## 10. 开发计划

### 10.1 开发阶段

**第一阶段：核心功能(2周)**
- 数据库设计和实现
- 基础CRUD功能开发
- 系统通知创建和发送机制

**第二阶段：高级功能(2周)**
- WebSocket实时通信
- 用户消息中心管理
- 多渠道通知推送机制

**第三阶段：优化和测试(1周)**
- 性能优化
- 安全性测试
- 用户体验优化

### 10.2 测试策略

1. **单元测试**
   - 业务逻辑测试
   - 工具类测试
   - 配置类测试

2. **集成测试**
   - API接口测试
   - 数据库操作测试
   - 消息队列测试

3. **性能测试**
   - 并发通知推送测试
   - 压力测试
   - 稳定性测试

---

**方案特点总结：**

✅ **专注系统通知**: 专为系统与用户间的信息交互设计，功能聚焦
✅ **高性能设计**: 多级缓存、异步处理、数据库优化
✅ **实时推送**: WebSocket + 消息队列实现实时通知推送
✅ **多渠道支持**: 站内信、邮件、短信等多渠道通知
✅ **灵活配置**: 支持定时推送、分组推送、条件推送
✅ **安全可靠**: 权限控制、内容安全、传输加密
✅ **易于维护**: 完善的文档、监控告警、运维支持

请查看以上方案设计，如有需要调整或补充的地方，请告知我，我会根据您的反馈完善方案。确认后我们可以开始具体的编码开发工作。