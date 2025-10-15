<template>
  <div class="users-view">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" :icon="Plus" @click="handleCreate">新增用户</el-button>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索用户名或邮箱"
          :prefix-icon="Search"
          style="width: 300px"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <!-- 用户表格 -->
      <el-table
        v-loading="loading"
        :data="users"
        style="width: 100%; margin-top: 20px"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="firstName" label="名字" width="120" />
        <el-table-column prop="lastName" label="姓氏" width="120" />
        <el-table-column prop="phoneNumber" label="电话" width="150" />
        <el-table-column label="角色" width="200">
          <template #default="{ row }">
            <el-tag
              v-for="role in row.roles"
              :key="role"
              :type="getRoleTagType(role)"
              size="small"
              style="margin-right: 4px"
            >
              {{ getRoleDisplayName(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              link
              :type="row.isDeleted === 1 ? 'success' : 'warning'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.isDeleted === 1 ? '启用' : '禁用' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="isEdit ? '不填写则不修改密码' : '请输入密码'"
            show-password
          />
        </el-form-item>
        <el-form-item label="名字" prop="firstName">
          <el-input v-model="form.firstName" placeholder="请输入名字" />
        </el-form-item>
        <el-form-item label="姓氏" prop="lastName">
          <el-input v-model="form.lastName" placeholder="请输入姓氏" />
        </el-form-item>
        <el-form-item label="电话" prop="phoneNumber">
          <el-input v-model="form.phoneNumber" placeholder="请输入电话号码" />
        </el-form-item>
        <el-form-item label="角色" prop="roleNames">
          <el-select
            v-model="form.roleNames"
            multiple
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in availableRoles"
              :key="role.name"
              :label="getRoleDisplayName(role.name)"
              :value="role.name"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const searchQuery = ref('')

// 用户列表数据
const users = ref([])

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 可用角色列表
const availableRoles = ref([])

// 表单数据
const form = reactive({
  id: null,
  username: '',
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  phoneNumber: '',
  roleNames: []
})

const formRef = ref(null)

// 表单验证规则
const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { 
      required: false,
      validator: (rule, value, callback) => {
        if (!isEdit.value && !value) {
          callback(new Error('请输入密码'))
        } else if (value && value.length < 6) {
          callback(new Error('密码长度不能少于6个字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  roleNames: [
    { required: true, message: '请至少选择一个角色', trigger: 'change' }
  ]
})

/**
 * 获取用户列表
 */
async function fetchUsers() {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1, // 后端页码从0开始
      size: pagination.size,
      sortBy: 'id',
      sortDir: 'DESC'
    }
    
    const response = await axios.get('/api/users', { params })
    users.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error(error.response?.data?.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取可用角色列表
 */
async function fetchRoles() {
  try {
    const response = await axios.get('/api/users/roles')
    availableRoles.value = response.data
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  }
}

/**
 * 搜索用户
 */
function handleSearch() {
  pagination.page = 1
  fetchUsers()
}

/**
 * 重置搜索
 */
function handleReset() {
  searchQuery.value = ''
  pagination.page = 1
  fetchUsers()
}

/**
 * 页码改变
 */
function handlePageChange(page) {
  pagination.page = page
  fetchUsers()
}

/**
 * 每页条数改变
 */
function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  fetchUsers()
}

/**
 * 新增用户
 */
function handleCreate() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

/**
 * 编辑用户
 */
function handleEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.username = row.username
  form.email = row.email
  form.password = ''
  form.firstName = row.firstName || ''
  form.lastName = row.lastName || ''
  form.phoneNumber = row.phoneNumber || ''
  form.roleNames = [...row.roles]
  dialogVisible.value = true
}

/**
 * 切换用户状态（启用/禁用）
 */
async function handleToggleStatus(row) {
  const action = row.isDeleted === 1 ? 'enable' : 'disable'
  const actionText = row.isDeleted === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}用户 "${row.username}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await axios.put(`/api/users/${row.id}/${action}`)
    ElMessage.success(`${actionText}成功`)
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${actionText}用户失败:`, error)
      ElMessage.error(error.response?.data?.message || `${actionText}用户失败`)
    }
  }
}

/**
 * 删除用户
 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await axios.delete(`/api/users/${row.id}`)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error(error.response?.data?.message || '删除用户失败')
    }
  }
}

/**
 * 提交表单
 */
async function handleSubmit() {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const data = {
      username: form.username,
      email: form.email,
      firstName: form.firstName,
      lastName: form.lastName,
      phoneNumber: form.phoneNumber,
      roleNames: form.roleNames
    }
    
    // 只有填写了密码才传递密码字段
    if (form.password) {
      data.password = form.password
    }
    
    if (isEdit.value) {
      await axios.put(`/api/users/${form.id}`, data)
      ElMessage.success('更新成功')
    } else {
      await axios.post('/api/users', data)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchUsers()
  } catch (error) {
    if (error.response) {
      console.error('提交失败:', error)
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

/**
 * 对话框关闭事件
 */
function handleDialogClose() {
  resetForm()
}

/**
 * 重置表单
 */
function resetForm() {
  form.id = null
  form.username = ''
  form.email = ''
  form.password = ''
  form.firstName = ''
  form.lastName = ''
  form.phoneNumber = ''
  form.roleNames = []
  
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

/**
 * 格式化日期
 */
function formatDate(timestamp) {
  if (!timestamp) return '-'
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 获取角色显示名称
 */
function getRoleDisplayName(roleName) {
  const roleMap = {
    'ROLE_ADMIN': '管理员',
    'ROLE_OWNER': '业主',
    'ROLE_TENANT': '租户'
  }
  return roleMap[roleName] || roleName
}

/**
 * 获取角色标签类型
 */
function getRoleTagType(roleName) {
  const typeMap = {
    'ROLE_ADMIN': 'danger',
    'ROLE_OWNER': 'warning',
    'ROLE_TENANT': 'success'
  }
  return typeMap[roleName] || 'info'
}

onMounted(() => {
  fetchUsers()
  fetchRoles()
})
</script>

<style scoped>
.users-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-tag) {
  margin: 2px;
}
</style>
