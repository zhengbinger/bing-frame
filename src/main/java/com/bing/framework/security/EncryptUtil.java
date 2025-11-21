/**
 * 配置文件密码加密工具类
 * 提供基于AES-256-GCM的配置文件密码加密解密功能
 * 支持ENC()格式标记的配置值自动识别
 * 
 * @author zhengbing
 * @date 2025-11-20
 */
package com.bing.framework.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class EncryptUtil {
    
    // 加密算法配置
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256; // 256位密钥
    private static final int GCM_IV_LENGTH = 12; // GCM推荐IV长度
    private static final int GCM_TAG_LENGTH = 128; // 认证标签长度
    
    // 加密值标识前缀和后缀
    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";
    
    /**
     * 生成AES密钥
     * @return Base64编码的密钥字符串
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("生成加密密钥失败", e);
            throw new RuntimeException("生成加密密钥失败", e);
        }
    }
    
    /**
     * 检查字符串是否为加密格式
     * @param value 要检查的值
     * @return 是否为ENC()格式
     */
    public static boolean isEncryptedValue(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        return value.startsWith(ENC_PREFIX) && value.endsWith(ENC_SUFFIX);
    }
    
    /**
     * 从加密格式中提取加密内容
     * @param encryptedValue ENC()格式的加密值
     * @return 提取出的加密内容
     */
    public static String extractEncryptedContent(String encryptedValue) {
        if (!isEncryptedValue(encryptedValue)) {
            throw new IllegalArgumentException("无效的加密值格式: " + encryptedValue);
        }
        return encryptedValue.substring(ENC_PREFIX.length(), encryptedValue.length() - ENC_SUFFIX.length());
    }
    
    /**
     * 加密数据
     * @param data 明文数据
     * @param key Base64编码的密钥
     * @return 加密后的数据（包含IV和认证标签）
     */
    public static String encrypt(String data, String key) {
        if (!StringUtils.hasText(data)) {
            return data;
        }
        
        try {
            // 解码Base64密钥
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
            
            // 执行加密
            byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));
            
            // 将IV和加密数据合并
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            // Base64编码返回
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("加密数据失败", e);
            throw new RuntimeException("加密数据失败", e);
        }
    }
    
    /**
     * 解密数据
     * @param encryptedData 加密数据（包含IV和认证标签）
     * @param key Base64编码的密钥
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedData, String key) {
        if (!StringUtils.hasText(encryptedData)) {
            return encryptedData;
        }
        
        try {
            // 如果是ENC()格式，先提取内容
            if (isEncryptedValue(encryptedData)) {
                encryptedData = extractEncryptedContent(encryptedData);
            }
            
            // 解码Base64密钥
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            
            // 解码Base64加密数据
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            
            // 提取IV和加密数据
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            
            byte[] encrypted = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);
            
            // 初始化解密器
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
            
            // 执行解密
            byte[] decryptedData = cipher.doFinal(encrypted);
            return new String(decryptedData, "UTF-8");
        } catch (Exception e) {
            log.error("解密数据失败", e);
            throw new RuntimeException("解密数据失败", e);
        }
    }
    
    /**
     * 包装加密值为ENC()格式
     * @param encryptedValue 加密后的值
     * @return ENC()格式的加密值
     */
    public static String wrapEncryptedValue(String encryptedValue) {
        return ENC_PREFIX + encryptedValue + ENC_SUFFIX;
    }
    
    /**
     * 主方法，用于命令行加密解密操作
     */
    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("用法：java -cp app.jar com.bing.framework.security.EncryptUtil [generateKey|encrypt|decrypt] [参数...]");
                return;
            }
            
            String command = args[0];
            
            switch (command) {
                case "generateKey":
                    String key = generateKey();
                    System.out.println("生成的密钥: " + key);
                    break;
                    
                case "encrypt":
                    if (args.length < 3) {
                        System.out.println("用法：encrypt [明文] [密钥]");
                        return;
                    }
                    String plaintext = args[1];
                    String encryptKey = args[2];
                    String encrypted = encrypt(plaintext, encryptKey);
                    System.out.println("加密结果: " + wrapEncryptedValue(encrypted));
                    break;
                    
                case "decrypt":
                    if (args.length < 3) {
                        System.out.println("用法：decrypt [密文] [密钥]");
                        return;
                    }
                    String ciphertext = args[1];
                    String decryptKey = args[2];
                    String decrypted = decrypt(ciphertext, decryptKey);
                    System.out.println("解密结果: " + decrypted);
                    break;
                    
                default:
                    System.out.println("未知命令: " + command);
                    break;
            }
        } catch (Exception e) {
            System.err.println("操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}