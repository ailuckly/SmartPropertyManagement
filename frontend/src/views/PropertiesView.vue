<template>
  <div class="properties-view">
    <!-- 物业列表卡片 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>物业列表</span>
          <div class="header-actions">
            <el-button type="success" :icon="Download" :loading="exporting" @click="handleExport">
              导出Excel
            </el-button>
            <el-button type="primary" @click="openCreateDialog">
              <el-icon><Plus /></el-icon>
              新建物业
            </el-button>
          </div>
        </div>
      </template>

          <!-- 筛选工具栏 -->
          <div class="filter-bar">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索地址、城市、邮编..." 
              clearable 
              :prefix-icon="Search"
              @input="handleSearchInput"
              style="width: 250px" 
            />
            <el-input v-model="filters.city" placeholder="按城市筛选" clearable style="width: 150px" />
            <el-select v-model="filters.status" placeholder="按状态筛选" clearable style="width: 150px">
              <el-option label="可出租" value="AVAILABLE" />
              <el-option label="已出租" value="LEASED" />
              <el-option label="维护中" value="UNDER_MAINTENANCE" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="applyFilters">筛选</el-button>
            <el-button :icon="RefreshLeft" @click="clearFilters">清空</el-button>
            <el-button type="info" :icon="Filter" @click="toggleAdvancedFilters">
              {{ showAdvancedFilters ? '隐藏高级筛选' : '高级筛选' }}
            </el-button>
          </div>

          <!-- 高级筛选面板 -->
          <el-collapse-transition>
            <div v-show="showAdvancedFilters" class="advanced-filters">
              <el-card shadow="never" body-style="padding: 16px;">
                <template #header>
                  <div class="card-header">
                    <span>高级筛选</span>
                    <el-button size="small" type="info" @click="resetAdvancedFilters">重置</el-button>
                  </div>
                </template>
                
                <el-form :model="advancedFilters" label-width="80px" class="advanced-filter-form">
                  <el-row :gutter="16">
                    <el-col :xs="24" :sm="12" :md="8">
                      <el-form-item label="物业类型">
                        <el-select v-model="advancedFilters.propertyType" placeholder="选择类型" clearable style="width: 100%;">
                          <el-option label="公寓" value="APARTMENT" />
                          <el-option label="独栋" value="HOUSE" />
                          <el-option label="商用" value="COMMERCIAL" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8">
                      <el-form-item label="租金范围">
                        <div style="display: flex; gap: 8px; align-items: center;">
                          <el-input-number 
                            v-model="advancedFilters.minRent" 
                            :min="0" 
                            :precision="2" 
                            placeholder="最低" 
                            style="width: 80px;"
                          />
                          <span>至</span>
                          <el-input-number 
                            v-model="advancedFilters.maxRent" 
                            :min="advancedFilters.minRent || 0" 
                            :precision="2" 
                            placeholder="最高" 
                            style="width: 80px;"
                          />
                        </div>
                      </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="8">
                      <el-form-item label="卧室数量">
                        <div style="display: flex; gap: 8px; align-items: center;">
                          <el-input-number 
                            v-model="advancedFilters.minBedrooms" 
                            :min="0" 
                            placeholder="最少" 
                            style="width: 70px;"
                          />
                          <span>至</span>
                          <el-input-number 
                            v-model="advancedFilters.maxBedrooms" 
                            :min="advancedFilters.minBedrooms || 0" 
                            placeholder="最多" 
                            style="width: 70px;"
                          />
                        </div>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-row style="margin-top: 16px;">
                    <el-col :span="24">
                      <div style="text-align: center;">
                        <el-button type="primary" :icon="Search" @click="applyAdvancedFilters">应用筛选</el-button>
                        <el-button @click="resetAdvancedFilters">重置筛选</el-button>
                      </div>
                    </el-col>
                  </el-row>
                </el-form>
              </el-card>
            </div>
          </el-collapse-transition>

          <!-- 批量操作工具栏 -->
          <div v-if="selectedRows.length > 0" class="batch-actions">
            <el-alert
              :title="`已选中 ${selectedRows.length} 项`"
              type="info"
              :closable="false"
              show-icon
              style="margin: 16px 0 8px 0;"
            />
            <div class="batch-buttons">
              <el-select v-model="batchStatus" placeholder="选择状态" style="width: 150px; margin-right: 8px;">
                <el-option label="可出租" value="AVAILABLE" />
                <el-option label="已出租" value="LEASED" />
                <el-option label="维护中" value="UNDER_MAINTENANCE" />
              </el-select>
              <el-button type="primary" :loading="batchUpdating" @click="batchUpdateStatus">批量更新状态</el-button>
              <el-button type="danger" :loading="batchDeleting" @click="batchDelete">批量删除</el-button>
              <el-button @click="clearSelection">取消选择</el-button>
            </div>
          </div>

          <!-- 表格 -->
          <el-table
            ref="tableRef"
            :data="properties"
            v-loading="loading"
            stripe
            @selection-change="handleSelectionChange"
            style="width: 100%; margin-top: 16px"
          >
            <el-table-column type="selection" width="50" />
            <el-table-column prop="id" label="ID" width="60" />
            
            <!-- 封面图片列 -->
            <el-table-column label="封面" width="80">
              <template #default="{ row }">
                <el-image
                  v-if="row.coverImage"
                  :src="getPreviewUrl(row.coverImage)"
                  fit="cover"
                  style="width: 60px; height: 60px; border-radius: 4px; cursor: pointer;"
                  :preview-src-list="[getPreviewUrl(row.coverImage)]"
                />
                <el-icon v-else style="font-size: 40px; color: #dcdfe6;">
                  <Picture />
                </el-icon>
              </template>
            </el-table-column>
            
            <el-table-column label="地址" min-width="200">
              <template #default="{ row }">
                <div class="address-cell">
                  <div class="address-main">{{ row.address }}</div>
                  <div class="address-sub">{{ row.city }} {{ row.state }} {{ row.zipCode }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="propertyType" label="类型" width="100">
              <template #default="{ row }">
                {{ renderType(row.propertyType) }}
              </template>
            </el-table-column>
            <el-table-column prop="rentAmount" label="租金(¥)" width="110" align="right">
              <template #default="{ row }">
                {{ row.rentAmount ?? '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ renderStatus(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ownerUsername" label="业主" width="110">
              <template #default="{ row }">
                {{ row.ownerUsername ?? '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="info" size="small" @click="viewDetails(row)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button link type="primary" size="small" @click="startEdit(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button link type="danger" size="small" @click="remove(row.id)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 物业表单弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '更新物业' : '新建物业'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="default"
        @submit.prevent="handleSubmit"
      >
        <el-form-item v-if="isAdmin" label="业主用户ID" prop="ownerId">
          <el-input-number
            v-model="form.ownerId"
            :min="1"
            placeholder="请输入业主用户ID"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" clearable />
        </el-form-item>

        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="请输入城市" clearable />
        </el-form-item>

        <el-form-item label="省/州" prop="state">
          <el-input v-model="form.state" placeholder="请输入省/州" clearable />
        </el-form-item>

        <el-form-item label="邮编" prop="zipCode">
          <el-input v-model="form.zipCode" placeholder="请输入邮编" clearable />
        </el-form-item>

        <el-form-item label="物业类型" prop="propertyType">
          <el-select v-model="form.propertyType" placeholder="请选择物业类型" style="width: 100%">
            <el-option label="公寓" value="APARTMENT" />
            <el-option label="独栋" value="HOUSE" />
            <el-option label="商用" value="COMMERCIAL" />
          </el-select>
        </el-form-item>

        <el-form-item label="卧室数量" prop="bedrooms">
          <el-input-number v-model="form.bedrooms" :min="0" style="width: 100%" />
        </el-form-item>

        <el-form-item label="卫生间数量" prop="bathrooms">
          <el-input-number v-model="form.bathrooms" :min="0" :step="0.5" style="width: 100%" />
        </el-form-item>

        <el-form-item label="面积(㎡)" prop="squareFootage">
          <el-input-number v-model="form.squareFootage" :min="0" style="width: 100%" />
        </el-form-item>

        <el-form-item label="月租(¥)" prop="rentAmount">
          <el-input-number v-model="form.rentAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="可出租" value="AVAILABLE" />
            <el-option label="已出租" value="LEASED" />
            <el-option label="维护中" value="UNDER_MAINTENANCE" />
          </el-select>
        </el-form-item>

        <!-- 图片上传 -->
        <el-form-item label="物业图片">
          <FileUpload
            v-model="form.images"
            category="PROPERTY_IMAGE"
            :entity-id="editingId"
            :limit="10"
            accept="image/*"
            tip-text="支持jpg/png/gif格式，单个文件不超过5MB，最多10张"
            @upload-success="handleImageUploadSuccess"
          />
        </el-form-item>

        <!-- 已上传图片预览和封面设置 -->
        <el-form-item v-if="form.images && form.images.length > 0" label="图片管理">
          <el-alert type="info" :closable="false" style="margin-bottom: 12px">
            <template #default>
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-icon><Picture /></el-icon>
                <span>点击图片可设置为封面</span>
              </div>
            </template>
          </el-alert>
          <div class="image-gallery">
            <div 
              v-for="img in form.images" 
              :key="img.id"
              class="image-item"
              :class="{'is-cover': img.isCover}"
              @click="setCoverImage(img.id)"
            >
              <el-image 
                :src="getPreviewUrl(img.storedFileName)" 
                fit="cover"
                style="width: 100%; height: 100%;"
                :preview-src-list="[getPreviewUrl(img.storedFileName)]"
                @click.stop
              />
              <el-tag v-if="img.isCover" type="success" size="small" class="cover-badge">封面</el-tag>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon style="margin-top: 12px" />

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ editingId ? '保存修改' : '创建物业' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 物业详情弹窗 -->
    <el-dialog
      v-model="detailsVisible"
      title="物业详情"
      width="800px"
      top="5vh"
    >
      <div v-if="currentProperty" class="property-details">
        <!-- 图片展示区 -->
        <div v-if="currentProperty.images && currentProperty.images.length > 0" class="details-images">
          <el-carousel height="400px" indicator-position="outside">
            <el-carousel-item v-for="img in currentProperty.images" :key="img.id">
              <el-image
                :src="getPreviewUrl(img.storedFileName)"
                fit="contain"
                style="width: 100%; height: 100%;"
                :preview-src-list="currentProperty.images.map(i => getPreviewUrl(i.storedFileName))"
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-if="img.isCover" class="carousel-cover-badge">
                <el-tag type="success" size="small">封面</el-tag>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
        <el-empty v-else description="暂无图片" :image-size="100" />

        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border class="details-section">
          <el-descriptions-item label="物业ID">
            <el-tag>{{ currentProperty.id }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentProperty.status)">
              {{ renderStatus(currentProperty.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="详细地址" :span="2">
            {{ currentProperty.address }}
          </el-descriptions-item>
          <el-descriptions-item label="城市">
            {{ currentProperty.city }}
          </el-descriptions-item>
          <el-descriptions-item label="省/州">
            {{ currentProperty.state }}
          </el-descriptions-item>
          <el-descriptions-item label="邮编">
            {{ currentProperty.zipCode }}
          </el-descriptions-item>
          <el-descriptions-item label="物业类型">
            {{ renderType(currentProperty.propertyType) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 房屋信息 -->
        <el-descriptions title="房屋信息" :column="2" border class="details-section">
          <el-descriptions-item label="卧室数量">
            {{ currentProperty.bedrooms ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="卫生间数量">
            {{ currentProperty.bathrooms ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="面积">
            {{ currentProperty.squareFootage ?? '-' }} ㎡
          </el-descriptions-item>
          <el-descriptions-item label="月租">
            <span style="color: #f56c6c; font-weight: bold; font-size: 16px;">
              ¥{{ currentProperty.rentAmount ?? '-' }}
            </span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 其他信息 -->
        <el-descriptions title="其他信息" :column="2" border class="details-section">
          <el-descriptions-item label="业主">
            {{ currentProperty.ownerUsername ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(currentProperty.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailsVisible = false">关闭</el-button>
          <el-button type="primary" @click="editFromDetails">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue';
import api from '../api/http';
import { useAuthStore } from '../stores/auth';
import { Search, RefreshLeft, Edit, Delete, Plus, Download, Picture, View, Filter } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { exportProperties } from '@/utils/download';
import FileUpload from '@/components/FileUpload.vue';

/**
 * This view combines a property editor (admins/owners) and a paginated table shared by all authenticated roles.
 * Form state lives in reactive objects to simplify reset logic after submission or cancellation.
 */
const authStore = useAuthStore();
const formRef = ref(null);
const tableRef = ref(null);
const properties = ref([]); // 表格数据源
const loading = ref(false);
const submitting = ref(false);
const exporting = ref(false);
const searchKeyword = ref('');
const filters = reactive({ city: '', status: '' });

// 批量操作相关
const selectedRows = ref([]);
const batchStatus = ref('');
const batchDeleting = ref(false);
const batchUpdating = ref(false);

// 高级筛选相关
const showAdvancedFilters = ref(false);
const advancedFilters = reactive({
  propertyType: '',
  minRent: null,
  maxRent: null,
  minBedrooms: null,
  maxBedrooms: null
});
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
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
  status: 'AVAILABLE',
  images: []  // 新增：物业图片
});

const rules = {
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  propertyType: [{ required: true, message: '请选择物业类型', trigger: 'change' }]
};

const editingId = ref(null); // null 表示当前为"新增"模式
const error = ref('');
const dialogVisible = ref(false); // 控制表单弹窗显示
const detailsVisible = ref(false); // 控制详情弹窗显示
const currentProperty = ref(null); // 当前查看的物业详情

const isAdmin = authStore.hasAnyRole(['ROLE_ADMIN']);

/**
 * Loads a page of properties from the backend. Pagination metadata is reused for the navigation controls.
 */
const fetchProperties = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page - 1, // Element UI 分页从 1 开始，后端从 0 开始
      size: pagination.size, 
      city: filters.city || undefined, 
      status: filters.status || undefined,
      keyword: searchKeyword.value || undefined
    };
    
    // 添加高级筛选参数
    if (advancedFilters.propertyType) {
      params.propertyType = advancedFilters.propertyType;
    }
    if (advancedFilters.minRent !== null) {
      params.minRent = advancedFilters.minRent;
    }
    if (advancedFilters.maxRent !== null) {
      params.maxRent = advancedFilters.maxRent;
    }
    if (advancedFilters.minBedrooms !== null) {
      params.minBedrooms = advancedFilters.minBedrooms;
    }
    if (advancedFilters.maxBedrooms !== null) {
      params.maxBedrooms = advancedFilters.maxBedrooms;
    }

    const { data } = await api.get('/properties', { params });
    
    // 为每个物业加载封面图片
    properties.value = await Promise.all(data.content.map(async (property) => {
      const coverImage = await loadCoverImage(property.id);
      return { ...property, coverImage };
    }));
    
    pagination.total = data.totalElements || 0;
  } catch (err) {
    ElMessage.error('加载物业列表失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 打开新建物业弹窗
 */
const openCreateDialog = () => {
  resetForm();
  dialogVisible.value = true;
};

/**
 * 关闭弹窗并重置表单
 */
const handleDialogClose = () => {
  resetForm();
};

/**
 * Restores the form to its initial state, keeping ownerId populated for owners to streamline data entry.
 */
const resetForm = () => {
  editingId.value = null;
  if (formRef.value) {
    formRef.value.resetFields();
  }
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
    status: 'AVAILABLE',
    images: []  // 重置图片
  });
  error.value = '';
};

/**
 * Sends either a create or update request depending on the presence of {@code editingId}.
 */
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    error.value = '';
    
    try {
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
        ElMessage.success('物业更新成功');
      } else {
        await api.post('/properties', payload);
        ElMessage.success('物业创建成功');
      }
      dialogVisible.value = false;
      resetForm();
      fetchProperties();
    } catch (err) {
      error.value = err.response?.data?.message ?? '保存失败，请检查输入信息';
    } finally {
      submitting.value = false;
    }
  });
};

/**
 * Populates the form with the selected row, switching the component into "edit" mode.
 */
const startEdit = async (item) => {
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
    status: item.status,
    images: []  // 先重置
  });
  
  // 加载物业图片
  await loadPropertyImages(item.id);
  
  dialogVisible.value = true;
};

/**
 * Removes a property after explicit confirmation from the user.
 */
const remove = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该物业？此操作不可撤销。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await api.delete(`/properties/${id}`);
    ElMessage.success('删除成功');
    fetchProperties();
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handlePageChange = (page) => {
  pagination.page = page;
  fetchProperties();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  pagination.page = 1;
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

const getStatusType = (status) => {
  switch (status) {
    case 'AVAILABLE':
      return 'success';
    case 'LEASED':
      return 'primary';
    case 'UNDER_MAINTENANCE':
      return 'warning';
    default:
      return 'info';
  }
};

const applyFilters = () => { 
  pagination.page = 1; 
  fetchProperties(); 
};

const clearFilters = () => { 
  filters.city = ''; 
  filters.status = '';
  searchKeyword.value = '';
  resetAdvancedFilters();
  pagination.page = 1; 
  fetchProperties(); 
};

// 搜索防抖
 let searchTimeout = null;
const handleSearchInput = () => {
  if (searchTimeout) clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    pagination.page = 1;
    fetchProperties();
  }, 500); // 500ms 防抖
};

/**
 * 切换高级筛选面板显示
 */
const toggleAdvancedFilters = () => {
  showAdvancedFilters.value = !showAdvancedFilters.value;
};

/**
 * 重置高级筛选条件
 */
const resetAdvancedFilters = () => {
  Object.assign(advancedFilters, {
    propertyType: '',
    minRent: null,
    maxRent: null,
    minBedrooms: null,
    maxBedrooms: null
  });
};

/**
 * 应用高级筛选
 */
const applyAdvancedFilters = () => {
  pagination.page = 1;
  fetchProperties();
};

/**
 * 导出物业列表为 Excel
 */
const handleExport = async () => {
  exporting.value = true;
  try {
    // 如果不是管理员，只导出自己的物业
    const ownerId = !isAdmin && authStore.user ? authStore.user.id : null;
    await exportProperties(ownerId);
    ElMessage.success('导出成功！');
  } catch (err) {
    ElMessage.error('导出失败，请稍后重试');
    console.error('导出错误:', err);
  } finally {
    exporting.value = false;
  }
};

/**
 * 处理表格选择变化
 */
const handleSelectionChange = (selection) => {
  selectedRows.value = selection;
};

/**
 * 清空选择
 */
const clearSelection = () => {
  if (tableRef.value) {
    tableRef.value.clearSelection();
  }
  selectedRows.value = [];
};

/**
 * 批量删除
 */
const batchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的物业');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedRows.value.length} 个物业？此操作不可撤销。`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    batchDeleting.value = true;
    const ids = selectedRows.value.map(row => row.id);
    
    await api.delete('/properties/batch', { data: ids });
    ElMessage.success(`成功删除 ${ids.length} 个物业`);
    
    clearSelection();
    fetchProperties();
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('批量删除失败');
      console.error('Batch delete error:', err);
    }
  } finally {
    batchDeleting.value = false;
  }
};

/**
 * 批量更新状态
 */
const batchUpdateStatus = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要更新的物业');
    return;
  }
  
  if (!batchStatus.value) {
    ElMessage.warning('请选择要更新的状态');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确认将选中的 ${selectedRows.value.length} 个物业状态更新为“${renderStatus(batchStatus.value)}”？`,
      '批量更新确认',
      {
        confirmButtonText: '确定更新',
        cancelButtonText: '取消',
        type: 'info'
      }
    );

    batchUpdating.value = true;
    const ids = selectedRows.value.map(row => row.id);
    
    await api.put('/properties/batch/status', {
      ids: ids,
      status: batchStatus.value
    });
    
    ElMessage.success(`成功更新 ${ids.length} 个物业状态`);
    
    clearSelection();
    batchStatus.value = '';
    fetchProperties();
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('批量更新失败');
      console.error('Batch update error:', err);
    }
  } finally {
    batchUpdating.value = false;
  }
};

/**
 * 加载物业图片
 */
const loadPropertyImages = async (propertyId) => {
  try {
    const { data } = await api.get(`/api/files/entity/PROPERTY_IMAGE/${propertyId}`);
    form.images = data || [];
    console.log('[PropertiesView] Loaded images:', form.images);
  } catch (err) {
    console.error('[PropertiesView] Failed to load images:', err);
    form.images = [];
  }
};

/**
 * 获取图片预览URL
 */
const getPreviewUrl = (storedFileName) => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  return `${baseUrl}/api/files/preview/${storedFileName}`;
};

/**
 * 设置封面图片
 */
const setCoverImage = async (fileId) => {
  if (!editingId.value) {
    ElMessage.warning('请先保存物业后再设置封面');
    return;
  }
  
  try {
    await api.put(`/api/files/${fileId}/set-cover`, null, {
      params: { propertyId: editingId.value }
    });
    ElMessage.success('封面设置成功');
    await loadPropertyImages(editingId.value);
  } catch (err) {
    ElMessage.error('封面设置失败');
    console.error('[PropertiesView] Failed to set cover:', err);
  }
};

/**
 * 图片上传成功回调
 */
const handleImageUploadSuccess = (file) => {
  console.log('[PropertiesView] Image uploaded:', file);
  ElMessage.success(`图片上传成功: ${file.originalFileName}`);
};

/**
 * 加载物业封面图片
 */
const loadCoverImage = async (propertyId) => {
  try {
    const { data } = await api.get(`/api/files/cover/${propertyId}`);
    return data?.storedFileName || null;
  } catch (err) {
    return null;
  }
};

/**
 * 查看物业详情
 */
const viewDetails = async (property) => {
  try {
    // 加载完整的物业信息和图片
    const { data: images } = await api.get(`/api/files/entity/PROPERTY_IMAGE/${property.id}`);
    
    currentProperty.value = {
      ...property,
      images: images || []
    };
    
    detailsVisible.value = true;
  } catch (err) {
    ElMessage.error('加载物业详情失败');
    console.error('[PropertiesView] Failed to load property details:', err);
  }
};

/**
 * 从详情页进入编辑
 */
const editFromDetails = () => {
  detailsVisible.value = false;
  startEdit(currentProperty.value);
};

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-';
  const date = new Date(dateTime);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

onMounted(() => {
  if (!isAdmin && authStore.user) {
    form.ownerId = authStore.user.id;
  }
  fetchProperties();
});
</script>

<style scoped>
.properties-view {
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
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

/* 批量操作样式 */
.batch-actions {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 12px;
  margin-top: 16px;
}

.batch-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-top: 8px;
}

/* 高级筛选样式 */
.advanced-filters {
  margin-top: 16px;
}

.advanced-filter-form .el-form-item {
  margin-bottom: 16px;
}

.advanced-filter-form .el-form-item__label {
  font-weight: 500;
  color: #606266;
}

.address-cell {
  line-height: 1.5;
}

.address-main {
  font-weight: 500;
  color: #303133;
}

.address-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 图片画廊样式 */
.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.image-item {
  position: relative;
  width: 100%;
  height: 120px;
  border: 2px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.image-item:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.image-item.is-cover {
  border-color: #67c23a;
  border-width: 3px;
}

.cover-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 10;
}

/* 物业详情样式 */
.property-details {
  max-height: 70vh;
  overflow-y: auto;
}

.details-images {
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
}

.carousel-cover-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}

.details-section {
  margin-top: 20px;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 60px;
}

/* 详情弹窗滚动条美化 */
.property-details::-webkit-scrollbar {
  width: 8px;
}

.property-details::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.property-details::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.property-details::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
