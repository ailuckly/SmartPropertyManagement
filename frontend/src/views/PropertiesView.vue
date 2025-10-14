<template>
  <div class="properties-view">
    <!-- 物业列表卡片 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>物业列表</span>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>
            新建物业
          </el-button>
        </div>
      </template>

          <!-- 筛选工具栏 -->
          <div class="filter-bar">
            <el-input v-model="filters.city" placeholder="按城市筛选" clearable style="width: 150px" />
            <el-select v-model="filters.status" placeholder="按状态筛选" clearable style="width: 150px">
              <el-option label="可出租" value="AVAILABLE" />
              <el-option label="已出租" value="LEASED" />
              <el-option label="维护中" value="UNDER_MAINTENANCE" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="applyFilters">筛选</el-button>
            <el-button :icon="RefreshLeft" @click="clearFilters">清空</el-button>
          </div>

          <!-- 表格 -->
          <el-table
            :data="properties"
            v-loading="loading"
            stripe
            style="width: 100%; margin-top: 16px"
          >
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column label="地址" min-width="200">
              <template #default="{ row }">
                <div class="address-cell">
                  <div class="address-main">{{ row.address }}</div>
                  <div class="address-sub">{{ row.city }} {{ row.state }} {{ row.zipCode }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="propertyType" label="类型" width="100">
              <template #default="{ row }">
                {{ renderType(row.propertyType) }}
              </template>
            </el-table-column>
            <el-table-column prop="rentAmount" label="租金(¥)" width="110" align="right">
              <template #default="{ row }">
                {{ row.rentAmount ?? '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ renderStatus(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ownerUsername" label="业主" width="110">
              <template #default="{ row }">
                {{ row.ownerUsername ?? '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
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

    <!-- 物业表单弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '更新物业' : '新建物业'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="default"
        @submit.prevent="handleSubmit"
      >
        <el-form-item v-if="isAdmin" label="业主用户ID" prop="ownerId">
          <el-input-number
            v-model="form.ownerId"
            :min="1"
            placeholder="请输入业主用户ID"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" clearable />
        </el-form-item>

        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="请输入城市" clearable />
        </el-form-item>

        <el-form-item label="省/州" prop="state">
          <el-input v-model="form.state" placeholder="请输入省/州" clearable />
        </el-form-item>

        <el-form-item label="邮编" prop="zipCode">
          <el-input v-model="form.zipCode" placeholder="请输入邮编" clearable />
        </el-form-item>

        <el-form-item label="物业类型" prop="propertyType">
          <el-select v-model="form.propertyType" placeholder="请选择物业类型" style="width: 100%">
            <el-option label="公寓" value="APARTMENT" />
            <el-option label="独栋" value="HOUSE" />
            <el-option label="商用" value="COMMERCIAL" />
          </el-select>
        </el-form-item>

        <el-form-item label="卧室数量" prop="bedrooms">
          <el-input-number v-model="form.bedrooms" :min="0" style="width: 100%" />
        </el-form-item>

        <el-form-item label="卫生间数量" prop="bathrooms">
          <el-input-number v-model="form.bathrooms" :min="0" :step="0.5" style="width: 100%" />
        </el-form-item>

        <el-form-item label="面积(㎡)" prop="squareFootage">
          <el-input-number v-model="form.squareFootage" :min="0" style="width: 100%" />
        </el-form-item>

        <el-form-item label="月租(¥)" prop="rentAmount">
          <el-input-number v-model="form.rentAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="可出租" value="AVAILABLE" />
            <el-option label="已出租" value="LEASED" />
            <el-option label="维护中" value="UNDER_MAINTENANCE" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon style="margin-top: 12px" />

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ editingId ? '保存修改' : '创建物业' }}
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
import { Search, RefreshLeft, Edit, Delete, Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

/**
 * This view combines a property editor (admins/owners) and a paginated table shared by all authenticated roles.
 * Form state lives in reactive objects to simplify reset logic after submission or cancellation.
 */
const authStore = useAuthStore();
const formRef = ref(null);
const properties = ref([]); // 表格数据源
const loading = ref(false);
const submitting = ref(false);
const filters = reactive({ city: '', status: '' });
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

const form = reactive({
  ownerId: '',
  address: '',
  city: '',
  state: '',
  zipCode: '',
  propertyType: '',
  bedrooms: null,
  bathrooms: null,
  squareFootage: null,
  rentAmount: null,
  status: 'AVAILABLE'
});

const rules = {
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  propertyType: [{ required: true, message: '请选择物业类型', trigger: 'change' }]
};

const editingId = ref(null); // null 表示当前为"新增"模式
const error = ref('');
const dialogVisible = ref(false); // 控制弹窗显示

const isAdmin = authStore.hasAnyRole(['ROLE_ADMIN']);

/**
 * Loads a page of properties from the backend. Pagination metadata is reused for the navigation controls.
 */
const fetchProperties = async () => {
  loading.value = true;
  try {
    const { data } = await api.get('/properties', {
      params: { 
        page: pagination.page - 1, // Element UI 分页从 1 开始，后端从 0 开始
        size: pagination.size, 
        city: filters.city || undefined, 
        status: filters.status || undefined 
      }
    });
    properties.value = data.content;
    pagination.total = data.totalElements || 0;
  } catch (err) {
    ElMessage.error('加载物业列表失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 打开新建物业弹窗
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

/**
 * Restores the form to its initial state, keeping ownerId populated for owners to streamline data entry.
 */
const resetForm = () => {
  editingId.value = null;
  if (formRef.value) {
    formRef.value.resetFields();
  }
  Object.assign(form, {
    ownerId: isAdmin ? '' : authStore.user?.id ?? '',
    address: '',
    city: '',
    state: '',
    zipCode: '',
    propertyType: '',
    bedrooms: null,
    bathrooms: null,
    squareFootage: null,
    rentAmount: null,
    status: 'AVAILABLE'
  });
  error.value = '';
};

/**
 * Sends either a create or update request depending on the presence of {@code editingId}.
 */
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    error.value = '';
    
    try {
      const payload = { ...form };
      if (!isAdmin) {
        payload.ownerId = null;
      }
      if (!payload.squareFootage) {
        payload.squareFootage = null;
      }
      if (!payload.bedrooms) {
        payload.bedrooms = null;
      }
      if (!payload.bathrooms) {
        payload.bathrooms = null;
      }

      if (editingId.value) {
        await api.put(`/properties/${editingId.value}`, payload);
        ElMessage.success('物业更新成功');
      } else {
        await api.post('/properties', payload);
        ElMessage.success('物业创建成功');
      }
      dialogVisible.value = false;
      resetForm();
      fetchProperties();
    } catch (err) {
      error.value = err.response?.data?.message ?? '保存失败，请检查输入信息';
    } finally {
      submitting.value = false;
    }
  });
};

/**
 * Populates the form with the selected row, switching the component into "edit" mode.
 */
const startEdit = (item) => {
  editingId.value = item.id;
  Object.assign(form, {
    ownerId: item.ownerId,
    address: item.address,
    city: item.city,
    state: item.state,
    zipCode: item.zipCode,
    propertyType: item.propertyType,
    bedrooms: item.bedrooms,
    bathrooms: item.bathrooms,
    squareFootage: item.squareFootage,
    rentAmount: item.rentAmount,
    status: item.status
  });
  dialogVisible.value = true;
};

/**
 * Removes a property after explicit confirmation from the user.
 */
const remove = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该物业？此操作不可撤销。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await api.delete(`/properties/${id}`);
    ElMessage.success('删除成功');
    fetchProperties();
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handlePageChange = (page) => {
  pagination.page = page;
  fetchProperties();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.page = 1;
  fetchProperties();
};

const renderType = (type) => {
  switch (type) {
    case 'APARTMENT':
      return '公寓';
    case 'HOUSE':
      return '独栋';
    case 'COMMERCIAL':
      return '商用';
    default:
      return type;
  }
};

const renderStatus = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return '可出租';
    case 'LEASED':
      return '已出租';
    case 'UNDER_MAINTENANCE':
      return '维护中';
    default:
      return status;
  }
};

const getStatusType = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return 'success';
    case 'LEASED':
      return 'primary';
    case 'UNDER_MAINTENANCE':
      return 'warning';
    default:
      return 'info';
  }
};

const applyFilters = () => { 
  pagination.page = 1; 
  fetchProperties(); 
};

const clearFilters = () => { 
  filters.city = ''; 
  filters.status = ''; 
  pagination.page = 1; 
  fetchProperties(); 
};

onMounted(() => {
  if (!isAdmin && authStore.user) {
    form.ownerId = authStore.user.id;
  }
  fetchProperties();
});
</script>

<style scoped>
.properties-view {
  padding: 0;
}

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

.address-cell {
  line-height: 1.5;
}

.address-main {
  font-weight: 500;
  color: #303133;
}

.address-sub {
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
