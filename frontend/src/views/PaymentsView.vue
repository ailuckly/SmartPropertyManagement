<template>
  <div class="payments-layout card">
    <header class="header">
      <h2>租金支付记录</h2>
      <div class="filter-group">
        <input
          v-model.number="filters.leaseId"
          type="number"
          min="1"
          placeholder="输入租约ID"
        />
        <button class="btn-primary" @click="fetchPayments">查询</button>
      </div>
    </header>

    <section v-if="isOwnerOrAdmin" class="form-section">
      <h3>登记租金支付</h3>
      <form @submit.prevent="handleSubmit" class="form-row">
        <div class="form-field">
          <label for="leaseId">租约ID</label>
          <input id="leaseId" v-model.number="form.leaseId" type="number" min="1" required />
        </div>
        <div class="form-field">
          <label for="amount">金额(¥)</label>
          <input id="amount" v-model.number="form.amount" type="number" min="0" step="0.01" required />
        </div>
        <div class="form-field">
          <label for="paymentDate">支付日期</label>
          <input id="paymentDate" v-model="form.paymentDate" type="date" required />
        </div>
        <div class="form-field">
          <label for="paymentMethod">支付方式</label>
          <input id="paymentMethod" v-model="form.paymentMethod" placeholder="如：银行转账" />
        </div>
        <div class="form-actions">
          <button class="btn-primary" type="submit">登记</button>
        </div>
      </form>
      <p v-if="error" class="error-msg">{{ error }}</p>
    </section>

    <div class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>租约</th>
            <th>金额</th>
            <th>支付日期</th>
            <th>方式</th>
            <th>登记时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in payments" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.leaseId }}</td>
            <td>{{ item.amount }}</td>
            <td>{{ item.paymentDate }}</td>
            <td>{{ item.paymentMethod ?? '-' }}</td>
            <td>{{ formatDateTime(item.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import api from '../api/http';
import { useAuthStore } from '../stores/auth';

/**
 * Payment centre. Owners/admins can record new payments, whereas authorised viewers can filter by lease.
 */
const authStore = useAuthStore();
const isOwnerOrAdmin = authStore.hasAnyRole(['ROLE_OWNER', 'ROLE_ADMIN']);

const filters = reactive({
  leaseId: ''
});

const form = reactive({
  leaseId: '',
  amount: '',
  paymentDate: '',
  paymentMethod: ''
});

const payments = ref([]);
const error = ref('');

/**
 * Loads the payments for the specified lease. The lease id is required for the backend API call.
 */
const fetchPayments = async () => {
  if (!filters.leaseId) {
    error.value = '请先输入租约ID';
    return;
  }
  error.value = '';
  try {
    const { data } = await api.get('/payments', {
      params: { leaseId: filters.leaseId, size: 50 }
    });
    payments.value = data.content;
  } catch (err) {
    error.value = err.response?.data?.message ?? '查询失败';
  }
};

/**
 * Persists a payment entry then refreshes the table so the new row is visible immediately.
 */
const handleSubmit = async () => {
  error.value = '';
  try {
    await api.post('/payments', form);
    if (!filters.leaseId) {
      filters.leaseId = form.leaseId;
    }
    fetchPayments();
    Object.assign(form, {
      leaseId: '',
      amount: '',
      paymentDate: '',
      paymentMethod: ''
    });
  } catch (err) {
    error.value = err.response?.data?.message ?? '登记失败，请检查输入';
  }
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
.payments-layout {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-group {
  display: flex;
  gap: 12px;
}

.form-section {
  border-top: 1px solid #e2e8f0;
  padding-top: 24px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 12px;
}

.form-actions {
  grid-column: span 4;
  display: flex;
  justify-content: flex-end;
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 12px 8px;
  border-bottom: 1px solid #e2e8f0;
}

.error-msg {
  color: #ef4444;
}
</style>
