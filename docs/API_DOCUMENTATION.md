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

## 7. 新增功能接口说明

### 7.1 登录记录管理接口

#### 7.1.1 接口概述

`LoginRecordController`提供了登录记录的查询、清理等RESTful API接口，用于管理用户登录历史记录。

#### 7.1.2 接口列表

| 接口路径 | 方法 | 功能描述 | 权限要求 |
| :--- | :--- | :--- | :--- |
| `/api/login-records` | `GET` | 获取登录记录列表（分页） | 需要管理员权限 |
| `/api/login-records/user/{userId}` | `GET` | 获取指定用户的登录记录 | 需要管理员权限或用户本人 |
| `/api/login-records/recent` | `GET` | 获取最近的登录记录 | 需要登录 |
| `/api/login-records/failed` | `GET` | 获取失败的登录记录 | 需要管理员权限 |
| `/api/login-records/clean` | `DELETE` | 清理过期的登录记录 | 需要管理员权限 |

#### 7.1.3 详细接口说明

**1. 获取登录记录列表**

```
GET /api/login-records
```

**请求参数**：
- `page`：页码，从1开始
- `size`：每页数量
- `username`：可选，用户名搜索
- `startTime`：可选，开始时间
- `endTime`：可选，结束时间
- `status`：可选，登录状态（0-失败，1-成功）

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "records": [
      {
        "id": 1,
        "userId": 1001,
        "username": "admin",
        "ipAddress": "192.168.1.100",
        "userAgent": "Mozilla/5.0...",
        "loginTime": "2025-11-11 10:30:00",
        "status": 1
      }
    ],
    "current": 1,
    "pages": 10
  }
}
```

**2. 获取指定用户的登录记录**

```
GET /api/login-records/user/{userId}
```

**请求参数**：
- `userId`：用户ID（路径参数）
- `page`：页码
- `size`：每页数量

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 20,
    "records": [
      {
        "id": 2,
        "userId": 1001,
        "username": "admin",
        "ipAddress": "192.168.1.101",
        "userAgent": "Chrome/90.0...",
        "loginTime": "2025-11-10 15:45:00",
        "status": 1
      }
    ],
    "current": 1,
    "pages": 2
  }
}
```

**3. 获取最近的登录记录**

```
GET /api/login-records/recent
```

**请求参数**：
- `limit`：限制返回数量，默认10条

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 3,
      "userId": 1002,
      "username": "user1",
      "ipAddress": "192.168.1.102",
      "userAgent": "Safari/14.0...",
      "loginTime": "2025-11-11 09:20:00",
      "status": 1
    }
  ]
}
```

**4. 获取失败的登录记录**

```
GET /api/login-records/failed
```

**请求参数**：
- `days`：查询最近多少天，默认7天
- `page`：页码
- `size`：每页数量

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 5,
    "records": [
      {
        "id": 4,
        "userId": null,
        "username": "admin",
        "ipAddress": "192.168.1.103",
        "userAgent": "Mozilla/5.0...",
        "loginTime": "2025-11-11 08:15:00",
        "status": 0
      }
    ],
    "current": 1,
    "pages": 1
  }
}
```

**5. 清理过期的登录记录**

```
DELETE /api/login-records/clean
```

**请求参数**：
- `days`：清理多少天前的记录，默认90天

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deletedCount": 150
  }
}
```

#### 7.1.4 相关实体类

**LoginRecordQueryDTO**
用于封装登录记录的查询条件：

| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `page` | Integer | 页码 |
| `size` | Integer | 每页数量 |
| `username` | String | 用户名 |
| `startTime` | String | 开始时间 |
| `endTime` | String | 结束时间 |
| `status` | Integer | 登录状态 |

**LoginRecord**
登录记录实体类：

| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `id` | Long | 记录ID |
| `userId` | Long | 用户ID |
| `username` | String | 用户名 |
| `ipAddress` | String | IP地址 |
| `userAgent` | String | 用户代理信息 |
| `loginTime` | Date | 登录时间 |
| `status` | Integer | 登录状态（0-失败，1-成功） |

### 7.2 验证码管理接口

#### 7.2.1 接口概述

`CaptchaController`提供了验证码生成和刷新的RESTful API接口，支持多种类型的验证码（图形验证码、滑动条验证码、短信验证码）。

#### 7.2.2 支持的验证码类型

| 类型 | 标识符 | 描述 | 适用场景 |
| :--- | :--- | :--- | :--- |
| 图形验证码 | `image` | 基于图像的字符验证码 | 通用场景，用户可以输入字符 |
| 滑动条验证码 | `slider` | 基于滑块拼图的验证码 | 移动端友好，提升用户体验 |
| 短信验证码 | `sms` | 通过短信发送的验证码 | 需要手机号验证的场景 |

#### 7.2.3 详细接口说明

**1. 生成指定类型验证码**

```
GET /api/captcha/generate/{type}
```

**路径参数**：
- `type`：验证码类型（image/sms/slider）

**请求示例**：
```http
GET /api/captcha/generate/slider
```

**响应示例（滑动条验证码）**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaKey": "s1s2s3s4s5s6s7s8s9s0",
    "captchaType": "slider",
    "captchaData": {
      "targetPosition": 150,
      "currentPosition": 0,
      "tolerance": 5,
      "timestamp": 1699876543210
    },
    "sliderWidth": 60,
    "backgroundWidth": 300,
    "sliderHeight": 40,
    "expireTime": 1699876543210
  }
}
```

**响应示例（图形验证码）**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaKey": "a1b2c3d4e5f6g7h8i9j0",
    "captchaContent": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAA...",
    "captchaType": "image",
    "expireTime": 1699876543210
  }
}
```

**2. 生成默认类型验证码**

```
GET /api/captcha/generate
```

**响应示例**：
返回使用默认类型（image）的验证码

**3. 刷新指定类型验证码**

```
GET /api/captcha/refresh/{type}
```

**路径参数**：
- `type`：验证码类型

**请求示例**：
```http
GET /api/captcha/refresh/slider
```

**响应示例**：
返回新的验证码数据，与生成接口格式相同

**4. 刷新默认类型验证码**

```
GET /api/captcha/refresh
```

**响应示例**：
返回使用默认类型的新验证码

#### 7.2.4 验证码验证

验证码验证通常在登录或其他需要验证的接口中完成，示例：

```json
{
  "username": "admin",
  "password": "password",
  "captchaKey": "a1b2c3d4e5f6g7h8i9j0",
  "captchaCode": "ABCD",
  "captchaType": "image"
}
```

#### 7.2.5 前端使用示例

**JavaScript - 生成滑动条验证码**：
```javascript
async function generateSliderCaptcha() {
  try {
    const response = await fetch('/api/captcha/generate/slider');
    const result = await response.json();
    
    if (result.code === 200) {
      // 使用返回的数据初始化滑动条界面
      const captchaData = result.data;
      initSliderInterface(captchaData);
    }
  } catch (error) {
    console.error('获取滑动条验证码失败:', error);
  }
}

function initSliderInterface(captchaData) {
  // 创建滑动条界面元素
  const sliderHtml = `
    <div class="slider-captcha">
      <div class="slider-track" style="width: ${captchaData.backgroundWidth}px;">
        <div class="slider-thumb" style="width: ${captchaData.sliderWidth}px;"></div>
      </div>
    </div>
  `;
  
  document.getElementById('captchaContainer').innerHTML = sliderHtml;
  document.getElementById('captchaKey').value = captchaData.captchaKey;
  
  // 初始化拖拽逻辑
  initSliderDrag(captchaData);
}
```

**React - 滑动条验证码组件**：
```jsx
import React, { useState, useEffect } from 'react';

const SliderCaptcha = ({ onSuccess, onError }) => {
  const [captchaData, setCaptchaData] = useState(null);
  const [sliderPosition, setSliderPosition] = useState(0);
  const [isDragging, setIsDragging] = useState(false);

  useEffect(() => {
    generateCaptcha();
  }, []);

  const generateCaptcha = async () => {
    try {
      const response = await fetch('/api/captcha/generate/slider');
      const result = await response.json();
      
      if (result.code === 200) {
        setCaptchaData(result.data);
      }
    } catch (error) {
      onError && onError('获取验证码失败');
    }
  };

  const handleMouseDown = (e) => {
    setIsDragging(true);
  };

  const handleMouseMove = (e) => {
    if (!isDragging || !captchaData) return;
    
    const track = e.currentTarget;
    const rect = track.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const maxPosition = captchaData.backgroundWidth - captchaData.sliderWidth;
    
    setSliderPosition(Math.max(0, Math.min(x, maxPosition)));
  };

  const handleMouseUp = () => {
    if (!isDragging) return;
    setIsDragging(false);
    
    // 验证滑动位置
    validatePosition();
  };

  const validatePosition = () => {
    const targetPosition = captchaData.captchaData.targetPosition;
    const tolerance = captchaData.captchaData.tolerance;
    
    if (Math.abs(sliderPosition - targetPosition) <= tolerance) {
      onSuccess && onSuccess(captchaData.captchaKey, sliderPosition, 'slider');
    } else {
      onError && onError('滑动位置不正确，请重试');
      generateCaptcha(); // 重新生成
    }
  };

  if (!captchaData) {
    return <div>加载中...</div>;
  }

  return (
    <div className="slider-captcha">
      <div 
        className="slider-track"
        style={{ width: captchaData.backgroundWidth }}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
      >
        <div 
          className="slider-thumb"
          style={{ 
            width: captchaData.sliderWidth,
            left: sliderPosition
          }}
          onMouseDown={handleMouseDown}
        >
          拖拽验证
        </div>
      </div>
    </div>
  );
};
```

#### 7.2.6 错误处理

| HTTP状态码 | 错误码 | 描述 | 解决方案 |
| :--- | :--- | :--- | :--- |
| 400 | INVALID_TYPE | 不支持的验证码类型 | 检查type参数是否正确 |
| 500 | GENERATION_FAILED | 验证码生成失败 | 检查服务器配置和Redis连接 |
| 500 | CAPTCHA_DISABLED | 验证码功能已禁用 | 检查配置项bing.captcha.enabled |

---

**文档版本**: 1.0  
**更新日期**: 2025-11-20  
**作者**: zhengbing