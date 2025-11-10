# API文档管理系统

本文档详细说明项目中使用的API文档管理工具Knife4j的配置、使用方法和最佳实践。

## 1. Knife4j介绍

Knife4j是基于Swagger 2的增强实现，提供了更友好的UI界面和更丰富的功能，适用于API文档的在线生成、调试和管理。

## 2. 配置说明

### 2.1 依赖配置

项目使用了以下Knife4j依赖：

```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-spring-boot-starter</artifactId>
    <version>3.0.3</version>
</dependency>
```

版本选择说明：
- 3.0.3版本兼容Spring Boot 2.7.x系列
- 确保与项目的Spring Boot版本（2.7.18）匹配

### 2.2 配置类

项目通过`SwaggerConfig.java`配置Knife4j：

```java
package com.bing.framework.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger/Knife4j配置类
 * 用于生成API文档
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("1.0.0")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.bing.framework.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Bing Framework API文档")
                .description("RESTful API文档，提供所有接口的详细说明")
                .contact(new Contact("Bing Framework Team", "", ""))
                .version("1.0.0")
                .build();
    }
}
```

### 2.3 应用配置

在`application.yml`中添加了以下配置：

```yaml
# Knife4j配置
knife4j:
  enable: true
  setting:
    # 语言设置为中文
    language: zh-CN
    # 显示请求参数缓存
    enable-request-cache: true
    # 开启登录认证
    enable-security: false
    # 忽略参数属性（Spring Security相关）
    ignore-parameters: 
      - token
      - Authorization
  # 增强功能配置
  enhancement:
    # 启用接口排序
    enable-order: true
    # 启用返回参数缓存
    enable-response-cache: true
```

## 3. 使用方法

### 3.1 访问文档

项目启动后，可以通过以下地址访问Knife4j文档界面：

```
http://localhost:8080/doc.html
```

### 3.2 接口调试

在Knife4j界面中，可以：
- 查看所有API接口的详细信息
- 直接在界面上进行接口测试
- 查看请求和响应的详细格式
- 导出API文档（支持Markdown、HTML、PDF等格式）

## 4. 开发规范

### 4.1 控制器文档注解

为了生成更详细的API文档，建议在控制器中使用以下注解：

```java
@Api(tags = "用户管理")  // 用于类上，描述控制器的作用
@RestController
@RequestMapping("/api/user")
public class UserController {

    @ApiOperation("获取用户列表")  // 用于方法上，描述接口功能
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", value = "页码", required = true, dataType = "int"),
        @ApiImplicitParam(name = "size", value = "每页数量", required = true, dataType = "int")
    })  // 用于描述请求参数
    @GetMapping("/list")
    public Result<Page<User>> getUserList(@RequestParam int page, @RequestParam int size) {
        // 方法实现
    }
}
```

### 4.2 实体类文档注解

对于请求和响应的实体类，可以使用以下注解：

```java
@Data
@ApiModel("用户信息")  // 用于类上，描述实体类
public class User {
    
    @ApiModelProperty("用户ID")  // 用于字段上，描述字段信息
    private Long id;
    
    @ApiModelProperty("用户名", required = true)
    private String username;
    
    @ApiModelProperty("邮箱")
    private String email;
}
```

## 5. 注意事项

1. **生产环境配置**：在生产环境中，建议关闭Knife4j，可以通过配置`knife4j.enable: false`或使用不同的profile来实现

2. **安全考虑**：如果需要在生产环境中暴露文档，建议配置安全认证（`enable-security: true`）

3. **版本管理**：随着API的迭代，需要及时更新文档中的版本信息

4. **缓存设置**：合理使用请求/响应缓存功能，可以提高文档界面的响应速度

## 6. 常见问题

### 6.1 文档不显示接口

- 检查控制器是否在配置的扫描包路径下
- 确保控制器类和方法添加了正确的Swagger注解
- 检查路径匹配规则是否正确

### 6.2 文档界面无法访问

- 检查项目是否正常启动
- 确认是否有拦截器或过滤器阻止了访问
- 检查端口号是否正确

### 6.3 参数类型显示错误

- 确保为实体类字段添加了`@ApiModelProperty`注解
- 检查`dataType`属性是否与实际类型匹配