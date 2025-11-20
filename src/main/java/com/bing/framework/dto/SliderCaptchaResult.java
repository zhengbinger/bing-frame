package com.bing.framework.dto;

/**
 * 滑动条验证码结果响应类
 * 封装滑动条验证码生成结果和验证数据
 * 
 * @author zhengbing
 * @date 2025-11-19
 */
public class SliderCaptchaResult extends CaptchaResult {
    
    /**
     * 滑动条验证码的具体数据
     */
    private SliderCaptchaData captchaData;
    
    /**
     * 滑动图片URL
     */
    private String sliderImage;
    
    /**
     * 背景图片URL
     */
    private String backgroundImage;
    
    /**
     * 滑块宽度
     */
    private int sliderWidth = 60;
    
    /**
     * 背景区域宽度
     */
    private int backgroundWidth = 300;
    
    /**
     * 滑块高度
     */
    private int sliderHeight = 40;
    
    // 默认构造函数
    public SliderCaptchaResult() {
        super();
    }
    
    /**
     * 完整构造函数
     * 
     * @param captchaKey 验证码键
     * @param captchaType 验证码类型
     * @param expireTime 过期时间
     * @param captchaData 验证码数据
     */
    public SliderCaptchaResult(String captchaKey, String captchaType, long expireTime, SliderCaptchaData captchaData) {
        setCaptchaKey(captchaKey);
        setCaptchaType(captchaType);
        setExpireTime(expireTime);
        this.captchaData = captchaData;
    }
    
    // Getters and Setters
    public SliderCaptchaData getCaptchaData() {
        return captchaData;
    }
    
    public void setCaptchaData(SliderCaptchaData captchaData) {
        this.captchaData = captchaData;
    }
    
    public String getSliderImage() {
        return sliderImage;
    }
    
    public void setSliderImage(String sliderImage) {
        this.sliderImage = sliderImage;
    }
    
    public String getBackgroundImage() {
        return backgroundImage;
    }
    
    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }
    
    public int getSliderWidth() {
        return sliderWidth;
    }
    
    public void setSliderWidth(int sliderWidth) {
        this.sliderWidth = sliderWidth;
    }
    
    public int getBackgroundWidth() {
        return backgroundWidth;
    }
    
    public void setBackgroundWidth(int backgroundWidth) {
        this.backgroundWidth = backgroundWidth;
    }
    
    public int getSliderHeight() {
        return sliderHeight;
    }
    
    public void setSliderHeight(int sliderHeight) {
        this.sliderHeight = sliderHeight;
    }
    
    /**
     * 获取验证码状态信息
     * 
     * @return 验证码状态字符串
     */
    public String getStatusInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("滑动条验证码 [");
        sb.append("类型=").append(getCaptchaType());
        sb.append(", 键=").append(getCaptchaKey());
        sb.append(", 目标位置=").append(captchaData != null ? captchaData.getTargetPosition() : "null");
        sb.append(", 容错范围=").append(captchaData != null ? captchaData.getTolerance() : "null");
        sb.append(", 过期时间=").append(getExpireTime());
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * 检查验证码是否已过期
     * 
     * @return 是否已过期
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > getExpireTime();
    }
    
    /**
     * 获取验证码剩余有效时间（秒）
     * 
     * @return 剩余秒数
     */
    public long getRemainingSeconds() {
        long remaining = getExpireTime() - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }
    
    @Override
    public String toString() {
        return "SliderCaptchaResult{" +
                "captchaKey='" + getCaptchaKey() + '\'' +
                ", captchaType='" + getCaptchaType() + '\'' +
                ", expireTime=" + getExpireTime() +
                ", captchaData=" + captchaData +
                ", sliderImage='" + sliderImage + '\'' +
                ", backgroundImage='" + backgroundImage + '\'' +
                ", sliderWidth=" + sliderWidth +
                ", backgroundWidth=" + backgroundWidth +
                ", sliderHeight=" + sliderHeight +
                '}';
    }
}