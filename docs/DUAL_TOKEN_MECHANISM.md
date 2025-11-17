# 双Token机制详细说明文档

## 概述

本项目实现了基于JWT的双Token（双令牌）认证机制，包括访问令牌（Access Token）和刷新令牌（Refresh Token），提供更安全的用户认证和会话管理功能。

**创建时间**：2025-11-17  
**作者**：zhengbing  
**文档版本**：v1.0

## 目录

1. [双Token机制原理](#1-双token机制原理)
2. [项目实现架构](#2-项目实现架构)
3. [核心组件说明](#3-核心组件说明)
4. [配置参数](#4-配置参数)
5. [API接口说明](#5-api接口说明)
6. [安全特性](#6-安全特性)
7. [开发指引](#7-开发指引)
8. [使用示例](#8-使用示例)
9. [最佳实践](#9-最佳实践)

## 1. 双Token机制原理

### 1.1 基本概念

**访问令牌（Access Token）**：
- 用于API请求的认证
- 有效期较短（默认24小时）
- 包含用户身份信息和权限
- 每次API请求都需要携带

**刷新令牌（Refresh Token）**：
- 用于获取新的访问令牌
- 有效期较长（默认168小时/7天）
- 相对安全，不需要在每次请求中传递
- 存储在服务器端（Redis）

### 1.2 工作流程

```mermaid
sequenceDiagram
    participant U as 用户
    participant C as 客户端
    participant S as 服务器
    participant R as Redis

    U->>C: 输入用户名密码
    C->>S: POST /api/auth/login
    S->>S: 验证用户凭据
    S->>S: 生成Access Token + Refresh Token
    S->>R: 存储Refresh Token (7天)
    S->>C: 返回令牌信息
    C->>U: 登录成功
    
    Note over C,S: 正常使用阶段
    
    C->>S: API请求 + Access Token
    S->>S: 验证Access Token
    alt Token有效
        S->>C: 返回数据
    else Token过期
        S->>C: 401 Unauthorized
        C->>S: POST /api/auth/refresh
        S->>R: 验证Refresh Token
        S->>S: 生成新的Access Token
        S->>C: 返回新令牌
        C->>S: 重试API请求
    end
```

### 1.3 安全优势

1. **访问令牌短期有效**：即使被窃取，攻击者只能在有限时间内使用
2. **刷新令牌服务端存储**：服务器可以控制刷新令牌的有效性和撤销
3. **设备指纹验证**：支持设备标识验证，防止令牌在不同设备间滥用
4. **并发控制**：支持用户多设备登录管理
5. **实时撤销**：服务端可以立即让特定令牌失效

## 2. 项目实现架构

### 2.1 架构概览

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端客户端     │    │   后端API       │    │   Redis缓存     │
│                 │    │                 │    │                 │
│ • 存储Access   │◄──►│ • JwtUtil      │◄──►│ • 刷新令牌存储  │
│ • 存储Refresh  │    │ • SecureJwt    │    │ • 用户token     │
│ • 自动刷新机制 │    │ • AuthController│    │ • 黑名单管理    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 2.2 核心组件

- **JwtUtil**：基础JWT令牌工具类
- **SecureJwtTokenProvider**：增强版安全令牌提供者
- **AuthController**：认证控制器，处理登录、刷新等接口
- **JwtInterceptor**：令牌验证拦截器
- **RedisUtil**：Redis工具类，用于令牌存储

## 3. 核心组件说明

### 3.1 JwtUtil.java

**主要功能**：
- 基础的JWT令牌生成和验证
- 访问令牌和刷新令牌的区分
- 令牌解析和用户信息提取

**核心方法**：
```java
// 生成访问令牌
public String generateToken(Long userId, String username)

// 生成刷新令牌
public String generateRefreshToken(Long userId, String username)

// 验证访问令牌
public boolean validateToken(String token)

// 验证刷新令牌
public boolean validateRefreshToken(String refreshToken)
```

### 3.2 SecureJwtTokenProvider.java

**主要功能**：
- 增强版安全令牌生成
- 设备指纹验证
- 地理位置感知
- 反重放攻击
- 令牌黑名单管理

**核心特性**：
- **设备指纹**：支持设备ID验证
- **位置感知**：支持客户端IP验证
- **反重放攻击**：使用tokenId防止重放
- **用户代理验证**：支持User-Agent验证

### 3.3 AuthController.java

**主要功能**：
- 用户登录认证
- 令牌刷新处理
- 用户注销
- 会话管理

**主要接口**：
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/refresh` - 刷新令牌
- `POST /api/auth/logout` - 用户注销

### 3.4 JwtInterceptor.java

**主要功能**：
- 自动验证API请求中的令牌
- 用户信息提取和上下文设置
- Redis令牌一致性检查

## 4. 配置参数

### 4.1 application.yml配置

```yaml
# JWT配置
jwt:
  # 令牌密钥
  secret: your-secret-key-here
  
  # 访问令牌过期时间（小时）
  expiration: 24
  
  # 刷新令牌过期时间（小时）
  refresh:
    expiration: 168
  
  # 证书版本（用于密钥轮换）
  certificate:
    version: v1
  
  # 安全特性配置
  security:
    # 启用设备指纹
    device-fingerprint: true
    # 启用位置感知
    location-aware: true
    # 启用反重放攻击
    anti-replay: true
    # 启用令牌黑名单
    token-blacklist-enabled: true
```

### 4.2 配置说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `jwt.secret` | String | default-secret-key | JWT签名密钥 |
| `jwt.expiration` | Integer | 24 | 访问令牌过期时间（小时） |
| `jwt.refresh.expiration` | Integer | 168 | 刷新令牌过期时间（小时） |
| `jwt.certificate.version` | String | v1 | 证书版本，用于密钥轮换 |
| `jwt.security.device-fingerprint` | Boolean | true | 是否启用设备指纹验证 |
| `jwt.security.location-aware` | Boolean | true | 是否启用位置感知 |
| `jwt.security.anti-replay` | Boolean | true | 是否启用反重放攻击 |
| `jwt.security.token-blacklist-enabled` | Boolean | true | 是否启用令牌黑名单 |

## 5. API接口说明

### 5.1 用户登录

**接口**：`POST /api/auth/login`

**请求参数**：
```json
{
    "username": "admin",
    "password": "123456",
    "captcha": "abcd",
    "captchaKey": "uuid"
}
```

**响应结果**：
```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "token": "eyJhbGciOiJIUzUxNiIsInR5cCI6IkpXVCJ9...",
        "refreshToken": "eyJhbGciOiJIUzUxNiIsInR5cCI6IkpXVCJ9...",
        "expiration": "2025-11-18T10:30:00.000+00:00",
        "refreshExpiration": "2025-11-24T10:30:00.000+00:00",
        "userId": 1,
        "username": "admin",
        "nickname": "系统管理员",
        "roles": ["ADMIN", "USER"]
    }
}
```

### 5.2 刷新令牌

**接口**：`POST /api/auth/refresh`

**请求参数**：
```
refreshToken: eyJhbGciOiJIUzUxNiIsInR5cCI6IkpXVCJ9...
```

**响应结果**：
```json
{
    "code": 200,
    "message": "刷新成功",
    "data": {
        "token": "eyJhbGciOiJIUzUxNiIsInR5cCI6IkpXVCJ9...",
        "refreshToken": "eyJhbGciOiJIUzUxNiIsInR5cCI6IkpXVCJ9...",
        "expiration": "2025-11-18T10:30:00.000+00:00",
        "refreshExpiration": "2025-11-24T10:30:00.000+00:00",
        "userId": 1,
        "username": "admin",
        "nickname": "系统管理员",
        "roles": ["ADMIN", "USER"]
    }
}
```

### 5.3 用户注销

**接口**：`POST /api/auth/logout`

**请求头**：
```
Authorization: Bearer eyJhbGciOiJIUzUxNiIsInR5cCI6IkpXVCJ9...
```

**响应结果**：
```json
{
    "code": 200,
    "message": "注销成功"
}
```

## 6. 安全特性

### 6.1 令牌安全

**签名算法**：HS512（HMAC SHA-512）
**加密强度**：256位密钥
**时间戳**：包含签发时间和过期时间
**唯一标识**：每个令牌包含唯一的tokenId

### 6.2 设备指纹验证

```java
// 支持的设备指纹信息
- Device ID（设备唯一标识）
- User-Agent（用户代理字符串）
- Client IP（客户端IP地址）
- Client Type（客户端类型：WEB、MOBILE、API等）
```

### 6.3 反重放攻击

- **Token ID机制**：每个令牌都有唯一的tokenId
- **一次性使用**：tokenId使用后会被标记，防止重复使用
- **时间窗口检查**：检查令牌是否在合理的时间窗口内使用

### 6.4 令牌黑名单

```java
// 黑名单管理功能
- 支持手动撤销令牌
- 支持批量撤销用户的所有令牌
- 支持基于设备类型的撤销
- 支持基于时间的自动清理
```

### 6.5 并发控制

- **多设备登录**：支持同一用户多设备同时在线
- **设备管理**：可以查看和管理用户的所有登录设备
- **强制下线**：管理员可以强制用户从特定设备下线

## 7. 开发指引

### 7.1 前端集成

#### 7.1.1 令牌存储

```javascript
// 建议的存储方案
const storeTokens = (token, refreshToken, expiration) => {
    // 存储到localStorage（注意XSS风险）
    localStorage.setItem('access_token', token);
    localStorage.setItem('refresh_token', refreshToken);
    localStorage.setItem('token_expires', expiration.getTime());
    
    // 或者存储到内存变量（推荐）
    window.tokenCache = {
        accessToken: token,
        refreshToken: refreshToken,
        expiration: expiration
    };
};
```

#### 7.1.2 自动刷新机制

```javascript
// 自动刷新令牌
let refreshing = false;

const autoRefreshToken = async () => {
    if (refreshing) return;
    
    const token = localStorage.getItem('access_token');
    const expiration = localStorage.getItem('token_expires');
    
    if (!token || !expiration) return;
    
    // 距离过期时间少于30分钟时刷新
    const timeToExpiry = expiration - Date.now();
    if (timeToExpiry < 30 * 60 * 1000) {
        refreshing = true;
        try {
            const response = await fetch('/api/auth/refresh', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `refreshToken=${localStorage.getItem('refresh_token')}`
            });
            
            if (response.ok) {
                const result = await response.json();
                storeTokens(
                    result.data.token,
                    result.data.refreshToken,
                    new Date(result.data.expiration)
                );
            }
        } catch (error) {
            console.error('Token refresh failed:', error);
            // 刷新失败，重新登录
            window.location.href = '/login';
        } finally {
            refreshing = false;
        }
    }
};
```

#### 7.1.3 请求拦截器

```javascript
// Axios请求拦截器
axios.interceptors.request.use(
    config => {
        const token = localStorage.getItem('access_token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器 - 处理401错误
axios.interceptors.response.use(
    response => response,
    async error => {
        if (error.response && error.response.status === 401) {
            // 尝试刷新令牌
            const originalRequest = error.config;
            if (!originalRequest._retry) {
                originalRequest._retry = true;
                
                try {
                    const response = await fetch('/api/auth/refresh', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                        },
                        body: `refreshToken=${localStorage.getItem('refresh_token')}`
                    });
                    
                    if (response.ok) {
                        const result = await response.json();
                        storeTokens(
                            result.data.token,
                            result.data.refreshToken,
                            new Date(result.data.expiration)
                        );
                        
                        // 重新发送原始请求
                        originalRequest.headers['Authorization'] = `Bearer ${result.data.token}`;
                        return axios(originalRequest);
                    }
                } catch (refreshError) {
                    // 刷新失败，跳转到登录页
                    localStorage.removeItem('access_token');
                    localStorage.removeItem('refresh_token');
                    localStorage.removeItem('token_expires');
                    window.location.href = '/login';
                    return Promise.reject(refreshError);
                }
            }
        }
        return Promise.reject(error);
    }
);
```

### 7.2 后端开发

#### 7.2.1 创建需要认证的接口

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/profile")
    public Result<UserProfile> getProfile() {
        // 通过JwtInterceptor自动获取用户信息
        Long userId = (Long) RequestContextHolder.currentRequestAttributes()
                .getAttribute("userId", RequestAttributes.SCOPE_REQUEST);
        
        UserProfile profile = userService.getUserProfile(userId);
        return Result.success(profile);
    }
    
    @PostMapping("/update")
    public Result<Void> updateProfile(@RequestBody UpdateProfileRequest request) {
        // 用户身份已通过拦截器验证
        Long userId = getCurrentUserId();
        userService.updateProfile(userId, request);
        return Result.success();
    }
}
```

#### 7.2.2 自定义安全验证

```java
@Component
public class CustomSecurityService {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisUtil redisUtil;
    
    /**
     * 验证用户权限
     */
    public boolean hasPermission(Long userId, String permission) {
        // 检查用户角色和权限
        return userService.checkUserPermission(userId, permission);
    }
    
    /**
     * 验证设备是否允许访问
     */
    public boolean isDeviceAllowed(Long userId, String deviceId) {
        // 检查设备是否在允许列表中
        Set<String> allowedDevices = userService.getUserAllowedDevices(userId);
        return allowedDevices.contains(deviceId);
    }
}
```

### 7.3 配置自定义参数

```java
@Configuration
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    
    private TokenConfig token = new TokenConfig();
    private DeviceConfig device = new DeviceConfig();
    
    // getters and setters
    
    public static class TokenConfig {
        private int accessTokenExpireHours = 24;
        private int refreshTokenExpireHours = 168;
        private boolean enableDeviceFingerprint = true;
        private boolean enableLocationAware = true;
        
        // getters and setters
    }
    
    public static class DeviceConfig {
        private int maxDevicesPerUser = 5;
        private boolean enableDeviceWhitelist = false;
        
        // getters and setters
    }
}
```

## 8. 使用示例

### 8.1 完整的登录流程

```java
@RestController
public class AuthExampleController {
    
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1. 验证用户凭据
        User user = userService.authenticate(request.getUsername(), request.getPassword());
        
        // 2. 生成令牌
        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
        
        // 3. 存储刷新令牌到Redis
        String refreshTokenKey = "refresh:token:" + refreshToken;
        redisUtil.set(refreshTokenKey, user.getId(), 
                     Duration.ofHours(jwtUtil.getRefreshExpiration()));
        
        // 4. 构建响应
        LoginResponse response = LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiration(new Date(System.currentTimeMillis() + 
                        TimeUnit.HOURS.toMillis(jwtUtil.getExpiration())))
                .refreshExpiration(new Date(System.currentTimeMillis() + 
                        TimeUnit.HOURS.toMillis(jwtUtil.getRefreshExpiration())))
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .roles(user.getRoles())
                .build();
        
        return Result.success(response);
    }
}
```

### 8.2 前端Vue.js集成示例

```javascript
// auth.js
export const auth = {
    async login(username, password) {
        try {
            const response = await axios.post('/api/auth/login', {
                username,
                password
            });
            
            const { token, refreshToken, expiration } = response.data.data;
            
            // 存储令牌
            localStorage.setItem('access_token', token);
            localStorage.setItem('refresh_token', refreshToken);
            localStorage.setItem('token_expires', new Date(expiration).getTime());
            
            // 设置axios默认头部
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            
            return response.data.data;
        } catch (error) {
            throw new Error('登录失败：' + error.response.data.message);
        }
    },
    
    async refreshToken() {
        const refreshToken = localStorage.getItem('refresh_token');
        if (!refreshToken) {
            throw new Error('No refresh token available');
        }
        
        try {
            const response = await axios.post('/api/auth/refresh', null, {
                params: { refreshToken }
            });
            
            const { token, refreshToken: newRefreshToken, expiration } = response.data.data;
            
            // 更新令牌
            localStorage.setItem('access_token', token);
            localStorage.setItem('refresh_token', newRefreshToken);
            localStorage.setItem('token_expires', new Date(expiration).getTime());
            
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            
            return response.data.data;
        } catch (error) {
            // 刷新失败，清除令牌并跳转到登录页
            localStorage.removeItem('access_token');
            localStorage.removeItem('refresh_token');
            localStorage.removeItem('token_expires');
            throw error;
        }
    },
    
    logout() {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('token_expires');
        delete axios.defaults.headers.common['Authorization'];
    },
    
    getToken() {
        return localStorage.getItem('access_token');
    },
    
    isLoggedIn() {
        const token = this.getToken();
        const expires = localStorage.getItem('token_expires');
        return token && expires && Date.now() < parseInt(expires);
    }
};
```

### 8.3 React集成示例

```jsx
// AuthContext.jsx
import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext();

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(null);
    
    useEffect(() => {
        // 初始化时检查本地存储的令牌
        const storedToken = localStorage.getItem('access_token');
        const storedUser = localStorage.getItem('user_info');
        
        if (storedToken && storedUser) {
            setToken(storedToken);
            setUser(JSON.parse(storedUser));
            axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
        }
    }, []);
    
    const login = async (username, password) => {
        try {
            const response = await axios.post('/api/auth/login', {
                username,
                password
            });
            
            const { token, refreshToken, userId, username: userName, nickname, roles, expiration } = response.data.data;
            
            setToken(token);
            setUser({ userId, username: userName, nickname, roles });
            
            // 存储到localStorage
            localStorage.setItem('access_token', token);
            localStorage.setItem('refresh_token', refreshToken);
            localStorage.setItem('user_info', JSON.stringify({ userId, username: userName, nickname, roles }));
            localStorage.setItem('token_expires', new Date(expiration).getTime());
            
            // 设置axios默认头部
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            
            return response.data.data;
        } catch (error) {
            throw new Error('登录失败：' + error.response.data.message);
        }
    };
    
    const logout = () => {
        setUser(null);
        setToken(null);
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('user_info');
        localStorage.removeItem('token_expires');
        delete axios.defaults.headers.common['Authorization'];
    };
    
    const value = {
        user,
        token,
        login,
        logout,
        isLoggedIn: !!token
    };
    
    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};
```

## 9. 最佳实践

### 9.1 安全最佳实践

1. **密钥管理**：
   - 使用强随机密钥（至少256位）
   - 定期轮换JWT密钥
   - 不同环境使用不同密钥

2. **令牌安全**：
   - 设置合理的过期时间
   - 实施令牌黑名单机制
   - 使用HTTPS传输令牌

3. **设备管理**：
   - 实施设备指纹验证
   - 限制每用户最大设备数
   - 支持设备撤销和监控

### 9.2 性能最佳实践

1. **Redis缓存**：
   - 合理设置缓存过期时间
   - 使用Redis集群提高可用性
   - 监控缓存命中率和性能

2. **令牌验证**：
   - 在Redis中缓存用户信息
   - 避免频繁的数据库查询
   - 使用连接池优化数据库访问

### 9.3 监控和日志

1. **监控指标**：
   - 令牌验证成功率
   - 刷新令牌使用率
   - 异常登录尝试数量
   - 并发用户数量

2. **日志记录**：
   - 记录所有认证相关事件
   - 监控可疑活动
   - 保留关键操作日志

### 9.4 错误处理

1. **常见错误码**：
   - `INVALID_TOKEN`：令牌无效或已过期
   - `INVALID_REFRESH_TOKEN`：刷新令牌无效
   - `TOKEN_EXPIRED`：令牌已过期
   - `DEVICE_NOT_ALLOWED`：设备不允许访问

2. **错误处理策略**：
   - 统一错误响应格式
   - 区分可恢复和不可恢复错误
   - 提供有意义的错误信息

### 9.5 部署建议

1. **环境配置**：
   - 生产环境使用更强的密钥
   - 启用所有安全特性
   - 配置适当的超时时间

2. **高可用性**：
   - 使用Redis集群
   - 配置负载均衡
   - 实施健康检查

---

## 总结

本项目的双Token机制提供了完整的安全认证解决方案，具备以下特点：

- ✅ **安全性高**：多层次安全验证，支持设备指纹和位置感知
- ✅ **用户体验好**：自动刷新令牌，无感知续期
- ✅ **可扩展性强**：支持多设备管理，灵活配置
- ✅ **监控完善**：全面的日志记录和性能监控
- ✅ **易于集成**：提供完整的前后端集成示例

通过合理配置和使用本双Token机制，可以构建安全、可靠、易用的用户认证系统。