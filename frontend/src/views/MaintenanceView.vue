<template>
  <div class="maintenance-view">
    <el-row :gutter="20">
      <!-- 表单卡片（仅租户可见） -->
      <el-col v-if="isTenant" :xs="24" :lg="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Tools /></el-icon>
              <span>提交维修申请</span>
            </div>
          </template>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="90px"
            size="default"
            @submit.prevent="handleSubmit"
          >
            <el-form-item label="物业ID" prop="propertyId">
              <el-input-number
                v-model="form.propertyId"
                :min="1"
                placeholder="请输入物业ID"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="问题描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="6"
                placeholder="请尽量描述清楚问题、影响范围与紧急程度"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" native-type="submit" :loading="submitting" style="width: 100%">
                提交申请
              </el-button>
            </el-form-item>
          </el-form>

          <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon style="margin-top: 12px" />
        </el-card>
      </el-col>

      <!-- 列表卡片 -->
      <el-col :xs="24" :lg="isTenant ? 16 : 24">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>维修请求列表</span>
              <el-button type="success" :icon="Download" :loading="exporting" @click="handleExport">
                导出Excel
              </el-button>
            </div>
          </template>

          <!-- 筛选工具栏 -->
          <div class="filter-bar">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索问题描述..." 
              clearable 
              :prefix-icon="Search"
              @input="handleSearchInput"
              style="width: 200px" 
            />
            <el-select v-model="filters.status" placeholder="按状态筛选" clearable style="width: 150px">
              <el-option label="待处理" value="PENDING" />
              <el-option label="处理中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
            <el-input-number
              v-model="filters.propertyId"
              :min="1"
              placeholder="按物业ID筛选"
              clearable
              style="width: 150px"
            />
            <el-button type="primary" :icon="Search" @click="applyFilters">筛选</el-button>
            <el-button :icon="RefreshLeft" @click="clearFilters">清空</el-button>
          </div>

          <!-- 表格 -->
          <el-table
            :data="requests"
            v-loading="loading"
            stripe
            style="width: 100%; margin-top: 16px"
          >
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column label="物业" min-width="180">
              <template #default="{ row }">
                <div class="property-cell">
                  <div class="property-id">#{{ row.propertyId }}</div>
                  <div class="property-address">{{ row.propertyAddress }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="tenantUsername" label="租客" width="100" />
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ renderStatus(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reportedAt" label="上报时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.reportedAt) }}
              </template>
            </el-table-column>
            <el-table-column v-if="isOwnerOrAdmin" label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-select
                  v-model="row.status"
                  size="small"
                  @change="updateStatus(row)"
                  style="width: 120px"
                >
                  <el-option label="待处理" value="PENDING" />
                  <el-option label="处理中" value="IN_PROGRESS" />
                  <el-option label="已完成" value="COMPLETED" />
                  <el-option label="已取消" value="CANCELLED" />
                </el-select>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
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
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import api from '../api/http';
import { useAuthStore } from '../stores/auth';
import { Search, RefreshLeft, Tools, Download } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { exportMaintenanceRequests } from '@/utils/download';

/**
 * Maintenance centre: tenants can submit requests while owners/admins can track and update ticket status.
 */
const authStore = useAuthStore();
const isTenant = authStore.hasAnyRole(['ROLE_TENANT']);
const isOwnerOrAdmin = authStore.hasAnyRole(['ROLE_OWNER', 'ROLE_ADMIN']);

const formRef = ref(null);
const requests = ref([]);
const loading = ref(false);
const submitting = ref(false);
const exporting = ref(false);
const searchKeyword = ref('');
const filters = reactive({ status: '', propertyId: null });
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

const form = reactive({
  propertyId: null,
  description: ''
});

const rules = {
  propertyId: [{ required: true, message: '请输入物业ID', trigger: 'blur' }],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 10, message: '问题描述至少需要10个字符', trigger: 'blur' }
  ]
};

const error = ref('');

/**
 * Retrieves maintenance requests appropriate for the logged-in user.
 */
const fetchRequests = async () => {
  loading.value = true;
  try {
    const { data } = await api.get('/maintenance-requests', {
      params: {
        page: pagination.page - 1,
        size: pagination.size,
        status: filters.status || undefined,
        propertyId: filters.propertyId || undefined,
        keyword: searchKeyword.value || undefined
      }
    });
    requests.value = data.content;
    pagination.total = data.totalElements || 0;
  } catch (err) {
    ElMessage.error('加载维修请求列表失败');
  } finally {
    loading.value = false;
  }
};

/**
 * Tenant submission handler. Returns the user to a clean form on success.
 */
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    error.value = '';
    
    try {
      await api.post('/maintenance-requests', form);
      ElMessage.success('维修申请提交成功');
      form.propertyId = null;
      form.description = '';
      if (formRef.value) {
        formRef.value.resetFields();
      }
      fetchRequests();
    } catch (err) {
      error.value = err.response?.data?.message ?? '提交失败，请稍后再试';
    } finally {
      submitting.value = false;
    }
  });
};

/**
 * Persists status changes for owners/admins and reverts to the latest data if the call fails.
 */
const updateStatus = async (item) => {
  try {
    await api.patch(`/maintenance-requests/${item.id}/status`, { status: item.status });
    ElMessage.success('状态更新成功');
  } catch (err) {
    ElMessage.error(err.response?.data?.message ?? '更新失败');
    fetchRequests();
  }
};

const handlePageChange = (page) => {
  pagination.page = page;
  fetchRequests();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.page = 1;
  fetchRequests();
};

const applyFilters = () => {
  pagination.page = 1;
  fetchRequests();
};

const clearFilters = () => {
  filters.status = '';
  filters.propertyId = null;
  searchKeyword.value = '';
  pagination.page = 1;
  fetchRequests();
};

// 搜索防抖
let searchTimeout = null;
const handleSearchInput = () => {
  if (searchTimeout) clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    pagination.page = 1;
    fetchRequests();
  }, 500);
};

const renderStatus = (status) => {
  switch (status) {
    case 'PENDING':
      return '待处理';
    case 'IN_PROGRESS':
      return '处理中';
    case 'COMPLETED':
      return '已完成';
    case 'CANCELLED':
      return '已取消';
    default:
      return status;
  }
};

const getStatusType = (status) => {
  switch (status) {
    case 'PENDING':
      return 'warning';
    case 'IN_PROGRESS':
      return 'primary';
    case 'COMPLETED':
      return 'success';
    case 'CANCELLED':
      return 'info';
    default:
      return 'info';
  }
};

const formatDate = (value) => {
  if (!value) return '-';
  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value));
};

/**
 * 导出维修记录为 Excel
 */
const handleExport = async () => {
  exporting.value = true;
  try {
    await exportMaintenanceRequests();
    ElMessage.success('导出成功！');
  } catch (err) {
    ElMessage.error('导出失败，请稍后重试');
    console.error('导出错误:', err);
  } finally {
    exporting.value = false;
  }
};

onMounted(() => {
  fetchRequests();
});
</script>

<style scoped>
.maintenance-view {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.property-cell {
  line-height: 1.5;
}

.property-id {
  font-weight: 500;
  color: #303133;
}

.property-address {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 1200px) {
  :deep(.el-col) {
    margin-bottom: 20px;
  }
}
</style>
