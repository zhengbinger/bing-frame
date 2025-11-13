package com.bing.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 验证码配置类
 * 提供验证码相关的配置信息，支持从配置文件中读取
 * 实现可配置化的验证码功能
 * 
 * @author zhengbing
 * @date 2025-11-13
 */
@Component
@ConfigurationProperties(prefix = "bing.captcha")
public class CaptchaConfig {
    
    /**
     * 是否启用验证码功能
     */
    private boolean enabled = true;
    
    /**
     * 默认验证码类型
     * 可选值：image（图形验证码）、sms（短信验证码）等
     */
    private String defaultType = "image";
    
    /**
     * 验证码有效期（分钟）
     */
    private int expireMinutes = 5;
    
    /**
     * 登录失败次数阈值，达到阈值时强制要求验证码
     */
    private int loginFailureThreshold = 3;
    
    /**
     * 图形验证码配置
     */
    private final ImageCaptchaConfig image = new ImageCaptchaConfig();
    
    /**
     * 短信验证码配置
     */
    private final SmsCaptchaConfig sms = new SmsCaptchaConfig();
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getDefaultType() {
        return defaultType;
    }
    
    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
    }
    
    public int getExpireMinutes() {
        return expireMinutes;
    }
    
    public void setExpireMinutes(int expireMinutes) {
        this.expireMinutes = expireMinutes;
    }
    
    public int getLoginFailureThreshold() {
        return loginFailureThreshold;
    }
    
    public void setLoginFailureThreshold(int loginFailureThreshold) {
        this.loginFailureThreshold = loginFailureThreshold;
    }
    
    public ImageCaptchaConfig getImage() {
        return image;
    }
    
    public SmsCaptchaConfig getSms() {
        return sms;
    }
    
    /**
     * 图形验证码配置内部类
     */
    public static class ImageCaptchaConfig {
        
        /**
         * 图片宽度
         */
        private int width = 120;
        
        /**
         * 图片高度
         */
        private int height = 40;
        
        /**
         * 字符数量
         */
        private int codeCount = 4;
        
        /**
         * 干扰线数量
         */
        private int lineCount = 5;
        
        public int getWidth() {
            return width;
        }
        
        public void setWidth(int width) {
            this.width = width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public void setHeight(int height) {
            this.height = height;
        }
        
        public int getCodeCount() {
            return codeCount;
        }
        
        public void setCodeCount(int codeCount) {
            this.codeCount = codeCount;
        }
        
        public int getLineCount() {
            return lineCount;
        }
        
        public void setLineCount(int lineCount) {
            this.lineCount = lineCount;
        }
    }
    
    /**
     * 短信验证码配置内部类
     */
    public static class SmsCaptchaConfig {
        
        /**
         * 验证码长度
         */
        private int codeLength = 6;
        
        /**
         * 发送间隔（秒）
         */
        private int sendIntervalSeconds = 60;
        
        public int getCodeLength() {
            return codeLength;
        }
        
        public void setCodeLength(int codeLength) {
            this.codeLength = codeLength;
        }
        
        public int getSendIntervalSeconds() {
            return sendIntervalSeconds;
        }
        
        public void setSendIntervalSeconds(int sendIntervalSeconds) {
            this.sendIntervalSeconds = sendIntervalSeconds;
        }
    }
}