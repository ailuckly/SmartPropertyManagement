<template>
  <div class="notifications-view">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>通知中心</span>
          <div class="header-actions">
            <el-button :icon="Check" @click="markAllAsRead" :disabled="unreadCount === 0">
              全部已读
            </el-button>
            <el-button :icon="Delete" @click="deleteAllRead">
              清空已读
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选工具栏 -->
      <div class="filter-bar">
        <el-radio-group v-model="filterType" @change="fetchNotifications">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="unread">未读</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 通知列表 -->
      <el-empty v-if="notifications.length === 0" description="暂无通知" />
      <div v-else class="notifications-list">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-item"
          :class="{ 'is-unread': !notification.isRead }"
        >
          <div class="notification-icon">
            <el-icon :size="24">
              <component :is="getIcon(notification.type)" />
            </el-icon>
          </div>
          <div class="notification-content">
            <div class="notification-header">
              <h4 class="notification-title">{{ notification.title }}</h4>
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
            </div>
            <p class="notification-body">{{ notification.content }}</p>
            <div class="notification-footer">
              <el-tag :type="getTypeTag(notification.type)" size="small">
                {{ getTypeText(notification.type) }}
              </el-tag>
            </div>
          </div>
          <div class="notification-actions">
            <el-button
              v-if="!notification.isRead"
              :icon="Check"
              circle
              size="small"
              @click="markAsRead(notification.id)"
              title="标记已读"
            />
            <el-button
              :icon="Delete"
              circle
              size="small"
              @click="deleteNotification(notification.id)"
              title="删除"
            />
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="pagination.total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { Check, Delete, Bell, Document, Tools, Wallet, Warning } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import api from '../api/http';

const notifications = ref([]);
const loading = ref(false);
const filterType = ref('all');
const unreadCount = ref(0);

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

/**
 * 获取通知列表
 */
const fetchNotifications = async () => {
  loading.value = true;
  try {
    const { data } = await api.get('/notifications', {
      params: {
        page: pagination.page - 1,
        size: pagination.size,
        unreadOnly: filterType.value === 'unread'
      }
    });
    notifications.value = data.content;
    pagination.total = data.totalElements || 0;
    
    // 更新未读数量
    await fetchUnreadCount();
  } catch (error) {
    ElMessage.error('加载通知失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 获取未读数量
 */
const fetchUnreadCount = async () => {
  try {
    const { data } = await api.get('/notifications/unread-count');
    unreadCount.value = data.count || 0;
  } catch (error) {
    console.error('获取未读数量失败:', error);
  }
};

/**
 * 标记单个通知为已读
 */
const markAsRead = async (id) => {
  try {
    await api.patch(`/notifications/${id}/read`);
    await fetchNotifications();
    ElMessage.success('已标记为已读');
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

/**
 * 标记所有通知为已读
 */
const markAllAsRead = async () => {
  try {
    const { data } = await api.patch('/notifications/read-all');
    await fetchNotifications();
    ElMessage.success(`已标记 ${data.count} 条通知为已读`);
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

/**
 * 删除单个通知
 */
const deleteNotification = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await api.delete(`/notifications/${id}`);
    await fetchNotifications();
    ElMessage.success('删除成功');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

/**
 * 清空所有已读通知
 */
const deleteAllRead = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有已读通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const { data } = await api.delete('/notifications/read');
    await fetchNotifications();
    ElMessage.success(`已删除 ${data.count} 条通知`);
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败');
    }
  }
};

const handlePageChange = (page) => {
  pagination.page = page;
  fetchNotifications();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.page = 1;
  fetchNotifications();
};

/**
 * 根据通知类型获取图标
 */
const getIcon = (type) => {
  const iconMap = {
    LEASE_CREATED: Document,
    LEASE_UPDATED: Document,
    LEASE_EXPIRING_SOON: Warning,
    LEASE_EXPIRED: Warning,
    MAINTENANCE_CREATED: Tools,
    MAINTENANCE_IN_PROGRESS: Tools,
    MAINTENANCE_COMPLETED: Tools,
    MAINTENANCE_CANCELLED: Tools,
    PAYMENT_RECEIVED: Wallet,
    PAYMENT_DUE: Wallet,
    SYSTEM_ANNOUNCEMENT: Bell,
    PROPERTY_STATUS_CHANGED: Document
  };
  return iconMap[type] || Bell;
};

/**
 * 根据通知类型获取标签类型
 */
const getTypeTag = (type) => {
  if (type.startsWith('LEASE')) return 'primary';
  if (type.startsWith('MAINTENANCE')) return 'warning';
  if (type.startsWith('PAYMENT')) return 'success';
  return 'info';
};

/**
 * 获取通知类型文本
 */
const getTypeText = (type) => {
  const textMap = {
    LEASE_CREATED: '租约',
    LEASE_UPDATED: '租约',
    LEASE_EXPIRING_SOON: '租约到期',
    LEASE_EXPIRED: '租约到期',
    MAINTENANCE_CREATED: '维修',
    MAINTENANCE_IN_PROGRESS: '维修',
    MAINTENANCE_COMPLETED: '维修',
    MAINTENANCE_CANCELLED: '维修',
    PAYMENT_RECEIVED: '支付',
    PAYMENT_DUE: '支付',
    SYSTEM_ANNOUNCEMENT: '系统',
    PROPERTY_STATUS_CHANGED: '物业'
  };
  return textMap[type] || '通知';
};

/**
 * 格式化时间
 */
const formatTime = (dateTime) => {
  if (!dateTime) return '';
  
  const date = new Date(dateTime);
  const now = new Date();
  const diff = now - date;
  
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
};

onMounted(() => {
  fetchNotifications();
});
</script>

<style scoped>
.notifications-view {
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
  margin-bottom: 16px;
}

.notifications-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.notification-item.is-unread {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.notification-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.notification-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fff;
  color: #409eff;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.notification-time {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.notification-body {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  word-break: break-word;
}

.notification-footer {
  display: flex;
  gap: 8px;
}

.notification-actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
