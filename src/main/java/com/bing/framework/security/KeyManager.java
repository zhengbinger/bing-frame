/**
 * 密钥管理器
 * 提供密钥的生成、存储、获取和轮换功能
 * 支持多环境密钥管理和密钥版本控制
 * 
 * @author zhengbing
 * @date 2025-11-20
 */
package com.bing.framework.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;

@Slf4j
@Configuration
public class KeyManager {
    
    // 密钥存储
    private final Map<String, String> keyStore = new ConcurrentHashMap<>();
    
    // 当前活动密钥
    private String activeKey;
    
    // 密钥版本前缀
    private static final String KEY_VERSION_PREFIX = "key_v";
    
    // 注入配置的密钥
    @Value("${app.encryption.key:}")
    private String configuredKey;
    
    @Value("${app.encryption.key-file:}")
    private String keyFilePath;
    
    @Value("${app.encryption.key-version:1}")
    private int keyVersion;
    
    // 资源加载器，用于读取密钥文件
    private final ResourceLoader resourceLoader;
    
    public KeyManager(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * 初始化密钥管理器
     */
    @PostConstruct
    public void init() {
        log.info("初始化密钥管理器...");
        
        try {
            // 加载密钥
            loadKeys();
            
            // 设置活动密钥
            if (StringUtils.hasText(activeKey)) {
                log.info("密钥管理器初始化完成，当前活动密钥版本: v{}", keyVersion);
            } else {
                log.warn("未找到有效的加密密钥，将使用默认生成的临时密钥");
                generateAndSetActiveKey();
            }
        } catch (Exception e) {
            log.error("初始化密钥管理器失败", e);
            throw new RuntimeException("初始化密钥管理器失败", e);
        }
    }
    
    /**
     * 加载所有可用密钥
     */
    private void loadKeys() {
        // 1. 尝试从配置属性加载密钥
        if (StringUtils.hasText(configuredKey)) {
            String versionedKeyName = KEY_VERSION_PREFIX + keyVersion;
            keyStore.put(versionedKeyName, configuredKey);
            activeKey = configuredKey;
            log.info("从配置属性加载密钥，版本: v{}", keyVersion);
            return;
        }
        
        // 2. 尝试从密钥文件加载密钥
        if (StringUtils.hasText(keyFilePath)) {
            try {
                String key = loadKeyFromFile(keyFilePath);
                if (StringUtils.hasText(key)) {
                    String versionedKeyName = KEY_VERSION_PREFIX + keyVersion;
                    keyStore.put(versionedKeyName, key);
                    activeKey = key;
                    log.info("从密钥文件加载密钥，版本: v{}", keyVersion);
                    return;
                }
            } catch (Exception e) {
                log.error("从密钥文件加载密钥失败: {}", keyFilePath, e);
            }
        }
        
        // 3. 尝试从环境变量加载密钥
        String envKey = System.getenv("ENCRYPTION_KEY");
        if (StringUtils.hasText(envKey)) {
            String versionedKeyName = KEY_VERSION_PREFIX + keyVersion;
            keyStore.put(versionedKeyName, envKey);
            activeKey = envKey;
            log.info("从环境变量加载密钥，版本: v{}", keyVersion);
        }
    }
    
    /**
     * 从文件加载密钥
     */
    private String loadKeyFromFile(String filePath) throws Exception {
        Resource resource = resourceLoader.getResource(filePath);
        if (resource.exists()) {
            return new String(Files.readAllBytes(Paths.get(resource.getURI()))).trim();
        } else {
            // 尝试直接作为文件路径读取
            if (Files.exists(Paths.get(filePath))) {
                return new String(Files.readAllBytes(Paths.get(filePath))).trim();
            }
        }
        return null;
    }
    
    /**
     * 生成并设置新的活动密钥
     */
    public String generateAndSetActiveKey() {
        String newKey = EncryptUtil.generateKey();
        int newVersion = keyVersion + 1;
        String versionedKeyName = KEY_VERSION_PREFIX + newVersion;
        
        keyStore.put(versionedKeyName, newKey);
        activeKey = newKey;
        keyVersion = newVersion;
        
        log.info("生成新的活动密钥，版本: v{}", newVersion);
        return newKey;
    }
    
    /**
     * 获取当前活动密钥
     */
    public String getActiveKey() {
        if (!StringUtils.hasText(activeKey)) {
            throw new IllegalStateException("未设置活动密钥");
        }
        return activeKey;
    }
    
    /**
     * 根据版本获取密钥
     */
    public String getKeyByVersion(int version) {
        String versionedKeyName = KEY_VERSION_PREFIX + version;
        String key = keyStore.get(versionedKeyName);
        
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("未找到版本为 v" + version + " 的密钥");
        }
        
        return key;
    }
    
    /**
     * 密钥轮换
     * 生成新密钥并更新活动密钥，但保留旧密钥用于解密
     */
    public void rotateKey() {
        generateAndSetActiveKey();
        log.info("密钥轮换完成，新密钥版本: v{}", keyVersion);
    }
    
    /**
     * 获取当前密钥版本
     */
    public int getCurrentKeyVersion() {
        return keyVersion;
    }
    
    /**
     * 检查密钥是否有效
     */
    public boolean isValidKey(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        
        try {
            // 验证密钥格式是否为有效的Base64编码的256位密钥
            byte[] keyBytes = Base64.getDecoder().decode(key);
            return keyBytes.length == 32; // 256位 = 32字节
        } catch (Exception e) {
            log.debug("无效的密钥格式", e);
            return false;
        }
    }
    
    /**
     * 清除所有密钥（仅用于测试环境）
     */
    public void clearAllKeys() {
        keyStore.clear();
        activeKey = null;
        keyVersion = 1;
        log.warn("已清除所有密钥，仅应在测试环境使用此功能");
    }
}