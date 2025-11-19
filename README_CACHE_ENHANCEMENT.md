# 本地缓存增强功能 - Redis降级机制

## 项目概述

本项目为bing-framework增加了高可用的本地缓存功能，实现了当Redis连接失败或未配置时的自动降级机制。通过自研的本地缓存系统，确保在任何情况下缓存服务都能正常工作，提高系统的稳定性和可用性。

## 主要特性

### 🚀 核心功能

- **自动降级机制**: 当Redis连续失败3次后自动切换到本地缓存
- **自动恢复**: 每30秒检查Redis状态，条件满足时自动恢复
- **手动切换**: 支持通过API或管理端点手动切换缓存源
- **无缝切换**: 应用程序无需修改现有代码即可享受降级保护

### 🔧 技术特性

- **线程安全**: 基于ConcurrentHashMap实现，确保多线程环境下的数据一致性
- **容量管理**: 支持LRU淘汰策略，防止内存溢出
- **过期管理**: 支持TTL过期时间，自动清理过期数据
- **性能优化**: 定期维护清理，优化内存使用
- **统计监控**: 提供详细的操作统计和性能指标

### 🛡️ 高可用特性

- **连接检查**: 启动时检查Redis连接状态，定期监控连接健康
- **异常隔离**: Redis异常不会影响应用程序正常运行
- **数据一致性**: 恢复后自动清空本地缓存保证一致性
- **分布式锁**: 支持Redis和本地缓存的分布式锁实现

## 项目结构

```
src/main/java/com/bing/framework/cache/
├── MemoryCache.java              # 本地缓存实现类
├── UnifiedCacheManager.java      # 统一缓存管理器
├── CacheService.java            # 高可用缓存服务
└── UnifiedCacheManager.java     # 缓存管理器接口

src/main/java/com/bing/framework/config/
└── CacheConfig.java             # 缓存配置类（已增强）

src/test/java/com/bing/framework/cache/
├── MemoryCacheTest.java         # 本地缓存单元测试
├── CacheServiceTest.java        # 缓存服务测试
└── CacheIntegrationTest.java    # 集成测试

docs/
└── CACHE_CONFIG_EXAMPLE.yml     # 配置示例和说明文档
```

## 快速开始

### 1. 配置依赖

确保项目中已包含Spring Boot Redis依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 2. 配置文件

根据`docs/CACHE_CONFIG_EXAMPLE.yml`配置缓存参数：

```yaml
spring:
  cache:
    type: redis
    redis:
      enabled: true          # Redis启用状态
      host: localhost        # Redis服务器地址
      port: 6379            # Redis端口
      time-to-live: 3600000 # 缓存过期时间（毫秒）
    local:
      max-size: 1000        # 本地缓存最大容量
      clean-interval: 300   # 清理间隔（秒）
      default-ttl: 1800     # 默认过期时间（秒）
```

### 3. 使用缓存服务

#### 基础用法

```java
@Autowired
private CacheService cacheService;

// 存储数据
cacheService.set("user:123", userData, 300); // 5分钟过期

// 获取数据
User user = (User) cacheService.get("user:123");

// 删除数据
cacheService.delete("user:123");

// 检查键是否存在
boolean exists = cacheService.hasKey("user:123");

// 获取过期时间
Long expireTime = cacheService.getExpire("user:123");
```

#### 计数器功能

```java
// 原子递增
Long counter = cacheService.increment("visit:count");
Long counterWithStep = cacheService.increment("visit:count", 5);

// 原子递减
Long decremented = cacheService.decrement("request:count");
Long decrementedWithStep = cacheService.decrement("request:count", 2);
```

#### 分布式锁

```java
String lockKey = "distributed:lock:user:123";
boolean lockAcquired = cacheService.tryLock(lockKey, 30, TimeUnit.SECONDS);

if (lockAcquired) {
    try {
        // 执行需要同步的操作
        performCriticalOperation();
    } finally {
        cacheService.releaseLock(lockKey);
    }
}
```

#### 手动缓存切换

```java
// 手动切换到Redis
cacheService.switchToRedis();

// 手动切换到本地缓存
cacheService.switchToLocal();

// 检查当前缓存源
boolean isRedisAvailable = cacheService.isRedisAvailable();
```

### 4. Spring Cache注解支持

```java
@Service
public class UserService {
    
    @Cacheable(value = "userCache", key = "'user:' + #userId")
    public User getUserById(String userId) {
        // 只有缓存未命中时才会执行
        return userRepository.findById(userId);
    }
    
    @CacheEvict(value = "userCache", key = "'user:' + #userId")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
    
    @CachePut(value = "userCache", key = "'user:' + #user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
```

## 降级机制说明

### 自动降级流程

1. **监控检测**: 实时监控Redis连接状态和操作失败率
2. **阈值判断**: 连续失败3次（可配置）后触发降级
3. **自动切换**: 切换到本地缓存模式，保证服务可用性
4. **定期检查**: 每30秒检查一次Redis状态
5. **自动恢复**: Redis恢复正常后自动切换回Redis模式

### 降级状态管理

- **日志记录**: 详细记录降级和恢复过程
- **状态监控**: 提供缓存源状态查询接口
- **告警支持**: 可集成到监控系统进行告警

### 数据一致性保证

- **写策略**: 降级期间数据仅写入本地缓存
- **读策略**: 恢复时优先使用Redis数据
- **清理机制**: 恢复后自动清空本地缓存避免脏数据

## 性能优化

### 本地缓存优化

- **内存管理**: 基于ConcurrentHashMap的线程安全实现
- **LRU淘汰**: 智能淘汰策略，优化内存使用
- **并发控制**: 支持高并发读写操作
- **定期维护**: 自动清理过期数据，保持缓存效率

### 配置调优建议

```yaml
# 开发环境
local:
  max-size: 500           # 较小容量
  clean-interval: 60      # 较短清理间隔
  default-ttl: 300        # 较短过期时间

# 生产环境
local:
  max-size: 10000         # 较大容量
  clean-interval: 600     # 中等清理间隔
  default-ttl: 1800       # 中等过期时间
```

## 监控和统计

### 统计信息获取

```java
CacheService.CacheStats stats = cacheService.getStats();
System.out.println(stats);
// 输出示例:
// CacheStats{totalOperations=1000, localCacheOperations=150, 
// redisOperations=850, hitRate=85.5%, size=234, ...}
```

### 统计指标说明

- `totalOperations`: 总操作数
- `localCacheOperations`: 本地缓存操作数
- `redisOperations`: Redis操作数
- `hitRate`: 缓存命中率
- `currentCacheSource`: 当前缓存源（REDIS/LOCAL）
- `consecutiveFailures`: 连续失败次数
- `size`: 当前缓存大小

### 管理端点

访问Spring Boot Actuator端点获取健康状态：

```bash
# 健康检查
GET /actuator/health

# 缓存统计
GET /actuator/metrics/cache.operations

# 详细统计（自定义端点）
GET /cache/stats
```

## 测试验证

### 单元测试

```bash
# 运行MemoryCache测试
mvn test -Dtest=MemoryCacheTest

# 运行CacheService测试
mvn test -Dtest=CacheServiceTest

# 运行集成测试
mvn test -Dtest=CacheIntegrationTest
```

### 测试覆盖场景

- ✅ 基本缓存操作（set/get/delete）
- ✅ 过期时间管理
- ✅ 容量限制和LRU淘汰
- ✅ 并发访问安全性
- ✅ 降级和恢复机制
- ✅ 分布式锁功能
- ✅ 错误处理和异常恢复
- ✅ 统计信息准确性

## 部署和运维

### 环境要求

- Java 8+
- Spring Boot 2.5+
- Redis 3.0+（可选）
- Maven 3.6+

### 部署检查清单

- [ ] 验证Redis连接配置
- [ ] 配置本地缓存参数
- [ ] 调整日志级别（建议INFO级别）
- [ ] 配置监控和告警
- [ ] 执行性能测试
- [ ] 验证降级机制

### 常见问题排查

#### Redis连接失败
```bash
# 检查Redis服务状态
redis-cli ping

# 检查网络连接
telnet redis-host 6379
```

#### 内存使用过高
```yaml
# 调优本地缓存配置
local:
  max-size: 5000        # 减少容量
  clean-interval: 300   # 增加清理频率
  default-ttl: 900      # 减少过期时间
```

#### 性能问题
```yaml
# 优化配置
local:
  concurrency-level: 16  # 调整并发级别
  eviction-policy: LRU   # 使用LRU策略
```

## 安全考虑

- **数据加密**: 敏感数据建议在应用层加密后存储
- **访问控制**: 限制缓存操作的访问权限
- **监控告警**: 设置异常访问模式告警
- **定期清理**: 定期清理敏感数据的缓存

## 版本历史

### v1.0.0 (2024-XX-XX)
- ✨ 初始版本发布
- ✅ 实现本地缓存核心功能
- ✅ 添加Redis降级机制
- ✅ 完成单元测试和集成测试
- ✅ 提供完整配置示例

### 计划功能
- 🔄 缓存预热机制
- 📊 性能监控面板
- 🔍 缓存数据分析
- 🚀 异步缓存更新
- 📈 智能缓存策略

## 贡献指南

欢迎提交Issue和Pull Request！

### 开发环境设置

1. 克隆项目到本地
2. 配置Redis（开发环境可选）
3. 运行测试确保功能正常
4. 提交代码并创建Pull Request

### 代码规范

- 遵循阿里巴巴Java开发手册
- 添加完整的单元测试
- 更新相关文档
- 确保向后兼容性

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 支持和联系

- 📧 技术支持: [联系邮箱]
- 📝 问题反馈: [GitHub Issues]
- 📚 文档更新: [Wiki页面]

---

**注意**: 本本地缓存方案适用于中小型应用场景。对于大规模分布式系统，建议结合专业的缓存中间件（如Redis Cluster、Caffeine等）来获得更好的性能和可靠性。