<template>
  <div class="payments-view">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>租金支付记录</span>
          <div class="header-actions">
            <el-input-number
              v-model="filters.leaseId"
              :min="1"
              placeholder="输入租约ID"
              style="width: 150px"
            />
            <el-button type="primary" :icon="Search" @click="fetchPayments">
              查询
            </el-button>
          </div>
        </div>
      </template>

      <!-- 登记表单（仅业主和管理员可见） -->
      <el-form
        v-if="isOwnerOrAdmin"
        ref="formRef"
        :model="form"
        :rules="rules"
        inline
        size="default"
        class="payment-form"
        @submit.prevent="handleSubmit"
      >
        <el-divider content-position="left">
          <el-icon><Money /></el-icon>
          登记租金支付
        </el-divider>

        <el-form-item label="租约ID" prop="leaseId">
          <el-input-number v-model="form.leaseId" :min="1" placeholder="请输入租约ID" style="width: 150px" />
        </el-form-item>

        <el-form-item label="金额(¥)" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" placeholder="请输入金额" style="width: 150px" />
        </el-form-item>

        <el-form-item label="支付日期" prop="paymentDate">
          <el-date-picker
            v-model="form.paymentDate"
            type="date"
            placeholder="选择支付日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 180px"
          />
        </el-form-item>

        <el-form-item label="支付方式" prop="paymentMethod">
          <el-input v-model="form.paymentMethod" placeholder="如：银行转账" clearable style="width: 180px" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="submitting">
            登记
          </el-button>
        </el-form-item>

        <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon style="margin-top: 12px; width: 100%" />
      </el-form>

      <!-- 表格 -->
      <div style="margin-top: 20px">
        <el-table
          :data="payments"
          v-loading="loading"
          stripe
          style="width: 100%"
          empty-text="暂无数据，请先输入租约ID查询"
        >
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="leaseId" label="租约ID" width="100" />
          <el-table-column prop="amount" label="金额(¥)" width="120" align="right">
            <template #default="{ row }">
              <span style="color: #67C23A; font-weight: 600">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="paymentDate" label="支付日期" width="130" />
          <el-table-column prop="paymentMethod" label="支付方式" width="150">
            <template #default="{ row }">
              <el-tag v-if="row.paymentMethod" size="small" type="info">
                {{ row.paymentMethod }}
              </el-tag>
              <span v-else style="color: #909399">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="登记时间" min-width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import api from '../api/http';
import { useAuthStore } from '../stores/auth';
import { Search, Money } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

/**
 * Payment centre. Owners/admins can record new payments, whereas authorised viewers can filter by lease.
 */
const authStore = useAuthStore();
const isOwnerOrAdmin = authStore.hasAnyRole(['ROLE_OWNER', 'ROLE_ADMIN']);

const formRef = ref(null);
const loading = ref(false);
const submitting = ref(false);

const filters = reactive({
  leaseId: null
});

const form = reactive({
  leaseId: null,
  amount: null,
  paymentDate: '',
  paymentMethod: ''
});

const rules = {
  leaseId: [{ required: true, message: '请输入租约ID', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  paymentDate: [{ required: true, message: '请选择支付日期', trigger: 'change' }]
};

const payments = ref([]); // 支付记录表格数据
const error = ref('');

/**
 * Loads the payments for the specified lease. The lease id is required for the backend API call.
 */
const fetchPayments = async () => {
  if (!filters.leaseId) {
    ElMessage.warning('请先输入租约ID');
    return;
  }
  
  loading.value = true;
  error.value = '';
  
  try {
    const { data } = await api.get('/payments', {
      params: { leaseId: filters.leaseId, size: 50 }
    });
    payments.value = data.content;
    ElMessage.success(`已查询到 ${data.content.length} 条记录`);
  } catch (err) {
    error.value = err.response?.data?.message ?? '查询失败';
    ElMessage.error(error.value);
  } finally {
    loading.value = false;
  }
};

/**
 * Persists a payment entry then refreshes the table so the new row is visible immediately.
 */
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    error.value = '';
    
    try {
      await api.post('/payments', form);
      ElMessage.success('登记成功');
      
      if (!filters.leaseId) {
        filters.leaseId = form.leaseId;
      }
      
      fetchPayments();
      
      // 重置表单
      form.leaseId = null;
      form.amount = null;
      form.paymentDate = '';
      form.paymentMethod = '';
      
      if (formRef.value) {
        formRef.value.resetFields();
      }
    } catch (err) {
      error.value = err.response?.data?.message ?? '登记失败，请检查输入';
    } finally {
      submitting.value = false;
    }
  });
};

const formatDateTime = (value) => {
  if (!value) return '-';
  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value));
};
</script>

<style scoped>
.payments-view {
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
  gap: 12px;
  align-items: center;
}

.payment-form {
  background-color: #f9fafb;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

:deep(.el-divider__text) {
  background-color: #f9fafb;
}

:deep(.payment-form .el-form-item) {
  margin-right: 20px;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .payment-form {
    padding: 15px;
  }
  
  :deep(.payment-form .el-form-item) {
    display: block;
    margin-right: 0;
  }
  
  :deep(.payment-form .el-form-item__content) {
    margin-left: 0 !important;
  }
}
</style>
