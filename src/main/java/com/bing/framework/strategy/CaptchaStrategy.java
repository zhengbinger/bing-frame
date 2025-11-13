package com.bing.framework.strategy;

import com.bing.framework.dto.CaptchaResult;

/**
 * 验证码策略接口
 * 定义验证码生成和验证的核心方法，支持不同类型的验证码实现
 * 为登录多因子校验提供可扩展的验证码机制
 * 
 * @author zhengbing
 * @date 2025-11-13
 */
public interface CaptchaStrategy {
    
    /**
     * 生成验证码
     * 
     * @param key 验证码唯一标识
     * @return 验证码生成结果，不同实现可能返回不同格式（如图片Base64、短信验证码等）
     */
    CaptchaResult generateCaptcha(String key);
    
    /**
     * 验证验证码
     * 
     * @param key 验证码唯一标识
     * @param code 用户输入的验证码
     * @return 是否验证通过
     */
    boolean validateCaptcha(String key, String code);
    
    /**
     * 获取验证码类型
     * 
     * @return 验证码类型标识
     */
    String getType();
    
    /**
     * 清理验证码
     * 
     * @param key 验证码唯一标识
     */
    void cleanCaptcha(String key);
}