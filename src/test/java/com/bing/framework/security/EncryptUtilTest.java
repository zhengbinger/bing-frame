/**
 * EncryptUtil单元测试
 * 验证配置文件密码加密工具类的加密解密功能
 * 
 * @author zhengbing
 * @date 2025-11-20
 */
package com.bing.framework.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EncryptUtilTest {
    
    private String testKey;
    private String testPlaintext;
    
    @BeforeEach
    void setUp() {
        // 生成测试密钥
        testKey = EncryptUtil.generateKey();
        testPlaintext = "测试明文数据123!@#";
    }
    
    /**
     * 测试密钥生成功能
     */
    @Test
    void testGenerateKey() {
        String key1 = EncryptUtil.generateKey();
        String key2 = EncryptUtil.generateKey();
        
        assertNotNull(key1);
        assertNotNull(key2);
        // 确保生成的密钥是唯一的
        assertNotEquals(key1, key2);
        // 确保密钥长度合理（Base64编码的256位密钥）
        assertTrue(key1.length() > 30);
    }
    
    /**
     * 测试加密解密功能
     */
    @Test
    void testEncryptAndDecrypt() {
        // 加密数据
        String encrypted = EncryptUtil.encrypt(testPlaintext, testKey);
        assertNotNull(encrypted);
        assertNotEquals(testPlaintext, encrypted);
        
        // 解密数据
        String decrypted = EncryptUtil.decrypt(encrypted, testKey);
        assertNotNull(decrypted);
        assertEquals(testPlaintext, decrypted);
    }
    
    /**
     * 测试ENC()格式包装和解包功能
     */
    @Test
    void testEncryptedValueFormat() {
        // 加密数据
        String encrypted = EncryptUtil.encrypt(testPlaintext, testKey);
        
        // 包装为ENC()格式
        String wrappedValue = EncryptUtil.wrapEncryptedValue(encrypted);
        assertTrue(wrappedValue.startsWith("ENC("));
        assertTrue(wrappedValue.endsWith(")"));
        
        // 检查是否识别为加密值
        assertTrue(EncryptUtil.isEncryptedValue(wrappedValue));
        assertFalse(EncryptUtil.isEncryptedValue(encrypted));
        assertFalse(EncryptUtil.isEncryptedValue(testPlaintext));
        
        // 提取加密内容
        String extracted = EncryptUtil.extractEncryptedContent(wrappedValue);
        assertEquals(encrypted, extracted);
        
        // 直接解密ENC()格式的值
        String decrypted = EncryptUtil.decrypt(wrappedValue, testKey);
        assertEquals(testPlaintext, decrypted);
    }
    
    /**
     * 测试空值处理
     */
    @Test
    void testEmptyValues() {
        // 空明文加密
        String encryptedNull = EncryptUtil.encrypt(null, testKey);
        assertNull(encryptedNull);
        
        String encryptedEmpty = EncryptUtil.encrypt("", testKey);
        assertEquals("", encryptedEmpty);
        
        // 空密文解密
        String decryptedNull = EncryptUtil.decrypt(null, testKey);
        assertNull(decryptedNull);
        
        String decryptedEmpty = EncryptUtil.decrypt("", testKey);
        assertEquals("", decryptedEmpty);
    }
    
    /**
     * 测试无效加密值格式
     */
    @Test
    void testInvalidEncryptedValueFormat() {
        // 无效的ENC格式
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptUtil.extractEncryptedContent("ENCinvalid");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            EncryptUtil.extractEncryptedContent("invalid)ENC(");
        });
    }
    
    /**
     * 测试错误密钥解密
     */
    @Test
    void testDecryptWithWrongKey() {
        String encrypted = EncryptUtil.encrypt(testPlaintext, testKey);
        String wrongKey = EncryptUtil.generateKey(); // 生成不同的密钥
        
        // 使用错误密钥解密应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            EncryptUtil.decrypt(encrypted, wrongKey);
        });
    }
    
    /**
     * 测试特殊字符处理
     */
    @Test
    void testSpecialCharacters() {
        String specialText = "特殊字符测试!@#$%^&*()_+-=[]{}|;':,.<>?/";
        
        String encrypted = EncryptUtil.encrypt(specialText, testKey);
        String decrypted = EncryptUtil.decrypt(encrypted, testKey);
        
        assertEquals(specialText, decrypted);
    }
}