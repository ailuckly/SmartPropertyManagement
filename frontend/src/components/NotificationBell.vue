<template>
  <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="notification-badge">
    <el-button :icon="Bell" circle @click="goToNotifications" />
  </el-badge>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Bell } from '@element-plus/icons-vue';
import api from '@/api/http';

const router = useRouter();
const unreadCount = ref(0);

const fetchUnreadCount = async () => {
  try {
    const { data } = await api.get('/notifications/unread-count');
    unreadCount.value = data.count || 0;
  } catch (error) {
    console.error('获取未读通知数量失败:', error);
  }
};

const goToNotifications = () => {
  router.push('/notifications');
};

// 初始加载
onMounted(() => {
  fetchUnreadCount();
  
  // 每30秒刷新一次未读数量
  setInterval(fetchUnreadCount, 30000);
});

// 暴露方法供外部调用
defineExpose({
  fetchUnreadCount
});
</script>

<style scoped>
.notification-badge {
  margin-right: 8px;
}

:deep(.el-badge__content) {
  border: 1px solid #fff;
}
</style>
