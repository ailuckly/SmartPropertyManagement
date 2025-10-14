# 智慧物业管理平台 - 前端开发规范

## 📋 目录

1. [技术栈](#技术栈)
2. [项目结构](#项目结构)
3. [设计规范](#设计规范)
4. [组件使用规范](#组件使用规范)
5. [页面实现说明](#页面实现说明)
6. [开发规范](#开发规范)
7. [常见问题](#常见问题)

---

## 技术栈

### 核心框架
- **Vue 3.4.27** - 渐进式 JavaScript 框架
- **Vite 5.3.3** - 下一代前端构建工具
- **Vue Router 4.3.3** - 官方路由管理器
- **Pinia 2.1.7** - Vue 状态管理库

### UI 组件库
- **Element Plus 最新版** - 基于 Vue 3 的企业级 UI 组件库
- **@element-plus/icons-vue** - Element Plus 图标库

### HTTP 请求
- **Axios 1.7.4** - 基于 Promise 的 HTTP 客户端

---

## 项目结构

```
frontend/
├── public/              # 静态资源
├── src/
│   ├── api/            # API 接口定义
│   │   └── http.js     # Axios 配置和拦截器
│   ├── assets/         # 资源文件（图片、字体等）
│   ├── components/     # 全局组件
│   │   └── NotifyHost.vue
│   ├── router/         # 路由配置
│   │   └── index.js
│   ├── stores/         # Pinia 状态管理
│   │   └── auth.js     # 认证状态
│   ├── styles/         # 全局样式
│   │   └── base.css    # 基础样式
│   ├── utils/          # 工具函数
│   │   └── notify.js   # 通知工具
│   ├── views/          # 页面组件
│   │   ├── LoginView.vue
│   │   ├── RegisterView.vue
│   │   ├── DashboardView.vue
│   │   ├── PropertiesView.vue
│   │   ├── LeasesView.vue
│   │   ├── MaintenanceView.vue
│   │   ├── PaymentsView.vue
│   │   └── NotFoundView.vue
│   ├── App.vue         # 根组件
│   └── main.js         # 入口文件
├── index.html
├── package.json
└── vite.config.js
```

---

## 设计规范

### 🎨 视觉风格

#### 主题定位
- **简洁大方** - 避免过度装饰
- **专业规范** - 符合企业级管理系统标准
- **统一一致** - 全局使用 Element Plus 组件

#### 色彩方案
```css
/* Element Plus 默认色彩 */
--el-color-primary: #409EFF    /* 主色调 - 蓝色 */
--el-color-success: #67C23A    /* 成功 - 绿色 */
--el-color-warning: #E6A23C    /* 警告 - 橙色 */
--el-color-danger: #F56C6C     /* 危险 - 红色 */
--el-color-info: #909399       /* 信息 - 灰色 */

/* 背景色 */
--background-color: #f5f7fa    /* 页面背景 */
--card-background: #ffffff     /* 卡片背景 */
```

#### 布局规范
- **页面容器**：使用 `<el-container>` 系列组件
- **内容区域**：使用 `<el-card>` 包裹内容
- **间距**：统一使用 `el-row` 的 `:gutter="20"`
- **响应式**：使用 `el-col` 的响应式属性 (`:xs`, `:sm`, `:md`, `:lg`, `:xl`)

#### 字体规范
```css
font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
```

---

## 组件使用规范

### 📦 常用组件

#### 1. 表单组件 (`el-form`)

```vue
<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-width="100px"
    size="default"
    @submit.prevent="handleSubmit"
  >
    <el-form-item label="字段名" prop="field">
      <el-input v-model="form.field" placeholder="请输入" clearable />
    </el-form-item>
    
    <el-form-item>
      <el-button type="primary" native-type="submit" :loading="submitting">
        提交
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive } from 'vue';

const formRef = ref(null);
const submitting = ref(false);

const form = reactive({
  field: ''
});

const rules = {
  field: [
    { required: true, message: '请输入字段名', trigger: 'blur' }
  ]
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    try {
      // API 调用
    } finally {
      submitting.value = false;
    }
  });
};
</script>
```

#### 2. 表格组件 (`el-table`)

```vue
<template>
  <el-table
    :data="tableData"
    v-loading="loading"
    stripe
    style="width: 100%"
  >
    <el-table-column prop="id" label="ID" width="60" />
    <el-table-column prop="name" label="名称" min-width="200" />
    <el-table-column prop="status" label="状态" width="100">
      <template #default="{ row }">
        <el-tag :type="getStatusType(row.status)" size="small">
          {{ renderStatus(row.status) }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column label="操作" width="150" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" size="small" @click="handleEdit(row)">
          编辑
        </el-button>
        <el-button link type="danger" size="small" @click="handleDelete(row.id)">
          删除
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>
```

#### 3. 分页组件 (`el-pagination`)

```vue
<template>
  <div class="pagination-wrapper">
    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script setup>
import { reactive } from 'vue';

const pagination = reactive({
  page: 1,      // Element UI 从 1 开始
  size: 10,
  total: 0
});

const handlePageChange = (page) => {
  pagination.page = page;
  fetchData();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.page = 1;
  fetchData();
};

// API 调用时需要转换
const fetchData = async () => {
  const { data } = await api.get('/endpoint', {
    params: {
      page: pagination.page - 1,  // 后端从 0 开始
      size: pagination.size
    }
  });
  pagination.total = data.totalElements || 0;
};
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
```

#### 4. 消息提示

```javascript
import { ElMessage, ElMessageBox } from 'element-plus';

// 成功提示
ElMessage.success('操作成功');

// 错误提示
ElMessage.error('操作失败');

// 警告提示
ElMessage.warning('请注意');

// 确认对话框
try {
  await ElMessageBox.confirm('确认删除？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  });
  // 用户点击确定后执行
} catch {
  // 用户点击取消
}
```

---

## 页面实现说明

### 1. 登录页面 (`LoginView.vue`)

**功能**
- 用户登录
- 表单验证
- 错误提示
- 跳转注册

**关键实现**
```vue
<el-card shadow="hover" class="login-card">
  <el-form ref="formRef" :model="form" :rules="rules">
    <el-form-item prop="username">
      <el-input v-model="form.username" :prefix-icon="User" />
    </el-form-item>
    <el-form-item prop="password">
      <el-input v-model="form.password" type="password" :prefix-icon="Lock" show-password />
    </el-form-item>
    <el-button type="primary" native-type="submit" :loading="authStore.loading">
      登录
    </el-button>
  </el-form>
</el-card>
```

**样式特点**
- 全屏居中布局
- 浅灰色背景 `#f5f7fa`
- 白色卡片悬浮效果
- 响应式设计

---

### 2. 仪表盘 (`DashboardView.vue`)

**功能**
- 统计数据展示（物业数量、租约数量、待处理维修）
- 欢迎信息和使用说明

**关键组件**
```vue
<!-- 统计卡片 -->
<el-card shadow="hover" class="stat-card">
  <el-statistic title="物业数量" :value="stats.properties">
    <template #prefix>
      <el-icon style="color: #409EFF"><OfficeBuilding /></el-icon>
    </template>
  </el-statistic>
</el-card>

<!-- 信息描述 -->
<el-descriptions :column="1" border>
  <el-descriptions-item label="快速导航">
    使用左侧导航栏可快速进入各个功能模块
  </el-descriptions-item>
</el-descriptions>
```

**数据获取**
```javascript
const fetchStats = async () => {
  loading.value = true;
  try {
    const [propertiesResponse, leasesResponse, maintenanceResponse] = 
      await Promise.all([
        api.get('/properties', { params: { size: 1 } }),
        api.get('/leases', { params: { size: 1 } }),
        api.get('/maintenance-requests', { params: { size: 50 } })
      ]);
    // 处理数据...
  } finally {
    loading.value = false;
  }
};
```

---

### 3. 物业管理 (`PropertiesView.vue`)

**功能**
- 物业列表查看（表格）
- 新增/编辑物业（表单）
- 删除物业（确认对话框）
- 筛选和分页

**布局结构**
```vue
<el-row :gutter="20">
  <!-- 左侧表单 -->
  <el-col :xs="24" :lg="8">
    <el-card>表单内容</el-card>
  </el-col>
  
  <!-- 右侧列表 -->
  <el-col :xs="24" :lg="16">
    <el-card>
      <筛选工具栏 />
      <el-table />
      <el-pagination />
    </el-card>
  </el-col>
</el-row>
```

**状态管理**
```javascript
const editingId = ref(null);  // null = 新增模式，有值 = 编辑模式

const startEdit = (item) => {
  editingId.value = item.id;
  Object.assign(form, item);
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const resetForm = () => {
  editingId.value = null;
  formRef.value?.resetFields();
};
```

---

### 4. 租约管理 (`LeasesView.vue`)

**特殊组件**
- `el-date-picker` - 日期选择器
- `el-input-number` - 数字输入框

**日期处理**
```vue
<el-date-picker
  v-model="form.startDate"
  type="date"
  format="YYYY-MM-DD"
  value-format="YYYY-MM-DD"
  style="width: 100%"
/>
```

---

### 5. 维修管理 (`MaintenanceView.vue`)

**角色区分**
```javascript
const isTenant = authStore.hasAnyRole(['ROLE_TENANT']);
const isOwnerOrAdmin = authStore.hasAnyRole(['ROLE_OWNER', 'ROLE_ADMIN']);
```

**内联状态更新**
```vue
<el-table-column v-if="isOwnerOrAdmin" label="操作">
  <template #default="{ row }">
    <el-select v-model="row.status" size="small" @change="updateStatus(row)">
      <el-option label="待处理" value="PENDING" />
      <el-option label="处理中" value="IN_PROGRESS" />
      <el-option label="已完成" value="COMPLETED" />
    </el-select>
  </template>
</el-table-column>
```

---

### 6. 收支记录 (`PaymentsView.vue`)

**查询机制**
- 必须先输入租约ID才能查询
- 登记后自动刷新列表

**内联表单布局**
```vue
<el-form inline class="payment-form">
  <el-form-item label="租约ID">...</el-form-item>
  <el-form-item label="金额">...</el-form-item>
  <el-form-item label="支付日期">...</el-form-item>
  <el-form-item>
    <el-button type="primary" native-type="submit">登记</el-button>
  </el-form-item>
</el-form>
```

---

## 开发规范

### 🔧 编码规范

#### 1. 组件命名
- 使用 PascalCase（大驼峰）
- 文件名与组件名一致
- 例：`PropertiesView.vue`

#### 2. 变量命名
- 使用 camelCase（小驼峰）
- 常量使用 UPPER_SNAKE_CASE
- 例：`formRef`, `MAX_SIZE`

#### 3. 函数命名
- 使用动词开头
- 例：`handleSubmit`, `fetchData`, `renderStatus`

#### 4. 组件通信
```javascript
// Props 定义
const props = defineProps({
  modelValue: String,
  disabled: Boolean
});

// Emits 定义
const emit = defineEmits(['update:modelValue', 'change']);

// 使用
emit('update:modelValue', newValue);
```

#### 5. 状态管理
```javascript
// 响应式数据
const loading = ref(false);
const form = reactive({ field: '' });

// 计算属性
const filteredList = computed(() => {
  return list.value.filter(item => item.status === 'active');
});
```

#### 6. 生命周期
```javascript
import { onMounted, onBeforeUnmount } from 'vue';

onMounted(() => {
  fetchData();
});

onBeforeUnmount(() => {
  // 清理工作
});
```

---

### 🎯 最佳实践

#### 1. 表单验证
✅ **推荐**
```javascript
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    // 提交逻辑
  });
};
```

❌ **不推荐**
```javascript
const handleSubmit = async () => {
  // 直接提交，没有验证
};
```

#### 2. 错误处理
✅ **推荐**
```javascript
try {
  await api.post('/endpoint', data);
  ElMessage.success('操作成功');
  fetchData();
} catch (err) {
  ElMessage.error(err.response?.data?.message ?? '操作失败');
}
```

#### 3. 加载状态
✅ **推荐**
```javascript
const loading = ref(false);

const fetchData = async () => {
  loading.value = true;
  try {
    const { data } = await api.get('/endpoint');
    // 处理数据
  } finally {
    loading.value = false;
  }
};
```

#### 4. 分页处理
✅ **推荐**
```javascript
// Element UI 页码从 1 开始，后端从 0 开始
const params = {
  page: pagination.page - 1,  // 转换
  size: pagination.size
};
```

#### 5. 删除确认
✅ **推荐**
```javascript
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await api.delete(`/endpoint/${id}`);
    ElMessage.success('删除成功');
    fetchData();
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};
```

---

### 📐 样式规范

#### 1. 使用 scoped 样式
```vue
<style scoped>
.my-component {
  /* 样式只作用于当前组件 */
}
</style>
```

#### 2. 深度选择器
```vue
<style scoped>
:deep(.el-input__inner) {
  /* 修改 Element Plus 组件内部样式 */
}
</style>
```

#### 3. 响应式设计
```vue
<style scoped>
@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
  }
}
</style>
```

#### 4. 常用布局类
```css
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
```

---

## 常见问题

### Q1: 为什么分页页码要减1？
**A:** Element Plus 的分页组件页码从 1 开始，而后端 Spring Boot 的分页从 0 开始，需要转换。

```javascript
// 前端发送请求
params: { page: pagination.page - 1 }

// 后端返回后处理
pagination.total = data.totalElements;
```

### Q2: 如何实现编辑/新增切换？
**A:** 使用 `editingId` 判断模式。

```javascript
const editingId = ref(null);

// 新增
const resetForm = () => {
  editingId.value = null;
  // 清空表单
};

// 编辑
const startEdit = (item) => {
  editingId.value = item.id;
  Object.assign(form, item);
};

// 提交
const handleSubmit = async () => {
  if (editingId.value) {
    await api.put(`/endpoint/${editingId.value}`, form);
  } else {
    await api.post('/endpoint', form);
  }
};
```

### Q3: 如何处理角色权限？
**A:** 使用 `v-if` 结合 auth store。

```vue
<el-col v-if="isOwnerOrAdmin" :xs="24" :lg="8">
  <!-- 仅管理员和业主可见 -->
</el-col>

<script setup>
const authStore = useAuthStore();
const isOwnerOrAdmin = authStore.hasAnyRole(['ROLE_OWNER', 'ROLE_ADMIN']);
</script>
```

### Q4: 如何统一错误提示？
**A:** 在 Axios 拦截器中统一处理。

```javascript
// api/http.js
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // 未登录，跳转登录页
    }
    return Promise.reject(error);
  }
);
```

### Q5: 为什么要使用 `reactive` 和 `ref`？
**A:** 
- `ref`: 用于基本类型（string, number, boolean）
- `reactive`: 用于对象和数组

```javascript
const count = ref(0);           // 基本类型
const form = reactive({         // 对象
  name: '',
  age: 0
});
```

---

## 📚 参考资料

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Element Plus 官方文档](https://element-plus.org/zh-CN/)
- [Vite 官方文档](https://cn.vitejs.dev/)
- [Pinia 官方文档](https://pinia.vuejs.org/zh/)

---

## 🔄 更新日志

### 2025-01-14
- ✅ 完成 Element Plus 集成
- ✅ 重构所有页面组件
- ✅ 统一设计规范
- ✅ 清理旧的自定义样式

---

**文档维护**: 前端开发团队  
**最后更新**: 2025-01-14
