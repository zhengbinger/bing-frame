# 缓存使用指南

本文档详细介绍了在Bing Framework中如何使用Spring Cache进行缓存管理。

## 1. 缓存概述

缓存是提高应用性能的重要手段，通过将频繁访问的数据存储在内存中，减少对数据库或其他昂贵操作的调用。Spring Cache提供了一个抽象的缓存机制，可以无缝集成不同的缓存提供者。

## 2. 缓存配置

### 2.1 基础配置

在项目中，缓存配置已在`application.yml`文件中设置：

```yaml
# 缓存配置
spring:
  cache:
    type: simple  # 使用简单缓存（内存缓存）
```

### 2.2 缓存类型支持

Spring Cache支持多种缓存类型，常用的包括：

- `simple`: 简单的基于内存的缓存，适用于单应用实例
- `redis`: Redis缓存，适用于分布式系统
- `caffeine`: 高性能的Java缓存库
- `hazelcast`: 分布式缓存系统

## 3. 缓存使用示例

### 3.1 启用缓存

在应用启动类上添加`@EnableCaching`注解启用缓存功能：

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching  // 启用缓存
public class BingFrameworkApplication {
    public static void main(String[] args) {
        SpringApplication.run(BingFrameworkApplication.class, args);
    }
}
```

### 3.2 在Service层使用缓存

以下是在用户服务中使用缓存的示例：

```java
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = "users")  // 设置默认缓存名称
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 根据ID查询用户，结果存入缓存
     */
    @Override
    @Cacheable(key = "#id")  // 缓存键为用户ID
    public User getUserById(Long id) {
        System.out.println("查询数据库，用户ID: " + id);
        return userMapper.selectById(id);
    }

    /**
     * 根据用户名查询用户，结果存入缓存
     */
    @Override
    @Cacheable(value = "users", key = "#username")
    public User getUserByUsername(String username) {
        System.out.println("查询数据库，用户名: " + username);
        return userMapper.selectByUsername(username);
    }

    /**
     * 更新用户信息，并更新缓存
     */
    @Override
    @CachePut(key = "#user.id")  // 更新缓存，键为用户ID
    public User updateUser(User user) {
        System.out.println("更新数据库，用户ID: " + user.getId());
        userMapper.updateById(user);
        return user;
    }

    /**
     * 删除用户，并清除相关缓存
     */
    @Override
    @CacheEvict(key = "#id")  // 删除缓存项
    public boolean deleteUser(Long id) {
        System.out.println("删除数据库记录，用户ID: " + id);
        return userMapper.deleteById(id) > 0;
    }

    /**
     * 清除所有用户缓存
     */
    @Override
    @CacheEvict(allEntries = true)  // 清除该缓存的所有项
    public void clearAllUserCache() {
        System.out.println("清除所有用户缓存");
    }
}
```

### 3.3 缓存注解详解

#### @Cacheable

用于标记方法结果应该被缓存。当调用标记了此注解的方法时，会先检查缓存中是否有对应的键值，如果有则直接返回缓存值，不执行方法体；如果没有则执行方法并将结果存入缓存。

**属性说明：**
- `value`/`cacheNames`: 指定缓存名称
- `key`: 指定缓存键，可以使用SpEL表达式
- `condition`: 条件表达式，只有满足条件时才进行缓存
- `unless`: 条件表达式，满足条件时不进行缓存

**示例：**
```java
@Cacheable(value = "users", key = "#id", condition = "#id > 0")
public User getUserById(Long id) {
    return userMapper.selectById(id);
}
```

#### @CachePut

用于更新缓存。无论缓存中是否已有对应的值，都会执行方法体，并将结果更新到缓存中。

**属性说明：**
- 与`@Cacheable`相同

**示例：**
```java
@CachePut(value = "users", key = "#user.id")
public User updateUser(User user) {
    userMapper.updateById(user);
    return user;
}
```

#### @CacheEvict

用于清除缓存。可以清除指定的缓存项或所有缓存项。

**属性说明：**
- 与`@Cacheable`相同
- `allEntries`: 是否清除所有缓存项，默认为false
- `beforeInvocation`: 是否在方法执行前清除缓存，默认为false（方法执行后清除）

**示例：**
```java
@CacheEvict(value = "users", key = "#id")
public void deleteUser(Long id) {
    userMapper.deleteById(id);
}

@CacheEvict(value = "users", allEntries = true)
public void clearAllUsers() {
    userMapper.deleteAll();
}
```

#### @CacheConfig

用于类级别设置缓存相关配置，为该类中的缓存注解提供默认值。

**属性说明：**
- `cacheNames`: 默认缓存名称
- `keyGenerator`: 默认键生成器
- `cacheManager`: 默认缓存管理器
- `cacheResolver`: 默认缓存解析器

**示例：**
```java
@CacheConfig(cacheNames = "users")
public class UserServiceImpl implements UserService {
    // 类中的缓存注解会使用"users"作为默认缓存名称
}
```

## 4. 自定义缓存配置

### 4.1 配置Redis缓存（生产环境推荐）

在生产环境中，通常使用Redis作为缓存。以下是配置Redis缓存的示例：

1. 添加Redis依赖到`pom.xml`：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

2. 在`application-prod.yml`中配置Redis：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
  cache:
    type: redis
    redis:
      time-to-live: 60000  # 缓存过期时间（毫秒）
      cache-null-values: true  # 是否缓存null值
      key-prefix: "bing:"  # 键前缀
```

### 4.2 自定义缓存管理器

可以创建配置类来自定义缓存管理器：

```java
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))  // 默认缓存过期时间
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();  // 不缓存null值

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration("users", 
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("roles", 
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(1)))
                .build();
    }
}
```

## 5. 缓存最佳实践

### 5.1 缓存键设计

- **唯一性**: 确保缓存键唯一，避免键冲突
- **简洁性**: 键名应该简洁明了，便于维护
- **一致性**: 相关操作（查询、更新、删除）使用相同的键生成逻辑

### 5.2 缓存失效策略

- **合理设置过期时间**: 避免缓存数据过期时间过长导致数据不一致
- **缓存穿透保护**: 对于不存在的数据，可以缓存一个特殊值，设置较短的过期时间
- **缓存预热**: 应用启动时预先加载热点数据到缓存
- **缓存更新**: 更新数据时同时更新缓存，或采用缓存失效策略

### 5.3 性能考虑

- **缓存粒度**: 适当控制缓存粒度，避免缓存过大的数据对象
- **批量操作**: 对于批量查询，考虑使用缓存批量键或分别缓存单个对象
- **异步缓存**: 对于耗时操作，可以考虑异步加载缓存

## 6. 缓存监控

在实际应用中，建议添加缓存监控，了解缓存命中率、缓存大小等指标。可以使用Spring Boot Actuator和Micrometer来监控缓存：

1. 添加依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

2. 配置监控端点：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: cache,health,info,prometheus
```

通过访问`/actuator/cache`可以查看缓存状态。

## 7. 总结

Spring Cache提供了一个简洁而强大的缓存抽象，可以轻松集成各种缓存实现。通过合理使用缓存，可以显著提高应用性能，减轻数据库负担。在实际应用中，需要根据业务场景选择合适的缓存策略和失效机制，并注意缓存一致性问题。

更多详细信息，请参考[Spring官方文档](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)。