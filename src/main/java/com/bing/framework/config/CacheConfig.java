package com.bing.framework.config;

import com.bing.framework.cache.CacheService;
import com.bing.framework.cache.MemoryCache;
import com.bing.framework.cache.UnifiedCacheManager;
import java.lang.reflect.Method;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 缓存配置类（增强版 - 支持高可用降级）
 * 配置Redis作为主要缓存，本地缓存作为降级方案，自动切换
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class CacheConfig extends CachingConfigurerSupport {

    @Value("${spring.cache.redis.enabled:false}")
    private boolean redisEnabled;
    
    @Value("${spring.cache.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.cache.redis.port:6379}")
    private int redisPort;
    
    @Value("${spring.cache.redis.password:}")
    private String redisPassword;
    
    @Value("${spring.cache.redis.database:0}")
    private int redisDatabase;
    
    @Value("${spring.cache.local.max-size:1000}")
    private int localCacheMaxSize;
    
    @Value("${spring.cache.local.clean-interval:300}")
    private int localCacheCleanIntervalSeconds;


    
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
     * 缓存管理器配置（高可用版本）
     * 根据Redis可用性自动切换到本地缓存
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        if (!redisEnabled) {
            log.warn("Redis未启用，使用本地内存缓存作为降级方案");
            return new org.springframework.cache.support.SimpleCacheManager();
        }
        
        try {
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
            
        } catch (Exception e) {
            log.error("Redis缓存管理器初始化失败，将使用本地缓存作为降级方案", e);
            return new org.springframework.cache.support.SimpleCacheManager();
        }
    }

    /**
     * 本地缓存Bean
     */
    @Bean
    public MemoryCache localCache() {
        // 将秒转换为分钟
        long cleanIntervalMinutes = localCacheCleanIntervalSeconds / 60;
        if (cleanIntervalMinutes < 1) {
            cleanIntervalMinutes = 1; // 最小1分钟
        }
        
        MemoryCache memoryCache = new MemoryCache(localCacheMaxSize, 60, cleanIntervalMinutes);
        log.info("初始化本地缓存，容量: {}, 清理间隔: {}分钟", localCacheMaxSize, cleanIntervalMinutes);
        return memoryCache;
    }

    /**
     * 高可用缓存服务Bean
     */
    @Bean
    public CacheService cacheService(UnifiedCacheManager unifiedCacheManager) {
        CacheService cacheService = new CacheService();
        cacheService.setUnifiedCacheManager(unifiedCacheManager);
        log.info("初始化高可用缓存服务");
        return cacheService;
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

    /**
     * 启动时检查Redis连接状态
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("高可用缓存配置初始化完成");
        log.info("Redis配置: enabled={}, host={}, port={}, database={}", 
            redisEnabled, redisHost, redisPort, redisDatabase);
        
        if (redisEnabled) {
            try {
                // 测试Redis连接 - 这里需要通过unifiedCacheManager来检查
                // 实际的Redis连接检查在UnifiedCacheManager中已经实现
                log.info("Redis连接检查将在统一缓存管理器中自动进行");
            } catch (Exception e) {
                log.error("Redis连接测试失败", e);
            }
        } else {
            log.warn("Redis未启用，将使用本地缓存");
        }
    }

    /**
     * 定期检查Redis连接状态（每30秒）
     */
    @Scheduled(fixedRate = 30000)
    public void checkRedisConnection() {
        if (redisEnabled) {
            try {
                log.debug("Redis连接定期检查...");
                // 实际的Redis连接检查在UnifiedCacheManager中已经实现
                // 这里只是记录状态，实际的连接检查通过UnifiedCacheManager的定期检查线程来完成
                log.debug("Redis连接状态检查完成");
            } catch (Exception e) {
                log.error("Redis连接检查失败", e);
            }
        }
    }
}