<template>
  <div class="layout">
    <section class="card form-card">
      <header>
        <h2>{{ editingId ? '更新物业' : '新增物业' }}</h2>
        <button v-if="editingId" class="link-btn" @click="resetForm">切换到新增</button>
      </header>
      <form @submit.prevent="handleSubmit" class="grid-form">
        <div class="form-field" v-if="isAdmin">
          <label for="ownerId">业主用户ID</label>
          <input id="ownerId" v-model.number="form.ownerId" type="number" min="1" placeholder="必填" />
        </div>
        <div class="form-field">
          <label for="address">详细地址</label>
          <input id="address" v-model="form.address" required />
        </div>
        <div class="form-field">
          <label for="city">城市</label>
          <input id="city" v-model="form.city" />
        </div>
        <div class="form-field">
          <label for="state">省/州</label>
          <input id="state" v-model="form.state" />
        </div>
        <div class="form-field">
          <label for="zipCode">邮编</label>
          <input id="zipCode" v-model="form.zipCode" />
        </div>
        <div class="form-field">
          <label for="propertyType">物业类型</label>
          <select id="propertyType" v-model="form.propertyType" required>
            <option disabled value="">请选择</option>
            <option value="APARTMENT">公寓</option>
            <option value="HOUSE">独栋</option>
            <option value="COMMERCIAL">商用</option>
          </select>
        </div>
        <div class="form-field">
          <label for="bedrooms">卧室数量</label>
          <input id="bedrooms" v-model.number="form.bedrooms" type="number" min="0" />
        </div>
        <div class="form-field">
          <label for="bathrooms">卫生间数量</label>
          <input id="bathrooms" v-model.number="form.bathrooms" type="number" step="0.5" min="0" />
        </div>
        <div class="form-field">
          <label for="squareFootage">面积(㎡)</label>
          <input id="squareFootage" v-model.number="form.squareFootage" type="number" min="0" />
        </div>
        <div class="form-field">
          <label for="rentAmount">月租(¥)</label>
          <input id="rentAmount" v-model.number="form.rentAmount" type="number" min="0" step="0.01" />
        </div>
        <div class="form-field">
          <label for="status">状态</label>
          <select id="status" v-model="form.status">
            <option value="AVAILABLE">可出租</option>
            <option value="LEASED">已出租</option>
            <option value="UNDER_MAINTENANCE">维护中</option>
          </select>
        </div>
        <div class="form-actions">
          <button class="btn-primary" type="submit">
            {{ editingId ? '保存修改' : '创建物业' }}
          </button>
        </div>
      </form>
      <p v-if="error" class="error-msg">{{ error }}</p>
    </section>

    <section class="card">
      <header class="table-header">
        <h2>物业列表</h2>
        <div class="filters">
          <input v-model="filters.city" placeholder="城市" />
          <select v-model="filters.status">
            <option value="">所有状态</option>
            <option value="AVAILABLE">可出租</option>
            <option value="LEASED">已出租</option>
            <option value="UNDER_MAINTENANCE">维护中</option>
          </select>
          <button class="btn-primary" @click="applyFilters">筛选</button>
          <button class="link-btn"      <div class="table-wrapper" v-if="!loading">
        <table class="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>地址</th>
              <th>类型</th>
              <th>租金</th>
              <th>状态</th>
              <th>业主</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in properties" :key="item.id">
              <td>{{ item.id }}</td>
              <td>
                <div class="address-cell">
                  <strong>{{ item.address }}</strong>
                  <small>{{ item.city }} {{ item.state }} {{ item.zipCode }}</small>
                </div>
              </td>
              <td>{{ renderType(item.propertyType) }}</td>
              <td>{{ item.rentAmount ?? '-' }}</td>
              <td><span :class="['status-pill', item.status.toLowerCase()]">{{ renderStatus(item.status) }}</span></td>
              <td>{{ item.ownerUsername ?? '-' }}</td>
              <td class="actions">
                <button @click="startEdit(item)">编辑</button>
                <button class="danger" @click="remove(item.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="card" style="padding:12px;">
        <div class="skeleton" style="height:16px; margin-bottom:12px;"></div>
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
 * This view combines a property editor (admins/owners) and a paginated table shared by all authenticated roles.
 * Form state lives in reactive objects to simplify reset logic after submission or cancellation.
 */
const authStore = useAuthStore();
const properties = ref([]); // 表格数据源
const loading = ref(false);
const filters = reactive({ city: '', status: '' });
const pagination = reactive({
  page: 0,
  size: 10,
  totalPages: 1
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

const editingId = ref(null); // null 表示当前为“新增”模式
const error = ref('');

const isAdmin = authStore.hasAnyRole(['ROLE_ADMIN']);

/**
 * Loads a page of properties from the backend. Pagination metadata is reused for the navigation controls.
 */
const fetchProperties = async () => {
  loading.value = true;
  try {
    const { data } = await api.get('/properties', {
      params: { page: pagination.page, size: pagination.size, city: filters.city || undefined, status: filters.status || undefined }
    });
    properties.value = data.content;
    pagination.totalPages = Math.max(data.totalPages, 1);
  } finally {
    loading.value = false;
  }
};

/**
 * Restores the form to its initial state, keeping ownerId populated for owners to streamline data entry.
 */
const resetForm = () => {
  editingId.value = null;
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
  try {
    error.value = '';
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
    } else {
      await api.post('/properties', payload);
    }
    resetForm();
    fetchProperties();
  } catch (err) {
    error.value = err.response?.data?.message ?? '保存失败，请检查输入信息';
  }
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
};

/**
 * Removes a property after explicit confirmation from the user.
 */
const remove = async (id) => {
  if (!confirm('确认删除该物业？')) return;
  await api.delete(`/properties/${id}`);
  fetchProperties();
};

const changePage = (page) => {
  pagination.page = page;
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

const applyFilters = () => { pagination.page = 0; fetchProperties(); };
const clearFilters = () => { filters.city = ''; filters.status = ''; pagination.page = 0; fetchProperties(); };

onMounted(() => {
  if (!isAdmin && authStore.user) {
    form.ownerId = authStore.user.id;
  }
  fetchProperties();
});
</script>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 24px;
}

@media (max-width: 1080px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

.form-card header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.link-btn {
  background: none;
  border: none;
  color: #2563eb;
  cursor: pointer;
}

.grid-form {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(2, 1fr);
}

.grid-form .form-field:nth-child(odd) {
  margin-right: 8px;
}

.grid-form .form-field:nth-child(even) {
  margin-left: 8px;
}

.grid-form .form-field:nth-child(odd),
.grid-form .form-field:nth-child(even) {
  min-width: 0;
}

.grid-form .form-field:nth-last-child(-n + 2) {
  grid-column: span 2;
}

.form-actions {
  grid-column: span 2;
  display: flex;
  justify-content: flex-end;
}

.error-msg {
  color: #ef4444;
  margin-top: 12px;
}

.filters { display:flex; flex-wrap: wrap; gap: 8px; align-items:center; }
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  text-align: left;
}

.address-cell strong {
  display: block;
}

.address-cell small {
  color: #64748b;
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

.status-pill {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 12px;
  color: #fff;
}

.status-pill.available {
  background: #22c55e;
}

.status-pill.leased {
  background: #2563eb;
}

.status-pill.under_maintenance {
  background: #f97316;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
