<template>
  <div class="file-upload-container">
    <!-- 文件上传区域 -->
    <el-upload
      ref="uploadRef"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :data="uploadData"
      :file-list="fileList"
      :list-type="listType"
      :accept="accept"
      :multiple="multiple"
      :limit="limit"
      :with-credentials="true"
      :before-upload="handleBeforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-remove="handleRemove"
      :on-preview="handlePreview"
      :disabled="disabled"
    >
      <!-- 上传按钮 -->
      <el-button v-if="listType !== 'picture-card'" type="primary" :icon="Upload">
        {{ buttonText }}
      </el-button>
      <template v-else>
        <el-icon><Plus /></el-icon>
      </template>

      <!-- 提示信息 -->
      <template #tip>
        <div class="el-upload__tip" v-if="showTip">
          {{ tipText }}
        </div>
      </template>
    </el-upload>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="800px">
      <img :src="previewUrl" alt="预览图片" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Plus } from '@element-plus/icons-vue'
import axios from 'axios'

/**
 * 文件上传组件
 * 支持图片和文档上传，可配置上传类型、数量限制等
 * 
 * @author Development Team
 * @since 2025-10-14
 */

// Props 定义
const props = defineProps({
  // 文件分类 (PROPERTY_IMAGE, LEASE_CONTRACT, MAINTENANCE_IMAGE等)
  category: {
    type: String,
    required: true,
    validator: (value) => {
      return ['PROPERTY_IMAGE', 'LEASE_CONTRACT', 'MAINTENANCE_IMAGE', 'USER_AVATAR', 'OTHER'].includes(value)
    }
  },
  // 关联实体ID（可选）
  entityId: {
    type: [Number, String],
    default: null
  },
  // 初始文件列表
  modelValue: {
    type: Array,
    default: () => []
  },
  // 列表类型 text/picture/picture-card
  listType: {
    type: String,
    default: 'picture-card'
  },
  // 是否支持多选
  multiple: {
    type: Boolean,
    default: true
  },
  // 最大上传数量
  limit: {
    type: Number,
    default: 5
  },
  // 接受的文件类型
  accept: {
    type: String,
    default: 'image/*'
  },
  // 按钮文字
  buttonText: {
    type: String,
    default: '点击上传'
  },
  // 提示文字
  tipText: {
    type: String,
    default: '支持jpg/png格式，单个文件不超过5MB'
  },
  // 是否显示提示
  showTip: {
    type: Boolean,
    default: true
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  }
})

// Emits 定义
const emit = defineEmits(['update:modelValue', 'upload-success', 'upload-error', 'remove'])

// 响应式数据
const uploadRef = ref(null)
const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

// 上传URL
const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/files/upload`
})

// 上传请求头（携带认证token）
const uploadHeaders = computed(() => {
  // el-upload 不会使用axios拦截器，需要手动添加Cookie
  const headers = {}
  
  // 从 document.cookie 中提取 ACCESS_TOKEN
  const cookies = document.cookie.split('; ')
  const accessTokenCookie = cookies.find(row => row.startsWith('ACCESS_TOKEN='))
  
  if (accessTokenCookie) {
    // 直接设置Cookie头，让后端能够识别
    const token = accessTokenCookie.split('=')[1]
    headers['Cookie'] = `ACCESS_TOKEN=${token}`
  }
  
  console.log('[FileUpload] Upload headers:', headers)
  return headers
})

// 上传额外数据
const uploadData = computed(() => {
  return {
    category: props.category,
    entityId: props.entityId || ''
  }
})

// 监听 modelValue 变化，同步到 fileList
watch(() => props.modelValue, (newVal) => {
  if (newVal && newVal.length > 0) {
    fileList.value = newVal.map(file => ({
      uid: file.id,
      name: file.originalFileName,
      status: 'success',
      url: getFileUrl(file.storedFileName),
      response: { file }
    }))
  } else {
    fileList.value = []
  }
}, { immediate: true })

/**
 * 获取文件访问URL
 */
function getFileUrl(storedFileName) {
  return `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/files/preview/${storedFileName}`
}

/**
 * 上传前的校验
 */
function handleBeforeUpload(file) {
  // 文件大小校验（根据分类）
  let maxSize = 5 * 1024 * 1024 // 默认5MB
  
  if (props.category === 'LEASE_CONTRACT') {
    maxSize = 10 * 1024 * 1024 // 合同文件10MB
  }
  
  if (file.size > maxSize) {
    ElMessage.error(`文件大小不能超过 ${maxSize / 1024 / 1024}MB`)
    return false
  }
  
  // 文件类型校验
  if (props.category.includes('IMAGE') || props.category === 'USER_AVATAR') {
    const isImage = file.type.startsWith('image/')
    if (!isImage) {
      ElMessage.error('只能上传图片文件')
      return false
    }
  }
  
  if (props.category === 'LEASE_CONTRACT') {
    const isPdf = file.type === 'application/pdf'
    if (!isPdf) {
      ElMessage.error('只能上传PDF文件')
      return false
    }
  }
  
  return true
}

/**
 * 上传成功回调
 */
function handleSuccess(response, file, fileList) {
  if (response.success) {
    ElMessage.success('上传成功')
    
    // 更新文件列表
    const uploadedFile = response.file
    const newFileList = fileList.map(f => {
      if (f.uid === file.uid) {
        return {
          ...f,
          response: { file: uploadedFile },
          url: getFileUrl(uploadedFile.storedFileName)
        }
      }
      return f
    })
    
    // 提取所有已上传的文件信息
    const files = newFileList
      .filter(f => f.status === 'success' && f.response?.file)
      .map(f => f.response.file)
    
    emit('update:modelValue', files)
    emit('upload-success', uploadedFile)
  } else {
    ElMessage.error(response.message || '上传失败')
    handleError(response, file, fileList)
  }
}

/**
 * 上传失败回调
 */
function handleError(error, file, fileList) {
  console.error('上传失败:', error)
  ElMessage.error('上传失败，请重试')
  emit('upload-error', error)
}

/**
 * 文件移除回调
 */
function handleRemove(file, fileList) {
  // 如果文件已经上传成功，调用删除API
  if (file.status === 'success' && file.response?.file) {
    const fileId = file.response.file.id
    deleteFile(fileId)
  }
  
  // 更新文件列表
  const files = fileList
    .filter(f => f.status === 'success' && f.response?.file)
    .map(f => f.response.file)
  
  emit('update:modelValue', files)
  emit('remove', file)
}

/**
 * 文件预览
 */
function handlePreview(file) {
  if (props.category.includes('IMAGE') || props.category === 'USER_AVATAR') {
    previewUrl.value = file.url
    previewVisible.value = true
  } else {
    // PDF等文档直接打开新窗口
    window.open(file.url, '_blank')
  }
}

/**
 * 删除文件（调用后端API）
 */
async function deleteFile(fileId) {
  try {
    await axios.delete(`/api/files/${fileId}`)
  } catch (error) {
    console.error('删除文件失败:', error)
  }
}

/**
 * 清空文件列表
 */
function clearFiles() {
  uploadRef.value?.clearFiles()
  emit('update:modelValue', [])
}

// 暴露方法给父组件
defineExpose({
  clearFiles
})
</script>

<style scoped>
.file-upload-container {
  width: 100%;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 120px;
  height: 120px;
}

:deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
  line-height: 120px;
}

.el-upload__tip {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}
</style>
