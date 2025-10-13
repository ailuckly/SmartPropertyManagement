<template>
  <div class="not-found card">
    <h2>页面不存在</h2>
    <p>很抱歉，您访问的页面已不存在或被移动。</p>
    <RouterLink to="/" class="btn-primary">返回首页</RouterLink>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router';
</script>

<style scoped>
.not-found {
  max-width: 420px;
  margin: 80px auto;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

a.btn-primary {
  display: inline-block;
  padding: 10px 16px;
}
</style>
