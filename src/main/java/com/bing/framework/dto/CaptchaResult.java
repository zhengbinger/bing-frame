package com.bing.framework.dto;

import lombok.Data;

/**
 * 验证码结果DTO
 * 封装验证码生成的结果信息，包括验证码内容、唯一标识等
 * 
 * @author zhengbing
 * @date 2025-11-05
 */
@Data
public class CaptchaResult {
    
    /**
     * 验证码唯一标识
     */
    private String captchaKey;
    
    /**
     * 验证码内容，不同类型有不同格式
     * - 图形验证码：Base64编码的图片
     * - 短信验证码：发送状态或提示信息
     * - 其他验证码：对应的内容格式
     */
    private String captchaContent;
    
    /**
     * 验证码类型
     */
    private String captchaType;
    
    /**
     * 验证码过期时间（毫秒时间戳）
     */
    private Long expireTime;
}