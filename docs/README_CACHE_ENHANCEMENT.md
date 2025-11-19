# 缓存增强功能文档

---

## 文档概述

本项目提供了完整的缓存增强功能，包括本地缓存、Redis缓存、双层缓存架构、自动降级机制等功能。本文档详细介绍了这些功能的使用方法、配置选项和最佳实践。

---

## 功能特性

### 1. 双层缓存架构

![缓存架构图](images/cache_architecture.svg)

采用 Redis + 本地缓存的双层缓存架构：
- **第一层（Redis）**: 分布式缓存，支持集群部署，容量大
- **第二层（本地缓存）**: 进程内缓存，响应速度快，可作为降级方案

### 2. 自动降级机制

![缓存降级策略图](images/cache_fallback_strategy.svg)

当Redis不可用时，系统会自动切换到本地缓存，确保服务可用性：
- 实时监控Redis连接状态
- 连续失败达到阈值时触发降级
- 支持自动恢复和手动切换

### 3. 缓存统计和监控

- 缓存命中率统计
- 响应时间监控
- 失败率统计
- 支持Prometheus指标导出

### 4. 分布式锁支持

基于Redis实现的分布式锁，确保并发场景下的数据一致性：
- 支持锁等待超时设置
- 支持锁自动过期
- 支持手动释放锁

### 5. 批量操作优化

- 批量设置缓存
- 批量获取缓存
- 批量删除缓存
- 提高操作效率

---

## 系统架构

### 架构组件

![系统架构图](images/cache_architecture.svg)

系统主要包含以下组件：

1. **应用层**
   - 业务代码使用缓存注解
   - 通过CacheService API直接操作缓存

2. **统一缓存管理器**
   - 负责缓存策略选择
   - 处理降级逻辑
   - 统计信息收集

3. **Redis缓存层**
   - 分布式缓存存储
   - 支持集群部署
   - 高可用配置

4. **本地缓存层**
   - 进程内缓存
   - Caffeine实现
   - LRU淘汰策略

5. **数据源层**
   - 原始数据存储
   - 数据库访问
   - 外部服务调用

6. **监控层**
   - 健康检查
   - 性能监控
   - 指标收集

### 缓存工作流程

![缓存工作流程图](images/cache_workflow.svg)

### 降级策略流程

![降级策略流程图](images/cache_fallback_strategy.svg)

---

## 性能分析

### 性能对比

![缓存性能对比图](images/cache_performance_comparison.svg)

| 指标项 | 数据库查询 | Redis缓存 | 本地缓存 |
|--------|------------|-----------|----------|
| **响应时间** | ~100-500ms | ~1-10ms | ~0.1-1ms |
| **内存使用** | 磁盘存储 | 内存+磁盘 | 内存 |
| **命中率** | 100% | 80-95% | 85-95% |
| **TPS** | 100-1000 | 5000-20000 | 50000+ |
| **并发能力** | 低 | 高 | 极高 |

### 适用场景

| 缓存类型 | 适用场景 | 不适用场景 |
|----------|----------|------------|
| **本地缓存** | 热点数据访问<br/>高频查询<br/>配置信息 | 跨实例共享数据<br/>数据一致性要求高 |
| **Redis缓存** | 分布式系统<br/>会话存储<br/>排行榜 | 极致性能要求<br/>简单的单实例应用 |
| **双层缓存** | 大部分业务场景<br/>高并发系统 | 数据实时性要求极高<br/>简单的小型应用 |

---

## 快速开始

### 1. 添加依赖

```xml
<!-- Maven -->
<dependency>
    <groupId>com.bing.framework</groupId>
    <artifactId>bing-cache</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Gradle -->
implementation 'com.bing.framework:bing-cache:1.0.0'
```

### 2. 启用缓存

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3. 配置Redis

```yaml
spring:
  cache:
    type: redis
    redis:
      host: localhost
      port: 6379
      timeout: 5000
      time-to-live: 3600000

app:
  cache:
    fallback:
      max-consecutive-failures: 3
      check-interval: 30000
```

### 4. 使用缓存

```java
@Service
public class UserService {
    
    @Cacheable(key = "#id")
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
    
    @CachePut(key = "#user.id")
    public User updateUser(User user) {
        return userMapper.updateById(user);
    }
    
    @CacheEvict(key = "#id")
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
}
```

---

## 配置详解

### 完整配置结构

![缓存配置结构图](images/cache_config_structure.svg)

详细的配置选项请参考 [CACHE_CONFIG_EXAMPLE.yml](CACHE_CONFIG_EXAMPLE.yml) 文件。

### 关键配置项说明

| 配置项 | 说明 | 默认值 | 推荐值 |
|--------|------|--------|--------|
| `spring.cache.redis.time-to-live` | Redis缓存默认过期时间 | 1800000ms | 3600000ms |
| `app.cache.local.max-size` | 本地缓存最大容量 | 1000 | 10000 |
| `app.cache.fallback.max-consecutive-failures` | 触发降级的连续失败次数 | 3 | 2-5 |
| `app.cache.fallback.check-interval` | 健康检查间隔 | 30000ms | 15000-60000ms |

---

## 使用指南

### 1. 注解式使用

最简单的方式是通过Spring Cache注解使用缓存：

```java
@Service
@CacheConfig(cacheNames = "users")
public class UserService {
    
    @Cacheable(key = "#id", condition = "#id > 0")
    public User getUserById(Long id) {
        // 只有缓存未命中时才执行
        return userMapper.selectById(id);
    }
    
    @CachePut(key = "#user.id")
    public User updateUser(User user) {
        // 始终执行并更新缓存
        return userMapper.updateById(user);
    }
    
    @CacheEvict(key = "#id")
    public void deleteUser(Long id) {
        // 执行后清除缓存
        userMapper.deleteById(id);
    }
}
```

### 2. 编程式使用

通过注入CacheService直接操作缓存：

```java
@Service
public class CacheServiceImpl {
    
    @Autowired
    private CacheService cacheService;
    
    public void exampleUsage() {
        // 设置缓存
        cacheService.set("user:123", user, 3600);
        
        // 获取缓存
        User user = (User) cacheService.get("user:123");
        
        // 删除缓存
        cacheService.delete("user:123");
        
        // 分布式锁
        if (cacheService.tryLock("lock:key", 30, 60, TimeUnit.SECONDS)) {
            try {
                // 执行业务逻辑
                processBusinessLogic();
            } finally {
                cacheService.releaseLock("lock:key");
            }
        }
    }
}
```

### 3. 条件缓存

根据业务条件决定是否缓存：

```java
@Cacheable(
    value = "products",
    key = "#category + ':' + #page",
    condition = "#category != null and #page <= 10",
    unless = "#result == null or #result.isEmpty()"
)
public List<Product> getProducts(String category, int page) {
    return productService.findByCategory(category, page);
}
```

### 4. 缓存更新策略

```java
@Service
public class ProductService {
    
    @Cacheable(key = "'product:' + #id")
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }
    
    // 方式1：使用@CachePut
    @CachePut(key = "'product:' + #product.id")
    public Product updateProduct(Product product) {
        return productMapper.updateById(product);
    }
    
    // 方式2：手动更新缓存
    @Transactional
    public Product updateProductManual(Long id, ProductUpdateDto dto) {
        // 更新数据库
        Product updated = productMapper.updateById(dto);
        
        // 更新缓存
        cacheService.set("product:" + id, updated, 3600);
        
        return updated;
    }
}
```

---

## 高级功能

### 1. 缓存预热

在系统启动时预加载热点数据：

```java
@Component
public class CacheWarmup {
    
    @Autowired
    private CacheService cacheService;
    
    @PostConstruct
    public void warmupCache() {
        // 预热用户数据
        List<User> hotUsers = userService.getHotUsers();
        hotUsers.forEach(user -> {
            cacheService.set("user:" + user.getId(), user, 7200);
        });
        
        // 预热系统配置
        Map<String, Object> configs = loadSystemConfigs();
        configs.forEach((key, value) -> {
            cacheService.set("config:" + key, value, 3600);
        });
    }
}
```

### 2. 缓存监控

集成Prometheus监控：

```java
@Service
public class CacheMonitor {
    
    @Autowired
    private CacheService cacheService;
    
    @EventListener
    public void handleCacheEvent(CacheEvent event) {
        // 记录缓存操作事件
        log.info("Cache operation: {}, key: {}, result: {}", 
                 event.getOperation(), event.getKey(), event.getResult());
    }
    
    @Scheduled(fixedRate = 30000) // 每30秒执行
    public void reportCacheStats() {
        CacheStatistics stats = cacheService.getStatistics();
        
        log.info("Cache Stats - Hit Rate: {}, Success Count: {}, " +
                 "Failure Count: {}, Average Response Time: {}ms",
                 stats.getHitRate(), stats.getSuccessCount(), 
                 stats.getFailureCount(), stats.getAverageResponseTime());
    }
}
```

### 3. 缓存清理策略

```java
@Component
public class CacheCleanup {
    
    @Autowired
    private CacheService cacheService;
    
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupExpiredCache() {
        log.info("开始清理过期缓存...");
        
        // 清理过期的用户缓存
        cleanupCacheByPattern("user:*");
        
        // 清理过期的临时数据
        cleanupCacheByPattern("temp:*");
        
        log.info("缓存清理完成");
    }
    
    private void cleanupCacheByPattern(String pattern) {
        Set<String> keys = cacheService.keys(pattern);
        for (String key : keys) {
            Long expire = cacheService.getExpire(key);
            if (expire != null && expire <= 0) {
                cacheService.delete(key);
            }
        }
    }
}
```

### 4. 分布式锁示例

```java
@Service
public class DistributedLockExample {
    
    @Autowired
    private CacheService cacheService;
    
    /**
     * 使用分布式锁保证并发安全
     */
    public void executeWithLock(String businessId) {
        String lockKey = "business:lock:" + businessId;
        
        // 获取分布式锁（最多等待30秒，锁定60秒）
        boolean lockAcquired = cacheService.tryLock(lockKey, 30, 60, TimeUnit.SECONDS);
        
        if (!lockAcquired) {
            throw new RuntimeException("无法获取分布式锁，请稍后重试");
        }
        
        try {
            // 执行业务逻辑
            processBusiness(businessId);
            
        } finally {
            // 确保释放锁
            cacheService.releaseLock(lockKey);
        }
    }
    
    /**
     * 计数器示例：限制API调用频率
     */
    public boolean rateLimit(String userId, int maxRequests, int windowSeconds) {
        String counterKey = "rate:limit:" + userId;
        
        // 获取当前计数器值
        Long currentCount = cacheService.getCounter(counterKey);
        if (currentCount == null) {
            currentCount = 0L;
        }
        
        if (currentCount >= maxRequests) {
            return false; // 超出限制
        }
        
        // 原子递增
        cacheService.increment(counterKey);
        
        // 如果是新计数器，设置过期时间
        if (currentCount == 0) {
            cacheService.expire(counterKey, windowSeconds);
        }
        
        return true;
    }
}
```

---

## 最佳实践

### 1. 缓存键设计

- 使用有意义的命名规范：`业务:对象:ID`
- 避免过长的键名
- 使用合适的分隔符：`:` 或 `-`

```yaml
# 推荐
user:123
product:category:electronics
session:abc123def456

# 不推荐
u_123
prod_cat_elec
sess_abc123def456789012345678901234567890
```

### 2. 缓存值大小控制

- 单个缓存值建议不超过1MB
- 大量数据建议分页存储
- 考虑使用压缩

```java
// 不推荐：缓存大量数据
@Cacheable(key = "users:all")
public List<User> getAllUsers() {
    return userMapper.selectAll(); // 可能包含大量用户
}

// 推荐：分页缓存
@Cacheable(key = "users:page:" + #page)
public List<User> getUsersByPage(int page) {
    return userMapper.selectByPage(page, pageSize);
}
```

### 3. 过期时间策略

- 根据数据特性设置不同的TTL
- 热点数据设置较长TTL
- 配置数据设置适中TTL
- 临时数据设置较短TTL

```java
// 热点数据：2小时
@Cacheable(key = "'user:hot:' + #id", cacheManager = "hotCacheManager")

// 配置数据：30分钟  
@Cacheable(key = "'config:' + #key", cacheManager = "configCacheManager")

// 临时数据：5分钟
@Cacheable(key = "'temp:' + #id", cacheManager = "tempCacheManager")
```

### 4. 缓存穿透防护

```java
@Cacheable(
    key = "'product:' + #id",
    unless = "#result == null"
)
public Product getProductById(Long id) {
    // 如果商品不存在，返回null不会被缓存
    return productMapper.selectById(id);
}

// 结合空值缓存防止穿透
public Product getProductWithEmptyCache(Long id) {
    String cacheKey = "product:" + id;
    
    // 先检查是否存在空值标记
    Boolean hasEmptyValue = (Boolean) cacheService.get(cacheKey + ":empty");
    if (Boolean.TRUE.equals(hasEmptyValue)) {
        return null;
    }
    
    Product product = productMapper.selectById(id);
    
    if (product == null) {
        // 缓存空值标记，TTL较短
        cacheService.set(cacheKey + ":empty", true, 300);
        return null;
    }
    
    // 缓存正常数据
    cacheService.set(cacheKey, product, 3600);
    return product;
}
```

### 5. 缓存雪崩防护

```java
@Service
public class CacheAvalancheProtection {
    
    @Autowired
    private CacheService cacheService;
    
    /**
     * 使用分布式锁防止缓存雪崩
     */
    public Product getProductWithLock(Long id) {
        String cacheKey = "product:" + id;
        
        // 先从缓存获取
        Product cached = (Product) cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 获取分布式锁
        String lockKey = "lock:" + cacheKey;
        boolean lockAcquired = cacheService.tryLock(lockKey, 10, 30, TimeUnit.SECONDS);
        
        if (!lockAcquired) {
            // 如果获取锁失败，等待一段时间后重试
            try {
                Thread.sleep(100);
                return getProductWithLock(id);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("线程被中断", e);
            }
        }
        
        try {
            // 双重检查缓存
            cached = (Product) cacheService.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            
            // 从数据库加载数据
            Product product = loadFromDatabase(id);
            
            // 随机化过期时间，防止雪崩
            int ttl = 3600 + new Random().nextInt(600); // 1小时到1小时10分钟
            cacheService.set(cacheKey, product, ttl);
            
            return product;
            
        } finally {
            cacheService.releaseLock(lockKey);
        }
    }
}
```

### 6. 缓存更新策略

```java
@Service
public class CacheUpdateStrategy {
    
    /**
     * 策略1：Cache-Aside模式（旁路缓存）
     */
    public User getUserCacheAside(Long id) {
        String cacheKey = "user:" + id;
        
        // 先查缓存
        User user = (User) cacheService.get(cacheKey);
        if (user != null) {
            return user;
        }
        
        // 缓存未命中，查数据库
        user = userMapper.selectById(id);
        if (user != null) {
            cacheService.set(cacheKey, user, 3600);
        }
        
        return user;
    }
    
    /**
     * 策略2：Write-Through模式（直写）
     */
    @CachePut(key = "#user.id")
    public User updateUserWriteThrough(User user) {
        return userMapper.updateById(user);
    }
    
    /**
     * 策略3：Write-Behind模式（回写）
     */
    private final Queue<UserUpdateRequest> updateQueue = new ConcurrentLinkedQueue<>();
    
    @Async
    public void updateUserWriteBehind(User user) {
        // 先更新缓存
        cacheService.set("user:" + user.getId(), user, 3600);
        
        // 异步更新数据库
        updateQueue.offer(new UserUpdateRequest(user));
    }
    
    @Scheduled(fixedRate = 5000) // 每5秒批量更新数据库
    public void flushUpdates() {
        List<UserUpdateRequest> batch = new ArrayList<>();
        UserUpdateRequest request;
        
        while ((request = updateQueue.poll()) != null && batch.size() < 100) {
            batch.add(request);
        }
        
        if (!batch.isEmpty()) {
            userMapper.updateBatchById(batch.stream()
                .map(UserUpdateRequest::getUser)
                .collect(Collectors.toList()));
        }
    }
}
```

---

## 故障排除

### 常见问题

#### 1. 缓存穿透
**现象**: 查询不存在的数据时，每次都直接查询数据库
**解决方案**: 
- 使用空值缓存
- 使用布隆过滤器

#### 2. 缓存雪崩
**现象**: 大量缓存同时过期，导致数据库瞬时压力过大
**解决方案**:
- 设置随机过期时间
- 使用分布式锁
- 多级缓存架构

#### 3. 缓存击穿
**现象**: 热点数据过期瞬间，大量请求直接访问数据库
**解决方案**:
- 使用分布式锁
- 永不过期+后台刷新

#### 4. 缓存与数据库不一致
**现象**: 缓存数据与数据库数据不一致
**解决方案**:
- 更新数据库后及时更新缓存
- 使用订阅数据库变更的方案
- 设置合适的过期时间

### 调试和监控

#### 1. 启用详细日志

```yaml
logging:
  level:
    com.bing.framework.cache: DEBUG
    org.springframework.cache: DEBUG
```

#### 2. 健康检查

```bash
# 检查缓存健康状态
curl http://localhost:8080/actuator/health

# 查看缓存指标
curl http://localhost:8080/actuator/metrics/cache.hit.rate
curl http://localhost:8080/actuator/metrics/cache.miss.rate
```

#### 3. 缓存统计

```java
@Service
public class CacheDiagnostic {
    
    @Autowired
    private CacheService cacheService;
    
    public void diagnostic() {
        CacheStatistics stats = cacheService.getStatistics();
        
        System.out.println("缓存统计信息:");
        System.out.println("总操作次数: " + stats.getTotalOperations());
        System.out.println("成功次数: " + stats.getSuccessCount());
        System.out.println("失败次数: " + stats.getFailureCount());
        System.out.println("命中率: " + String.format("%.2f%%", stats.getHitRate() * 100));
        System.out.println("平均响应时间: " + stats.getAverageResponseTime() + "ms");
    }
}
```

---

## 版本历史

### v1.0.0
- 初始版本发布
- 支持双层缓存架构
- 支持自动降级机制
- 支持分布式锁
- 支持缓存统计和监控

### 路线图
- v1.1.0: 支持缓存集群和分片
- v1.2.0: 增加缓存压缩功能
- v1.3.0: 支持更丰富的缓存策略
- v2.0.0: 支持多数据中心同步

---

## 贡献指南

欢迎提交Issue和Pull Request来帮助改进这个项目。

### 开发环境设置
1. 克隆项目到本地
2. 安装Redis服务器
3. 运行测试用例
4. 提交代码前确保所有测试通过

### 代码规范
- 遵循Google Java代码规范
- 添加适当的单元测试
- 更新相关文档
- 确保向后兼容性

---

## 许可证

本项目采用MIT许可证，详见 [LICENSE](../LICENSE) 文件。

---

## 联系方式

- 项目地址: https://github.com/bing-framework/bing-cache
- 问题反馈: https://github.com/bing-framework/bing-cache/issues
- 技术支持: bing-framework@example.com

---

*最后更新: 2024-12-19*