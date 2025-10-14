<template>
  <div class="leases-layout">
    <section v-if="isOwnerOrAdmin" class="card form-card">
      <header>
        <h2>{{ editingId ? '更新租约' : '创建租约' }}</h2>
        <button v-if="editingId" class="link-btn" @click="resetForm">取消编辑</button>
      </header>
      <form @submit.prevent="handleSubmit" class="grid-form">
        <div class="form-field">
          <label for="propertyId">物业ID</label>
          <input id="propertyId" v-model.number="form.propertyId" type="number" min="1" required />
        </div>
        <div class="form-field">
          <label for="tenantId">租户ID</label>
          <input id="tenantId" v-model.number="form.tenantId" type="number" min="1" required />
        </div>
        <div class="form-field">
          <label for="startDate">开始日期</label>
          <input id="startDate" v-model="form.startDate" type="date" required />
        </div>
        <div class="form-field">
          <label for="endDate">结束日期</label>
          <input id="endDate" v-model="form.endDate" type="date" required />
        </div>
        <div class="form-field">
          <label for="rentAmount">月租(¥)</label>
          <input id="rentAmount" v-model.number="form.rentAmount" type="number" min="0" step="0.01" required />
        </div>
        <div class="form-field">
          <label for="status">状态</label>
          <select id="status" v-model="form.status">
            <option value="ACTIVE">生效中</option>
            <option value="EXPIRED">已到期</option>
            <option value="TERMINATED">已终止</option>
          </select>
        </div>
        <div class="form-actions">
          <button class="btn-primary" type="submit">{{ editingId ? '保存修改' : '创建租约' }}</button>
        </div>
      </form>
      <p v-if="error" class="error-msg">{{ error }}</p>
    </section>

    <section class="card">
      <header class="table-header">
        <h2>租约列表</h2>
        <div class="filters">
<select v-model="filters.status" class="input">
            <option value="">所有状态</option>
            <option value="ACTIVE">生效中</option>
            <option value="EXPIRED">已到期</option>
            <option value="TERMINATED">已终止</option>
          </select>
<input v-model.number="filters.propertyId" type="number" min="1" placeholder="物业ID" class="input" />
          <button class="btn-primary" @click="applyFilters">筛选</button>
<button class="btn-link" @click="clearFilters">清空</button>
          <div class="pagination">
<button class="btn" :disabled="pagination.page === 0" @click="changePage(pagination.page - 1)">上一页</button>
            <span>{{ pagination.page + 1 }} / {{ pagination.totalPages }}</span>
<button class="btn" :disabled="pagination.page + 1 >= pagination.totalPages" @click="changePage(pagination.page + 1)">下一页</button>
          </div>
        </div>
      </header>
      <div class="table-wrapper" v-if="!loading">
        <table class="table">
          <thead>
            <tr>
              <th class="id-col">ID</th>
              <th>物业</th>
              <th>租户</th>
              <th>租期</th>
              <th class="num">月租</th>
              <th>状态</th>
              <th v-if="isOwnerOrAdmin">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in leases" :key="item.id">
              <td class="id-col">{{ item.id }}</td>
              <td>
                <div class="address-cell">
                  <strong>#{{ item.propertyId }}</strong>
                  <small>{{ item.propertyAddress }}</small>
                </div>
              </td>
              <td>{{ item.tenantUsername }}</td>
              <td>{{ formatDate(item.startDate) }} ~ {{ formatDate(item.endDate) }}</td>
              <td class="num">{{ item.rentAmount }}</td>
              <td><span :class="['status-pill', item.status.toLowerCase()]">{{ renderStatus(item.status) }}</span></td>
              <td v-if="isOwnerOrAdmin" class="actions">
                <button @click="startEdit(item)">编辑</button>
                <button class="danger" @click="remove(item.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="card empty-card">
        <div class="skeleton" style="height:16px; margin-bottom:12px;"></div>
        <div class="skeleton" style="height:16px; margin-bottom:12px;"></div>
        <div class="skeleton" style="height:16px; margin-bottom:12px;"></div>
        <div class="skeleton" style="height:16px;"></div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import api from '../api/http';
import { useAuthStore } from '../stores/auth';

/**
 * Lease management screen. Owners/admins can create or edit leases, while tenants only get the table view.
 */
const authStore = useAuthStore();
const isOwnerOrAdmin = authStore.hasAnyRole(['ROLE_OWNER', 'ROLE_ADMIN']);

const form = reactive({
  propertyId: '',
  tenantId: '',
  startDate: '',
  endDate: '',
  rentAmount: '',
  status: 'ACTIVE'
});

const leases = ref([]); // 租约表格
const loading = ref(false);
const filters = reactive({ status: '', propertyId: '' });
const pagination = reactive({
  page: 0,
  size: 10,
  totalPages: 1
});

const editingId = ref(null);
const error = ref('');

/**
 * Loads leases visible to the current user and updates pagination metadata.
 */
const fetchLeases = async () => {
  loading.value = true;
  try {
    const { data } = await api.get('/leases', {
      params: {
        page: pagination.page,
        size: pagination.size,
        status: filters.status || undefined,
        propertyId: filters.propertyId || undefined
      }
    });
    leases.value = data.content;
    pagination.totalPages = Math.max(data.totalPages, 1);
  } finally {
    loading.value = false;
  }
};

/**
 * Handles create/update operations depending on whether we are currently editing a row.
 */
const handleSubmit = async () => {
  try {
    error.value = '';
    if (editingId.value) {
      await api.put(`/leases/${editingId.value}`, form);
    } else {
      await api.post('/leases', form);
    }
    resetForm();
    fetchLeases();
  } catch (err) {
    error.value = err.response?.data?.message ?? '保存失败，请确认输入信息';
  }
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
};

/**
 * Deletes a lease after confirming with the user (owners/admins only).
 */
const remove = async (id) => {
  if (!confirm('确认删除该租约？')) return;
  await api.delete(`/leases/${id}`);
  fetchLeases();
};

const changePage = (page) => {
  pagination.page = page;
  fetchLeases();
};

const resetForm = () => {
  editingId.value = null;
  Object.assign(form, {
    propertyId: '',
    tenantId: '',
    startDate: '',
    endDate: '',
    rentAmount: '',
    status: 'ACTIVE'
  });
  error.value = '';
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

const formatDate = (value) => {
  if (!value) return '-';
  return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium' }).format(new Date(value));
};

const applyFilters = () => { pagination.page = 0; fetchLeases(); };
const clearFilters = () => { filters.status=''; filters.propertyId=''; pagination.page = 0; fetchLeases(); };

onMounted(fetchLeases);
</script>

<style scoped>
.leases-layout {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 24px;
}

@media (max-width: 1080px) {
  .leases-layout {
    grid-template-columns: 1fr;
  }
}

.grid-form {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.grid-form .form-field:nth-child(odd),
.grid-form .form-field:nth-child(even) {
  min-width: 0;
}

.form-actions {
  grid-column: span 2;
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

.status-pill {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 12px;
  color: #fff;
}

.status-pill.active {
  background: #22c55e;
}

.status-pill.expired {
  background: #f97316;
}

.status-pill.terminated {
  background: #ef4444;
}

.actions button {
  margin-right: 8px;
  padding: 4px 8px;
}

.actions .danger {
  color: #ef4444;
  border: 1px solid #ef4444;
  background: none;
}
.error-msg {
  color: #ef4444;
  margin-top: 12px;
}
</style>
