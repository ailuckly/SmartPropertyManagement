# 收支记录API完整指南

## 概述

收支记录功能已经完善，支持以下特性：
- ✅ 查询所有收支记录（根据用户角色自动过滤）
- ✅ 按租约ID筛选收支记录
- ✅ 收支记录包含物业地址和租客姓名
- ✅ 分页支持

## API端点

### 1. 获取所有收支记录

**端点**: `GET /api/payments`

**说明**: 查询所有可见的收支记录。系统会根据当前用户角色自动过滤：
- **管理员（ROLE_ADMIN）**: 可以查看所有收支记录
- **业主（ROLE_OWNER）**: 只能查看自己物业的收支记录  
- **租客（ROLE_TENANT）**: 只能查看自己租约的收支记录

**请求参数**:
- `page` (可选): 页码，从0开始，默认0
- `size` (可选): 每页大小，默认20
- `sort` (可选): 排序，例如 `paymentDate,desc`

**示例请求**:
```bash
GET /api/payments?page=0&size=10
```

**响应示例**:
```json
{
  "content": [
    {
      "id": 1,
      "leaseId": 1,
      "propertyAddress": "北京市朝阳区xxx小区1号楼101室",
      "tenantName": "张三",
      "amount": 2500.00,
      "paymentDate": "2024-06-05",
      "paymentMethod": "银行转账",
      "createdAt": "2024-06-05T10:30:00Z"
    }
  ],
  "currentPage": 0,
  "totalPages": 7,
  "totalElements": 64,
  "pageSize": 10
}
```

### 2. 按租约ID筛选收支记录

**端点**: `GET /api/payments?leaseId={id}`

**说明**: 查询指定租约的所有收支记录。系统会检查当前用户是否有权查看该租约的记录。

**请求参数**:
- `leaseId` (必填): 租约ID
- `page` (可选): 页码，从0开始，默认0
- `size` (可选): 每页大小，默认20

**示例请求**:
```bash
GET /api/payments?leaseId=1&page=0&size=10
```

**响应**: 同上

### 3. 记录收支

**端点**: `POST /api/payments`

**说明**: 记录新的收支信息（仅管理员或业主可以执行）

**请求体**:
```json
{
  "leaseId": 1,
  "amount": 2500.00,
  "paymentDate": "2024-10-14",
  "paymentMethod": "银行转账"
}
```

**响应**: 返回创建的收支记录（格式同上）

## 数据模型

### PaymentDto

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 收支记录ID |
| leaseId | Long | 关联的租约ID |
| propertyAddress | String | 物业地址 |
| tenantName | String | 租客姓名 |
| amount | BigDecimal | 金额 |
| paymentDate | LocalDate | 支付日期 |
| paymentMethod | String | 支付方式（如：银行转账、支付宝、微信支付等） |
| createdAt | Instant | 记录创建时间 |

## 权限说明

### 查询权限
- **管理员**: 可以查看所有收支记录
- **业主**: 可以查看自己物业相关的收支记录
- **租客**: 只能查看自己租约的收支记录

### 写入权限
- **管理员**: 可以记录任何收支
- **业主**: 可以记录自己物业相关的收支
- **租客**: 无法记录收支

## 前端集成指南

### 1. 主页面（收支记录列表）

点击"收支记录"菜单后，调用：
```javascript
GET /api/payments?page=0&size=20
```

系统会自动根据当前登录用户的角色返回相应的数据。

### 2. 筛选功能

如果需要按租约筛选，添加 `leaseId` 参数：
```javascript
GET /api/payments?leaseId=1&page=0&size=20
```

### 3. 分页

使用返回的分页信息：
- `totalPages`: 总页数
- `totalElements`: 总记录数
- `currentPage`: 当前页码

## 测试命令

### PowerShell测试脚本
```powershell
# 登录
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username":"admin","password":"admin123"}' `
    -SessionVariable session

# 获取所有收支记录
$payments = Invoke-WebRequest -Uri "http://localhost:8080/api/payments?page=0&size=10" `
    -Method GET `
    -WebSession $session

# 按租约ID筛选
$filtered = Invoke-WebRequest -Uri "http://localhost:8080/api/payments?leaseId=1" `
    -Method GET `
    -WebSession $session
```

## 注意事项

1. **需要重启应用**: 如果修改了代码，需要重启Spring Boot应用才能生效
2. **认证**: 所有API都需要登录认证，确保请求包含有效的Cookie
3. **时区**: 所有时间字段使用UTC时区
4. **精度**: 金额使用BigDecimal类型，精度为小数点后2位

## 数据库变更

本次更新包含以下改进：

1. **PaymentRepository**: 添加了 `@EntityGraph` 注解，确保查询时自动加载关联的租约、物业和租客信息
2. **PaymentDto**: 新增 `propertyAddress` 和 `tenantName` 字段
3. **PaymentController**: 支持可选的 `leaseId` 参数进行筛选
4. **PaymentService**: 增强了角色权限控制逻辑

## 故障排查

如果遇到问题：

1. **propertyAddress 或 tenantName 为空**: 确保应用已重启以加载新的 `@EntityGraph` 配置
2. **401 Unauthorized**: 检查是否已登录，Cookie是否有效
3. **403 Forbidden**: 检查当前用户是否有权限访问对应的记录
4. **筛选不工作**: 确认 `leaseId` 参数拼写正确，应用已重启

## 更新记录

**2025-10-14**:
- ✅ 添加了获取所有收支记录的功能
- ✅ 添加了按租约ID筛选的功能
- ✅ 增强了PaymentDto，包含物业地址和租客姓名
- ✅ 优化了数据库查询，使用EntityGraph避免N+1问题
- ✅ 完善了权限控制逻辑
