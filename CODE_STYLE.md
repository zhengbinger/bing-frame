# 项目代码风格规范

## 🚨 配置文件格式规范

- **默认使用YAML格式**：所有Spring Boot配置文件（如application.properties）应默认使用YAML格式（application.yml）
- **格式要求**：保持缩进一致（2个空格），使用小写字母和连字符分隔
- **注释规范**：为重要配置添加中文注释
- **例外情况**：仅在特定场景（如需要与第三方系统集成且有严格格式要求）下才使用properties格式

## 🚨 Java版本规范（重要更新）

### 默认Java 8规范
**自2024年8月21日起，所有代码默认使用Java 8语法，无需单独声明例外情况。**

### Java 8限制规范
- **禁止使用Java 9+新特性**：
  - `CompletableFuture.failedFuture()` → 使用`new CompletableFuture<>()` + `completeExceptionally()`
  - `List.of()`、`Set.of()`、`Map.of()` → 使用`Arrays.asList()`、`Collections.singleton()`
  - `var`关键字 → 使用显式类型声明
  - 接口私有方法 → 使用抽象类或工具类

### 例外情况声明
**仅在需要使用Java 9+特性时，必须在相关代码处添加明确注释**：
```java
// Java 9+特性例外：使用failedFuture简化异常处理
// 例外原因：项目已升级至Java 11
return CompletableFuture.failedFuture(e);
```

## 类注释标准格式

所有Java类必须包含规范的类注释，格式如下：

```java
/**
 * [类功能描述]
 * [实现方式说明]
 * [主要用途]
 * 
 * @author zhengbing
 * @date YYYY-MM-DD
 */
public class ClassName {
    // 类实现
}
```

### 格式说明：
1. **类功能描述**：简要说明类的核心功能和职责
2. **实现方式说明**：描述关键实现细节（如设计模式、核心算法等）
3. **主要用途**：说明类的典型使用场景和适用范围
4. **@author**：固定为项目作者名 `zhengbing`
5. **@date**：类创建日期，格式为 `YYYY-MM-DD`（使用创建当天日期）

### 示例：
```java
/**
 * 用户上下文工具类
 * 使用ThreadLocal存储当前线程的用户信息，实现请求线程内用户数据的共享
 * 提供从请求中提取用户信息并设置到上下文、获取当前用户、清理上下文等静态方法
 * 
 * @author zhengbing
 * @date 2025-07-29
 */
public class UserContext {
    // 类实现
}
```

## 方法注释标准格式

所有公共方法必须包含规范的方法注释，格式如下：

```java
/**
 * [方法功能描述]
 * 
 * @param param1 参数1描述
 * @param param2 参数2描述
 * @return 返回值描述
 */
public ReturnType methodName(ParamType param1, ParamType param2) {
    // 方法实现
}
```

### 格式说明：
1. **方法功能描述**：简要说明方法的核心功能和职责
2. **空行**：在方法描述和参数行之间必须有一个空行
3. **参数描述**：为每个参数提供描述，格式为 `@param 参数名 参数描述`
4. **返回值描述**：说明返回值的含义和可能的类型

### 示例：
```java
/**
 * 根据用户ID获取用户信息
 * 
 * @param userId 用户ID
 * @return 用户信息对象
 */
public User getUserById(Long userId) {
    // 方法实现
}
```

## 导入语句规范

为保持代码整洁，所有Java源文件必须遵循以下导入语句规范：

### 未使用导入清理
- **保存时清理**：开发人员在保存代码时必须清理所有未使用的导入语句
- **构建时验证**：项目构建过程会自动验证并拒绝包含未使用导入的代码
- **IDE配置**：建议配置IDE自动清理未使用导入，如IntelliJ IDEA中的"Optimize Imports on the Fly"选项

### 导入语句格式
- 按包路径字母顺序排列导入语句
- 标准Java库导入应放在最前面
- 第三方库导入应放在中间
- 项目内部包导入应放在最后
- 使用空行分隔不同类别的导入

## 注意事项
- 类注释应放在类定义的正上方，与类声明直接相邻
- 方法注释应放在方法定义的正上方，与方法声明直接相邻
- 保持注释内容简洁明了，突出核心信息
- 所有公共类、工具类、公共方法必须遵循此注释规范
- 日期格式必须严格遵循 `YYYY-MM-DD` 格式
- 项目已配置强制Java 8编译，违反规范将导致构建失败