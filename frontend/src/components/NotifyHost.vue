<template>
  <div class="toasts" aria-live="polite" aria-atomic="true">
    <div v-for="t in toasts" :key="t.id" class="toast" :class="t.type">
      <span class="msg">{{ t.message }}</span>
      <button class="close" @click="remove(t.id)" aria-label="关闭">×</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, reactive } from 'vue';

let counter = 0;
const toasts = reactive([]);

const add = (payload) => {
  const id = ++counter;
  const toast = { id, type: payload.type || 'info', message: String(payload.message || '') };
  toasts.push(toast);
  setTimeout(() => remove(id), payload.duration ?? 3000);
};

const remove = (id) => {
  const idx = toasts.findIndex((t) => t.id === id);
  if (idx !== -1) toasts.splice(idx, 1);
};

const handler = (e) => add(e.detail || {});

onMounted(() => {
  window.addEventListener('app:notify', handler);
});

onBeforeUnmount(() => {
  window.removeEventListener('app:notify', handler);
});
</script>

<style scoped>
.toasts {
  position: fixed;
  right: 16px;
  top: 80px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 50;
}
.toast {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 10px;
  color: #0f172a;
  background: #fff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12);
}
.toast.success { border-left: 4px solid #22c55e; }
.toast.error { border-left: 4px solid #ef4444; }
.toast.info { border-left: 4px solid #3b82f6; }
.toast .close {
  background: none;
  border: none;
  font-size: 18px;
  color: #64748b;
  cursor: pointer;
}
.toast .msg { white-space: pre-wrap; }
</style>
