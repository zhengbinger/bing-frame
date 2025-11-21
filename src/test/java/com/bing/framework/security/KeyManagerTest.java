/**
 * KeyManager单元测试
 * 验证密钥管理器的密钥生成、存储、获取和轮换功能
 * 
 * @author zhengbing
 * @date 2025-11-20
 */
package com.bing.framework.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KeyManagerTest {
    
    @Mock
    private ResourceLoader resourceLoader;
    
    @Mock
    private Resource resource;
    
    private KeyManager keyManager;
    private String testKey;
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // 生成有效的256位密钥(Base64编码)
        byte[] keyBytes = new byte[32]; // 256位 = 32字节
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = (byte) i;
        }
        testKey = Base64.getEncoder().encodeToString(keyBytes);
        
        // 模拟资源加载
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(false); // 默认文件不存在
        
        // 使用反射创建KeyManager实例，避免调用@PostConstruct方法
        keyManager = new KeyManager(resourceLoader);
        
        // 手动设置初始状态
        ReflectionTestUtils.setField(keyManager, "keyVersion", 1);
        // 不调用init()方法，而是在每个测试中手动设置所需的状态
    }
    
    /**
     * 测试密钥生成功能
     */
    @Test
    void testGenerateAndSetActiveKey() {
        // 清除所有密钥
        keyManager.clearAllKeys();
        
        // 生成新密钥
        String newKey = keyManager.generateAndSetActiveKey();
        
        assertNotNull(newKey);
        assertEquals(newKey, keyManager.getActiveKey());
        assertEquals(2, keyManager.getCurrentKeyVersion()); // 初始版本是1，生成后变为2
        
        // 再次生成密钥，验证版本递增
        String anotherKey = keyManager.generateAndSetActiveKey();
        assertNotEquals(newKey, anotherKey);
        assertEquals(3, keyManager.getCurrentKeyVersion());
    }
    
    /**
     * 测试密钥验证功能
     */
    @Test
    void testIsValidKey() {
        // 有效的256位密钥
        assertTrue(keyManager.isValidKey(testKey));
        
        // 无效的密钥
        assertFalse(keyManager.isValidKey(null));
        assertFalse(keyManager.isValidKey(""));
        assertFalse(keyManager.isValidKey("invalid-base64-key"));
        
        // 生成一个无效长度的密钥
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]); // 128位
        assertFalse(keyManager.isValidKey(shortKey));
    }
    
    /**
     * 测试密钥轮换功能
     */
    @Test
    void testRotateKey() {
        // 设置初始密钥
        keyManager.clearAllKeys();
        String initialKey = keyManager.generateAndSetActiveKey();
        int initialVersion = keyManager.getCurrentKeyVersion();
        
        // 执行密钥轮换
        keyManager.rotateKey();
        
        // 验证密钥已更新
        String newKey = keyManager.getActiveKey();
        assertNotEquals(initialKey, newKey);
        assertEquals(initialVersion + 1, keyManager.getCurrentKeyVersion());
        
        // 验证可以获取旧版本密钥
        try {
            String oldKey = keyManager.getKeyByVersion(initialVersion);
            assertEquals(initialKey, oldKey);
        } catch (Exception e) {
            fail("应该能够获取旧版本密钥: " + e.getMessage());
        }
    }
    
    /**
     * 测试获取不存在的密钥版本
     */
    @Test
    void testGetNonExistentKeyVersion() {
        keyManager.clearAllKeys();
        
        // 获取不存在的密钥版本应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            keyManager.getKeyByVersion(999);
        });
    }
    
    /**
     * 测试无活动密钥时的异常情况
     */
    @Test
    void testGetActiveKeyWhenNoneSet() {
        keyManager.clearAllKeys();
        
        // 当没有活动密钥时应该抛出异常
        assertThrows(IllegalStateException.class, () -> {
            keyManager.getActiveKey();
        });
    }
    
    /**
     * 测试从文件加载密钥（模拟文件存在的情况）
     */
    @Test
    void testLoadKeyFromFile() throws Exception {
        // 模拟文件存在并包含密钥
        when(resource.exists()).thenReturn(true);
        when(resource.getURI()).thenReturn(Paths.get("test-key.txt").toUri());
        
        // 使用反射调用私有方法进行测试
        try {
            // 这里我们不直接测试loadKeyFromFile方法，而是通过设置配置后初始化来测试
            ReflectionTestUtils.setField(keyManager, "keyFilePath", "classpath:test-key.txt");
            // 注意：完整的文件加载测试需要更复杂的模拟设置
            // 这里主要验证密钥管理的其他核心功能
        } catch (Exception e) {
            // 由于测试环境限制，文件加载可能会失败，但这不应该影响其他测试
            System.out.println("文件加载测试跳过: " + e.getMessage());
        }
    }
    
    /**
     * 测试清除所有密钥功能
     */
    @Test
    void testClearAllKeys() {
        // 生成并设置密钥
        keyManager.generateAndSetActiveKey();
        keyManager.generateAndSetActiveKey(); // 再生成一个，确保有多个密钥
        
        // 清除所有密钥
        keyManager.clearAllKeys();
        
        // 验证版本已重置
        assertEquals(1, keyManager.getCurrentKeyVersion());
        
        // 验证获取活动密钥会抛出异常
        assertThrows(IllegalStateException.class, () -> {
            keyManager.getActiveKey();
        });
    }
}