<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ userName }} <el-tag :type="roleTagType" size="small">{{ roleText }}</el-tag></h2>
          <p class="welcome-desc">{{ welcomeMessage }}</p>
        </div>
        <div class="quick-actions">
          <template v-if="isAdmin">
            <el-button type="primary" :icon="OfficeBuilding" @click="$router.push('/properties')">管理物业</el-button>
            <el-button type="success" :icon="User" @click="$router.push('/users')">用户管理</el-button>
          </template>
          <template v-else-if="isOwner">
            <el-button type="primary" :icon="OfficeBuilding" @click="$router.push('/properties')">我的物业</el-button>
            <el-button type="success" :icon="Document" @click="$router.push('/leases')">租约管理</el-button>
          </template>
          <template v-else-if="isTenant">
            <el-button type="primary" :icon="OfficeBuilding" @click="$router.push('/properties')">浏览物业</el-button>
            <el-button type="warning" :icon="Tools" @click="$router.push('/maintenance')">维修申请</el-button>
          </template>
        </div>
      </div>
    </el-card>

    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" v-loading="loading.overview" style="margin-top: 20px">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic :title="isAdmin ? '总物业数' : '我的物业'" :value="stats.totalProperties || 0">
            <template #prefix>
              <el-icon style="color: #409EFF"><OfficeBuilding /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic :title="isAdmin ? '总租约数' : '我的租约'" :value="stats.totalLeases || 0">
            <template #prefix>
              <el-icon style="color: #67C23A"><Document /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" v-if="isAdmin">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="系统用户" :value="stats.totalUsers || 0">
            <template #prefix>
              <el-icon style="color: #909399"><User /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="isAdmin ? 6 : 12">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="待处理维修" :value="stats.pendingMaintenances || 0">
            <template #prefix>
              <el-icon style="color: #E6A23C"><Tools /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px" v-if="isOwnerOrAdmin">
      <!-- 物业状态分布 -->
      <el-col :xs="24" :md="12" :lg="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><DataLine /></el-icon>
              <span>物业状态分布</span>
            </div>
          </template>
          <v-chart
            v-loading="loading.propertyStatus"
            :option="propertyStatusOption"
            :autoresize="true"
            style="height: 300px"
          />
        </el-card>
      </el-col>

      <!-- 物业类型分布 -->
      <el-col :xs="24" :md="12" :lg="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Histogram /></el-icon>
              <span>物业类型分布</span>
            </div>
          </template>
          <v-chart
            v-loading="loading.propertyType"
            :option="propertyTypeOption"
            :autoresize="true"
            style="height: 300px"
          />
        </el-card>
      </el-col>

      <!-- 维修请求统计 -->
      <el-col :xs="24" :md="12" :lg="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Tools /></el-icon>
              <span>维修请求统计</span>
            </div>
          </template>
          <v-chart
            v-loading="loading.maintenance"
            :option="maintenanceStatusOption"
            :autoresize="true"
            style="height: 300px"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="20" style="margin-top: 20px" v-if="isOwnerOrAdmin">
      <!-- 月度收支趋势 -->
      <el-col :xs="24" :lg="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>月度收支趋势（近6个月）</span>
            </div>
          </template>
          <v-chart
            v-loading="loading.paymentTrend"
            :option="paymentTrendOption"
            :autoresize="true"
            style="height: 350px"
          />
        </el-card>
      </el-col>

      <!-- 租约到期提醒 -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Calendar /></el-icon>
              <span>租约到期提醒（未来3个月）</span>
            </div>
          </template>
          <v-chart
            v-loading="loading.leaseExpiring"
            :option="leaseExpiringOption"
            :autoresize="true"
            style="height: 350px"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 近期活动 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <el-icon><InfoFilled /></el-icon>
          <span>近期活动</span>
        </div>
      </template>
      <el-timeline v-loading="loading.activities">
        <el-timeline-item
          v-for="(activity, index) in recentActivities"
          :key="index"
          :timestamp="activity.date"
          :type="activity.type === 'lease' ? 'success' : 'warning'"
          placement="top"
        >
          <el-card>
            <h4>{{ activity.title }}</h4>
            <p>{{ activity.description }}</p>
          </el-card>
        </el-timeline-item>
        <el-empty v-if="!recentActivities.length" description="暂无活动记录" />
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api/http';
import { useAuthStore } from '../stores/auth';
import {
  OfficeBuilding,
  Document,
  Tools,
  User,
  InfoFilled,
  DataLine,
  Histogram,
  TrendCharts,
  Calendar
} from '@element-plus/icons-vue';

const authStore = useAuthStore();
const router = useRouter();
const isAdmin = authStore.hasAnyRole(['ROLE_ADMIN']);
const isOwner = authStore.hasAnyRole(['ROLE_OWNER']);
const isTenant = authStore.hasAnyRole(['ROLE_TENANT']);
const isOwnerOrAdmin = authStore.hasAnyRole(['ROLE_OWNER', 'ROLE_ADMIN']);

// 用户信息
const userName = computed(() => authStore.user?.username || '用户');
const roleText = computed(() => {
  if (isAdmin) return '管理员';
  if (isOwner) return '业主';
  if (isTenant) return '租户';
  return '用户';
});
const roleTagType = computed(() => {
  if (isAdmin) return 'danger';
  if (isOwner) return 'success';
  if (isTenant) return 'primary';
  return 'info';
});
const welcomeMessage = computed(() => {
  const hour = new Date().getHours();
  const greeting = hour < 12 ? '早上好' : hour < 18 ? '下午好' : '晚上好';
  
  if (isAdmin) {
    return `${greeting}！今天系统运行正常，有 ${stats.pendingMaintenances} 个待处理的维修请求。`;
  } else if (isOwner) {
    return `${greeting}！您有 ${stats.totalProperties} 个物业，${stats.totalLeases} 个租约在管。`;
  } else if (isTenant) {
    return `${greeting}！希望您在这里生活愉快。`;
  }
  return `${greeting}！`;
});

// 统计数据
const stats = reactive({
  totalProperties: 0,
  totalLeases: 0,
  totalUsers: 0,
  pendingMaintenances: 0
});

// 加载状态
const loading = reactive({
  overview: false,
  propertyStatus: false,
  propertyType: false,
  maintenance: false,
  paymentTrend: false,
  leaseExpiring: false,
  activities: false
});

// 近期活动
const recentActivities = ref([]);

// 图表配置
const propertyStatusOption = ref({});
const propertyTypeOption = ref({});
const maintenanceStatusOption = ref({});
const paymentTrendOption = ref({});
const leaseExpiringOption = ref({});

// 中文映射
const statusNameMap = {
  AVAILABLE: '可出租',
  LEASED: '已出租',
  UNDER_MAINTENANCE: '维护中'
};

const typeNameMap = {
  APARTMENT: '公寓',
  HOUSE: '独栋',
  COMMERCIAL: '商用'
};

const maintenanceNameMap = {
  PENDING: '待处理',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
};

// 获取概览数据
const fetchOverview = async () => {
  loading.overview = true;
  try {
    const { data } = await api.get('/dashboard/overview');
    Object.assign(stats, data);
  } catch (error) {
    console.error('加载概览数据失败', error);
  } finally {
    loading.overview = false;
  }
};

// 获取物业状态分布
const fetchPropertyStatus = async () => {
  loading.propertyStatus = true;
  try {
    const { data } = await api.get('/dashboard/property-status');
    const chartData = Object.entries(data).map(([key, value]) => ({
      name: statusNameMap[key] || key,
      value: value
    }));

    propertyStatusOption.value = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          type: 'pie',
          radius: '50%',
          data: chartData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ],
      color: ['#67C23A', '#409EFF', '#E6A23C']
    };
  } catch (error) {
    console.error('加载物业状态失败', error);
  } finally {
    loading.propertyStatus = false;
  }
};

// 获取物业类型分布
const fetchPropertyType = async () => {
  loading.propertyType = true;
  try {
    const { data } = await api.get('/dashboard/property-type');
    const chartData = Object.entries(data).map(([key, value]) => ({
      name: typeNameMap[key] || key,
      value: value
    }));

    propertyTypeOption.value = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: chartData.map(item => item.name)
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: chartData.map(item => item.value),
          type: 'bar',
          itemStyle: {
            color: '#409EFF'
          }
        }
      ]
    };
  } catch (error) {
    console.error('加载物业类型失败', error);
  } finally {
    loading.propertyType = false;
  }
};

// 获取维修请求统计
const fetchMaintenanceStatus = async () => {
  loading.maintenance = true;
  try {
    const { data } = await api.get('/dashboard/maintenance-status');
    const chartData = Object.entries(data).map(([key, value]) => ({
      name: maintenanceNameMap[key] || key,
      value: value
    }));

    maintenanceStatusOption.value = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'horizontal',
        bottom: 'bottom'
      },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 18,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: chartData
        }
      ],
      color: ['#E6A23C', '#409EFF', '#67C23A', '#909399']
    };
  } catch (error) {
    console.error('加载维修统计失败', error);
  } finally {
    loading.maintenance = false;
  }
};

// 获取收支趋势
const fetchPaymentTrend = async () => {
  loading.paymentTrend = true;
  try {
    const { data } = await api.get('/dashboard/payment-trend');
    
    paymentTrendOption.value = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['收入', '支出']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.months || []
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '¥{value}'
        }
      },
      series: [
        {
          name: '收入',
          type: 'line',
          data: data.income || [],
          smooth: true,
          itemStyle: {
            color: '#67C23A'
          },
          areaStyle: {
            color: 'rgba(103, 194, 58, 0.2)'
          }
        },
        {
          name: '支出',
          type: 'line',
          data: data.expense || [],
          smooth: true,
          itemStyle: {
            color: '#F56C6C'
          },
          areaStyle: {
            color: 'rgba(245, 108, 108, 0.2)'
          }
        }
      ]
    };
  } catch (error) {
    console.error('加载收支趋势失败', error);
  } finally {
    loading.paymentTrend = false;
  }
};

// 获取租约到期统计
const fetchLeaseExpiring = async () => {
  loading.leaseExpiring = true;
  try {
    const { data } = await api.get('/dashboard/lease-expiring');
    
    leaseExpiringOption.value = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: data.months || []
      },
      yAxis: {
        type: 'value',
        minInterval: 1
      },
      series: [
        {
          data: data.counts || [],
          type: 'bar',
          itemStyle: {
            color: '#E6A23C'
          },
          label: {
            show: true,
            position: 'top'
          }
        }
      ]
    };
  } catch (error) {
    console.error('加载租约到期数据失败', error);
  } finally {
    loading.leaseExpiring = false;
  }
};

// 获取近期活动
const fetchRecentActivities = async () => {
  loading.activities = true;
  try {
    const { data } = await api.get('/dashboard/recent-activities');
    recentActivities.value = data;
  } catch (error) {
    console.error('加载近期活动失败', error);
  } finally {
    loading.activities = false;
  }
};

onMounted(() => {
  if (authStore.isAuthenticated) {
    fetchOverview();
    fetchRecentActivities();
    
    if (isOwnerOrAdmin) {
      fetchPropertyStatus();
      fetchPropertyType();
      fetchMaintenanceStatus();
      fetchPaymentTrend();
      fetchLeaseExpiring();
    }
  }
});
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.welcome-card {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}

.welcome-card :deep(.el-card__body) {
  padding: 24px 32px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.welcome-text h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 12px;
}

.welcome-desc {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.quick-actions .el-button {
  border-radius: 4px;
}

@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .quick-actions {
    width: 100%;
    flex-direction: column;
  }
  
  .quick-actions .el-button {
    width: 100%;
  }
}

.stat-card {
  transition: transform 0.3s;
  margin-bottom: 20px;
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

:deep(.el-timeline-item__content) {
  padding-bottom: 20px;
}

:deep(.el-timeline-item__card) h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 14px;
}

:deep(.el-timeline-item__card) p {
  margin: 0;
  color: #606266;
  font-size: 13px;
}

@media (max-width: 768px) {
  .stat-card {
    margin-bottom: 12px;
  }
  
  :deep(.el-col) {
    margin-bottom: 12px;
  }
}
</style>
