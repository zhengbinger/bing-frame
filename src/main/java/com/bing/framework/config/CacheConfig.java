package com.bing.framework.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 缓存配置类
 * 配置Redis作为缓存实现，定义缓存管理器和键生成器
 * 
 * @author zhengbing
 */
// 添加@Lazy注解实现延迟初始化，提升启动性能
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig extends CachingConfigurerSupport {


    
    @Value("${spring.cache.redis.key-prefix:bing:}")
    private String keyPrefix;
    
    @Value("${spring.cache.redis.time-to-live:3600000}")
    private long timeToLive;
    
    @Value("${spring.cache.redis.cache-null-values:false}")
    private boolean cacheNullValues;

    /**
     * 自定义缓存键生成器
     * 生成格式: 类名.方法名.参数值
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                sb.append(target.getClass().getName()).append(".");
                sb.append(method.getName()).append(":");
                for (Object param : params) {
                    sb.append(param.toString()).append(",");
                }
                // 移除最后的逗号
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                return sb.toString();
            }
        };
    }
    
    /**
     * RedisTemplate配置
     * 设置序列化器，防止数据乱码
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<?> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        
        template.setConnectionFactory(factory);
        // key序列化方式
        template.setKeySerializer(redisSerializer);
        // value序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash key序列化
        template.setHashKeySerializer(redisSerializer);
        // hash value序列化
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 缓存管理器配置
     * 设置默认缓存过期时间等，与application.yml配置保持一致
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        log.info("初始化Redis缓存管理器，前缀: {}, 过期时间: {}ms", keyPrefix, timeToLive);
        
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        // 解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        
        // 配置序列化（解决乱码的问题）
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(timeToLive)) // 使用配置的过期时间
                .prefixCacheNameWith(keyPrefix) // 使用配置的key前缀
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));
        
        // 根据配置决定是否缓存空值
        if (!cacheNullValues) {
            config = config.disableCachingNullValues();
        }
        
        RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
        
        log.info("Redis缓存管理器初始化完成，缓存名称: auditLogCache, userCache, whiteListCache");
        return cacheManager;
    }
    
    /**
     * 缓存错误处理器
     * 捕获Redis缓存操作中的异常，避免缓存问题影响业务逻辑
     * 记录详细的异常信息，便于问题排查
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                handleCacheError(exception, cache, key);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                handleCacheError(exception, cache, key);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                handleCacheError(exception, cache, key);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                String cacheName = cache != null ? cache.getName() : "unknown";
                log.error("Redis缓存清除异常 - 缓存名称: {}", cacheName, exception);
            }

            private void handleCacheError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                String cacheName = cache != null ? cache.getName() : "unknown";
                String keyStr = key != null ? key.toString() : "unknown";
                log.error("Redis缓存操作异常 - 缓存名称: {}, 键: {}", cacheName, keyStr, exception);
            }
        };
    }
}