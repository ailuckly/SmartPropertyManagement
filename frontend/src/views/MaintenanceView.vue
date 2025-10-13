<template>
  <div class="maintenance-layout">
    <section v-if="isTenant" class="card form-card">
      <h2>提交维修申请</h2>
      <form @submit.prevent="handleSubmit">
        <div class="form-field">
          <label for="propertyId">物业ID</label>
          <input id="propertyId" v-model.number="form.propertyId" type="number" min="1" required />
        </div>
        <div class="form-field">
          <label for="description">问题描述</label>
          <textarea
            id="description"
            v-model="form.description"
            required
            rows="4"
            placeholder="请尽量描述清楚问题、影响范围与紧急程度"
          ></textarea>
        </div>
        <button class="btn-primary" type="submit">提交申请</button>
        <p v-if="error" class="error-msg">{{ error }}</p>
      </form>
    </section>

    <section class="card">
      <header class="table-header">
        <h2>维修请求列表</h2>
        <div class="pagination">
          <button :disabled="pagination.page === 0" @click="changePage(pagination.page - 1)">
            上一页
          </button>
          <span>{{ pagination.page + 1 }} / {{ pagination.totalPages }}</span>
          <button
            :disabled="pagination.page + 1 >= pagination.totalPages"
            @click="changePage(pagination.page + 1)"
          >
            下一页
          </button>
        </div>
      </header>
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>物业</th>
              <th>租客</th>
              <th>描述</th>
              <th>状态</th>
              <th>上报时间</th>
              <th v-if="isOwnerOrAdmin">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in requests" :key="item.id">
              <td>{{ item.id }}</td>
              <td>
                <div class="address-cell">
                  <strong>#{{ item.propertyId }}</strong>
                  <small>{{ item.propertyAddress }}</small>
                </div>
              </td>
              <td>{{ item.tenantUsername }}</td>
              <td>{{ item.description }}</td>
              <td>
                <span :class="['status-pill', item.status.toLowerCase()]">{{ renderStatus(item.status) }}</span>
              </td>
              <td>{{ formatDate(item.reportedAt) }}</td>
              <td v-if="isOwnerOrAdmin" class="actions">
                <select v-model="item.status" @change="updateStatus(item)">
                  <option value="PENDING">待处理</option>
                  <option value="IN_PROGRESS">处理中</option>
                  <option value="COMPLETED">已完成</option>
                  <option value="CANCELLED">已取消</option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import api from '../api/http';
import { useAuthStore } from '../stores/auth';

/**
 * Maintenance centre: tenants can submit requests while owners/admins can track and update ticket status.
 */
const authStore = useAuthStore();
const isTenant = authStore.hasAnyRole(['ROLE_TENANT']);
const isOwnerOrAdmin = authStore.hasAnyRole(['ROLE_OWNER', 'ROLE_ADMIN']);

const form = reactive({
  propertyId: '',
  description: ''
});

const requests = ref([]);
const pagination = reactive({
  page: 0,
  size: 10,
  totalPages: 1
});

const error = ref('');

/**
 * Retrieves maintenance requests appropriate for the logged-in user.
 */
const fetchRequests = async () => {
  const { data } = await api.get('/maintenance-requests', {
    params: { page: pagination.page, size: pagination.size }
  });
  requests.value = data.content;
  pagination.totalPages = Math.max(data.totalPages, 1);
};

/**
 * Tenant submission handler. Returns the user to a clean form on success.
 */
const handleSubmit = async () => {
  error.value = '';
  try {
    await api.post('/maintenance-requests', form);
    form.propertyId = '';
    form.description = '';
    fetchRequests();
  } catch (err) {
    error.value = err.response?.data?.message ?? '提交失败，请稍后再试';
  }
};

/**
 * Persists status changes for owners/admins and reverts to the latest data if the call fails.
 */
const updateStatus = async (item) => {
  try {
    await api.patch(`/maintenance-requests/${item.id}/status`, { status: item.status });
  } catch (err) {
    alert(err.response?.data?.message ?? '更新失败');
    fetchRequests();
  }
};

const changePage = (page) => {
  pagination.page = page;
  fetchRequests();
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

const formatDate = (value) => {
  if (!value) return '-';
  return new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value));
};

onMounted(fetchRequests);
</script>

<style scoped>
.maintenance-layout {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 24px;
}

@media (max-width: 1024px) {
  .maintenance-layout {
    grid-template-columns: 1fr;
  }
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

.status-pill {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 12px;
  color: #fff;
}

.status-pill.pending {
  background: #f97316;
}

.status-pill.in_progress {
  background: #2563eb;
}

.status-pill.completed {
  background: #22c55e;
}

.status-pill.cancelled {
  background: #94a3b8;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 12px;
}

.actions select {
  padding: 4px 6px;
}

.error-msg {
  color: #ef4444;
  margin-top: 12px;
}
</style>
