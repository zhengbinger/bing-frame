# Spring Boot 脚手架程序

## 项目简介

这是一个基于Spring Boot 2.7.x和Java 8的脚手架程序，集成了主流的Java开发技术，提供了完整的项目结构和常用功能模块，可以作为企业级应用开发的基础框架。

## 技术栈

- **基础框架**：Spring Boot 2.7.18
- **Java版本**：Java 8
- **ORM框架**：MyBatis-Plus 3.5.3.1、Spring Data JPA
- **数据库**：MySQL
- **安全框架**：Spring Security
- **API文档**：Knife4j 3.0.3（Swagger增强版）
- **缓存**：Spring Cache
- **国际化**：Spring MessageSource
- **工具类**：Lombok、Fastjson、Hutool
- **构建工具**：Maven

## 项目结构

```
src/main/java/com/bing/bing_framework/
├── BingFrameworkApplication.java    # 主应用类
├── annotation/                      # 自定义注解
├── config/                          # 配置类
├── controller/                      # 控制器层
├── dto/                             # 数据传输对象
├── entity/                          # 实体类
├── exception/                       # 异常处理
├── interceptor/                     # 拦截器
├── mapper/                          # Mapper接口
├── service/                         # 业务逻辑层
│   └── impl/                        # 业务逻辑实现
└── util/                            # 工具类
src/main/resources/
├── application.yml                  # 应用配置
├── i18n/                            # 国际化资源文件
│   ├── messages.properties          # 默认英文消息
│   └── messages_zh_CN.properties    # 中文消息
└── mapper/                          # MyBatis XML文件
```

## 功能模块

### 1. 用户管理

提供完整的用户CRUD操作接口，包括：
- 根据ID查询用户
- 查询所有用户
- 新增用户
- 更新用户
- 删除用户
- 批量删除用户

### 2. API文档

集成Knife4j（Swagger增强版），自动生成API文档，可通过以下地址访问：
```
http://localhost:8081/api/doc.html
```

### 3. 异常处理

全局异常处理机制，统一处理系统异常，返回标准化的错误信息。

### 4. 统一响应

标准化的API响应格式，便于前端处理。

### 5. 错误码多语言支持

实现了错误码体系的多语言支持功能：
- 支持中文和英文两种语言
- 通过Accept-Language请求头自动识别语言环境
- 提供语言切换测试接口
- 全局异常处理自动返回对应语言的错误消息

### 5. 跨域支持

配置了CORS，支持跨域请求。

### 6. 缓存支持

集成Spring Cache，可根据需要进行缓存配置。

## 快速开始

### 1. 环境准备

- JDK 1.8
- Maven 3.6+
- MySQL 5.7+

### 2. 数据库配置

建表脚本已移至项目的sql脚本目录：`src/main/resources/sql/init.sql`

请执行该脚本创建数据库和用户表。

### 3. 配置修改

修改`application.yml`文件中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bing?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

### 4. 国际化配置（可选）

项目默认支持中文和英文两种语言，默认语言为中文。可以通过以下方式切换语言：

1. 修改`application.yml`中的默认语言配置：
```yaml
spring:
  messages:
    default-locale: en  # 切换为英文默认
```

2. 通过HTTP请求头切换：
```
Accept-Language: en-US
```

### 4. 启动应用

方式一：使用Maven命令启动
```bash
mvn spring-boot:run
```

方式二：打包后运行
```bash
mvn clean package
java -jar target/bing-framework-0.0.1-SNAPSHOT.jar
```

### 5. 访问应用

- 应用地址：http://localhost:8081/api
- Knife4j文档：http://localhost:8081/api/doc.html
- 国际化测试接口：http://localhost:8081/api/i18n/error-info

## API接口

### 用户管理接口

| 接口URL | 方法 | 功能描述 |
| :--- | :--- | :--- |
| `/api/user/{id}` | `GET` | 根据ID查询用户 |
| `/api/user/list` | `GET` | 查询所有用户 |
| `/api/user/` | `POST` | 新增用户 |
| `/api/user/` | `PUT` | 更新用户 |
| `/api/user/{id}` | `DELETE` | 删除用户 |
| `/api/user/batch` | `DELETE` | 批量删除用户 |

## 注意事项

1. 本脚手架使用Java 8开发，确保JDK版本正确。
2. 数据库连接信息需要根据实际环境进行修改。
3. 生产环境中需要修改默认的数据库用户名和密码。
4. 生产环境中建议关闭Knife4j文档。
5. 实际项目中建议加强安全配置，如添加JWT认证等。
6. 如需添加更多语言支持，只需在i18n目录下创建对应的消息属性文件即可（如messages_en_US.properties）。

## 扩展建议

1. 添加JWT认证机制
2. 集成Redis缓存
3. 添加日志系统（如ELK）
4. 集成消息队列（如RabbitMQ）
5. 添加分布式锁机制
6. 集成定时任务调度
7. 添加限流、熔断等服务治理功能
8. 扩展国际化支持更多语言
9. 添加缓存国际化消息功能，提高性能