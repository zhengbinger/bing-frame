package com.bing.framework.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 密码强度验证工具类
 * 用于验证密码是否符合安全要求
 * 
 * @author zhengbing
 * @date 2025-11-10
 */
@Component
@Slf4j
public class PasswordValidator {
    
    // 密码最小长度
    private static final int MIN_LENGTH = 8;
    // 密码最大长度
    private static final int MAX_LENGTH = 20;
    
    // 包含至少一个大写字母
    private static final String UPPERCASE_PATTERN = ".*[A-Z].*";
    // 包含至少一个小写字母
    private static final String LOWERCASE_PATTERN = ".*[a-z].*";
    // 包含至少一个数字
    private static final String DIGIT_PATTERN = ".*\\d.*";
    // 包含至少一个特殊字符
    private static final String SPECIAL_CHAR_PATTERN = ".*[!@#$%^&*(),.?\":{}|<>].*";
    
    /**
     * 验证密码强度
     * 
     * @param password 待验证的密码
     * @return 验证结果，如果密码符合要求则返回null，否则返回错误消息
     */
    public String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "密码不能为空";
        }
        
        // 检查密码长度
        if (password.length() < MIN_LENGTH) {
            return "密码长度不能少于" + MIN_LENGTH + "位";
        }
        
        if (password.length() > MAX_LENGTH) {
            return "密码长度不能超过" + MAX_LENGTH + "位";
        }
        
        // 检查是否包含大写字母
        if (!containsPattern(password, UPPERCASE_PATTERN)) {
            return "密码必须包含至少一个大写字母";
        }
        
        // 检查是否包含小写字母
        if (!containsPattern(password, LOWERCASE_PATTERN)) {
            return "密码必须包含至少一个小写字母";
        }
        
        // 检查是否包含数字
        if (!containsPattern(password, DIGIT_PATTERN)) {
            return "密码必须包含至少一个数字";
        }
        
        // 检查是否包含特殊字符
        if (!containsPattern(password, SPECIAL_CHAR_PATTERN)) {
            return "密码必须包含至少一个特殊字符";
        }
        
        // 检查是否包含连续字符（如123456或abcdef）
        if (containsConsecutiveCharacters(password)) {
            return "密码不能包含连续字符";
        }
        
        // 检查是否包含重复字符
        if (containsRepeatedCharacters(password)) {
            return "密码不能包含连续重复的字符";
        }
        
        return null; // 密码符合要求
    }
    
    /**
     * 检查密码是否包含指定的模式
     * 
     * @param password 密码
     * @param pattern 正则表达式模式
     * @return 是否包含指定模式
     */
    private boolean containsPattern(String password, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(password);
        return m.matches();
    }
    
    /**
     * 检查密码是否包含连续字符
     * 
     * @param password 密码
     * @return 是否包含连续字符
     */
    private boolean containsConsecutiveCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);
            
            // 检查是否是连续递增的字符
            if (c2 - c1 == 1 && c3 - c2 == 1) {
                return true;
            }
            
            // 检查是否是连续递减的字符
            if (c1 - c2 == 1 && c2 - c3 == 1) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查密码是否包含重复字符
     * 
     * @param password 密码
     * @return 是否包含重复字符
     */
    private boolean containsRepeatedCharacters(String password) {
        for (int i = 0; i < password.length() - 1; i++) {
            if (password.charAt(i) == password.charAt(i + 1)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取密码强度等级（1-5级）
     * 
     * @param password 密码
     * @return 密码强度等级
     */
    public int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int strength = 0;
        
        // 基础长度分
        if (password.length() >= MIN_LENGTH) {
            strength++;
        }
        if (password.length() >= 12) {
            strength++;
        }
        if (password.length() >= 16) {
            strength++;
        }
        
        // 复杂度分
        if (containsPattern(password, UPPERCASE_PATTERN)) {
            strength++;
        }
        if (containsPattern(password, LOWERCASE_PATTERN)) {
            strength++;
        }
        if (containsPattern(password, DIGIT_PATTERN)) {
            strength++;
        }
        if (containsPattern(password, SPECIAL_CHAR_PATTERN)) {
            strength++;
        }
        
        // 减分项
        if (containsConsecutiveCharacters(password)) {
            strength--;
        }
        if (containsRepeatedCharacters(password)) {
            strength--;
        }
        
        // 确保强度等级在1-5之间
        return Math.max(1, Math.min(5, strength));
    }
}