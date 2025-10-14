<template>
  <div class="dashboard">
    <el-row :gutter="20" v-loading="loading">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="物业数量" :value="stats.properties">
            <template #prefix>
              <el-icon style="color: #409EFF">
                <OfficeBuilding />
              </el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="租约数量" :value="stats.leases">
            <template #prefix>
              <el-icon style="color: #67C23A">
                <Document />
              </el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="待处理维修" :value="stats.pendingMaintenances">
            <template #prefix>
              <el-icon style="color: #E6A23C">
                <Tools />
              </el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="welcome-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <el-icon :size="20" color="#409EFF"><InfoFilled /></el-icon>
          <span>欢迎使用智慧物业管理平台</span>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="快速导航">
          使用左侧导航栏可快速进入各个功能模块
        </el-descriptions-item>
        <el-descriptions-item label="管理员/业主">
          可以登记物业信息、创建租约、管理收支记录
        </el-descriptions-item>
        <el-descriptions-item label="租户">
          可以在线提交维修申请并实时追踪处理进度
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import api from '../api/http';
import { useAuthStore } from '../stores/auth';
import { OfficeBuilding, Document, Tools, InfoFilled } from '@element-plus/icons-vue';

/**
 * Lightweight dashboard that pulls high-level counts for properties, leases and maintenance requests.
 */
// 仪表盘展示的核心统计数据
const stats = reactive({
  properties: 0,
  leases: 0,
  pendingMaintenances: 0
});

const loading = ref(false);
const authStore = useAuthStore();

/**
 * Aggregates counts from several endpoints. Failures are tolerated so the page still renders partial data.
 */
const fetchStats = async () => {
  loading.value = true;
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
  } finally {
    loading.value = false;
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
  padding: 0;
}

.stat-card {
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  font-size: 16px;
}

:deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

:deep(.el-statistic__number) {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
  width: 120px;
}
</style>
