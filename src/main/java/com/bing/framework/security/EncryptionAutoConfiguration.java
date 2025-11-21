/**
 * 加密功能自动配置类
 * 提供配置文件密码加密功能的自动装配
 * 自动注册加密相关的组件和配置
 * 
 * @author zhengbing
 * @date 2025-11-20
 */
package com.bing.framework.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 加密功能自动配置类
 * 当app.encryption.enabled=true时启用
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(EncryptionAutoConfiguration.EncryptionProperties.class)
@ConditionalOnProperty(prefix = "app.encryption", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EncryptionAutoConfiguration {
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    /**
     * 配置密钥管理器
     */
    @Bean
    public KeyManager keyManager(EncryptionProperties properties) {
        log.info("初始化密钥管理器，加密功能已启用");
        return new KeyManager(resourceLoader);
    }
    
    /**
     * 配置加密工具类的Bean实例
     * 方便在应用中注入使用
     */
    @Bean
    public EncryptUtil encryptUtil() {
        log.info("配置加密工具类Bean");
        return new EncryptUtil();
    }
    
    /**
     * 加密配置属性类
     * 用于绑定app.encryption前缀的配置属性
     */
    @ConfigurationProperties(prefix = "app.encryption")
    public static class EncryptionProperties {
        
        // 是否启用加密功能
        private boolean enabled = true;
        
        // 加密密钥
        private String key;
        
        // 密钥文件路径
        private String keyFile;
        
        // 当前密钥版本
        private int keyVersion = 1;
        
        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getKey() {
            return key;
        }
        
        public void setKey(String key) {
            this.key = key;
        }
        
        public String getKeyFile() {
            return keyFile;
        }
        
        public void setKeyFile(String keyFile) {
            this.keyFile = keyFile;
        }
        
        public int getKeyVersion() {
            return keyVersion;
        }
        
        public void setKeyVersion(int keyVersion) {
            this.keyVersion = keyVersion;
        }
    }
}