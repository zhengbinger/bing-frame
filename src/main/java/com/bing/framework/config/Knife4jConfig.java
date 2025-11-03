package com.bing.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Knife4j配置类
 * 用于配置API文档生成
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bing Framework API文档")
                        .version("1.0.0")
                        .description("Bing Framework RESTful API文档")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}