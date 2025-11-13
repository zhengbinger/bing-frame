package com.bing.framework.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码策略工厂
 * 管理不同类型的验证码策略实现，提供按类型获取策略的功能
 * 支持运行时切换不同的验证码实现
 * 
 * @author zhengbing
 * @date 2025-11-13
 */
@Component
public class CaptchaStrategyFactory {
    
    // 存储所有验证码策略实现
    private final Map<String, CaptchaStrategy> strategyMap = new ConcurrentHashMap<>();
    
    /**
     * 构造函数，自动注入所有验证码策略实现
     */
    @Autowired
    public CaptchaStrategyFactory(Map<String, CaptchaStrategy> strategyMap) {
        // 将所有实现注入到map中
        this.strategyMap.putAll(strategyMap);
    }
    
    /**
     * 根据类型获取验证码策略
     * 
     * @param type 验证码类型
     * @return 对应的验证码策略实现
     * @throws IllegalArgumentException 当指定类型的策略不存在时抛出
     */
    public CaptchaStrategy getStrategy(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("验证码类型不能为空");
        }
        
        CaptchaStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的验证码类型: " + type);
        }
        
        return strategy;
    }
    
    /**
     * 注册新的验证码策略
     * 
     * @param type 验证码类型
     * @param strategy 验证码策略实现
     */
    public void registerStrategy(String type, CaptchaStrategy strategy) {
        if (type == null || type.trim().isEmpty() || strategy == null) {
            throw new IllegalArgumentException("验证码类型和策略不能为空");
        }
        
        strategyMap.put(type, strategy);
    }
    
    /**
     * 检查是否支持指定类型的验证码
     * 
     * @param type 验证码类型
     * @return 是否支持
     */
    public boolean supports(String type) {
        return type != null && strategyMap.containsKey(type);
    }
}