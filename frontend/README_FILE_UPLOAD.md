# 文件上传实时预览功能说明

## ✨ 功能特性

### 1. **实时本地预览**
- 📸 选择图片后**立即显示**预览（无需等待上传完成）
- 🚀 使用浏览器 `URL.createObjectURL()` API 创建本地预览
- 💨 预览速度快，用户体验好

### 2. **服务器图片显示**
- ☁️ 上传成功后，自动切换到服务器图片 URL
- 🔄 自动释放本地 blob URL，避免内存泄漏
- 🖼️ 通过 `/api/files/preview/{fileName}` 访问服务器图片

### 3. **多种上传模式**
- `picture-card`: 卡片式图片墙（默认）
- `picture`: 带缩略图的列表
- `text`: 纯文本列表

## 🎯 使用示例

### 基础图片上传（picture-card 模式）

```vue
<template>
  <FileUpload
    v-model="images"
    category="PROPERTY_IMAGE"
    :entity-id="propertyId"
    :limit="10"
    accept="image/*"
    tip-text="支持jpg/png格式，单个文件不超过5MB，最多10张"
    @upload-success="handleUploadSuccess"
  />
</template>

<script setup>
import { ref } from 'vue'
import FileUpload from '@/components/FileUpload.vue'

const propertyId = ref(1)
const images = ref([])

function handleUploadSuccess(file) {
  console.log('上传成功:', file)
}
</script>
```

### 文档上传（text 模式）

```vue
<template>
  <FileUpload
    v-model="contract"
    category="LEASE_CONTRACT"
    :entity-id="leaseId"
    list-type="text"
    :multiple="false"
    :limit="1"
    accept=".pdf,application/pdf"
    button-text="上传合同"
    tip-text="只支持PDF格式，单个文件不超过10MB"
  />
</template>

<script setup>
import { ref } from 'vue'
import FileUpload from '@/components/FileUpload.vue'

const leaseId = ref(1)
const contract = ref([])
</script>
```

## 🔧 工作流程

### 图片上传流程：

```
1. 用户选择图片
   ↓
2. handleChange() - 创建本地 blob URL
   ↓ (立即显示预览)
3. handleBeforeUpload() - 验证文件
   ↓
4. 开始上传到服务器
   ↓ (显示上传进度)
5. handleSuccess() - 上传成功
   ↓
6. 释放 blob URL
   ↓
7. 替换为服务器 URL
   ↓
8. 显示服务器图片 ✅
```

## 📋 Props 说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `category` | String | **必填** | 文件分类：`PROPERTY_IMAGE`, `LEASE_CONTRACT`, `MAINTENANCE_IMAGE`, `USER_AVATAR`, `OTHER` |
| `entityId` | Number/String | `null` | 关联实体ID（可选） |
| `modelValue` | Array | `[]` | v-model 绑定的文件列表 |
| `listType` | String | `picture-card` | 列表类型：`picture-card`, `picture`, `text` |
| `multiple` | Boolean | `true` | 是否支持多选 |
| `limit` | Number | `5` | 最大上传数量 |
| `accept` | String | `image/*` | 接受的文件类型 |
| `buttonText` | String | `点击上传` | 按钮文字 |
| `tipText` | String | `...` | 提示文字 |
| `showTip` | Boolean | `true` | 是否显示提示 |
| `disabled` | Boolean | `false` | 是否禁用 |

## 🎨 Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `update:modelValue` | `files: Array` | 文件列表更新 |
| `upload-success` | `file: Object` | 单个文件上传成功 |
| `upload-error` | `error: Object` | 上传失败 |
| `remove` | `file: Object` | 文件移除 |

## 🔍 调试技巧

打开浏览器控制台，查看详细的日志输出：

```
[FileUpload] File changed: {...}
[FileUpload] Created local preview URL: blob:http://localhost:5173/xxx
[FileUpload] Before upload: {...}
[FileUpload] Upload success response: {...}
[FileUpload] Revoked local blob URL
[FileUpload] Server preview URL: http://localhost:8080/api/files/preview/xxx.jpg
```

## 🚀 测试页面

访问 `/file-upload-test` 路由查看完整的测试示例。

## ⚠️ 注意事项

1. **CORS 配置**：确保后端 CORS 允许前端域名
2. **Cookie 认证**：使用 `withCredentials: true` 携带认证 Cookie
3. **内存管理**：上传成功后自动释放 blob URL，避免内存泄漏
4. **文件大小限制**：
   - 图片：最大 5MB
   - 文档：最大 10MB

## 📦 后端 API

### 上传文件
```
POST /api/files/upload
Content-Type: multipart/form-data

参数:
- file: 文件（必填）
- category: 分类（必填）
- entityId: 实体ID（可选）
- description: 描述（可选）
```

### 预览/下载文件
```
GET /api/files/preview/{fileName}   # 在线预览
GET /api/files/download/{fileName}  # 下载
```

### 获取文件列表
```
GET /api/files/entity/{category}/{entityId}
```

### 删除文件
```
DELETE /api/files/{fileId}
```

## 🎉 完成！

现在你的文件上传组件已经支持实时预览了！选择图片后会立即显示预览，上传成功后自动切换到服务器图片。
