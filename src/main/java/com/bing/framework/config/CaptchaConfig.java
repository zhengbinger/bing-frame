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
    
    /**
     * 滑动条验证码配置
     */
    private final SliderCaptchaConfig slider = new SliderCaptchaConfig();
    
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
    
    public SliderCaptchaConfig getSlider() {
        return slider;
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
    }
    
    /**
     * 滑动条验证码配置内部类
     */
    public static class SliderCaptchaConfig {
        
        /**
         * 滑动条总宽度
         */
        private int totalWidth = 300;
        
        /**
         * 滑块宽度
         */
        private int sliderWidth = 60;
        
        /**
         * 滑块高度
         */
        private int sliderHeight = 40;
        
        /**
         * 默认容错范围（像素）
         */
        private int defaultTolerance = 5;
        
        /**
         * 最小目标位置百分比（相对于滑动条宽度）
         */
        private double minTargetPercentage = 0.3;
        
        /**
         * 最大目标位置百分比（相对于滑动条宽度）
         */
        private double maxTargetPercentage = 0.7;
        
        /**
         * 是否启用图片模式（滑动条和背景显示图片）
         */
        private boolean enableImageMode = false;
        
        /**
         * 滑动条滑块图片URL
         */
        private String sliderImageUrl;
        
        /**
         * 背景图片URL
         */
        private String backgroundImageUrl;
        
        public int getTotalWidth() {
            return totalWidth;
        }
        
        public void setTotalWidth(int totalWidth) {
            this.totalWidth = totalWidth;
        }
        
        public int getSliderWidth() {
            return sliderWidth;
        }
        
        public void setSliderWidth(int sliderWidth) {
            this.sliderWidth = sliderWidth;
        }
        
        public int getSliderHeight() {
            return sliderHeight;
        }
        
        public void setSliderHeight(int sliderHeight) {
            this.sliderHeight = sliderHeight;
        }
        
        public int getDefaultTolerance() {
            return defaultTolerance;
        }
        
        public void setDefaultTolerance(int defaultTolerance) {
            this.defaultTolerance = defaultTolerance;
        }
        
        public double getMinTargetPercentage() {
            return minTargetPercentage;
        }
        
        public void setMinTargetPercentage(double minTargetPercentage) {
            this.minTargetPercentage = minTargetPercentage;
        }
        
        public double getMaxTargetPercentage() {
            return maxTargetPercentage;
        }
        
        public void setMaxTargetPercentage(double maxTargetPercentage) {
            this.maxTargetPercentage = maxTargetPercentage;
        }
        
        public boolean isEnableImageMode() {
            return enableImageMode;
        }
        
        public void setEnableImageMode(boolean enableImageMode) {
            this.enableImageMode = enableImageMode;
        }
        
        public String getSliderImageUrl() {
            return sliderImageUrl;
        }
        
        public void setSliderImageUrl(String sliderImageUrl) {
            this.sliderImageUrl = sliderImageUrl;
        }
        
        public String getBackgroundImageUrl() {
            return backgroundImageUrl;
        }
        
        public void setBackgroundImageUrl(String backgroundImageUrl) {
            this.backgroundImageUrl = backgroundImageUrl;
        }
        
        /**
         * 计算最小目标位置（像素）
         * 
         * @return 最小目标位置
         */
        public int getMinTargetPosition() {
            return (int) (totalWidth * minTargetPercentage);
        }
        
        /**
         * 计算最大目标位置（像素）
         * 
         * @return 最大目标位置
         */
        public int getMaxTargetPosition() {
            return (int) (totalWidth * maxTargetPercentage);
        }
        
        /**
         * 检查配置是否有效
         * 
         * @return 是否有效
         */
        public boolean isValid() {
            return totalWidth > 0 && 
                   sliderWidth > 0 && 
                   sliderHeight > 0 && 
                   defaultTolerance >= 0 &&
                   minTargetPercentage >= 0 && 
                   minTargetPercentage <= 1 &&
                   maxTargetPercentage >= 0 && 
                   maxTargetPercentage <= 1 &&
                   minTargetPercentage <= maxTargetPercentage;
        }
    }
}