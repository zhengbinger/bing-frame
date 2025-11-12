# 组织管理模块文档

## 1. 模块概述

组织管理模块是系统中的核心功能模块，提供了组织架构的管理功能，支持多级组织结构、树形结构展示，以及用户与组织的多对多关联关系管理。

### 1.1 主要功能

- **组织管理**：创建、更新、删除组织，支持层级结构
- **组织查询**：按ID、代码、父组织等条件查询组织信息
- **组织树**：获取完整的组织树形结构
- **用户组织关联**：建立用户与组织的多对多关联关系
- **主组织设置**：为用户设置主组织
- **批量操作**：支持批量关联、批量解除关联等操作

## 2. 数据模型设计

### 2.1 组织表 (organization)

**表结构**：

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `bigint(20)` | `PRIMARY KEY` | 组织ID |
| `name` | `varchar(255)` | `NOT NULL` | 组织名称 |
| `code` | `varchar(50)` | `UNIQUE NOT NULL` | 组织代码 |
| `parent_id` | `bigint(20)` | `DEFAULT 0` | 父组织ID，0表示顶级组织 |
| `path` | `varchar(1000)` | `DEFAULT ''` | 组织路径，用于快速查找层级关系 |
| `sort` | `int(11)` | `DEFAULT 0` | 排序字段 |
| `enabled` | `tinyint(1)` | `DEFAULT 1` | 是否启用，1启用，0禁用 |
| `description` | `varchar(1000)` | | 组织描述 |
| `create_time` | `datetime` | `DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | `datetime` | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

**索引**：
- `idx_parent_id` ON `parent_id`
- `idx_code` ON `code`
- `idx_path` ON `path`

### 2.2 用户组织关联表 (user_organization)

**表结构**：

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | `bigint(20)` | `PRIMARY KEY` | 关联ID |
| `user_id` | `bigint(20)` | `NOT NULL` | 用户ID |
| `organization_id` | `bigint(20)` | `NOT NULL` | 组织ID |
| `is_main` | `tinyint(1)` | `DEFAULT 0` | 是否为主组织，1是，0否 |
| `create_time` | `datetime` | `DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | `datetime` | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

**索引**：
- `idx_user_id` ON `user_id`
- `idx_organization_id` ON `organization_id`
- `idx_user_org` ON `(user_id, organization_id)` UNIQUE
- `idx_user_main` ON `(user_id, is_main)`

**外键约束**：
- `fk_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
- `fk_organization_id` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`) ON DELETE CASCADE

## 3. 实体类

### 3.1 Organization 实体类

```java
public class Organization {
    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private String path;
    private Integer sort;
    private Boolean enabled;
    private String description;
    private Date createTime;
    private Date updateTime;
    
    // 非数据库字段，用于树形结构展示
    @TableField(exist = false)
    private List<Organization> children;
    
    // getter 和 setter 方法
}
```

### 3.2 UserOrganization 实体类

```java
public class UserOrganization {
    private Long id;
    private Long userId;
    private Long organizationId;
    private Boolean isMain;
    private Date createTime;
    private Date updateTime;
    
    // 非数据库字段，用于关联查询
    @TableField(exist = false)
    private User user;
    
    @TableField(exist = false)
    private Organization organization;
    
    // getter 和 setter 方法
}
```

## 4. Service 层接口

### 4.1 OrganizationService 接口

```java
public interface OrganizationService extends IService<Organization> {
    // 根据ID获取组织
    Organization getOrganizationById(Long id);
    
    // 根据代码获取组织
    Organization getOrganizationByCode(String code);
    
    // 创建组织
    boolean createOrganization(Organization organization);
    
    // 更新组织
    boolean updateOrganization(Organization organization);
    
    // 删除组织
    boolean deleteOrganization(Long id);
    
    // 根据父组织ID获取子组织列表
    List<Organization> getOrganizationsByParentId(Long parentId);
    
    // 获取组织树形结构
    List<Organization> getOrganizationTree();
    
    // 获取启用的组织列表
    List<Organization> getEnabledOrganizations();
    
    // 检查是否存在循环引用
    boolean checkCircularReference(Long organizationId, Long parentId);
    
    // 检查组织是否有子组织
    boolean hasChildren(Long organizationId);
    
    // 根据条件分页查询组织
    IPage<Organization> getOrganizationPage(Page<Organization> page, @Param("ew") Wrapper<Organization> queryWrapper);
}
```

### 4.2 UserOrganizationService 接口

```java
public interface UserOrganizationService extends IService<UserOrganization> {
    // 根据用户ID获取关联的所有组织
    List<Organization> getUserOrganizationsByUserId(Long userId);
    
    // 根据组织ID获取关联的所有用户
    List<UserOrganization> getUserOrganizationsByOrganizationId(Long organizationId);
    
    // 获取用户的主组织
    UserOrganization getUserMainOrganization(Long userId);
    
    // 关联用户和组织
    boolean bindUserOrganization(Long userId, Long organizationId, boolean isMain);
    
    // 批量关联用户和组织
    boolean batchBindUserOrganizations(Long userId, List<Long> organizationIds, Long mainOrganizationId);
    
    // 取消用户和组织的关联
    boolean unbindUserOrganization(Long userId, Long organizationId);
    
    // 批量取消用户和组织的关联
    boolean batchUnbindUserOrganizations(Long userId, List<Long> organizationIds);
    
    // 设置用户的主组织
    boolean setMainOrganization(Long userId, Long organizationId);
    
    // 检查用户是否已关联到指定组织
    boolean isUserInOrganization(Long userId, Long organizationId);
}
```

## 5. Controller API 接口

### 5.1 OrganizationController API

| API路径 | 方法 | 描述 | 参数 |
| :--- | :--- | :--- | :--- |
| `/api/organization/{id}` | `GET` | 根据ID获取组织信息 | id: 组织ID |
| `/api/organization/code/{code}` | `GET` | 根据代码获取组织信息 | code: 组织代码 |
| `/api/organization` | `POST` | 创建组织 | Organization对象 |
| `/api/organization` | `PUT` | 更新组织 | Organization对象 |
| `/api/organization/{id}` | `DELETE` | 删除组织 | id: 组织ID |
| `/api/organization/tree` | `GET` | 获取组织树形结构 | 无 |
| `/api/organization/parent/{parentId}` | `GET` | 根据父组织ID获取子组织列表 | parentId: 父组织ID |
| `/api/organization/enabled` | `GET` | 获取启用的组织列表 | 无 |
| `/api/organization/page` | `GET` | 分页查询组织 | page: 页码<br>limit: 每页数量<br>name: 组织名称(可选)<br>code: 组织代码(可选)<br>enabled: 是否启用(可选) |
| `/api/organization/check/children/{id}` | `GET` | 检查组织是否有子组织 | id: 组织ID |
| `/api/organization/check/circular` | `GET` | 检查是否存在循环引用 | organizationId: 组织ID<br>parentId: 父组织ID |

### 5.2 UserOrganizationController API

| API路径 | 方法 | 描述 | 参数 |
| :--- | :--- | :--- | :--- |
| `/api/user-organization/user/{userId}` | `GET` | 根据用户ID查询关联的所有组织 | userId: 用户ID |
| `/api/user-organization/organization/{organizationId}` | `GET` | 根据组织ID查询关联的所有用户 | organizationId: 组织ID |
| `/api/user-organization/main/{userId}` | `GET` | 查询用户的主组织 | userId: 用户ID |
| `/api/user-organization/bind` | `POST` | 关联用户和组织 | userId: 用户ID<br>organizationId: 组织ID<br>isMain: 是否为主组织(可选，默认false) |
| `/api/user-organization/batch-bind` | `POST` | 批量关联用户和组织 | userId: 用户ID<br>organizationIds: 组织ID列表<br>mainOrganizationId: 主组织ID(可选) |
| `/api/user-organization/unbind` | `DELETE` | 取消用户和组织的关联 | userId: 用户ID<br>organizationId: 组织ID |
| `/api/user-organization/batch-unbind` | `DELETE` | 批量取消用户和组织的关联 | userId: 用户ID<br>organizationIds: 组织ID列表 |
| `/api/user-organization/set-main` | `PUT` | 设置用户的主组织 | userId: 用户ID<br>organizationId: 组织ID |
| `/api/user-organization/check` | `GET` | 检查用户是否已关联到指定组织 | userId: 用户ID<br>organizationId: 组织ID |

## 6. 使用示例

### 6.1 创建组织示例

```java
// 创建顶级组织
Organization topOrg = new Organization();
topOrg.setName("总公司");
topOrg.setCode("TOP_COMPANY");
topOrg.setParentId(0L);
topOrg.setSort(1);
topOrg.setEnabled(true);
topOrg.setDescription("公司总部");
organizationService.createOrganization(topOrg);

// 创建子组织
Organization dept1 = new Organization();
dept1.setName("技术部");
dept1.setCode("TECH_DEPT");
dept1.setParentId(topOrg.getId());
dept1.setSort(1);
dept1.setEnabled(true);
dept1.setDescription("负责产品研发");
organizationService.createOrganization(dept1);
```

### 6.2 建立用户组织关联示例

```java
// 为用户关联多个组织，并设置一个为主组织
Long userId = 1001L;
Long orgId1 = 101L; // 技术部
Long orgId2 = 102L; // 市场部
Long orgId3 = 103L; // 财务部

// 方法1：逐个关联
userOrganizationService.bindUserOrganization(userId, orgId1, true); // 设为主组织
userOrganizationService.bindUserOrganization(userId, orgId2, false);
userOrganizationService.bindUserOrganization(userId, orgId3, false);

// 方法2：批量关联
List<Long> orgIds = Arrays.asList(orgId1, orgId2, orgId3);
userOrganizationService.batchBindUserOrganizations(userId, orgIds, orgId1); // 设置orgId1为主组织

// 切换主组织
userOrganizationService.setMainOrganization(userId, orgId2);

// 查询用户的所有组织
List<Organization> userOrgs = userOrganizationService.getUserOrganizationsByUserId(userId);

// 查询用户的主组织
UserOrganization mainOrg = userOrganizationService.getUserMainOrganization(userId);
```

### 6.3 获取组织树示例

```java
// 获取完整的组织树形结构
List<Organization> organizationTree = organizationService.getOrganizationTree();

// 遍历组织树
for (Organization org : organizationTree) {
    System.out.println("组织名称: " + org.getName());
    System.out.println("组织代码: " + org.getCode());
    if (org.getChildren() != null && !org.getChildren().isEmpty()) {
        System.out.println("子组织数量: " + org.getChildren().size());
        // 递归遍历子组织
    }
}
```

## 7. 注意事项

1. **组织层级关系**：系统支持无限层级的组织结构，但建议层级不要过深，以保证查询性能

2. **循环引用检测**：创建或更新组织时，系统会自动检测是否存在循环引用，避免数据异常

3. **主组织唯一性**：每个用户最多只能有一个主组织，设置新的主组织时，系统会自动取消原主组织

4. **删除组织限制**：如果组织下有子组织或已关联用户，删除操作将会失败，需要先处理关联关系

5. **事务管理**：所有涉及用户组织关联的批量操作都使用了事务管理，确保数据一致性

6. **数据安全**：建议在实际应用中添加权限控制，确保用户只能管理其有权限的组织数据

## 8. 维护与扩展

1. **功能扩展**：如需扩展组织属性，可以直接修改`Organization`实体类和对应的数据库表

2. **查询优化**：对于大型组织架构，可以考虑在`path`字段上添加索引，并使用路径查询优化树形结构查询性能

3. **缓存策略**：可以为常用的组织查询添加缓存，如组织树、启用的组织列表等

4. **监控告警**：建议添加关键操作的日志记录，以便问题排查和审计