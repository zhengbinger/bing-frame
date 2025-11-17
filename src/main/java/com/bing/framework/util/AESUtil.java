package com.bing.framework.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加密工具类
 * 提供AES加密解密功能，用于敏感数据加密
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
@Slf4j
public class AESUtil {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String DEFAULT_KEY = "12345678901234567890123456789012"; // 32字节密钥
    
    /**
     * 生成AES密钥
     */
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }
    
    /**
     * 加密数据
     */
    public static String encrypt(String data) throws Exception {
        return encrypt(data, DEFAULT_KEY);
    }
    
    /**
     * 使用指定密钥加密数据
     */
    public static String encrypt(String data, String key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        
        // 生成随机IV (16字节)
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        
        // 将IV和加密数据连接
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }
    
    /**
     * 解密数据
     */
    public static String decrypt(String encryptedData) throws Exception {
        return decrypt(encryptedData, DEFAULT_KEY);
    }
    
    /**
     * 使用指定密钥解密数据
     */
    public static String decrypt(String encryptedData, String key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        
        // 解码Base64数据
        byte[] combined = Base64.getDecoder().decode(encryptedData);
        
        // 提取IV (前16字节)
        byte[] iv = new byte[16];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // 提取加密数据
        byte[] encrypted = new byte[combined.length - iv.length];
        System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);
        
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
        byte[] decryptedData = cipher.doFinal(encrypted);
        return new String(decryptedData);
    }
}