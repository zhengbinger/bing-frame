package com.bing.framework;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
        // 禁用JPA自动配置，因为项目主要使用MyBatis
        JpaRepositoriesAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        // 禁用Spring Security自动配置，使用自定义安全配置
        SecurityAutoConfiguration.class
})
@MapperScan("com.bing.framework.mapper") // 扫描Mapper接口
@EnableTransactionManagement // 启用事务管理
@EnableCaching // 启用缓存
public class BingFrameworkApplication {

	public static void main(String[] args) {
		System.out.println("开始启动应用...");
		// 正常启动Spring Boot应用，Spring Boot应用默认会一直运行，不需要额外阻塞
		SpringApplication.run(BingFrameworkApplication.class, args);
		System.out.println("应用启动成功！访问地址：http://localhost:8081/api");
		System.out.println("Knife4j文档地址：http://localhost:8081/api/doc.html");
		// 移除System.in.read()，因为在某些环境下可能导致问题
	}

}