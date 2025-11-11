package com.bing.framework.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 提供Redis操作的便捷方法，封装了RedisTemplate的常用操作，简化开发流程
 * 包含String、Hash、Set、List等数据类型的基本操作，以及分布式锁等高级功能
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ================================ String类型操作 ================================

    /**
     * 设置缓存（永久有效）
     * 
     * @param key 缓存键，非空
     * @param value 缓存值，可以是任意可序列化对象
     * @return boolean 操作是否成功
     * @throws NullPointerException 当key为null时抛出
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置缓存并指定过期时间
     * 
     * @param key 缓存键，非空
     * @param value 缓存值，可以是任意可序列化对象
     * @param time 过期时间，必须大于0
     * @param timeUnit 时间单位，如TimeUnit.SECONDS
     * @return boolean 操作是否成功
     * @throws IllegalArgumentException 当time小于等于0时抛出
     * @throws NullPointerException 当key或timeUnit为null时抛出
     */
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            if (time <= 0) {
                throw new IllegalArgumentException("过期时间必须大于0");
            }
            redisTemplate.opsForValue().set(key, value, time, timeUnit);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取缓存
     * 
     * @param key 缓存键
     * @return Object 缓存值，如果键不存在则返回null
     * @throws NullPointerException 当key为null时返回null
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     * 
     * @param key 缓存键（可以传多个）
     * @return boolean 是否成功删除至少一个缓存项
     * @throws NullPointerException 当传入的key数组为null时返回false
     */
    public boolean delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                return redisTemplate.delete(key[0]);
            } else {
                List<String> keyList = new ArrayList<>(key.length);
                for (String k : key) {
                    keyList.add(k);
                }
                return redisTemplate.delete(keyList) > 0;
            }
        }
        return false;
    }

    /**
     * 设置缓存过期时间
     * 
     * @param key 缓存键
     * @param time 过期时间，必须大于0
     * @param timeUnit 时间单位，如TimeUnit.SECONDS
     * @return boolean 操作是否成功
     * @throws IllegalArgumentException 当time小于等于0时抛出
     * @throws NullPointerException 当key或timeUnit为null时抛出
     */
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, timeUnit);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取缓存过期时间
     * 
     * @param key 缓存键
     * @param timeUnit 时间单位，如TimeUnit.SECONDS
     * @return long 剩余过期时间（单位：指定的timeUnit），返回0表示永久有效
     * @throws NullPointerException 当key或timeUnit为null时抛出
     */
    public long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 判断键是否存在
     * 
     * @param key 缓存键
     * @return boolean true表示存在，false表示不存在
     * @throws NullPointerException 当key为null时返回false
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增操作（原子操作）
     * 
     * @param key 缓存键
     * @param delta 递增因子，必须大于0
     * @return long 递增后的值
     * @throws RuntimeException 当递增因子小于等于0时抛出
     * @throws NullPointerException 当key为null时抛出
     */
    public long increment(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减操作（原子操作）
     * 
     * @param key 缓存键
     * @param delta 递减因子，必须大于0
     * @return long 递减后的值
     * @throws RuntimeException 当递减因子小于等于0时抛出
     * @throws NullPointerException 当key为null时抛出
     */
    public long decrement(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ================================ Hash类型操作 ================================

    /**
     * 获取Hash中的单个字段值
     * 
     * @param key 缓存键，非空
     * @param item 字段名，非空
     * @return Object 字段值，如果键或字段不存在则返回null
     * @throws NullPointerException 当key或item为null时抛出
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取Hash中的所有字段和值
     * 
     * @param key 缓存键，非空
     * @return Map<Object, Object> Hash中的所有键值对，如果键不存在则返回空Map
     * @throws NullPointerException 当key为null时抛出
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 批量设置Hash中的多个字段值
     * 
     * @param key 缓存键，非空
     * @param map 包含多个字段和对应值的Map
     * @return boolean 操作是否成功
     * @throws NullPointerException 当key或map为null时抛出
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 批量设置Hash中的多个字段值并设置过期时间
     * 
     * @param key 缓存键，非空
     * @param map 包含多个字段和对应值的Map
     * @param time 过期时间，必须大于0
     * @param timeUnit 时间单位，如TimeUnit.SECONDS
     * @return boolean 操作是否成功
     * @throws IllegalArgumentException 当time小于等于0时抛出
     * @throws NullPointerException 当key、map或timeUnit为null时抛出
     */
    public boolean hmset(String key, Map<String, Object> map, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time, timeUnit);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置Hash中的单个字段值
     * 
     * @param key 缓存键，非空
     * @param item 字段名，非空
     * @param value 字段值，可以是任意可序列化对象
     * @return boolean 操作是否成功
     * @throws NullPointerException 当key或item为null时抛出
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置Hash中的单个字段值并设置过期时间
     * 
     * @param key 缓存键，非空
     * @param item 字段名，非空
     * @param value 字段值，可以是任意可序列化对象
     * @param time 过期时间，必须大于0
     * @param timeUnit 时间单位，如TimeUnit.SECONDS
     * @return boolean 操作是否成功
     * @throws IllegalArgumentException 当time小于等于0时抛出
     * @throws NullPointerException 当key、item或timeUnit为null时抛出
     */
    public boolean hset(String key, String item, Object value, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time, timeUnit);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除Hash中的一个或多个字段
     * 
     * @param key 缓存键，非空
     * @param item 要删除的字段（可以传多个）
     * @throws NullPointerException 当key为null时抛出
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断Hash中是否存在指定字段
     * 
     * @param key 缓存键，非空
     * @param item 字段名，非空
     * @return boolean true表示存在，false表示不存在
     * @throws NullPointerException 当key或item为null时抛出
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * Hash字段递增操作（原子操作）
     * 
     * @param key 缓存键，非空
     * @param item 字段名，非空
     * @param by 递增因子
     * @return double 递增后的值
     * @throws NullPointerException 当key或item为null时抛出
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * Hash字段递减操作（原子操作）
     * 
     * @param key 缓存键，非空
     * @param item 字段名，非空
     * @param by 递减因子
     * @return double 递减后的值
     * @throws NullPointerException 当key或item为null时抛出
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    // ================================ Set类型操作 ================================

    /**
     * 获取Set中的所有元素
     * 
     * @param key 缓存键，非空
     * @return Set<Object> Set中的所有元素，如果键不存在则返回null
     * @throws NullPointerException 当key为null时返回null
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断Set中是否包含指定元素
     * 
     * @param key 缓存键，非空
     * @param value 要检查的元素
     * @return boolean true表示包含，false表示不包含
     * @throws NullPointerException 当key为null时返回false
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向Set中添加一个或多个元素
     * 
     * @param key 缓存键，非空
     * @param values 要添加的元素（可以传多个）
     * @return long 成功添加的元素数量（不包括已存在的元素）
     * @throws NullPointerException 当key为null时返回0
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 向Set中添加一个或多个元素并设置过期时间
     * 
     * @param key 缓存键，非空
     * @param time 过期时间，必须大于0
     * @param timeUnit 时间单位，如TimeUnit.SECONDS
     * @param values 要添加的元素（可以传多个）
     * @return long 成功添加的元素数量（不包括已存在的元素）
     * @throws IllegalArgumentException 当time小于等于0时抛出
     * @throws NullPointerException 当key或timeUnit为null时返回0
     */
    public long sSetAndTime(String key, long time, TimeUnit timeUnit, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time, timeUnit);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取Set中元素的数量
     * 
     * @param key 缓存键，非空
     * @return long Set中元素的数量，如果键不存在则返回0
     * @throws NullPointerException 当key为null时返回0
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 从Set中移除一个或多个元素
     * 
     * @param key 缓存键，非空
     * @param values 要移除的元素（可以传多个）
     * @return long 成功移除的元素数量
     * @throws NullPointerException 当key为null时返回0
     */
    public long setRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ================================ List类型操作 ================================

    /**
     * 获取List指定范围内的元素
     * 
     * @param key 缓存键，非空
     * @param start 起始索引，从0开始
     * @param end 结束索引，-1表示最后一个元素
     * @return List<Object> 指定范围内的元素列表，如果键不存在则返回null
     * @throws IllegalArgumentException 当start>end时抛出
     * @throws NullPointerException 当key为null时返回null
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取List的长度
     * 
     * @param key 缓存键，非空
     * @return long List的长度，如果键不存在则返回0
     * @throws NullPointerException 当key为null时返回0
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 通过索引获取List中的元素
     * 
     * @param key 缓存键，非空
     * @param index 索引，0表示第一个元素，-1表示最后一个元素
     * @return Object 指定索引位置的元素，如果索引超出范围或键不存在则返回null
     * @throws NullPointerException 当key为null时返回null
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将元素添加到List的右侧（尾部）
     * 
     * @param key 缓存键，非空
     * @param value 要添加的元素
     * @return boolean 操作是否成功
     * @throws NullPointerException 当key为null时返回false
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将元素添加到List的右侧（尾部）并设置过期时间
     * 
     * @param key 缓存键，非空
     * @param value 要添加的元素
     * @param time 过期时间，必须大于0
     * @param timeUnit 时间单位，如TimeUnit.SECONDS
     * @return boolean 操作是否成功
     * @throws IllegalArgumentException 当time小于等于0时抛出
     * @throws NullPointerException 当key或timeUnit为null时返回false
     */
    public boolean lSet(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time, timeUnit);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将多个元素添加到List的右侧（尾部）
     * 
     * @param key 缓存键，非空
     * @param value 要添加的元素列表
     * @return boolean 操作是否成功
     * @throws NullPointerException 当key或value为null时返回false
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将多个元素添加到List的右侧（尾部）并设置过期时间
     * 
     * @param key 缓存键，非空
     * @param value 要添加的元素列表
     * @param time 过期时间，必须大于0
     * @param timeUnit 时间单位，如TimeUnit.SECONDS
     * @return boolean 操作是否成功
     * @throws IllegalArgumentException 当time小于等于0时抛出
     * @throws NullPointerException 当key、value或timeUnit为null时返回false
     */
    public boolean lSet(String key, List<Object> value, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time, timeUnit);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新List中指定索引位置的元素
     * 
     * @param key 缓存键，非空
     * @param index 索引，必须在List范围内
     * @param value 新的元素值
     * @return boolean 操作是否成功
     * @throws IndexOutOfBoundsException 当索引超出List范围时抛出
     * @throws NullPointerException 当key为null时返回false
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除List中指定数量的元素
     * 
     * @param key 缓存键，非空
     * @param count 移除策略：count>0从左侧开始移除count个等于value的元素；count<0从右侧开始移除；count=0移除所有等于value的元素
     * @param value 要移除的元素值
     * @return long 成功移除的元素数量
     * @throws NullPointerException 当key为null时返回0
     */
    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ================================ 分布式锁相关 ================================

    /**
     * 获取分布式锁
     * <p>
     * 使用setIfAbsent命令实现原子性操作，确保设置锁和设置过期时间在同一个命令中执行
     * 避免死锁问题，requestId用于标识锁的持有者，方便后续正确释放锁
     * </p>
     * 
     * @param lockKey 锁键，用于标识锁的唯一性
     * @param requestId 请求标识，用于标识当前获取锁的客户端，通常使用UUID
     * @param expireTime 锁的过期时间，防止死锁
     * @param timeUnit 时间单位，如TimeUnit.SECONDS
     * @return boolean true表示获取锁成功，false表示获取锁失败
     * @throws IllegalArgumentException 当expireTime小于等于0时抛出
     * @throws NullPointerException 当lockKey、requestId或timeUnit为null时抛出
     */
    public boolean getLock(String lockKey, String requestId, long expireTime, TimeUnit timeUnit) {
        if (expireTime <= 0) {
            throw new IllegalArgumentException("过期时间必须大于0");
        }
        return redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, expireTime, timeUnit);
    }

    /**
     * 释放分布式锁
     * <p>
     * 使用Lua脚本确保原子性检查和删除操作，防止错误地释放其他客户端持有的锁
     * 通过比较requestId确认是锁的持有者才释放，增强了锁的安全性
     * </p>
     * 
     * @param lockKey 锁键
     * @param requestId 请求标识，必须与获取锁时使用的requestId一致
     * @return boolean true表示释放锁成功，false表示释放锁失败
     * @throws NullPointerException 当lockKey或requestId为null时抛出
     */
    public boolean releaseLock(String lockKey, String requestId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), requestId);
        return result != null && result > 0;
    }

    // ================================ 通用操作 ================================

    /**
     * 根据模式删除匹配的所有键
     * <p>
     * 支持通配符模式，如：user:* 匹配所有以user:开头的键
     * 注意：在生产环境中使用keys命令可能会影响Redis性能，建议在从节点上执行或使用SCAN命令
     * </p>
     * 
     * @param pattern 键模式，支持*、?、[]等通配符
     * @throws NullPointerException 当pattern为null时抛出
     */
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 根据模式获取匹配的所有键
     * <p>
     * 支持通配符模式，如：user:* 匹配所有以user:开头的键
     * 注意：在生产环境中使用keys命令可能会影响Redis性能，建议在从节点上执行或使用SCAN命令
     * </p>
     * 
     * @param pattern 键模式，支持*、?、[]等通配符
     * @return Set<String> 匹配的键集合，如果没有匹配的键则返回null
     * @throws NullPointerException 当pattern为null时抛出
     */
    public Set<String> getKeysByPattern(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 批量设置多个键值对
     * <p>
     * 原子性操作，要么全部设置成功，要么全部设置失败
     * 适用于需要一次性设置多个缓存项的场景，可以减少网络通信次数
     * </p>
     * 
     * @param map 包含多个键值对的Map
     * @throws NullPointerException 当map为null时抛出
     */
    public void multiSet(Map<String, Object> map) {
        redisTemplate.opsForValue().multiSet(map);
    }

    /**
     * 批量获取多个键的值
     * <p>
     * 一次性获取多个键的值，减少网络通信次数
     * 返回值列表的顺序与输入的键列表顺序一致，不存在的键对应的值为null
     * </p>
     * 
     * @param keys 键集合
     * @return List<Object> 对应键的值列表，按照keys的顺序返回，如果键不存在则对应位置为null
     * @throws NullPointerException 当keys为null时抛出
     */
    public List<Object> multiGet(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }
}