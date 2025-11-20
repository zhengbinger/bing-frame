package com.bing.framework.dto;

/**
 * 滑动条验证码数据模型
 * 包含验证码的核心验证参数
 * 
 * @author zhengbing
 * @date 2025-11-19
 */
public class SliderCaptchaData {
    
    /**
     * 目标滑动位置（像素）
     */
    private int targetPosition;
    
    /**
     * 当前滑动位置（像素）
     */
    private int currentPosition;
    
    /**
     * 验证容错范围（像素）
     */
    private int tolerance;
    
    /**
     * 验证码生成时间戳
     */
    private long timestamp;
    
    /**
     * 滑块图片URL（可选）
     */
    private String sliderImageUrl;
    
    /**
     * 背景图片URL（可选）
     */
    private String backgroundImageUrl;
    
    // 默认构造函数
    public SliderCaptchaData() {
    }
    
    // 完整构造函数
    public SliderCaptchaData(int targetPosition, int currentPosition, int tolerance, long timestamp) {
        this.targetPosition = targetPosition;
        this.currentPosition = currentPosition;
        this.tolerance = tolerance;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public int getTargetPosition() {
        return targetPosition;
    }
    
    public void setTargetPosition(int targetPosition) {
        this.targetPosition = targetPosition;
    }
    
    public int getCurrentPosition() {
        return currentPosition;
    }
    
    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }
    
    public int getTolerance() {
        return tolerance;
    }
    
    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
     * 检查验证码是否已过期
     * 
     * @param expireMinutes 过期分钟数
     * @return 是否已过期
     */
    public boolean isExpired(int expireMinutes) {
        long expireTime = this.timestamp + expireMinutes * 60 * 1000;
        return System.currentTimeMillis() > expireTime;
    }
    
    /**
     * 获取已进行的滑动进度百分比
     * 
     * @param totalLength 滑动条总长度
     * @return 滑动进度百分比（0-100）
     */
    public int getProgressPercentage(int totalLength) {
        if (totalLength <= 0) {
            return 0;
        }
        return (int) Math.round((double) currentPosition / totalLength * 100);
    }
    
    @Override
    public String toString() {
        return "SliderCaptchaData{" +
                "targetPosition=" + targetPosition +
                ", currentPosition=" + currentPosition +
                ", tolerance=" + tolerance +
                ", timestamp=" + timestamp +
                ", sliderImageUrl='" + sliderImageUrl + '\'' +
                ", backgroundImageUrl='" + backgroundImageUrl + '\'' +
                '}';
    }
}