# 数据库重构变更总结 v2.0

本次重构按照**阿里巴巴MySQL规范**重新设计数据库。

**日期**: 2025-01-14  
**影响**: 数据库、Entity类、配置文件

---

## 🎯 核心变更

### 1. 数据库级别变更

#### 数据库重命名
```
旧: property_db / smart_property_db
新: smart_property_system
```

#### 表名规范化（复数 → 单数）
| 旧表名 | 新表名 | 说明 |
|--------|--------|------|
| `users` | `user` | 用户表 |
| `roles` | `role` | 角色表 |
| `user_roles` | `user_role` | 用户角色关联表 |
| `properties` | `property` | 物业表 |
| `leases` | `lease` | 租约表 |
| `payments` | `payment` | 支付记录表 |
| `maintenance_requests` | `maintenance_request` | 维修请求表 |
| `refresh_tokens` | `refresh_token` | 刷新令牌表 |

#### 字段规范化
所有表新增阿里巴巴规范必备字段：

| 旧字段 | 新字段 | 类型 | 说明 |
|--------|--------|------|------|
| `created_at` | `gmt_create` | DATETIME | 创建时间 |
| N/A | `gmt_modified` | DATETIME | 修改时间（自动更新） |
| N/A | `is_deleted` | TINYINT | 逻辑删除标志 |

#### 移除物理外键
- ✅ 删除所有 FOREIGN KEY 约束
- ✅ 保留索引以提升查询性能
- ✅ 通过应用层保证数据完整性

---

## 📁 新增文件

### 数据库脚本
```
docs/database/
├── 00_db_drop_and_create.sql    # 删除旧库，创建新库
├── 01_db_schema.sql             # 表结构定义
├── 02_db_init_admin.sql         # 初始化管理员
├── 03_db_mock_data.sql          # 模拟数据（80+条）
├── 04_db_verify.sql             # 数据验证查询
└── README.md                     # 详细使用文档
```

---

## 🔧 后端代码变更

### 1. Entity类变更

#### 所有Entity类的共同变更：

##### 时间戳类型变更
```java
// 旧
import java.time.Instant;
private Instant createdAt;

// 新
import java.time.LocalDateTime;
private LocalDateTime gmtCreate;
private LocalDateTime gmtModified;
```

##### 新增审计字段
```java
@CreationTimestamp
@Column(name = "gmt_create", updatable = false, nullable = false)
private LocalDateTime gmtCreate;

@UpdateTimestamp
@Column(name = "gmt_modified", nullable = false)
private LocalDateTime gmtModified;

@Column(name = "is_deleted", nullable = false)
@Builder.Default
private Integer isDeleted = 0;
```

##### 表名映射变更
```java
// 旧
@Table(name = "users")

// 新
@Table(name = "user")
```

#### 具体Entity变更清单

##### ✅ User.java
- 表名: `users` → `user`
- 中间表: `user_roles` → `user_role`
- 时间字段: `Instant createdAt` → `LocalDateTime gmtCreate`
- 新增: `gmtModified`, `isDeleted`

##### ✅ Role.java
- 表名: `roles` → `role`
- 字段类型: `RoleName name` → `String name`（直接存储字符串）
- 新增: `gmtCreate`, `gmtModified`, `isDeleted`

##### ✅ Property.java
- 表名: `properties` → `property`
- 时间字段: `Instant createdAt` → `LocalDateTime gmtCreate`
- 新增: `gmtModified`, `isDeleted`

##### ✅ Lease.java
- 表名: `leases` → `lease`
- 时间字段: `Instant createdAt` → `LocalDateTime gmtCreate`
- 新增: `gmtModified`, `isDeleted`

##### ✅ Payment.java
- 表名: `payments` → `payment`
- 时间字段: `Instant createdAt` → `LocalDateTime gmtCreate`
- 新增: `gmtModified`, `isDeleted`

##### ✅ MaintenanceRequest.java
- 表名: `maintenance_requests` → `maintenance_request`
- 时间字段: `Instant reportedAt/completedAt` → `LocalDateTime`
- 新增: `gmtCreate`, `gmtModified`, `isDeleted`

##### ✅ RefreshToken.java
- 表名: `refresh_tokens` → `refresh_token`
- 时间字段: `Instant expiryDate` → `LocalDateTime expiryDate`
- 新增: `gmtCreate`, `gmtModified`, `isDeleted`

### 2. 配置文件变更

#### application.properties
```properties
# 旧
spring.datasource.url=jdbc:mysql://localhost:3306/property_db?...

# 新
spring.datasource.url=jdbc:mysql://localhost:3306/smart_property_system?...
```

---

## 📊 模拟数据说明

### 数据统计
- **角色**: 3个（ADMIN, OWNER, TENANT）
- **用户**: 11个（1管理员 + 3业主 + 7租户）
- **物业**: 20个（郑州14个、洛阳3个、开封2个、其他1个）
- **租约**: 15个（11活跃 + 2过期 + 2终止）
- **支付记录**: 80+条（2024年6月 - 2025年1月）
- **维修请求**: 20条（4待处理 + 4处理中 + 10已完成 + 2已取消）

### 默认账户
| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | ROLE_ADMIN |
| owner_wang | admin123 | ROLE_OWNER |
| owner_li | admin123 | ROLE_OWNER |
| owner_zhang | admin123 | ROLE_OWNER |
| tenant_zhao | admin123 | ROLE_TENANT |
| tenant_chen | admin123 | ROLE_TENANT |
| tenant_liu | admin123 | ROLE_TENANT |
| tenant_huang | admin123 | ROLE_TENANT |
| tenant_xu | admin123 | ROLE_TENANT |
| tenant_sun | admin123 | ROLE_TENANT |
| tenant_ma | admin123 | ROLE_TENANT |

---

## 🚀 部署步骤

### Step 1: 执行数据库脚本
```bash
# 登录MySQL
mysql -u root -p

# 按顺序执行（使用绝对路径）
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/00_db_drop_and_create.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/01_db_schema.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/02_db_init_admin.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/03_db_mock_data.sql
source E:/University/Henan-University-of-Technology/2025-09/courseExperimentProject/CourseDesign/docs/database/04_db_verify.sql
```

### Step 2: 验证数据库
执行完毕后，检查是否有错误信息，并查看验证查询的输出结果。

### Step 3: 启动后端服务
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Step 4: 验证后端
- 检查控制台是否有SQL错误
- 访问 http://localhost:8080/actuator/health
- 测试登录接口（用户名：admin，密码：admin123）

### Step 5: 启动前端
```bash
cd frontend
npm install
npm run dev
```

---

## ⚠️ 重要注意事项

### 1. 时间类型变更影响
由于时间类型从 `Instant` 改为 `LocalDateTime`，请注意：

**Service层处理**：
- 如果代码中有 `Instant.now()`，需改为 `LocalDateTime.now()`
- 时间比较逻辑保持不变
- 前端传递的ISO8601时间字符串会自动转换

**示例**：
```java
// 旧代码
private Instant createdAt = Instant.now();

// 新代码
private LocalDateTime gmtCreate = LocalDateTime.now();
```

### 2. Role字段类型变更
Role 的 name 字段从 `RoleName` 枚举改为 `String`：

```java
// 旧代码
role.setName(RoleName.ROLE_ADMIN);

// 新代码
role.setName("ROLE_ADMIN");
```

### 3. JPA自动建表
由于配置了 `spring.jpa.hibernate.ddl-auto=update`：
- 首次启动会自动同步表结构
- 不会删除已有数据
- 建议先手动执行SQL脚本，再启动应用

### 4. 逻辑删除
新增的 `is_deleted` 字段用于逻辑删除：
- 0: 未删除
- 1: 已删除
- 需要在Service层实现逻辑删除逻辑
- 查询时需要过滤 `is_deleted = 0` 的记录

---

## 🔍 验证清单

### 数据库验证
- [ ] 数据库 `smart_property_system` 已创建
- [ ] 8个表已创建（单数表名）
- [ ] 所有表包含 gmt_create, gmt_modified, is_deleted 字段
- [ ] 管理员账户可以登录
- [ ] 模拟数据已正确插入

### 后端验证
- [ ] 项目编译成功（无错误）
- [ ] 应用启动成功
- [ ] 控制台无SQL错误
- [ ] JPA自动建表成功
- [ ] API接口正常响应

### 功能验证
- [ ] 用户登录功能正常
- [ ] 物业列表查询正常
- [ ] 租约管理功能正常
- [ ] 支付记录查询正常
- [ ] 维修请求功能正常

---

## 📝 后续工作建议

### 1. Service层适配
需要检查并更新所有Service类中使用时间字段的代码：
- 查找所有 `Instant` 类型的使用
- 替换为 `LocalDateTime`
- 测试时间相关的业务逻辑

### 2. 实现逻辑删除
在Repository或Service层实现逻辑删除：
```java
// 示例
public void deleteUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setIsDeleted(1);
    user.setGmtModified(LocalDateTime.now());
    userRepository.save(user);
}
```

### 3. 查询过滤
在所有查询中添加 `is_deleted = 0` 的过滤：
```java
// 示例
@Query("SELECT u FROM User u WHERE u.isDeleted = 0")
List<User> findAllActiveUsers();
```

### 4. 审计日志
考虑添加操作日志表，记录数据的创建、修改、删除操作。

---

## 🆘 常见问题

### Q1: 启动时报 "Table doesn't exist" 错误？
**A**: 确保已执行数据库脚本，并且 `spring.datasource.url` 中的数据库名正确。

### Q2: 字段映射错误？
**A**: 清除Hibernate缓存并重启：
```bash
mvn clean
rm -rf target/
mvn spring-boot:run
```

### Q3: 时间格式不对？
**A**: 检查 `application.properties` 中的时区设置：
```properties
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jackson.time-zone=UTC
```

### Q4: 如何回滚到旧版本？
**A**: 
1. 备份当前数据
2. 恢复旧的数据库备份
3. 使用Git恢复旧的Entity代码
```bash
git checkout HEAD~1 -- backend/src/main/java/com/example/propertymanagement/model/
```

---

## 📞 技术支持

如有问题，请：
1. 查看 `docs/database/README.md`
2. 检查控制台错误日志
3. 联系开发团队

---

**变更负责人**: 课程设计开发组  
**审核状态**: ✅ 已完成  
**版本**: 2.0  
**最后更新**: 2025-01-14
