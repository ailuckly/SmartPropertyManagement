<template>
  <div class="profile-view">
    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </div>
          </template>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            v-loading="loading.profile"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" placeholder="请输入用户名" />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" type="email" />
            </el-form-item>

            <el-form-item label="名字" prop="firstName">
              <el-input v-model="profileForm.firstName" placeholder="请输入名字" />
            </el-form-item>

            <el-form-item label="姓氏" prop="lastName">
              <el-input v-model="profileForm.lastName" placeholder="请输入姓氏" />
            </el-form-item>

            <el-form-item label="电话号码" prop="phoneNumber">
              <el-input v-model="profileForm.phoneNumber" placeholder="请输入电话号码" />
            </el-form-item>

            <el-form-item label="创建时间">
              <el-text>{{ formatDate(userInfo.gmtCreate) }}</el-text>
            </el-form-item>

            <el-form-item label="用户角色">
              <el-tag
                v-for="role in userInfo.roles"
                :key="role"
                type="primary"
                style="margin-right: 8px"
              >
                {{ roleNameMap[role] || role }}
              </el-tag>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile" :loading="loading.profile">
                保存修改
              </el-button>
              <el-button @click="resetProfileForm">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 修改密码卡片 -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </div>
          </template>

          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            v-loading="loading.password"
          >
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入旧密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（至少6个字符）"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>

            <el-alert
              title="密码要求：至少6个字符，建议包含字母、数字和特殊字符"
              type="info"
              :closable="false"
              style="margin-bottom: 16px"
            />

            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="loading.password">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { User, Lock } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import api from '../api/http';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
const profileFormRef = ref(null);
const passwordFormRef = ref(null);

// 用户信息
const userInfo = reactive({
  id: null,
  username: '',
  email: '',
  firstName: '',
  lastName: '',
  phoneNumber: '',
  roles: [],
  gmtCreate: null
});

// 个人信息表单
const profileForm = reactive({
  username: '',
  email: '',
  firstName: '',
  lastName: '',
  phoneNumber: ''
});

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 加载状态
const loading = reactive({
  profile: false,
  password: false
});

// 角色名称映射
const roleNameMap = {
  'ROLE_ADMIN': '管理员',
  'ROLE_OWNER': '业主',
  'ROLE_TENANT': '租户'
};

// 个人信息验证规则
const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
};

// 密码验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在6-100个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

// 获取用户资料
const fetchProfile = async () => {
  loading.profile = true;
  try {
    const { data } = await api.get('/users/profile');
    Object.assign(userInfo, data);
    Object.assign(profileForm, {
      username: data.username,
      email: data.email,
      firstName: data.firstName || '',
      lastName: data.lastName || '',
      phoneNumber: data.phoneNumber || ''
    });
  } catch (error) {
    ElMessage.error('加载用户信息失败');
  } finally {
    loading.profile = false;
  }
};

// 更新用户资料
const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return;
  
  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    loading.profile = true;
    try {
      const { data } = await api.put('/users/profile', profileForm);
      Object.assign(userInfo, data);
      
      // 更新 auth store 中的用户信息
      await authStore.fetchCurrentUser();
      
      ElMessage.success('个人信息更新成功');
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '更新失败');
    } finally {
      loading.profile = false;
    }
  });
};

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    loading.password = true;
    try {
      await api.put('/users/change-password', {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      });
      
      ElMessage.success('密码修改成功');
      resetPasswordForm();
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '密码修改失败');
    } finally {
      loading.password = false;
    }
  });
};

// 重置个人信息表单
const resetProfileForm = () => {
  Object.assign(profileForm, {
    username: userInfo.username,
    email: userInfo.email,
    firstName: userInfo.firstName || '',
    lastName: userInfo.lastName || '',
    phoneNumber: userInfo.phoneNumber || ''
  });
  if (profileFormRef.value) {
    profileFormRef.value.clearValidate();
  }
};

// 重置密码表单
const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate();
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleString('zh-CN');
};

onMounted(() => {
  fetchProfile();
});
</script>

<style scoped>
.profile-view {
  padding: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  font-size: 16px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

@media (max-width: 1200px) {
  :deep(.el-col) {
    margin-bottom: 20px;
  }
}
</style>
