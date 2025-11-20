package com.bing.framework.strategy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bing.framework.config.CaptchaConfig;
import com.bing.framework.dto.CaptchaResult;
import com.bing.framework.dto.SliderCaptchaResult;
import com.bing.framework.dto.SliderCaptchaData;
import com.bing.framework.strategy.CaptchaStrategy;
import com.bing.framework.util.RedisUtil;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 滑动条验证码策略实现
 * 生成和验证滑动条验证码，通过滑动操作验证用户是否为真人
 * 提供更高的安全性和用户体验
 * 
 * @author zhengbing
 * @date 2025-11-19
 */
@Component("slider")
public class SliderCaptchaStrategy implements CaptchaStrategy {

    private static final Logger log = LoggerFactory.getLogger(SliderCaptchaStrategy.class);
    
    private static final String CAPTCHA_PREFIX = "captcha:slider:";
    private static final int DEFAULT_VALIDATION_TOLERANCE = 5; // 像素容错范围
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private CaptchaConfig captchaConfig;
    
    private final Random random = new Random();
    
    /**
     * 生成滑动条验证码
     * 
     * @param key 验证码唯一标识
     * @return 滑动条验证码生成结果
     */
    @Override
    public CaptchaResult generateCaptcha(String key) {
        try {
            // 生成滑动目标位置（随机在滑块中间区域）
            int targetPosition = generateRandomTargetPosition();
            
            // 生成验证数据
            SliderCaptchaData captchaData = new SliderCaptchaData();
            captchaData.setTargetPosition(targetPosition);
            captchaData.setCurrentPosition(0);
            captchaData.setTolerance(DEFAULT_VALIDATION_TOLERANCE);
            captchaData.setTimestamp(System.currentTimeMillis());
            
            // 计算有效期
            int expireMinutes = captchaConfig.getExpireMinutes();
            long expireTime = System.currentTimeMillis() + expireMinutes * 60 * 1000;
            
            // 存储验证数据到Redis
            String dataKey = CAPTCHA_PREFIX + key;
            redisUtil.set(dataKey, captchaData, expireMinutes * 60, TimeUnit.SECONDS);
            
            // 构建返回结果
            SliderCaptchaResult result = new SliderCaptchaResult();
            result.setCaptchaKey(key);
            result.setCaptchaType("slider");
            result.setCaptchaData(captchaData);
            result.setExpireTime(expireTime);
            
            log.info("滑动条验证码生成成功，key: {}, targetPosition: {}", key, targetPosition);
            return result;
            
        } catch (Exception e) {
            log.error("滑动条验证码生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("滑动条验证码生成失败", e);
        }
    }
    
    /**
     * 验证滑动条验证码
     * 
     * @param key 验证码唯一标识
     * @param code 用户输入的滑动位置数据（JSON格式）
     * @return 是否验证通过
     */
    @Override
    public boolean validateCaptcha(String key, String code) {
        if (key == null || code == null) {
            return false;
        }
        
        try {
            // 获取存储的验证数据
            String dataKey = CAPTCHA_PREFIX + key;
            SliderCaptchaData storedData = (SliderCaptchaData) redisUtil.get(dataKey);
            
            if (storedData == null) {
                log.warn("滑动条验证码已过期或不存在，key: {}", key);
                return false;
            }
            
            // 解析用户输入的滑动位置
            int userPosition = parseUserPosition(code);
            
            // 验证滑动位置
            boolean isValid = validateSliderPosition(storedData.getTargetPosition(), userPosition, storedData.getTolerance());
            
            // 验证通过后清理验证码
            if (isValid) {
                cleanCaptcha(key);
                log.info("滑动条验证码验证成功，key: {}, userPosition: {}", key, userPosition);
            } else {
                log.warn("滑动条验证码验证失败，key: {}, targetPosition: {}, userPosition: {}", 
                        key, storedData.getTargetPosition(), userPosition);
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("滑动条验证码验证异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取验证码类型
     * 
     * @return 验证码类型标识
     */
    @Override
    public String getType() {
        return "slider";
    }
    
    /**
     * 清理验证码
     * 
     * @param key 验证码唯一标识
     */
    @Override
    public void cleanCaptcha(String key) {
        try {
            String dataKey = CAPTCHA_PREFIX + key;
            redisUtil.delete(dataKey);
            log.info("滑动条验证码清理成功，key: {}", key);
        } catch (Exception e) {
            log.error("滑动条验证码清理失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 生成随机目标位置
     * 目标位置控制在滑块总长度的30%-70%之间，避免过简单或过困难
     * 
     * @return 目标位置（像素）
     */
    private int generateRandomTargetPosition() {
        // 假设滑块总长度为300px，目标位置在90-210px之间
        int minPosition = 90;
        int maxPosition = 210;
        return minPosition + random.nextInt(maxPosition - minPosition + 1);
    }
    
    /**
     * 解析用户输入的滑动位置
     * 
     * @param code 用户输入数据（JSON格式或数字字符串）
     * @return 滑动位置
     */
    private int parseUserPosition(String code) {
        try {
            // 如果是纯数字字符串，直接解析
            if (code.matches("^-?\\d+$")) {
                return Integer.parseInt(code);
            }
            
            // 如果是JSON格式，尝试解析（这里简化处理）
            // 实际项目中可以使用JSON解析库如Jackson或Gson
            if (code.contains("\"position\":")) {
                String positionStr = code.replaceAll(".*\"position\"\\s*:\\s*(\\d+).*", "$1");
                return Integer.parseInt(positionStr);
            }
            
            // 默认返回0
            return 0;
            
        } catch (NumberFormatException e) {
            log.warn("无法解析滑动位置数据: {}", code);
            return 0;
        }
    }
    
    /**
     * 验证滑动位置是否正确
     * 
     * @param targetPosition 目标位置
     * @param userPosition 用户滑动位置
     * @param tolerance 容错范围
     * @return 是否验证通过
     */
    private boolean validateSliderPosition(int targetPosition, int userPosition, int tolerance) {
        return Math.abs(userPosition - targetPosition) <= tolerance;
    }
}