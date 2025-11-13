package com.bing.framework.strategy.impl;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.config.CaptchaConfig;
import com.bing.framework.dto.CaptchaResult;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.strategy.CaptchaStrategy;
import com.bing.framework.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码策略实现
 * 生成和验证短信验证码，实际使用时需集成短信发送服务
 * 
 * @author zhengbing
 * @date 2025-11-13
 */
@Component("sms")
public class SmsCaptchaStrategy implements CaptchaStrategy {

    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private CaptchaConfig captchaConfig;
    
    private final Random random = new Random();

    @Override
    public CaptchaResult generateCaptcha(String key) {
        // 检查发送频率限制
        String intervalKey = "sms:interval:" + key;
        if (redisUtil.hasKey(intervalKey)) {
            throw new BusinessException(ErrorCode.CAPTCHA_FREQUENCY_LIMIT, "短信验证码发送过于频繁");
        }
        
        // 生成随机数字验证码
        String captchaCode = generateCode(captchaConfig.getSms().getCodeLength());
        
        // 将验证码保存到Redis
        redisUtil.set("captcha:" + key, captchaCode, captchaConfig.getExpireMinutes(), TimeUnit.MINUTES);
        
        // 设置发送间隔限制
        redisUtil.set(intervalKey, "1", captchaConfig.getSms().getSendIntervalSeconds(), TimeUnit.SECONDS);
        
        // TODO: 实际使用时，这里需要集成短信发送服务
        // sendSms(key, captchaCode);
        
        // 创建结果对象
        CaptchaResult result = new CaptchaResult();
        result.setCaptchaKey(key);
        result.setCaptchaContent("短信验证码已发送，请注意查收");
        result.setCaptchaType(getType());
        result.setExpireTime(System.currentTimeMillis() + captchaConfig.getExpireMinutes() * 60 * 1000);
        
        return result;
    }

    @Override
    public boolean validateCaptcha(String key, String code) {
        if (key == null || code == null) {
            return false;
        }
        
        String captchaKey = "captcha:" + key;
        String storedCode = (String) redisUtil.get(captchaKey);
        
        if (storedCode == null) {
            return false;
        }
        
        // 短信验证码通常区分大小写
        boolean isValid = storedCode.equals(code);
        
        // 验证通过后删除验证码
        if (isValid) {
            cleanCaptcha(key);
        }
        
        return isValid;
    }

    @Override
    public String getType() {
        return "sms";
    }

    @Override
    public void cleanCaptcha(String key) {
        redisUtil.delete("captcha:" + key);
        // 可以选择是否删除发送间隔限制
        // redisUtil.delete("sms:interval:" + key);
    }
    
    /**
     * 生成指定长度的数字验证码
     */
    private String generateCode(int codeLength) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }
    
    /**
     * 发送短信验证码（示例方法，实际使用时需集成短信服务）
     */
    private void sendSms(String phoneNumber, String code) {
        // TODO: 集成短信发送服务，如阿里云短信、腾讯云短信等
        // 这里只是示例，实际项目中需要替换为真实的短信发送逻辑
        System.out.println("向手机号 " + phoneNumber + " 发送验证码: " + code);
    }
    
    /**
     * 检查是否可以发送短信验证码
     * @param phoneNumber 手机号
     * @return 是否可以发送
     */
    public boolean canSend(String phoneNumber) {
        String intervalKey = "sms:interval:" + phoneNumber;
        return redisUtil.get(intervalKey) == null;
    }
}