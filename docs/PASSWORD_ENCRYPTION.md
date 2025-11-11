# 密码加密与重置功能使用指南

本文档详细介绍了项目中密码加密和重置功能的实现原理和使用方法。

## 1. 功能概述

项目使用BCrypt算法对用户密码进行安全加密，并提供以下功能：

- 用户密码加密存储
- 密码验证功能
- 密码重置功能
- 随机密码生成功能

## 2. 加密实现原理

### 2.1 加密算法

使用BCrypt哈希算法进行密码加密，该算法具有以下特点：

- 单向哈希函数，不可逆
- 自动生成随机盐值，提高安全性
- 适应性强，可以通过工作因子调整算法强度
- 安全性高，广泛应用于企业级应用

### 2.2 实现方式

使用Spring Security提供的BCryptPasswordEncoder进行密码加密和验证：

```java
// 密码加密
String encodedPassword = passwordEncoder.encode(rawPassword);

// 密码验证
boolean isMatch = passwordEncoder.matches(rawPassword, encodedPassword);
```

## 3. 核心组件

### 3.1 BCryptPasswordEncoder

项目使用Spring Security的`BCryptPasswordEncoder`作为密码加密和验证的核心组件，它提供以下主要方法：

```java
// 密码加密
String encode(CharSequence rawPassword)

// 密码验证
boolean matches(CharSequence rawPassword, String encodedPassword)
```

## 4. 用户密码处理流程

### 4.1 用户注册/保存

当用户注册或保存用户信息时，系统会自动对密码进行加密处理：

```java
// 密码加密
if (user.getPassword() != null) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
}
```

### 4.2 用户信息更新

当更新用户信息时，如果密码字段有变化，系统会自动重新加密：

```java
// 如果密码有更新，需要加密
if (user.getPassword() != null && !user.getPassword().equals(existingUser.getPassword())) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
}
```

## 5. 密码重置功能

### 5.1 手动设置新密码

通过`resetPassword`方法可以手动设置用户的新密码：

```java
boolean resetPassword(Long id, String newPassword)
```

### 5.2 生成随机密码

通过`generateAndResetPassword`方法可以生成一个8位的随机密码并设置给用户：

```java
String generateAndResetPassword(Long id)
```

## 6. API接口使用

### 6.1 重置密码接口

```http
PUT /user/{id}/password
```

请求体：
```json
{
  "newPassword": "新密码内容"
}
```

### 6.2 生成随机密码接口

```http
POST /user/{id}/random-password
```

响应：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "randomPassword": "生成的随机密码"
  }
}
```

## 7. 安全最佳实践

1. **密码复杂度要求**：建议在前端添加密码复杂度验证，要求包含大小写字母、数字和特殊字符
2. **密码传输安全**：确保所有密码相关的API使用HTTPS协议
3. **密码历史记录**：生产环境建议添加密码历史记录，防止用户重复使用旧密码
4. **密码过期策略**：可根据业务需求添加密码定期过期机制
5. **失败尝试限制**：建议添加登录失败次数限制，防止暴力破解

## 8. 示例代码

### 8.1 验证用户登录

```java
public boolean login(String username, String password) {
    // 根据用户名查询用户
    User user = userMapper.selectByUsername(username);
    if (user == null) {
        return false;
    }
    
    // 验证密码
    return passwordEncoder.matches(password, user.getPassword());
}
```

### 8.2 生成随机密码并发送邮件

```java
public void resetPasswordAndSendEmail(Long userId) {
    // 生成随机密码
    String randomPassword = userService.generateAndResetPassword(userId);
    
    // 获取用户信息
    User user = userService.getUserById(userId);
    
    // 发送邮件（示例代码，实际项目中需要实现邮件发送逻辑）
    // emailService.sendPasswordResetEmail(user.getEmail(), randomPassword);
}
```