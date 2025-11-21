package com.bing.framework;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 主应用类
 * Spring Boot应用的入口点
 * 包含应用的配置和启动逻辑
 * 
 * @author zhengbing
 * @date 2025-11-01
 */
// 禁用不必要的自动配置类，优化启动性能
@SpringBootApplication(exclude = {
        // 禁用Spring Security自动配置，使用自定义安全配置
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@MapperScan("com.bing.framework.mapper") // 扫描Mapper接口
@EnableTransactionManagement // 启用事务管理
@EnableCaching // 启用缓存
@EnableAspectJAutoProxy // 启用AOP功能
public class BingFrameworkApplication {
    private static final Logger log = LoggerFactory.getLogger(BingFrameworkApplication.class);

	public static void main(String[] args) {
        log.info("开始启动Bing Framework应用...");
		// 启动Spring Boot应用并获取应用上下文
		ConfigurableApplicationContext context = SpringApplication.run(BingFrameworkApplication.class, args);
        log.info("应用启动成功！访问地址：http://localhost:8081/api");
        log.info("应用正在运行中，请按 Ctrl+C 停止应用");
	}

}