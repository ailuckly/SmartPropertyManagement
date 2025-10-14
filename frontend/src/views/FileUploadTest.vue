<template>
  <div class="file-upload-test">
    <el-card shadow="never">
      <template #header>
        <h2>文件上传功能测试</h2>
      </template>

      <!-- 物业图片上传测试 -->
      <div class="test-section">
        <h3>1. 物业图片上传</h3>
        <p>测试上传物业图片（支持多张）</p>
        
        <FileUpload
          v-model="propertyImages"
          category="PROPERTY_IMAGE"
          :entity-id="testPropertyId"
          :limit="10"
          accept="image/*"
          tip-text="支持jpg/png/gif格式，单个文件不超过5MB，最多10张"
          @upload-success="handlePropertyUploadSuccess"
        />

        <div class="file-list" v-if="propertyImages.length > 0">
          <h4>已上传的文件：</h4>
          <ul>
            <li v-for="file in propertyImages" :key="file.id">
              {{ file.originalFileName }} ({{ formatFileSize(file.fileSize) }})
            </li>
          </ul>
        </div>
      </div>

      <el-divider />

      <!-- 租约合同上传测试 -->
      <div class="test-section">
        <h3>2. 租约合同上传</h3>
        <p>测试上传租约合同（PDF文件）</p>
        
        <FileUpload
          v-model="leaseContract"
          category="LEASE_CONTRACT"
          :entity-id="testLeaseId"
          list-type="text"
          :multiple="false"
          :limit="1"
          accept=".pdf,application/pdf"
          button-text="上传合同"
          tip-text="只支持PDF格式，单个文件不超过10MB"
          @upload-success="handleContractUploadSuccess"
        />

        <div class="file-list" v-if="leaseContract.length > 0">
          <h4>已上传的合同：</h4>
          <ul>
            <li v-for="file in leaseContract" :key="file.id">
              {{ file.originalFileName }} ({{ formatFileSize(file.fileSize) }})
              <el-button link type="primary" @click="previewFile(file)">预览</el-button>
            </li>
          </ul>
        </div>
      </div>

      <el-divider />

      <!-- 维修图片上传测试 -->
      <div class="test-section">
        <h3>3. 维修问题图片上传</h3>
        <p>测试上传维修请求相关的图片</p>
        
        <FileUpload
          v-model="maintenanceImages"
          category="MAINTENANCE_IMAGE"
          :entity-id="testMaintenanceId"
          :limit="5"
          accept="image/*"
          tip-text="支持jpg/png格式，单个文件不超过5MB，最多5张"
          @upload-success="handleMaintenanceUploadSuccess"
        />

        <div class="file-list" v-if="maintenanceImages.length > 0">
          <h4>已上传的文件：</h4>
          <ul>
            <li v-for="file in maintenanceImages" :key="file.id">
              {{ file.originalFileName }} ({{ formatFileSize(file.fileSize) }})
            </li>
          </ul>
        </div>
      </div>

      <el-divider />

      <!-- 测试控制 -->
      <div class="test-controls">
        <el-button type="primary" @click="loadTestFiles">加载测试数据</el-button>
        <el-button @click="clearAll">清空所有上传</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import FileUpload from '../components/FileUpload.vue'
import axios from 'axios'

/**
 * 文件上传功能测试页面
 * 用于测试文件上传、预览、删除等功能
 * 
 * @author Development Team
 * @since 2025-10-14
 */

// 测试用的实体ID（可以随意设置）
const testPropertyId = ref(1)
const testLeaseId = ref(1)
const testMaintenanceId = ref(1)

// 文件列表
const propertyImages = ref([])
const leaseContract = ref([])
const maintenanceImages = ref([])

/**
 * 处理物业图片上传成功
 */
function handlePropertyUploadSuccess(file) {
  console.log('物业图片上传成功:', file)
  ElMessage.success(`图片上传成功: ${file.originalFileName}`)
}

/**
 * 处理合同上传成功
 */
function handleContractUploadSuccess(file) {
  console.log('合同上传成功:', file)
  ElMessage.success(`合同上传成功: ${file.originalFileName}`)
}

/**
 * 处理维修图片上传成功
 */
function handleMaintenanceUploadSuccess(file) {
  console.log('维修图片上传成功:', file)
  ElMessage.success(`图片上传成功: ${file.originalFileName}`)
}

/**
 * 格式化文件大小显示
 */
function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 预览文件
 */
function previewFile(file) {
  const url = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/files/preview/${file.storedFileName}`
  window.open(url, '_blank')
}

/**
 * 加载测试数据
 */
async function loadTestFiles() {
  try {
    // 加载物业图片
    const propertyRes = await axios.get(`/api/files/entity/PROPERTY_IMAGE/${testPropertyId.value}`)
    if (propertyRes.data) {
      propertyImages.value = propertyRes.data
    }

    // 加载租约合同
    const leaseRes = await axios.get(`/api/files/entity/LEASE_CONTRACT/${testLeaseId.value}`)
    if (leaseRes.data) {
      leaseContract.value = leaseRes.data
    }

    // 加载维修图片
    const maintenanceRes = await axios.get(`/api/files/entity/MAINTENANCE_IMAGE/${testMaintenanceId.value}`)
    if (maintenanceRes.data) {
      maintenanceImages.value = maintenanceRes.data
    }

    ElMessage.success('测试数据加载成功')
  } catch (error) {
    console.error('加载测试数据失败:', error)
    ElMessage.warning('暂无测试数据')
  }
}

/**
 * 清空所有上传
 */
function clearAll() {
  propertyImages.value = []
  leaseContract.value = []
  maintenanceImages.value = []
  ElMessage.info('已清空所有上传')
}
</script>

<style scoped>
.file-upload-test {
  padding: 20px;
}

.test-section {
  margin-bottom: 30px;
}

.test-section h3 {
  margin-bottom: 10px;
  color: #303133;
}

.test-section p {
  margin-bottom: 20px;
  color: #909399;
  font-size: 14px;
}

.file-list {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.file-list h4 {
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.file-list ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.file-list li {
  padding: 8px 0;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-list li:last-child {
  border-bottom: none;
}

.test-controls {
  margin-top: 30px;
  text-align: center;
}

.test-controls .el-button {
  margin: 0 10px;
}
</style>
