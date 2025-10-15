# 文件系统集成计划

> 创建时间：2025-10-14 15:28
> 状态：文件基础设施已完成 ✅，**需要集成到业务模块** ⚠️

---

## 📊 当前状态分析

### ✅ 已完成的基础设施

1. **后端基础设施**
   - ✅ `FileController` - 文件上传、预览、下载、删除接口
   - ✅ `FileService` - 文件存储逻辑
   - ✅ `FileRepository` - 数据库操作
   - ✅ `File` 实体 - 文件元数据
   - ✅ `FileCategory` 枚举 - 文件分类
   - ✅ `FileStorageConfig` - 存储配置
   - ✅ 本地文件存储（`uploads/` 目录）

2. **前端基础设施**
   - ✅ `FileUpload.vue` 组件 - 通用文件上传组件
   - ✅ 实时预览功能（本地 blob + 服务器图片）
   - ✅ 文件删除功能
   - ✅ 文件分类支持

3. **测试页面**
   - ✅ `FileUploadTest.vue` - 测试页面可以正常工作

---

## ❌ 当前缺失的部分

### 1. **业务模块未集成文件上传**
- ❌ 物业管理 - 没有图片上传
- ❌ 租约管理 - 没有合同上传
- ❌ 维修管理 - 没有问题图片上传
- ❌ 用户资料 - 没有头像上传

### 2. **业务模块未显示已上传文件**
- ❌ 物业详情页 - 不显示图片
- ❌ 租约详情页 - 不显示合同
- ❌ 维修详情 - 不显示问题图片
- ❌ 用户头像 - 不显示头像

### 3. **文件与业务实体的关联**
- ❌ 需要确认实体是否需要添加关联字段
- ❌ 需要在查询时加载关联文件

---

## 🎯 集成计划（按优先级）

### 第一步：物业管理 - 图片上传集成 ⭐⭐⭐⭐⭐

**业务价值：** 高 - 物业图片是核心展示内容

**开发内容：**

#### 后端修改
```java
// 1. 确认 File 实体已经有 entityId 字段（已有）
// 2. 确认 FileCategory 有 PROPERTY_IMAGE（已有）
// 3. 无需修改后端代码，接口已经支持！
```

#### 前端修改
```vue
<!-- PropertiesView.vue -->
<template>
  <!-- 在物业表单中添加 -->
  <el-form-item label="物业图片" prop="images">
    <FileUpload
      v-model="form.images"
      category="PROPERTY_IMAGE"
      :entity-id="editingId"
      :limit="10"
      accept="image/*"
      tip-text="支持jpg/png格式，单个文件不超过5MB，最多10张"
    />
  </el-form-item>

  <!-- 在表单下方添加封面设置 -->
  <div v-if="form.images && form.images.length > 0">
    <el-alert type="info" :closable="false">
      点击图片可设置为封面
    </el-alert>
    <div class="image-gallery">
      <div 
        v-for="img in form.images" 
        :key="img.id"
        @click="setCoverImage(img.id)"
        :class="{'is-cover': img.isCover}"
      >
        <img :src="getPreviewUrl(img.storedFileName)" />
        <el-tag v-if="img.isCover" type="success">封面</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import FileUpload from '@/components/FileUpload.vue'

// 在 form 中添加 images 字段
const form = reactive({
  // ... 其他字段
  images: []
})

// 加载物业详情时，获取关联图片
async function loadPropertyImages(propertyId) {
  const res = await api.get(`/api/files/entity/PROPERTY_IMAGE/${propertyId}`)
  form.images = res.data
}

// 设置封面图片
async function setCoverImage(fileId) {
  await api.put(`/api/files/${fileId}/set-cover`, { propertyId: editingId })
  ElMessage.success('封面设置成功')
  await loadPropertyImages(editingId)
}
</script>
```

**预计时间：** 1-2 小时

---

### 第二步：维修管理 - 问题图片上传 ⭐⭐⭐⭐⭐

**业务价值：** 高 - 便于问题诊断

**开发内容：**

#### 前端修改
```vue
<!-- MaintenanceView.vue -->
<template>
  <!-- 在维修请求提交表单中添加 -->
  <el-form-item label="问题图片">
    <FileUpload
      v-model="form.images"
      category="MAINTENANCE_IMAGE"
      :entity-id="null"
      :limit="5"
      accept="image/*"
      tip-text="支持jpg/png格式，单个文件不超过5MB，最多5张"
    />
  </el-form-item>
</template>

<script setup>
import FileUpload from '@/components/FileUpload.vue'

const form = reactive({
  propertyId: null,
  description: '',
  images: []  // 新增
})

// 提交后，需要关联 entityId
async function handleSubmit() {
  // ... 提交维修请求
  const requestId = response.data.id
  
  // 更新图片的 entityId
  if (form.images.length > 0) {
    // 方案1：后端提供批量更新接口
    // 方案2：前端先上传时不传 entityId，提交后再关联
  }
}
</script>
```

**注意：** 维修请求需要先创建，然后才有 ID 关联图片。有两种方案：
1. **方案A（推荐）：** 先提交维修请求，获得 ID，然后上传图片时传递 entityId
2. **方案B：** 先上传图片（entityId=null），提交后批量更新 entityId

**预计时间：** 1-2 小时

---

### 第三步：租约管理 - 合同文件上传 ⭐⭐⭐⭐

**业务价值：** 中高 - 合同存档需求

**开发内容：**

#### 前端修改
```vue
<!-- LeasesView.vue -->
<template>
  <el-form-item label="租约合同">
    <FileUpload
      v-model="form.contract"
      category="LEASE_CONTRACT"
      :entity-id="editingId"
      list-type="text"
      :multiple="false"
      :limit="1"
      accept=".pdf,.doc,.docx,application/pdf"
      button-text="上传合同"
      tip-text="支持PDF、Word格式，单个文件不超过10MB"
    />
  </el-form-item>
</template>

<script setup>
import FileUpload from '@/components/FileUpload.vue'

const form = reactive({
  // ... 其他字段
  contract: []  // 新增
})
</script>
```

**预计时间：** 1 小时

---

### 第四步：用户资料 - 头像上传 ⭐⭐⭐

**业务价值：** 中 - 个性化需求

**开发内容：**

#### 前端修改
```vue
<!-- ProfileView.vue -->
<template>
  <el-card>
    <div class="avatar-section">
      <el-avatar :size="100" :src="avatarUrl" />
      <FileUpload
        v-model="avatar"
        category="USER_AVATAR"
        :entity-id="currentUserId"
        :limit="1"
        accept="image/*"
        list-type="picture-card"
        @upload-success="handleAvatarUpload"
      />
    </div>
  </el-card>
</template>

<script setup>
import FileUpload from '@/components/FileUpload.vue'

const avatar = ref([])
const avatarUrl = computed(() => {
  if (avatar.value.length > 0) {
    return getPreviewUrl(avatar.value[0].storedFileName)
  }
  return '/default-avatar.png'
})
</script>
```

**预计时间：** 1 小时

---

## 📝 集成步骤详解

### 步骤 1：确认后端接口（已完成 ✅）

后端接口已经完整，无需修改：
- `POST /api/files/upload` - 上传文件
- `GET /api/files/preview/{fileName}` - 预览文件
- `GET /api/files/entity/{category}/{entityId}` - 获取关联文件
- `DELETE /api/files/{fileId}` - 删除文件
- `PUT /api/files/{fileId}/set-cover` - 设置封面

### 步骤 2：前端集成（待开发）

#### A. 导入组件
```vue
<script setup>
import FileUpload from '@/components/FileUpload.vue'
</script>
```

#### B. 在 form 中添加字段
```javascript
const form = reactive({
  // 原有字段...
  images: []  // 或 contract: [] 或 avatar: []
})
```

#### C. 在表单中使用组件
```vue
<el-form-item label="xxx">
  <FileUpload
    v-model="form.images"
    category="PROPERTY_IMAGE"
    :entity-id="editingId"
    :limit="10"
  />
</el-form-item>
```

#### D. 加载已有文件
```javascript
async function loadFiles(entityId) {
  const res = await api.get(`/api/files/entity/PROPERTY_IMAGE/${entityId}`)
  form.images = res.data
}
```

---

## ⚠️ 注意事项

### 1. entityId 的处理

**问题：** 创建时还没有 ID，如何关联文件？

**解决方案：**
```javascript
// 方案A：先创建实体，后上传文件
async function handleSubmit() {
  // 1. 先创建实体（不包含文件）
  const response = await api.post('/api/properties', formData)
  const propertyId = response.data.id
  
  // 2. 文件组件会自动上传，entityId 传递 propertyId
}

// 方案B：上传文件时不传 entityId，创建后再关联
// 这需要后端提供批量更新接口
```

**推荐：** 使用方案A，分两步操作。

### 2. 文件显示的优化

```vue
<!-- 物业详情页显示图片 -->
<div class="property-images">
  <el-image
    v-for="img in property.images"
    :key="img.id"
    :src="getPreviewUrl(img.storedFileName)"
    :preview-src-list="imageUrls"
    fit="cover"
  />
</div>
```

### 3. 性能优化

- 图片列表加载时使用缩略图
- 大图使用懒加载
- 文件列表使用虚拟滚动（如果很多）

---

## 🚀 推荐开发顺序

### 第一阶段（今天）
1. **物业管理 - 图片上传** (1-2小时)
   - 最常用的功能
   - 视觉效果好，用户感知强

### 第二阶段（明天）
2. **维修管理 - 问题图片** (1-2小时)
   - 提升维修诊断效率
3. **租约管理 - 合同上传** (1小时)
   - 完善文档管理

### 第三阶段（后续）
4. **用户资料 - 头像上传** (1小时)
   - 个性化功能

**总计：** 4-6 小时完成所有集成

---

## ✅ 验收标准

### 物业管理
- [ ] 可以在创建物业时上传图片
- [ ] 可以在编辑物业时上传/删除图片
- [ ] 可以设置封面图片
- [ ] 物业列表显示封面图片
- [ ] 物业详情显示所有图片

### 维修管理
- [ ] 可以在提交维修请求时上传问题图片
- [ ] 维修详情显示问题图片
- [ ] 可以预览图片

### 租约管理
- [ ] 可以上传租约合同
- [ ] 可以预览/下载合同
- [ ] 租约详情显示合同信息

### 用户资料
- [ ] 可以上传头像
- [ ] 头像显示在导航栏
- [ ] 头像显示在个人资料页

---

## 📊 进度追踪

- [ ] 第一步：物业管理 - 图片上传（0%）
- [ ] 第二步：维修管理 - 问题图片（0%）
- [ ] 第三步：租约管理 - 合同上传（0%）
- [ ] 第四步：用户资料 - 头像上传（0%）

---

## 🎯 现在开始？

**我建议立即开始"物业管理 - 图片上传"集成！**

你想：
1. **A. 立即开始物业图片上传集成** ✅ 推荐
2. **B. 先看看具体怎么改 PropertiesView.vue**
3. **C. 想先从维修管理开始**
4. **D. 其他想法**

告诉我你的选择，我会提供详细的代码修改方案！🚀
