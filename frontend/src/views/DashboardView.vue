<template>
  <div class="dashboard">
    <section class="grid">
      <article class="card stat-card">
        <span class="label">物业数量</span>
        <strong>{{ stats.properties }}</strong>
      </article>
      <article class="card stat-card">
        <span class="label">租约数量</span>
        <strong>{{ stats.leases }}</strong>
      </article>
      <article class="card stat-card">
        <span class="label">待处理维修</span>
        <strong>{{ stats.pendingMaintenances }}</strong>
      </article>
    </section>

    <section class="card welcome-card">
      <h2>欢迎回来</h2>
      <p>在这里可以集中管理物业、租约、缴费和维修申请。</p>
      <ul>
        <li>使用导航栏快速进入模块</li>
        <li>管理员/业主可登记物业与租约</li>
        <li>租户可在线提交维修申请并追踪进度</li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue';
import api from '../api/http';
import { useAuthStore } from '../stores/auth';

/**
 * Lightweight dashboard that pulls high-level counts for properties, leases and maintenance requests.
 */
const stats = reactive({
  properties: 0,
  leases: 0,
  pendingMaintenances: 0
});

const authStore = useAuthStore();

/**
 * Aggregates counts from several endpoints. Failures are tolerated so the page still renders partial data.
 */
const fetchStats = async () => {
  try {
    const [propertiesResponse, leasesResponse, maintenanceResponse] = await Promise.all([
      api.get('/properties', { params: { size: 1 } }).catch(() => null),
      api.get('/leases', { params: { size: 1 } }).catch(() => null),
      api.get('/maintenance-requests', { params: { size: 50 } }).catch(() => null)
    ]);

    if (propertiesResponse) {
      stats.properties =
        propertiesResponse.data.totalElements ??
        (propertiesResponse.data.content ? propertiesResponse.data.content.length : 0);
    }

    if (leasesResponse) {
      stats.leases =
        leasesResponse.data.totalElements ??
        (leasesResponse.data.content ? leasesResponse.data.content.length : 0);
    }

    if (maintenanceResponse) {
      const items = maintenanceResponse.data.content ?? [];
      stats.pendingMaintenances = items.filter((item) => item.status === 'PENDING').length;
    }
  } catch (error) {
    console.error('加载仪表盘数据失败', error);
  }
};

onMounted(() => {
  if (authStore.isAuthenticated) {
    fetchStats();
  }
});
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  text-align: center;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  padding: 24px;
}

.stat-card .label {
  font-size: 14px;
  opacity: 0.9;
}

.stat-card strong {
  display: block;
  margin-top: 12px;
  font-size: 32px;
  font-weight: 700;
}

.welcome-card h2 {
  margin-top: 0;
}

.welcome-card ul {
  padding-left: 18px;
  color: #334155;
}
</style>
