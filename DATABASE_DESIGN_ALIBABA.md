# 数据库设计 - 遵循阿里巴巴开发规范

## 📋 设计原则

本项目严格遵循[《阿里巴巴Java开发手册》](https://github.com/alibaba/p3c)中的数据库规约：

### 🚫 禁用物理外键约束

**规约说明**：
- 【强制】不要使用外键与级联，一切外键概念必须在应用层解决
- 【强制】外键影响插入速度，有触发器风险

**技术实现**：
1. 所有实体类移除 `@ManyToOne`、`@OneToMany` 等JPA关联注解
2. 使用逻辑外键字段（如 `user_id`、`property_id`）
3. 在应用层维护数据一致性

## 🗃 表结构设计

### 用户表 (user)
```sql
CREATE TABLE user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    first_name VARCHAR(50) COMMENT '名',
    last_name VARCHAR(50) COMMENT '姓',
    phone_number VARCHAR(20) COMMENT '手机号',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    INDEX idx_username (username),
    INDEX idx_email (email)
) COMMENT '用户表';
```

### 角色表 (role)
```sql
CREATE TABLE role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名',
    description VARCHAR(200) COMMENT '角色描述',
    PRIMARY KEY (id),
    INDEX idx_name (name)
) COMMENT '角色表';
```

### 用户角色关联表 (user_role) - 无外键约束
```sql
CREATE TABLE user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) COMMENT '用户角色关联表';
```

### 物业表 (property)
```sql
CREATE TABLE property (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '物业ID',
    owner_id BIGINT NOT NULL COMMENT '业主用户ID',
    owner_username VARCHAR(50) COMMENT '业主用户名(冗余字段)',
    address VARCHAR(255) NOT NULL COMMENT '详细地址',
    city VARCHAR(100) COMMENT '城市',
    state VARCHAR(100) COMMENT '省/州',
    zip_code VARCHAR(20) COMMENT '邮编',
    property_type VARCHAR(20) NOT NULL COMMENT '物业类型',
    bedrooms INT COMMENT '卧室数量',
    bathrooms DECIMAL(3,1) COMMENT '卫生间数量',
    square_footage INT COMMENT '面积(平方英尺)',
    status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态',
    rent_amount DECIMAL(10,2) COMMENT '租金',
    cover_image_path VARCHAR(255) COMMENT '封面图片路径',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    INDEX idx_owner_id (owner_id),
    INDEX idx_city (city),
    INDEX idx_status (status),
    INDEX idx_property_type (property_type)
) COMMENT '物业表';
```

### 租约表 (lease)
```sql
CREATE TABLE lease (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '租约ID',
    property_id BIGINT NOT NULL COMMENT '物业ID',
    property_address VARCHAR(255) COMMENT '物业地址(冗余字段)',
    tenant_id BIGINT NOT NULL COMMENT '租户用户ID',
    tenant_username VARCHAR(50) COMMENT '租户用户名(冗余字段)',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    rent_amount DECIMAL(10,2) NOT NULL COMMENT '租金',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    INDEX idx_property_id (property_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_end_date (end_date),
    INDEX idx_status (status)
) COMMENT '租约表';
```

### 维修申请表 (maintenance_request)
```sql
CREATE TABLE maintenance_request (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '维修申请ID',
    property_id BIGINT NOT NULL COMMENT '物业ID',
    property_address VARCHAR(255) COMMENT '物业地址(冗余字段)',
    tenant_id BIGINT NOT NULL COMMENT '租户用户ID',
    tenant_username VARCHAR(50) COMMENT '租户用户名(冗余字段)',
    description TEXT NOT NULL COMMENT '问题描述',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    reported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
    completed_at DATETIME COMMENT '完成时间',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    INDEX idx_property_id (property_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status),
    INDEX idx_reported_at (reported_at)
) COMMENT '维修申请表';
```

### 支付记录表 (payment)
```sql
CREATE TABLE payment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
    lease_id BIGINT NOT NULL COMMENT '租约ID',
    tenant_id BIGINT COMMENT '租户用户ID(冗余字段)',
    tenant_username VARCHAR(50) COMMENT '租户用户名(冗余字段)',
    property_id BIGINT COMMENT '物业ID(冗余字段)',
    property_address VARCHAR(255) COMMENT '物业地址(冗余字段)',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    payment_date DATE NOT NULL COMMENT '支付日期',
    payment_method VARCHAR(50) COMMENT '支付方式',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    INDEX idx_lease_id (lease_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_property_id (property_id),
    INDEX idx_payment_date (payment_date)
) COMMENT '支付记录表';
```

### 通知表 (notification)
```sql
CREATE TABLE notification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    type VARCHAR(50) NOT NULL COMMENT '通知类型',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    recipient_id BIGINT NOT NULL COMMENT '接收者用户ID',
    recipient_username VARCHAR(50) COMMENT '接收者用户名(冗余字段)',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已读',
    related_entity_type VARCHAR(50) COMMENT '关联实体类型',
    related_entity_id BIGINT COMMENT '关联实体ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    read_at DATETIME COMMENT '阅读时间',
    PRIMARY KEY (id),
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at),
    INDEX idx_is_read (is_read)
) COMMENT '通知表';
```

### 审计日志表 (audit_logs)
```sql
CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(100) COMMENT '操作用户名',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id VARCHAR(100) COMMENT '资源ID',
    description VARCHAR(500) COMMENT '操作描述',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT 'User Agent',
    result VARCHAR(20) COMMENT '操作结果',
    error_message VARCHAR(1000) COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_resource_type (resource_type),
    INDEX idx_created_at (created_at)
) COMMENT '审计日志表';
```

## 🔄 数据一致性保证

### 应用层外键约束检查
```java
@Service
@Transactional
public class DataIntegrityService {
    
    /**
     * 删除用户前检查关联数据
     */
    public void checkUserDependencies(Long userId) {
        // 检查是否有关联的物业
        if (propertyRepository.existsByOwnerId(userId)) {
            throw new BusinessException("用户存在关联物业，无法删除");
        }
        
        // 检查是否有关联的租约
        if (leaseRepository.existsByTenantId(userId)) {
            throw new BusinessException("用户存在关联租约，无法删除");
        }
        
        // 检查是否有关联的维修申请
        if (maintenanceRepository.existsByTenantId(userId)) {
            throw new BusinessException("用户存在关联维修申请，无法删除");
        }
    }
}
```

### 冗余字段自动维护
```java
@EventListener
public class DataConsistencyListener {
    
    /**
     * 用户信息更新时，同步更新冗余字段
     */
    @EventListener
    public void handleUserUpdateEvent(UserUpdateEvent event) {
        Long userId = event.getUserId();
        String newUsername = event.getNewUsername();
        
        // 更新物业表中的业主用户名
        propertyService.updateOwnerUsername(userId, newUsername);
        
        // 更新租约表中的租户用户名
        leaseService.updateTenantUsername(userId, newUsername);
        
        // 更新通知表中的接收者用户名
        notificationService.updateRecipientUsername(userId, newUsername);
    }
}
```

## 📊 索引策略

### 主要索引设计原则：
1. **主键索引**：所有表都有自增主键
2. **唯一索引**：用户名、邮箱等唯一字段
3. **业务索引**：根据查询场景建立复合索引
4. **时间索引**：创建时间、修改时间等时间字段
5. **状态索引**：经常用于筛选的状态字段

### 复合索引示例：
```sql
-- 物业查询复合索引
CREATE INDEX idx_property_owner_status ON property(owner_id, status, city);

-- 租约查询复合索引  
CREATE INDEX idx_lease_property_date ON lease(property_id, end_date, status);

-- 通知查询复合索引
CREATE INDEX idx_notification_recipient_read ON notification(recipient_id, is_read, created_at);
```

## 🛡 数据安全

### 软删除设计
- 所有业务表都包含 `is_deleted` 字段
- 删除操作改为更新 `is_deleted = 1`
- 查询时默认过滤已删除数据

### 审计字段标准
- `gmt_create`：创建时间，使用 `@CreationTimestamp`
- `gmt_modified`：修改时间，使用 `@UpdateTimestamp`  
- `is_deleted`：删除标记，默认值 0

## ⚡ 性能优化

### 冗余字段设计
- 避免频繁的关联查询
- 常用显示字段进行冗余存储
- 通过应用层事件机制保持数据同步

### 分页查询优化
```java
// 使用覆盖索引优化大表分页
@Query("SELECT new PropertyDto(p.id, p.address, p.ownerUsername, p.status) " +
       "FROM Property p WHERE p.ownerId = :ownerId AND p.isDeleted = 0")
Page<PropertyDto> findPropertiesWithCovering(@Param("ownerId") Long ownerId, Pageable pageable);
```

---

**遵循规范的好处**：
1. 🚀 **性能提升**：无外键约束，插入和删除更快
2. 🔧 **维护简单**：不会因为外键约束导致的死锁和阻塞问题
3. 📈 **扩展性强**：便于分库分表和微服务拆分
4. 🛡 **风险可控**：通过应用层逻辑确保数据一致性，更灵活可控