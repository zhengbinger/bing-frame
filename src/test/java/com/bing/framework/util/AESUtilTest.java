package com.bing.framework.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AES加密工具类测试类
 * 测试AES加密解密功能，包括正常加密解密、异常处理、密钥生成等场景
 * 
 * @author zhengbing
 * @date 2025-11-17
 */
class AESUtilTest {

    private static final String TEST_DATA = "Hello, Bing Framework! 这是一段测试数据。";
    private static final String TEST_KEY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456"; // 32字节密钥
    private static final String DEFAULT_KEY = "BingFrameworkKey2025!";
    private static final String INVALID_KEY_TOO_SHORT = "Short";
    private static final String INVALID_KEY_TOO_LONG = "ThisKeyIsWayTooLongForAES256AndShouldFailValidation";
    
    private String encryptedData;
    private String encryptedDataWithCustomKey;

    @BeforeEach
    void setUp() throws Exception {
        // 预生成加密数据用于测试
        encryptedData = AESUtil.encrypt(TEST_DATA);
        encryptedDataWithCustomKey = AESUtil.encrypt(TEST_DATA, TEST_KEY);
    }

    @Test
    @DisplayName("生成AES密钥")
    void testGenerateKey() throws Exception {
        // 执行测试
        SecretKey key = AESUtil.generateKey();
        
        // 验证结果
        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
        assertEquals(256, key.getEncoded().length * 8); // 256位密钥
    }

    @Test
    @DisplayName("使用默认密钥加密数据")
    void testEncryptWithDefaultKey() throws Exception {
        // 执行测试
        String result = AESUtil.encrypt(TEST_DATA);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertNotEquals(TEST_DATA, result); // 加密后应该不同于原始数据
        
        // 验证是有效的Base64编码
        assertDoesNotThrow(() -> Base64.getDecoder().decode(result));
        
        // 验证可以正确解密
        String decrypted = AESUtil.decrypt(result);
        assertEquals(TEST_DATA, decrypted);
    }

    @Test
    @DisplayName("使用自定义密钥加密数据")
    void testEncryptWithCustomKey() throws Exception {
        // 执行测试
        String result = AESUtil.encrypt(TEST_DATA, TEST_KEY);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertNotEquals(TEST_DATA, result);
        
        // 验证是有效的Base64编码
        assertDoesNotThrow(() -> Base64.getDecoder().decode(result));
        
        // 验证可以正确解密
        String decrypted = AESUtil.decrypt(result, TEST_KEY);
        assertEquals(TEST_DATA, decrypted);
    }

    @Test
    @DisplayName("加密空字符串")
    void testEncryptEmptyString() throws Exception {
        // 执行测试
        String result = AESUtil.encrypt("");
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isEmpty()); // 空字符串加密后仍有内容
        
        // 验证可以正确解密
        String decrypted = AESUtil.decrypt(result);
        assertEquals("", decrypted);
    }

    @Test
    @DisplayName("加密null值")
    void testEncryptNullValue() {
        // 执行测试并验证异常
        assertThrows(Exception.class, () -> {
            AESUtil.encrypt(null);
        });
    }

    @Test
    @DisplayName("使用默认密钥解密数据")
    void testDecryptWithDefaultKey() throws Exception {
        // 执行测试
        String result = AESUtil.decrypt(encryptedData);
        
        // 验证结果
        assertEquals(TEST_DATA, result);
    }

    @Test
    @DisplayName("使用自定义密钥解密数据")
    void testDecryptWithCustomKey() throws Exception {
        // 执行测试
        String result = AESUtil.decrypt(encryptedDataWithCustomKey, TEST_KEY);
        
        // 验证结果
        assertEquals(TEST_DATA, result);
    }

    @Test
    @DisplayName("解密无效的Base64数据")
    void testDecryptInvalidBase64() {
        // 执行测试并验证异常
        assertThrows(Exception.class, () -> {
            AESUtil.decrypt("invalid_base64_data!");
        });
    }

    @Test
    @DisplayName("解密非AES加密的数据")
    void testDecryptNonAESData() {
        // 执行测试并验证异常
        assertThrows(Exception.class, () -> {
            AESUtil.decrypt("This is just plain text, not encrypted data");
        });
    }

    @Test
    @DisplayName("解密null值")
    void testDecryptNullValue() {
        // 执行测试并验证异常
        assertThrows(Exception.class, () -> {
            AESUtil.decrypt(null);
        });
    }

    @Test
    @DisplayName("解密空字符串")
    void testDecryptEmptyString() {
        // 空字符串应该抛出异常，因为正常的AES加密不会产生空字符串
        assertThrows(Exception.class, () -> {
            AESUtil.decrypt("");
        });
    }

    @Test
    @DisplayName("使用错误密钥解密")
    void testDecryptWithWrongKey() throws Exception {
        String wrongKey = "WrongKey123456789012345678901234";
        
        // 执行测试并验证异常
        assertThrows(Exception.class, () -> {
            AESUtil.decrypt(encryptedDataWithCustomKey, wrongKey);
        });
    }

    @Test
    @DisplayName("加密解密中文字符")
    void testEncryptDecryptChineseCharacters() throws Exception {
        // 测试数据包含中文字符
        String chineseData = "这是中文测试数据！包含特殊字符：@#$%^&*()_+-={}[]|\\:;\"'<>,.?/~`";
        
        // 加密
        String encrypted = AESUtil.encrypt(chineseData);
        
        // 解密
        String decrypted = AESUtil.decrypt(encrypted);
        
        // 验证结果
        assertEquals(chineseData, decrypted);
    }

    @Test
    @DisplayName("加密解密特殊字符")
    void testEncryptDecryptSpecialCharacters() throws Exception {
        // 测试数据包含各种特殊字符
        String specialData = "!@#$%^&*()_+-={}[]|\\:;\"'<>,.?/~`\n\t\r\b\f";
        
        // 加密
        String encrypted = AESUtil.encrypt(specialData);
        
        // 解密
        String decrypted = AESUtil.decrypt(encrypted);
        
        // 验证结果
        assertEquals(specialData, decrypted);
    }

    @Test
    @DisplayName("加密解密长字符串")
    void testEncryptDecryptLongString() throws Exception {
        // 构建长字符串
        StringBuilder longDataBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longDataBuilder.append("这是一段测试数据").append(i).append(" ");
        }
        String longData = longDataBuilder.toString();
        
        // 加密
        String encrypted = AESUtil.encrypt(longData);
        
        // 解密
        String decrypted = AESUtil.decrypt(encrypted);
        
        // 验证结果
        assertEquals(longData, decrypted);
    }

    @Test
    @DisplayName("相同数据多次加密结果不同")
    void testSameDataDifferentEncryption() throws Exception {
        // 多次加密相同数据
        String encrypted1 = AESUtil.encrypt(TEST_DATA);
        String encrypted2 = AESUtil.encrypt(TEST_DATA);
        String encrypted3 = AESUtil.encrypt(TEST_DATA);
        
        // 验证结果
        assertNotEquals(encrypted1, encrypted2);
        assertNotEquals(encrypted2, encrypted3);
        assertNotEquals(encrypted1, encrypted3);
        
        // 但都可以正确解密
        assertEquals(TEST_DATA, AESUtil.decrypt(encrypted1));
        assertEquals(TEST_DATA, AESUtil.decrypt(encrypted2));
        assertEquals(TEST_DATA, AESUtil.decrypt(encrypted3));
    }

    @Test
    @DisplayName("使用不同密钥加密相同数据")
    void testSameDataDifferentKeys() throws Exception {
        String key1 = "12345678901234567890123456789011"; // 32字节
        String key2 = "12345678901234567890123456789012"; // 32字节
        
        // 使用不同密钥加密相同数据
        String encrypted1 = AESUtil.encrypt(TEST_DATA, key1);
        String encrypted2 = AESUtil.encrypt(TEST_DATA, key2);
        
        // 验证结果
        assertNotEquals(encrypted1, encrypted2);
        
        // 交叉验证应该失败
        assertThrows(Exception.class, () -> {
            AESUtil.decrypt(encrypted1, key2);
        });
        
        assertThrows(Exception.class, () -> {
            AESUtil.decrypt(encrypted2, key1);
        });
    }

    @Test
    @DisplayName("密钥长度验证")
    void testKeyLengthValidation() throws Exception {
        // 测试有效长度的密钥（16字节和32字节）
        String[] validKeys = {
            "1234567890123456", // 16字节 (128位)
            "12345678901234567890123456789012" // 32字节 (256位)
        };
        
        for (String key : validKeys) {
            assertDoesNotThrow(() -> {
                String encrypted = AESUtil.encrypt(TEST_DATA, key);
                String decrypted = AESUtil.decrypt(encrypted, key);
                assertEquals(TEST_DATA, decrypted);
            }, "密钥: " + key + " (" + key.length() + "字节) 应该可以正常使用");
        }
        
        // 测试无效长度的密钥（应该抛出异常）
        String[] invalidKeys = {
            "12", // 2字节 - 太短
            "12345678901234567890123456789012345678901234567890" // 50字节 - 太长
        };
        
        for (String key : invalidKeys) {
            assertThrows(Exception.class, () -> {
                AESUtil.encrypt(TEST_DATA, key);
            }, "密钥: " + key + " (" + key.length() + "字节) 应该抛出异常");
        }
    }

    @Test
    @DisplayName("加密解密二进制安全")
    void testBinarySafe() throws Exception {
        // 测试包含0字节的数据
        String binaryData = "test\0data\0with\0nulls";
        
        // 加密
        String encrypted = AESUtil.encrypt(binaryData);
        
        // 解密
        String decrypted = AESUtil.decrypt(encrypted);
        
        // 验证结果
        assertEquals(binaryData, decrypted);
    }

    @Test
    @DisplayName("加密性能测试 - 大数据量")
    void testPerformanceWithLargeData() throws Exception {
        // 构建大数据
        StringBuilder largeDataBuilder = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeDataBuilder.append("批量测试数据 ").append(i).append(" ");
        }
        String largeData = largeDataBuilder.toString();
        
        // 测量加密时间
        long startTime = System.currentTimeMillis();
        String encrypted = AESUtil.encrypt(largeData);
        long encryptTime = System.currentTimeMillis() - startTime;
        
        // 测量解密时间
        startTime = System.currentTimeMillis();
        String decrypted = AESUtil.decrypt(encrypted);
        long decryptTime = System.currentTimeMillis() - startTime;
        
        // 验证结果
        assertEquals(largeData, decrypted);
        
        // 验证性能合理（不超过10秒）
        assertTrue(encryptTime < 10000, "加密时间过长: " + encryptTime + "ms");
        assertTrue(decryptTime < 10000, "解密时间过长: " + decryptTime + "ms");
    }
}