package com.bing.framework.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;

/**
 * 安全工具类
 * 提供密码加密、验证等安全相关功能
 * 使用hutool库的加密工具，提供SHA256等安全的加密算法
 * 
 * @author zhengbing
 * @date 2025-11-03
 */
public class SecurityUtil {

    /**
     * 默认盐值，实际项目中可以使用动态盐值
     */
    private static final String DEFAULT_SALT = "bing-framework-salt-2025";
    
    /**
     * 使用SHA256算法对密码进行加密
     * 
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        if (password == null) {
            return null;
        }
        // 密码加盐，增强安全性
        String saltedPassword = password + DEFAULT_SALT;
        // 使用SHA256加密
        return DigestUtil.sha256Hex(saltedPassword);
    }
    
    /**
     * 验证密码是否正确
     * 
     * @param inputPassword 输入的密码
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verifyPassword(String inputPassword, String encryptedPassword) {
        if (inputPassword == null || encryptedPassword == null) {
            return false;
        }
        String encryptedInput = encryptPassword(inputPassword);
        return encryptedInput.equals(encryptedPassword);
    }
    
    /**
     * 生成随机密码
     * 
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        // 密码字符集：包含大小写字母、数字和特殊字符
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = SecureUtil.random().nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
}