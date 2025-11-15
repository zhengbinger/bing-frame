# Bing Framework 安全功能使用指南

## 概述

Bing Framework提供了一套完整的安全功能，包括请求频率限制、输入验证和SQL注入防护、API签名验证、敏感操作二次验证、安全HTTP响应头等。这些功能可以有效提高应用的安全性。

## 安全功能清单

### 1. 请求频率限制（Rate Limiting）
- **技术栈**：Spring Cloud Gateway + Redis
- **功能**：防止API滥用和暴力攻击
- **实现**：分布式限流，支持多种限流算法

### 2. 输入验证和SQL注入防护
- **实现方式**：SecureInput注解 + InputValidationAspect切面
- **功能**：对用户输入进行严格验证，防止XSS和SQL注入攻击

### 3. API签名验证机制
- **实现方式**：ApiSignature注解 + 验证切面
- **功能**：确保API请求的完整性和真实性

### 4. 敏感操作二次验证
- **实现方式**：SensitiveOperation注解 + 验证机制
- **功能**：对敏感操作进行额外的安全验证

### 5. 安全HTTP响应头配置
- **功能**：设置CSP、X-Frame-Options等安全响应头
- **防护**：防止XSS、点击劫持等攻击

## 快速开始

### 1. 基本配置

在application.yml中配置Redis连接（用于限流功能）：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms

# 自定义安全配置
bing:
  security:
    # API签名配置
    api-signature:
      enabled: true
      secret: your-secret-key
      expiration: 300 # 秒
    
    # 敏感操作配置
    sensitive-operation:
      enabled: true
      require-sms: true
      sms-expiration: 300 # 秒
```

### 2. 启动类配置

确保在启动类上添加必要的注解：

```java
@SpringBootApplication
@EnableGatewayAutoConfiguration // 启用Gateway配置
public class BingFrameworkApplication {
    public static void main(String[] args) {
        SpringApplication.run(BingFrameworkApplication.class, args);
    }
}
```

## 功能使用详解

### 1. 请求频率限制

#### 1.1 基于注解的限流

在需要限流的方法上添加`@RateLimit`注解：

```java
@RestController
public class UserController {
    
    @GetMapping("/api/users")
    @RateLimit(
        permits = 100,        // 每秒允许的请求数
        refillPeriod = 1,     // 填充周期（秒）
        burstCapacity = 200,  // 突发容量
        keyExpression = "#request.getRemoteAddr()" // 限流键表达式
    )
    public Result<List<User>> getUsers(HttpServletRequest request) {
        return userService.getUsers();
    }
    
    @PostMapping("/api/login")
    @RateLimit(
        permits = 5,          // 每秒5次登录尝试
        refillPeriod = 60,    // 1分钟填充一次
        burstCapacity = 10,   // 最多允许10次突发
        keyExpression = "#request.getRemoteAddr()",
        message = "登录尝试过于频繁，请稍后再试"
    )
    public Result<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return userService.login(request);
    }
}
```

#### 1.2 基于配置的全局限流

在配置文件中设置全局限流规则：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rate_limited_route
          uri: lb://bing-service
          predicates:
            - Path=/api/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@userKeyResolver}"
```

### 2. 输入验证和SQL注入防护

#### 2.1 基本输入验证

在需要验证的方法参数上添加`@SecureInput`注解：

```java
@RestController
public class UserController {
    
    @PostMapping("/api/users")
    public Result<User> createUser(
            @RequestBody @SecureInput(
                maxLength = 100,
                allowSpecialChars = false,
                htmlEncode = true,
                sqlInjectionCheck = true
            ) UserCreateRequest request) {
        return userService.createUser(request);
    }
    
    @GetMapping("/api/users/{id}")
    public Result<User> getUserById(
            @PathVariable @SecureInput(
                maxLength = 20,
                numericOnly = true
            ) String id) {
        return userService.getUserById(id);
    }
}
```

#### 2.2 自定义验证规则

```java
public class UserCreateRequest {
    @SecureInput(
        required = true,
        minLength = 2,
        maxLength = 50,
        pattern = "^[a-zA-Z0-9_]+$",
        htmlEncode = true
    )
    private String username;
    
    @SecureInput(
        required = true,
        minLength = 6,
        maxLength = 100,
        pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        emailFormat = true
    )
    private String email;
    
    // getter和setter...
}
```

### 3. API签名验证

#### 3.1 客户端签名生成

```javascript
// 生成API签名的JavaScript示例
function generateApiSignature(requestData, secret, timestamp) {
    // 1. 排序请求参数
    const sortedKeys = Object.keys(requestData).sort();
    
    // 2. 构建签名字符串
    let signString = `timestamp=${timestamp}`;
    sortedKeys.forEach(key => {
        signString += `&${key}=${requestData[key]}`;
    });
    
    // 3. 生成签名
    const signature = CryptoJS.HmacSHA256(signString, secret).toString();
    
    return signature;
}

// 使用示例
const requestData = {
    userId: '12345',
    action: 'transfer',
    amount: '1000.00'
};
const timestamp = Date.now().toString();
const signature = generateApiSignature(requestData, 'your-secret-key', timestamp);

// 发送请求
fetch('/api/transfer', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'X-Timestamp': timestamp,
        'X-Signature': signature
    },
    body: JSON.stringify(requestData)
});
```

#### 3.2 服务端验证

在需要验证的方法上添加`@ApiSignature`注解：

```java
@RestController
public class PaymentController {
    
    @PostMapping("/api/payment/transfer")
    @ApiSignature(
        requiredParams = {"userId", "amount", "targetAccount"},
        expiration = 300, // 5分钟过期
        ignoreTimestamp = false
    )
    public Result<TransferResponse> transfer(@RequestBody TransferRequest request) {
        return paymentService.transfer(request);
    }
    
    @GetMapping("/api/payment/query")
    @ApiSignature(
        requiredParams = {"transactionId"},
        expiration = 300
    )
    public Result<TransactionInfo> queryTransaction(@RequestParam String transactionId) {
        return paymentService.queryTransaction(transactionId);
    }
}
```

### 4. 敏感操作二次验证

#### 4.1 需要短信验证的操作

```java
@RestController
public class UserController {
    
    @PostMapping("/api/user/password/reset")
    @SensitiveOperation(
        operationType = SensitiveOperationType.PASSWORD_RESET,
        requireVerification = true,
        verificationType = VerificationType.SMS_CODE,
        cooldownSeconds = 300
    )
    public Result<String> resetPassword(
            @RequestBody PasswordResetRequest request) {
        return userService.resetPassword(request);
    }
    
    @PostMapping("/api/user/bank-account/add")
    @SensitiveOperation(
        operationType = SensitiveOperationType.BANK_ACCOUNT_BINDING,
        requireVerification = true,
        verificationType = VerificationType.SMS_CODE,
        cooldownSeconds = 600
    )
    public Result<String> addBankAccount(
            @RequestBody BankAccountRequest request) {
        return userService.addBankAccount(request);
    }
}
```

#### 4.2 需要密码验证的操作

```java
@RestController
public class UserController {
    
    @PostMapping("/api/user/profile/update")
    @SensitiveOperation(
        operationType = SensitiveOperationType.PROFILE_UPDATE,
        requireVerification = true,
        verificationType = VerificationType.PASSWORD
    )
    public Result<String> updateProfile(
            @RequestBody ProfileUpdateRequest request) {
        return userService.updateProfile(request);
    }
}
```

### 5. 安全HTTP响应头配置

#### 5.1 配置文件方式

```yaml
bing:
  security:
    headers:
      # 内容安全策略
      content-security-policy: "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"
      # 防止点击劫持
      x-frame-options: "DENY"
      # XSS保护
      x-content-type-options: "nosniff"
      # 引用者策略
      referrer-policy: "strict-origin-when-cross-origin"
      # HSTS配置
      strict-transport-security: "max-age=31536000; includeSubDomains"
```

#### 5.2 编程方式配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .contentTypeOptions()  // 防止MIME类型嗅探
                .frameOptions().deny()  // 防止点击劫持
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)  // HSTS有效期1年
                    .includeSubdomains(true)     // 包含子域名
                )
                .contentSecurityPolicy(cspConfig -> cspConfig
                    .policyDirectives("default-src 'self'")
                )
                .referrerPolicy(referrerConfig -> referrerConfig
                    .policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
            );
        
        return http.build();
    }
}
```

## 安全配置最佳实践

### 1. 限流配置建议

```yaml
# 登录接口 - 严格限流
/login:
  permits: 5
  refillPeriod: 60  # 每分钟5次
  burstCapacity: 10

# 普通API - 中等限流
/api/user/*:
  permits: 100
  refillPeriod: 1   # 每秒100次
  burstCapacity: 200

# 公共接口 - 宽松限流
/api/public/*:
  permits: 1000
  refillPeriod: 1   # 每秒1000次
  burstCapacity: 2000
```

### 2. 输入验证建议

```java
// 用户名验证
@SecureInput(
    required = true,
    minLength = 3,
    maxLength = 20,
    pattern = "^[a-zA-Z0-9_]+$",  // 只允许字母、数字、下划线
    sqlInjectionCheck = true,
    htmlEncode = true
)
private String username;

// 密码验证
@SecureInput(
    required = true,
    minLength = 8,
    maxLength = 64,
    allowSpecialChars = false,
    htmlEncode = true
)
private String password;

// 邮箱验证
@SecureInput(
    required = true,
    emailFormat = true,
    maxLength = 100
)
private String email;

// 手机号验证
@SecureInput(
    required = true,
    pattern = "^1[3-9]\\d{9}$",  // 中国手机号正则
    numericOnly = true
)
private String phone;
```

### 3. API签名配置建议

```yaml
bing:
  security:
    api-signature:
      enabled: true
      secret: "${API_SIGNATURE_SECRET:default-secret}"  # 从环境变量读取
      expiration: 300  # 5分钟
      allowedMethods: ["POST", "PUT", "PATCH"]  # 只对修改请求要求签名
```

### 4. 敏感操作配置建议

```java
// 高危操作 - 需要多重验证
@SensitiveOperation(
    operationType = SensitiveOperationType.DELETE_ACCOUNT,
    requireVerification = true,
    verificationType = VerificationType.BOTH,  // 短信+密码
    cooldownSeconds = 1800  # 30分钟冷却
)

// 资金相关操作
@SensitiveOperation(
    operationType = SensitiveOperationType.TRANSFER_MONEY,
    requireVerification = true,
    verificationType = VerificationType.SMS_CODE,
    cooldownSeconds = 300,
    amountThreshold = 10000  # 超过10000需要验证
)
```

## 常见问题

### Q1: 限流失败，接口仍然被高频调用

**A**: 检查以下几点：
1. 确保Redis服务正常运行
2. 检查注解参数配置是否正确
3. 查看限流键表达式是否正确识别用户
4. 检查是否有多个服务实例，需要分布式限流

### Q2: API签名验证失败

**A**: 常见原因和解决方案：
1. **时间戳过期**：检查客户端和服务端时间同步
2. **签名算法错误**：确认使用正确的HMAC-SHA256算法
3. **参数排序错误**：参数必须按字典序排列
4. **密钥错误**：检查签名密钥是否正确

### Q3: 敏感操作验证不生效

**A**: 检查配置：
1. 确认`@SensitiveOperation`注解正确配置
2. 检查验证类型设置是否正确
3. 确认冷却时间设置是否合理
4. 查看相关服务是否正确注入

### Q4: 安全响应头不生效

**A**: 排查步骤：
1. 检查配置文件语法是否正确
2. 确认SecurityConfig类是否正确配置
3. 检查是否有其他配置覆盖了安全头设置
4. 使用浏览器开发者工具检查响应头

## 监控和日志

### 1. 安全事件监控

```java
// 安全事件监听器示例
@Component
public class SecurityEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityEventListener.class);
    
    @EventListener
    public void handleRateLimitExceeded(RateLimitExceededEvent event) {
        logger.warn("请求频率超限: IP={}, URI={}, Count={}", 
            event.getClientIp(), event.getRequestUri(), event.getRequestCount());
        // 发送告警通知
    }
    
    @EventListener
    public void handleSensitiveOperation(SensitiveOperationEvent event) {
        logger.info("敏感操作执行: User={}, Operation={}, Method={}", 
            event.getUserId(), event.getOperationType(), event.getVerificationMethod());
    }
}
```

### 2. 安全日志配置

```xml
<!-- logback-spring.xml -->
<configuration>
    <!-- 安全相关日志 -->
    <logger name="com.bing.framework.security" level="INFO" additivity="false">
        <appender-ref ref="SECURITY_FILE"/>
        <appender-ref ref="STDOUT"/>
    </logger>
    
    <appender name="SECURITY_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/security.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/security.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
</configuration>
```

## 安全检查清单

### 部署前检查

- [ ] Redis服务正常运行（用于分布式限流）
- [ ] API签名密钥已设置并妥善保管
- [ ] 安全响应头配置正确
- [ ] 限流规则配置合理
- [ ] 敏感操作验证规则正确
- [ ] 输入验证规则覆盖所有用户输入点

### 生产环境监控

- [ ] 监控限流事件频率
- [ ] 监控API签名验证失败率
- [ ] 监控敏感操作验证触发频率
- [ ] 定期检查安全日志
- [ ] 监控异常请求模式

### 定期安全维护

- [ ] 定期轮换API签名密钥
- [ ] 根据业务调整限流策略
- [ ] 更新安全配置和规则
- [ ] 审查敏感操作验证流程
- [ ] 更新安全依赖库版本

## 技术支持

如果在实施过程中遇到问题，可以：

1. 查看相关组件的日志输出
2. 检查配置文件语法是否正确
3. 参考本文档的常见问题解答
4. 查看源代码实现细节
5. 联系开发团队获取支持

---

*本文档将随着安全功能的更新持续维护和完善。*