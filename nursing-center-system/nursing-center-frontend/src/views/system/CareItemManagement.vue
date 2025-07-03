<template>
  <div class="care-item-management">
    <!-- 页面头部 -->
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>护理项目管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>系统管理</el-breadcrumb-item>
          <el-breadcrumb-item>护理项目管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>

    <!-- 查询区域 -->
    <el-card class="search-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>护理项目查询</span>
          <el-button type="primary" @click="showAddDialog" :icon="Plus">
            新增护理项目
          </el-button>
        </div>
      </template>

      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="项目名称">
            <el-input
              v-model="searchForm.keyword"
              placeholder="请输入项目名称"
              style="width: 200px"
              clearable
            />
          </el-form-item>
          <el-form-item label="项目分类">
            <el-select
              v-model="searchForm.category"
              placeholder="请选择分类"
              style="width: 150px"
              clearable
            >
              <el-option label="基础护理" value="BASIC" />
              <el-option label="专业护理" value="PROFESSIONAL" />
              <el-option label="生活护理" value="LIFE" />
              <el-option label="康复护理" value="REHABILITATION" />
              <el-option label="心理护理" value="PSYCHOLOGICAL" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              style="width: 120px"
              clearable
            >
              <el-option label="启用" value="true" />
              <el-option label="禁用" value="false" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 护理项目列表 -->
      <el-table
        :data="careItemList"
        v-loading="loading"
        stripe
        border
        style="width: 100%">
        <el-table-column prop="itemName" label="项目名称" width="150" align="center" />
        <el-table-column prop="description" label="项目描述" min-width="200" align="center" />
        <el-table-column label="项目分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)" size="small">
              {{ getCategoryText(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="服务时长(分钟)" width="130" align="center">
          <template #default="{ row }">
            <span>{{ row.duration || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格(元)" width="120" align="center">
          <template #default="{ row }">
            <span class="price-text">{{ row.price ? `¥${row.price.toFixed(2)}` : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.isActive"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)">
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="searchForm.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      :close-on-click-modal="false">
      <el-form
        :model="careItemForm"
        :rules="careItemRules"
        ref="careItemFormRef"
        label-width="120px">
        <el-form-item label="项目名称" prop="itemName">
          <el-input
            v-model="careItemForm.itemName"
            placeholder="请输入项目名称"
          />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="careItemForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="项目分类" prop="category">
          <el-select
            v-model="careItemForm.category"
            placeholder="请选择项目分类"
            style="width: 100%">
            <el-option label="基础护理" value="BASIC" />
            <el-option label="专业护理" value="PROFESSIONAL" />
            <el-option label="生活护理" value="LIFE" />
            <el-option label="康复护理" value="REHABILITATION" />
            <el-option label="心理护理" value="PSYCHOLOGICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务时长(分钟)" prop="duration">
          <el-input-number
            v-model="careItemForm.duration"
            :min="1"
            :max="480"
            placeholder="请输入服务时长"
            style="width: 200px"
          />
          <span class="form-tip">单位：分钟，最长8小时</span>
        </el-form-item>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number
            v-model="careItemForm.price"
            :min="0"
            :precision="2"
            placeholder="请输入价格"
            style="width: 200px"
          />
          <span class="form-tip">单次服务价格，可为空</span>
        </el-form-item>
        <el-form-item label="状态" prop="isActive">
          <el-radio-group v-model="careItemForm.isActive">
            <el-radio :label="true">启用</el-radio>
            <el-radio :label="false">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleConfirm"
          :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { systemApi } from '@/api/system'
import type { CareItem } from '@/api/system'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

// 分页当前页（前端显示用，从1开始）
const currentPage = ref(1)

// 表单数据
const searchForm = reactive({
  keyword: '',
  category: '',
  status: '',
  page: 0,
  size: 10
})

const careItemForm = reactive({
  id: 0,
  itemName: '',
  description: '',
  category: '',
  duration: 0,
  price: 0,
  isActive: true
})

// 表单验证规则
const careItemRules = {
  itemName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 30, message: '项目名称长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择项目分类', trigger: 'change' }
  ],
  duration: [
    { type: 'number', min: 1, max: 480, message: '服务时长在1-480分钟之间', trigger: 'blur' }
  ],
  price: [
    { type: 'number', min: 0, message: '价格必须大于等于0', trigger: 'blur' }
  ]
}

// 数据列表
const careItemList = ref<CareItem[]>([])
const total = ref(0)

// 表单引用
const careItemFormRef = ref()

// 计算属性
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑护理项目' : '新增护理项目'
})

// 方法
const handleSearch = async () => {
  searchForm.page = 0
  currentPage.value = 1
  await fetchCareItemList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    category: '',
    status: '',
    page: 0,
    size: 10
  })
  currentPage.value = 1
  fetchCareItemList()
}

const handleSizeChange = (val: number) => {
  searchForm.size = val
  searchForm.page = 0
  currentPage.value = 1
  fetchCareItemList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  searchForm.page = val - 1
  fetchCareItemList()
}

// 获取护理项目列表
const fetchCareItemList = async () => {
  try {
    loading.value = true
    console.log('查询参数:', searchForm)
    
    const response = await systemApi.getCareItems()
    console.log('护理项目列表响应:', response)
    
    // 处理响应数据 - 新接口直接返回数组
    careItemList.value = response || []
    total.value = careItemList.value.length
  } catch (error) {
    console.error('获取护理项目列表失败:', error)
    ElMessage.error('获取护理项目列表失败')
  } finally {
    loading.value = false
  }
}

// 显示新增对话框
const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑护理项目
const handleEdit = (careItem: CareItem) => {
  isEdit.value = true
  Object.assign(careItemForm, {
    id: careItem.id,
    itemName: careItem.itemName,
    description: careItem.description,
    category: careItem.category,
    duration: careItem.duration,
    price: careItem.price,
    isActive: careItem.isActive
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(careItemForm, {
    id: 0,
    itemName: '',
    description: '',
    category: '',
    duration: 0,
    price: 0,
    isActive: true
  })
  careItemFormRef.value?.clearValidate()
}

// 状态切换
const handleStatusChange = async (careItem: CareItem) => {
  try {
    await systemApi.updateCareItem({ ...careItem })
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
    // 恢复原状态
    careItem.isActive = !careItem.isActive
  }
}

// 确认提交
const handleConfirm = async () => {
  try {
    await careItemFormRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      await systemApi.updateCareItem(careItemForm)
      ElMessage.success('护理项目更新成功')
    } else {
      await systemApi.createCareItem(careItemForm)
      ElMessage.success('护理项目创建成功')
    }

    dialogVisible.value = false
    await fetchCareItemList()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 删除护理项目
const handleDelete = async (careItem: CareItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除护理项目 ${careItem.itemName} 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await systemApi.deleteCareItem(careItem.id)
    ElMessage.success('护理项目删除成功')
    await fetchCareItemList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除护理项目失败:', error)
      ElMessage.error('删除护理项目失败')
    }
  }
}

// 工具方法
const getCategoryType = (category: string) => {
  const categoryTypes: Record<string, string> = {
    'BASIC': 'primary',
    'PROFESSIONAL': 'success',
    'LIFE': 'info',
    'REHABILITATION': 'warning',
    'PSYCHOLOGICAL': 'danger'
  }
  return categoryTypes[category] || 'primary'
}

const getCategoryText = (category: string) => {
  const categoryTexts: Record<string, string> = {
    'BASIC': '基础护理',
    'PROFESSIONAL': '专业护理',
    'LIFE': '生活护理',
    'REHABILITATION': '康复护理',
    'PSYCHOLOGICAL': '心理护理'
  }
  return categoryTexts[category] || '未知'
}

const formatDateTime = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCareItemList()
})
</script>

<style scoped>
.care-item-management {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

.page-header-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-weight: 600;
}

.search-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.price-text {
  color: #e6a23c;
  font-weight: 600;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

:deep(.el-table) {
  background: rgba(255, 255, 255, 0.9);
}

:deep(.el-table th) {
  background: rgba(64, 158, 255, 0.1);
}

:deep(.el-pagination) {
  background: rgba(255, 255, 255, 0.9);
  padding: 15px;
  border-radius: 8px;
}

:deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}
</style>
