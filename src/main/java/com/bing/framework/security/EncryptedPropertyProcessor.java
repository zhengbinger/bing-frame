/**
 * 加密属性处理器
 * 实现Spring Boot的EnvironmentPostProcessor接口，在应用启动时自动解密配置文件中的加密值
 * 支持ENC()格式标记的敏感配置自动解密
 * 
 * @author zhengbing
 * @date 2025-11-20
 */
package com.bing.framework.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EncryptedPropertyProcessor implements EnvironmentPostProcessor {
    
    // 加密配置前缀
    private static final String ENCRYPTION_KEY_PROPERTY = "app.encryption.key";
    private static final String ENCRYPTION_KEY_FILE_PROPERTY = "app.encryption.key-file";
    
    // 需要忽略解密的属性模式
    private static final Pattern IGNORE_PATTERN = Pattern.compile(".*password.*|.*secret.*|.*key.*", Pattern.CASE_INSENSITIVE);
    
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        log.info("开始处理加密配置属性...");
        
        try {
            // 获取加密密钥
            String encryptionKey = getEncryptionKey(environment);
            if (!StringUtils.hasText(encryptionKey)) {
                log.warn("未配置加密密钥，跳过加密属性解密");
                return;
            }
            
            // 遍历所有属性源并解密加密的属性
            decryptProperties(environment, encryptionKey);
            
            log.info("加密配置属性处理完成");
        } catch (Exception e) {
            log.error("处理加密配置属性时发生错误", e);
            throw new RuntimeException("处理加密配置属性失败", e);
        }
    }
    
    /**
     * 获取加密密钥
     * 优先级：1. 环境变量 2. 配置文件属性 3. 密钥文件
     */
    private String getEncryptionKey(ConfigurableEnvironment environment) {
        // 首先尝试从环境变量获取
        String envKey = System.getenv("ENCRYPTION_KEY");
        if (StringUtils.hasText(envKey)) {
            log.info("从环境变量获取加密密钥");
            return envKey;
        }
        
        // 然后尝试从配置属性获取
        String propKey = environment.getProperty(ENCRYPTION_KEY_PROPERTY);
        if (StringUtils.hasText(propKey)) {
            log.info("从配置属性获取加密密钥");
            return propKey;
        }
        
        // 最后尝试从密钥文件获取
        String keyFilePath = environment.getProperty(ENCRYPTION_KEY_FILE_PROPERTY);
        if (StringUtils.hasText(keyFilePath)) {
            log.info("从密钥文件获取加密密钥: {}", keyFilePath);
            try {
                // 这里可以实现从文件读取密钥的逻辑
                // 为简化实现，这里暂不实现文件读取功能
                log.warn("密钥文件读取功能暂未实现");
            } catch (Exception e) {
                log.error("读取密钥文件失败: {}", keyFilePath, e);
            }
        }
        
        return null;
    }
    
    /**
     * 解密环境中的加密属性
     */
    private void decryptProperties(ConfigurableEnvironment environment, String encryptionKey) {
        Map<String, Object> decryptedProperties = new HashMap<>();
        
        // 遍历所有属性源
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) propertySource;
                String[] propertyNames = enumerablePropertySource.getPropertyNames();
                
                for (String propertyName : propertyNames) {
                    Object propertyValue = enumerablePropertySource.getProperty(propertyName);
                    if (propertyValue instanceof String) {
                        String value = (String) propertyValue;
                        // 检查是否为加密值
                        if (EncryptUtil.isEncryptedValue(value)) {
                            try {
                                // 解密属性值
                                String decryptedValue = EncryptUtil.decrypt(value, encryptionKey);
                                decryptedProperties.put(propertyName, decryptedValue);
                                log.debug("解密配置属性: {} (部分隐藏)", maskSensitivePropertyName(propertyName));
                            } catch (Exception e) {
                                log.error("解密配置属性失败: {}", propertyName, e);
                                throw new RuntimeException("解密配置属性失败: " + propertyName, e);
                            }
                        }
                    }
                }
            }
        }
        
        // 将解密后的属性添加到环境中，优先级最高
        if (!decryptedProperties.isEmpty()) {
            environment.getPropertySources().addFirst(
                    new MapPropertySource("decryptedProperties", decryptedProperties));
            log.info("成功解密 {} 个配置属性", decryptedProperties.size());
        }
    }
    
    /**
     * 掩码敏感属性名，用于日志输出
     */
    private String maskSensitivePropertyName(String propertyName) {
        if (IGNORE_PATTERN.matcher(propertyName).matches()) {
            // 对敏感属性名进行部分掩码
            int length = propertyName.length();
            if (length > 8) {
                return propertyName.substring(0, 4) + "****" + propertyName.substring(length - 4);
            } else {
                return "********";
            }
        }
        return propertyName;
    }
    
    /**
     * 自定义MapPropertySource，用于存储解密后的属性
     */
    private static class MapPropertySource extends EnumerablePropertySource<Map<String, Object>> {
        
        public MapPropertySource(String name, Map<String, Object> source) {
            super(name, source);
        }
        
        @Override
        public Object getProperty(String name) {
            return this.source.get(name);
        }
        
        @Override
        public boolean containsProperty(String name) {
            return this.source.containsKey(name);
        }
        
        @Override
        public String[] getPropertyNames() {
            return this.source.keySet().toArray(new String[0]);
        }
    }
}