<template>
  <div class="care-level-management">
    <!-- 页面头部 -->
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>护理级别管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>系统管理</el-breadcrumb-item>
          <el-breadcrumb-item>护理级别管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>

    <!-- 查询区域 -->
    <el-card class="search-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>护理级别查询</span>
          <el-button type="primary" @click="showAddDialog" :icon="Plus">
            新增护理级别
          </el-button>
        </div>
      </template>

      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="级别名称">
            <el-input
              v-model="searchForm.keyword"
              placeholder="请输入级别名称"
              style="width: 200px"
              clearable
            />
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

      <!-- 护理级别列表 -->
      <el-table
        :data="careLevelList"
        v-loading="loading"
        stripe
        border
        style="width: 100%">
        <el-table-column prop="levelName" label="级别名称" width="150" align="center" />
        <el-table-column prop="description" label="级别描述" min-width="200" align="center" />
        <el-table-column prop="price" label="价格(元/月)" width="120" align="center">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="服务项目" min-width="300" align="center">
          <template #default="{ row }">
            <div class="service-items">
              <el-tag
                v-for="item in row.serviceItems"
                :key="item"
                size="small"
                type="success"
                class="service-tag">
                {{ item }}
              </el-tag>
            </div>
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
      width="700px"
      :close-on-click-modal="false">
      <el-form
        :model="careLevelForm"
        :rules="careLevelRules"
        ref="careLevelFormRef"
        label-width="100px">
        <el-form-item label="级别名称" prop="levelName">
          <el-input
            v-model="careLevelForm.levelName"
            placeholder="请输入级别名称"
          />
        </el-form-item>
        <el-form-item label="级别描述" prop="description">
          <el-input
            v-model="careLevelForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入级别描述"
          />
        </el-form-item>
        <el-form-item label="价格(元/月)" prop="price">
          <el-input-number
            v-model="careLevelForm.price"
            :min="0"
            :precision="2"
            placeholder="请输入价格"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="服务项目" prop="serviceItems">
          <div class="service-items-input">
            <div class="service-item-list">
              <el-tag
                v-for="(item, index) in careLevelForm.serviceItems"
                :key="index"
                closable
                @close="removeServiceItem(index)"
                class="service-item-tag">
                {{ item }}
              </el-tag>
            </div>
            <div class="add-service-item">
              <el-input
                v-model="newServiceItem"
                placeholder="请输入服务项目"
                @keyup.enter="addServiceItem"
                style="width: 200px"
              />
              <el-button @click="addServiceItem" :disabled="!newServiceItem.trim()">
                添加
              </el-button>
            </div>
          </div>
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
import type { CareLevel } from '@/api/system'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const newServiceItem = ref('')

// 分页当前页（前端显示用，从1开始）
const currentPage = ref(1)

// 表单数据
const searchForm = reactive({
  keyword: '',
  page: 0,
  size: 10
})

const careLevelForm = reactive({
  id: 0,
  levelName: '',
  description: '',
  price: 0,
  serviceItems: [] as string[]
})

// 表单验证规则
const careLevelRules = {
  levelName: [
    { required: true, message: '请输入级别名称', trigger: 'blur' },
    { min: 2, max: 20, message: '级别名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格必须大于等于0', trigger: 'blur' }
  ],
  serviceItems: [
    { required: true, message: '请添加至少一个服务项目', trigger: 'change' }
  ]
}

// 数据列表
const careLevelList = ref<CareLevel[]>([])
const total = ref(0)

// 表单引用
const careLevelFormRef = ref()

// 计算属性
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑护理级别' : '新增护理级别'
})

// 方法
const handleSearch = async () => {
  searchForm.page = 0
  currentPage.value = 1
  await fetchCareLevelList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    page: 0,
    size: 10
  })
  currentPage.value = 1
  fetchCareLevelList()
}

const handleSizeChange = (val: number) => {
  searchForm.size = val
  searchForm.page = 0
  currentPage.value = 1
  fetchCareLevelList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  searchForm.page = val - 1
  fetchCareLevelList()
}

// 获取护理级别列表
const fetchCareLevelList = async () => {
  try {
    loading.value = true
    console.log('查询参数:', searchForm)
    
    const response = await systemApi.getCareLevels()
    console.log('护理级别列表响应:', response)
    
    // 处理响应数据 - 新接口直接返回数组
    careLevelList.value = response || []
    total.value = careLevelList.value.length
  } catch (error) {
    console.error('获取护理级别列表失败:', error)
    ElMessage.error('获取护理级别列表失败')
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

// 编辑护理级别
const handleEdit = (careLevel: CareLevel) => {
  isEdit.value = true
  Object.assign(careLevelForm, {
    id: careLevel.id,
    levelName: careLevel.levelName,
    description: careLevel.description,
    price: careLevel.price,
    serviceItems: [...(careLevel.serviceItems || [])]
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(careLevelForm, {
    id: 0,
    levelName: '',
    description: '',
    price: 0,
    serviceItems: []
  })
  newServiceItem.value = ''
  careLevelFormRef.value?.clearValidate()
}

// 添加服务项目
const addServiceItem = () => {
  const item = newServiceItem.value.trim()
  if (item && !careLevelForm.serviceItems.includes(item)) {
    careLevelForm.serviceItems.push(item)
    newServiceItem.value = ''
  }
}

// 移除服务项目
const removeServiceItem = (index: number) => {
  careLevelForm.serviceItems.splice(index, 1)
}

// 确认提交
const handleConfirm = async () => {
  try {
    await careLevelFormRef.value.validate()
    
    if (careLevelForm.serviceItems.length === 0) {
      ElMessage.error('请添加至少一个服务项目')
      return
    }
    
    submitLoading.value = true

    if (isEdit.value) {
      await systemApi.updateCareLevel(careLevelForm)
      ElMessage.success('护理级别更新成功')
    } else {
      await systemApi.createCareLevel(careLevelForm)
      ElMessage.success('护理级别创建成功')
    }

    dialogVisible.value = false
    await fetchCareLevelList()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 删除护理级别
const handleDelete = async (careLevel: CareLevel) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除护理级别 ${careLevel.levelName} 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await systemApi.deleteCareLevel(careLevel.id)
    ElMessage.success('护理级别删除成功')
    await fetchCareLevelList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除护理级别失败:', error)
      ElMessage.error('删除护理级别失败')
    }
  }
}

// 工具方法
const formatDateTime = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCareLevelList()
})
</script>

<style scoped>
.care-level-management {
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

.service-items {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.service-tag {
  margin: 2px;
}

.service-items-input {
  width: 100%;
}

.service-item-list {
  margin-bottom: 10px;
  min-height: 40px;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
}

.service-item-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.add-service-item {
  display: flex;
  gap: 10px;
  align-items: center;
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
