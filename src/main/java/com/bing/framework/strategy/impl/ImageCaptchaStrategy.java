package com.bing.framework.strategy.impl;

import com.bing.framework.common.ErrorCode;
import com.bing.framework.config.CaptchaConfig;
import com.bing.framework.dto.CaptchaResult;
import com.bing.framework.exception.BusinessException;
import com.bing.framework.strategy.CaptchaStrategy;
import com.bing.framework.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码策略实现
 * 生成基于BufferedImage的图形验证码，包含随机字符、干扰线和噪点
 * 
 * @author zhengbing
 * @date 2025-11-13
 */
@Component("image")
public class ImageCaptchaStrategy implements CaptchaStrategy {

    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private CaptchaConfig captchaConfig;
    
    // 字符集
    private static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    private final Random random = new Random();

    @Override
    public CaptchaResult generateCaptcha(String key) {
        // 创建验证码图片
        int width = captchaConfig.getImage().getWidth();
        int height = captchaConfig.getImage().getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        
        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // 设置边框
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(0, 0, width - 1, height - 1);
        
        // 绘制干扰线
        for (int i = 0; i < captchaConfig.getImage().getLineCount(); i++) {
            drawLine(g, width, height);
        }
        
        // 绘制噪点
        for (int i = 0; i < width * height / 150; i++) {
            drawDot(g, width, height);
        }
        
        // 生成随机验证码
        String captchaCode = generateCode(captchaConfig.getImage().getCodeCount());
        
        // 绘制验证码字符
        drawString(g, captchaCode, width, height);
        
        // 释放资源
        g.dispose();
        
        // 将验证码保存到Redis
        redisUtil.set("captcha:" + key, captchaCode, captchaConfig.getExpireMinutes(), TimeUnit.MINUTES);
        
        // 将图片转换为Base64
        String base64Image = imageToBase64(image);
        
        // 创建结果对象
        CaptchaResult result = new CaptchaResult();
        result.setCaptchaKey(key);
        result.setCaptchaContent(base64Image);
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
        
        // 验证码不区分大小写
        boolean isValid = storedCode.equalsIgnoreCase(code);
        
        // 验证通过后删除验证码
        if (isValid) {
            cleanCaptcha(key);
        }
        
        return isValid;
    }

    @Override
    public String getType() {
        return "image";
    }

    @Override
    public void cleanCaptcha(String key) {
        redisUtil.delete("captcha:" + key);
    }
    
    /**
     * 生成随机验证码字符串
     */
    private String generateCode(int codeCount) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < codeCount; i++) {
            builder.append(CHAR_SET.charAt(random.nextInt(CHAR_SET.length())));
        }
        return builder.toString();
    }
    
    /**
     * 绘制验证码字符
     */
    private void drawString(Graphics g, String code, int width, int height) {
        g.setFont(new Font("Arial", Font.BOLD, 20));
        
        int charCount = code.length();
        int charWidth = (width - 40) / charCount;
        
        for (int i = 0; i < charCount; i++) {
            g.setColor(getRandomColor());
            g.drawString(String.valueOf(code.charAt(i)), 20 + i * charWidth, (height / 2) + 5 + random.nextInt(10));
        }
    }
    
    /**
     * 绘制干扰线
     */
    private void drawLine(Graphics g, int width, int height) {
        g.setColor(getRandomColor());
        int x1 = random.nextInt(width);
        int y1 = random.nextInt(height);
        int x2 = random.nextInt(width);
        int y2 = random.nextInt(height);
        g.drawLine(x1, y1, x2, y2);
    }
    
    /**
     * 绘制噪点
     */
    private void drawDot(Graphics g, int width, int height) {
        g.setColor(getRandomColor());
        g.fillRect(random.nextInt(width), random.nextInt(height), 2, 2);
    }
    
    /**
     * 获取随机颜色
     */
    private Color getRandomColor() {
        return new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200));
    }
    
    /**
     * 将BufferedImage转换为Base64字符串
     */
    private String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码图片转换失败");
        }
    }
}