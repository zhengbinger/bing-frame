package com.bing.framework.strategy.impl;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.config.CaptchaConfig;
import com.bing.framework.context.RequestContext;
import com.bing.framework.dto.CaptchaResult;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.strategy.CaptchaStrategy;
import com.bing.framework.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(SmsCaptchaStrategy.class);

    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private CaptchaConfig captchaConfig;
    
    private final Random random = new Random();

    @Override
    public CaptchaResult generateCaptcha(String key) {
        // 获取客户端IP地址作为刷频限制的标识
        String clientIp = RequestContext.getClientIp();
        if (clientIp == null) {
            clientIp = "unknown";
        }
        
        // 检查发送频率限制（基于IP限制）
        String ipIntervalKey = "sms:ip:interval:" + clientIp;
        if (redisUtil.hasKey(ipIntervalKey)) {
            throw new BusinessException(ErrorCode.CAPTCHA_FREQUENCY_LIMIT, "发送过于频繁，请稍后再试");
        }
        
        // 检查每日发送限制（基于IP限制）
        String dailyKey = "sms:ip:daily:" + clientIp;
        Integer dailyCount = (Integer) redisUtil.get(dailyKey);
        if (dailyCount != null && dailyCount >= 10) { // 每天最多10次
            throw new BusinessException(ErrorCode.CAPTCHA_FREQUENCY_LIMIT, "今日发送次数已达上限");
        }
        
        // 生成随机数字验证码
        String captchaCode = generateCode(captchaConfig.getSms().getCodeLength());
        
        // 将验证码保存到Redis
        redisUtil.set("captcha:" + key, captchaCode, captchaConfig.getExpireMinutes(), TimeUnit.MINUTES);
        
        // 设置发送间隔限制（基于IP）
        redisUtil.set(ipIntervalKey, "1", captchaConfig.getSms().getSendIntervalSeconds(), TimeUnit.SECONDS);
        
        // 增加每日发送计数
        if (dailyCount == null) {
            redisUtil.set(dailyKey, 1, 24, TimeUnit.HOURS);
        } else {
            redisUtil.set(dailyKey, dailyCount + 1, 24, TimeUnit.HOURS);
        }
        
        // 实际使用时，这里需要集成短信发送服务
        // sendSms(key, captchaCode);
        // 建议集成阿里云短信、腾讯云短信或华为云短信服务
        log.info("短信验证码生成完成，手机号后4位: {}, 客户端IP: {}, 验证码: {}", 
                key.substring(key.length() - 4), clientIp, captchaCode);
        
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
        // 集成短信发送服务，如阿里云短信、腾讯云短信等
        // 建议使用统一的短信服务接口，支持多服务商切换
        // 示例实现：
        try {
            log.info("向手机号 {} 发送验证码: {}", phoneNumber, code);
            // 实际调用短信服务API
            // smsService.sendVerificationCode(phoneNumber, code, "您的验证码为：" + code + "，5分钟内有效");
        } catch (Exception e) {
            log.error("发送短信验证码失败，手机号: {}", phoneNumber, e);
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED, "验证码发送失败，请稍后重试");
        }
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