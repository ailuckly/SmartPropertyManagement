<template>
  <div class="leases-view">
    <!-- 租约列表卡片 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>租约列表</span>
          <div class="header-actions">
            <el-button type="success" :icon="Download" :loading="exporting" @click="handleExport">
              导出Excel
            </el-button>
            <el-button v-if="isOwnerOrAdmin" type="primary" @click="openCreateDialog">
              <el-icon><Plus /></el-icon>
              创建租约
            </el-button>
          </div>
        </div>
      </template>

          <!-- 筛选工具栏 -->
          <div class="filter-bar">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索租户、物业地址..." 
              clearable 
              :prefix-icon="Search"
              @input="handleSearchInput"
              style="width: 220px" 
            />
            <el-select v-model="filters.status" placeholder="按状态筛选" clearable style="width: 150px">
              <el-option label="生效中" value="ACTIVE" />
              <el-option label="已到期" value="EXPIRED" />
              <el-option label="已终止" value="TERMINATED" />
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
            :data="leases"
            v-loading="loading"
            stripe
            style="width: 100%; margin-top: 16px"
          >
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column label="物业" min-width="200">
              <template #default="{ row }">
                <div class="property-cell">
                  <div class="property-id">#{{ row.propertyId }}</div>
                  <div class="property-address">{{ row.propertyAddress }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="tenantUsername" label="租户" width="110" />
            <el-table-column label="租期" min-width="200">
              <template #default="{ row }">
                {{ formatDate(row.startDate) }} ~ {{ formatDate(row.endDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="rentAmount" label="月租(¥)" width="110" align="right" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ renderStatus(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column v-if="isOwnerOrAdmin" label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="startEdit(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button link type="danger" size="small" @click="remove(row.id)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
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

    <!-- 租约表单弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '更新租约' : '创建租约'"
      width="600px"
      @close="handleDialogClose"
    >
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

        <el-form-item label="租户ID" prop="tenantId">
          <el-input-number
            v-model="form.tenantId"
            :min="1"
            placeholder="请输入租户ID"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="选择开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="选择结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="月租(¥)" prop="rentAmount">
          <el-input-number
            v-model="form.rentAmount"
            :min="0"
            :precision="2"
            placeholder="请输入月租金额"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="生效中" value="ACTIVE" />
            <el-option label="已到期" value="EXPIRED" />
            <el-option label="已终止" value="TERMINATED" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon style="margin-top: 12px" />

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ editingId ? '保存修改' : '创建租约' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import api from '../api/http';
import { useAuthStore } from '../stores/auth';
import { Search, RefreshLeft, Edit, Delete, Plus, Download } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { exportLeases } from '@/utils/download';

/**
 * Lease management screen. Owners/admins can create or edit leases, while tenants only get the table view.
 */
const authStore = useAuthStore();
const isOwnerOrAdmin = authStore.hasAnyRole(['ROLE_OWNER', 'ROLE_ADMIN']);

const formRef = ref(null);
const leases = ref([]);
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
  tenantId: null,
  startDate: '',
  endDate: '',
  rentAmount: null,
  status: 'ACTIVE'
});

const rules = {
  propertyId: [{ required: true, message: '请输入物业ID', trigger: 'blur' }],
  tenantId: [{ required: true, message: '请输入租户ID', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  rentAmount: [{ required: true, message: '请输入月租金额', trigger: 'blur' }]
};

const editingId = ref(null);
const error = ref('');
const dialogVisible = ref(false); // 控制弹窗显示

/**
 * Loads leases visible to the current user and updates pagination metadata.
 */
const fetchLeases = async () => {
  loading.value = true;
  try {
    const { data } = await api.get('/leases', {
      params: {
        page: pagination.page - 1,
        size: pagination.size,
        status: filters.status || undefined,
        propertyId: filters.propertyId || undefined,
        keyword: searchKeyword.value || undefined
      }
    });
    leases.value = data.content;
    pagination.total = data.totalElements || 0;
  } catch (err) {
    ElMessage.error('加载租约列表失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 打开创建租约弹窗
 */
const openCreateDialog = () => {
  resetForm();
  dialogVisible.value = true;
};

/**
 * 关闭弹窗并重置表单
 */
const handleDialogClose = () => {
  resetForm();
};

const resetForm = () => {
  editingId.value = null;
  if (formRef.value) {
    formRef.value.resetFields();
  }
  Object.assign(form, {
    propertyId: null,
    tenantId: null,
    startDate: '',
    endDate: '',
    rentAmount: null,
    status: 'ACTIVE'
  });
  error.value = '';
};

/**
 * Handles create/update operations depending on whether we are currently editing a row.
 */
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    error.value = '';
    
    try {
      if (editingId.value) {
        await api.put(`/leases/${editingId.value}`, form);
        ElMessage.success('租约更新成功');
      } else {
        await api.post('/leases', form);
        ElMessage.success('租约创建成功');
      }
      dialogVisible.value = false;
      resetForm();
      fetchLeases();
    } catch (err) {
      error.value = err.response?.data?.message ?? '保存失败，请确认输入信息';
    } finally {
      submitting.value = false;
    }
  });
};

/**
 * Switches the form into edit mode using the values from the selected table row.
 */
const startEdit = (item) => {
  editingId.value = item.id;
  Object.assign(form, {
    propertyId: item.propertyId,
    tenantId: item.tenantId,
    startDate: item.startDate,
    endDate: item.endDate,
    rentAmount: item.rentAmount,
    status: item.status
  });
  dialogVisible.value = true;
};

/**
 * Deletes a lease after confirming with the user (owners/admins only).
 */
const remove = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该租约？此操作不可撤销。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await api.delete(`/leases/${id}`);
    ElMessage.success('删除成功');
    fetchLeases();
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handlePageChange = (page) => {
  pagination.page = page;
  fetchLeases();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.page = 1;
  fetchLeases();
};

const formatDate = (value) => {
  if (!value) return '-';
  return value;
};

const renderStatus = (status) => {
  switch (status) {
    case 'ACTIVE':
      return '生效中';
    case 'EXPIRED':
      return '已到期';
    case 'TERMINATED':
      return '已终止';
    default:
      return status;
  }
};

const getStatusType = (status) => {
  switch (status) {
    case 'ACTIVE':
      return 'success';
    case 'EXPIRED':
      return 'info';
    case 'TERMINATED':
      return 'danger';
    default:
      return 'info';
  }
};

const applyFilters = () => {
  pagination.page = 1;
  fetchLeases();
};

const clearFilters = () => {
  filters.status = '';
  filters.propertyId = null;
  searchKeyword.value = '';
  pagination.page = 1;
  fetchLeases();
};

// 搜索防抖
let searchTimeout = null;
const handleSearchInput = () => {
  if (searchTimeout) clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    pagination.page = 1;
    fetchLeases();
  }, 500);
};

/**
 * 导出租约列表为 Excel
 */
const handleExport = async () => {
  exporting.value = true;
  try {
    await exportLeases();
    ElMessage.success('导出成功！');
  } catch (err) {
    ElMessage.error('导出失败，请稍后重试');
    console.error('导出错误:', err);
  } finally {
    exporting.value = false;
  }
};

onMounted(() => {
  fetchLeases();
});
</script>

<style scoped>
.leases-view {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 8px;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
