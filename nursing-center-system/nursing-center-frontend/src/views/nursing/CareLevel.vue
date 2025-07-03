<template>
  <div class="care-level">
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>护理级别管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>护理管理</el-breadcrumb-item>
          <el-breadcrumb-item>护理级别管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="级别名称">
              <el-input v-model="searchForm.levelName" placeholder="请输入级别名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%">
                <el-option label="启用" value="ACTIVE" />
                <el-option label="停用" value="INACTIVE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item>
              <div class="button-group">
                <el-button type="primary" @click="handleSearch">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="handleReset">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
                <el-button type="primary" @click="handleAdd">
                  <el-icon><Plus /></el-icon>
                  新增级别
                </el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="always">      <el-table :data="levelList" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="levelName" label="级别名称" width="150" align="center" />
        <el-table-column prop="levelCode" label="级别编码" width="120" align="center" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip align="left" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      <el-table-column label="操作" width="320" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button size="small" type="primary" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                修改状态
              </el-button>
              <el-button size="small" type="warning" @click="handleConfigItems(row)">
                <el-icon><Setting /></el-icon>
                配置项目
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchLevelList"
          @current-change="fetchLevelList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑/查看对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px">
        <el-form-item label="级别名称" prop="levelName">
          <el-input v-model="formData.levelName" placeholder="请输入级别名称" :disabled="isViewMode || isEdit" />
        </el-form-item>
        <el-form-item label="级别编码" prop="levelCode">
          <el-input v-model="formData.levelCode" placeholder="请输入级别编码" :disabled="isViewMode || isEdit" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status" :disabled="isViewMode">
            <el-radio label="ACTIVE">启用</el-radio>
            <el-radio label="INACTIVE">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" v-if="!isEdit">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            :disabled="isViewMode"
            placeholder="请输入描述" />
        </el-form-item>
        <el-form-item v-if="!isAdd && formData.createTime" label="创建时间">
          <el-input :value="formatDateTime(formData.createTime)" disabled />
        </el-form-item>
        <el-form-item v-if="!isAdd && formData.updateTime" label="更新时间">
          <el-input :value="formatDateTime(formData.updateTime)" disabled />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">
            {{ isViewMode ? '关闭' : '取消' }}
          </el-button>
          <el-button
            v-if="!isViewMode"
            type="primary"
            @click="handleSubmit"
            :loading="submitting">
            {{ isAdd ? '创建' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>    <!-- 护理项目配置对话框 -->
    <el-dialog
      v-model="configDialogVisible"
      title="护理项目配置"
      width="1200px"
      :close-on-click-modal="false">

      <div class="config-content">
        <div class="level-info">
          <el-alert
            :title="`正在为 ${currentLevel?.levelName} 配置护理项目`"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 20px" />
        </div>

        <!-- 已配置的护理项目 -->
        <div class="configured-items">
          <h3>已配置项目</h3>
          <el-table
            :data="configuredItems"
            v-loading="configLoading"
            stripe
            border
            max-height="250"
            style="margin-bottom: 20px">            <el-table-column prop="itemName" label="项目名称" width="150" />
            <el-table-column prop="itemCode" label="项目编码" width="120" />
            <el-table-column label="执行周期" width="120" align="center">
              <template #default="{ row }">
                {{ row.executeCycle }}天
              </template>
            </el-table-column>
            <el-table-column prop="price" label="单价(元)" width="100" align="center">
              <template #default="{ row }">
                ¥{{ row.price?.toLocaleString() || '0' }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="danger"
                  @click="handleRemoveItem(row)">
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 添加护理项目 -->
        <div class="add-items">
          <h3>添加护理项目</h3>
          <div class="item-selector">
            <el-transfer
              v-model="selectedItemIds"
              :data="availableItems"
              :titles="['可选项目', '待添加项目']"
              :props="{
                key: 'id',
                label: 'label',
                disabled: 'disabled'
              }"
              filterable
              :filter-method="filterMethod"
              style="text-align: left">              <template #default="{ option }">
                <div class="transfer-item">
                  <div class="item-header">
                    <span class="item-name">{{ option.itemName }}</span>
                  </div>                  <div class="item-details">
                    <span class="item-price">¥{{ option.price?.toLocaleString() || '0' }}</span>
                  </div>
                </div>
              </template>
            </el-transfer>
          </div>        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="configDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleSaveConfig"
            :loading="configSubmitting"
            :disabled="selectedItemIds.length === 0">
            保存配置
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, View, Edit, Setting } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { careApi, type CareLevel, type CareLevelDTO, type CareLevelItemConfig, type CareItem } from '@/api/care'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const isAdd = ref(false)
const isViewMode = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 搜索表单
const searchForm = reactive({
  levelName: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表格数据
const levelList = ref<CareLevel[]>([])

// 护理项目配置相关
const configDialogVisible = ref(false)
const currentLevel = ref<CareLevel | null>(null)
const configuredItems = ref<CareItem[]>([])
const availableItems = ref<(CareItem & { label: string; disabled: boolean })[]>([])
const selectedItemIds = ref<number[]>([])
const configLoading = ref(false)
const configSubmitting = ref(false)

// 表单数据
const formData = reactive({
  id: null as number | null,
  levelName: '',
  levelCode: '',
  status: 'ACTIVE',
  description: '',
  createTime: '',
  updateTime: ''
})

// 表单验证规则
const formRules: FormRules = {
  levelName: [
    { required: true, message: '请输入级别名称', trigger: 'blur' }
  ],
  levelCode: [
    { required: true, message: '请输入级别编码', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  if (isViewMode.value) return '查看护理级别'
  if (isAdd.value) return '新增护理级别'
  return '修改护理级别'
})

// 工具函数
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 方法
const fetchLevelList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    }
    const response = await careApi.careLevel.list(params)
    console.log('API完整响应:', response)

    // 处理多种可能的后端响应格式
    let records: any[] = []
    let total = 0

    const responseAny = response as any

    if (response.data) {
      // 标准分页响应格式
      records = response.data.records || response.data.content || []
      total = response.data.total || response.data.totalElements || 0
    } else if (Array.isArray(responseAny)) {
      // 直接返回数组格式
      records = responseAny
      total = responseAny.length
    } else if (responseAny.records) {
      // 兼容其他格式
      records = responseAny.records
      total = responseAny.total || 0
    }    // 处理后端返回的数据，转换状态字段
    levelList.value = records.map((item: any) => ({
      id: item.id,
      levelName: item.level_name || item.levelName,
      levelCode: item.level_code || item.levelCode,
      description: item.description || '',
      status: item.status === 1 || item.status === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE',
      createTime: item.create_time || item.createTime,
      updateTime: item.update_time || item.updateTime
    }))

    pagination.total = total
    console.log('处理后的数据:', levelList.value)
    console.log('分页信息:', { total, page: pagination.page, size: pagination.size })
  } catch (error) {
    console.error('获取护理级别列表失败:', error)
    ElMessage.error('获取护理级别列表失败')
    levelList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchLevelList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    levelName: '',
    status: ''
  })
  pagination.page = 1
  fetchLevelList()
}

const handleAdd = () => {
  isAdd.value = true
  isEdit.value = false
  isViewMode.value = false
  resetForm()
  dialogVisible.value = true
}

const handleView = (row: CareLevel) => {
  Object.assign(formData, row)
  isAdd.value = false
  isEdit.value = false
  isViewMode.value = true
  dialogVisible.value = true
}

const handleEdit = (row: CareLevel) => {
  Object.assign(formData, row)
  isAdd.value = false
  isEdit.value = true
  isViewMode.value = false
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (valid) {
      submitForm()
    }
  })
}

const submitForm = async () => {
  submitting.value = true
  try {
    const submitData: CareLevelDTO = {
      id: formData.id || undefined,
      levelName: formData.levelName,
      levelCode: formData.levelCode,
      status: formData.status, // 保持字符串类型：'ACTIVE' 或 'INACTIVE'
      description: formData.description
    }

    if (!isAdd.value && formData.id) {
      await careApi.careLevel.update(submitData)
    } else {
      delete submitData.id
      await careApi.careLevel.create(submitData)
    }

    const action = isAdd.value ? '创建' : '更新'
    ElMessage.success(`${action}成功`)
    dialogVisible.value = false
    fetchLevelList()
  } catch (error) {
    const action = isAdd.value ? '创建' : '更新'
    console.error(`${action}失败:`, error)
    ElMessage.error(`${action}失败`)
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  Object.assign(formData, {
    id: null,
    levelName: '',
    levelCode: '',
    status: 'ACTIVE',
    description: '',
    createTime: '',
    updateTime: ''
  })

  // 清除表单验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 护理项目配置相关函数
const handleConfigItems = async (row: CareLevel) => {
  currentLevel.value = row
  configDialogVisible.value = true
  await loadConfigData()
}

// 加载配置数据
const loadConfigData = async () => {
  if (!currentLevel.value) return

  configLoading.value = true
  try {
    const [configuredResponse, availableResponse] = await Promise.all([
      careApi.careLevel.getItemConfigs(currentLevel.value.id),
      careApi.careItem.list({ status: 1, pageNum: 1, pageSize: 1000 })
    ])

    // 配置的护理项目
    const configuredItemConfigs = configuredResponse || []
    console.log('已配置的护理项目配置:', configuredItemConfigs)
    configuredItems.value = configuredItemConfigs.map(config => config.careItem)
    const configuredItemIds = configuredItems.value.map(item => item.id)
    console.log('已配置的护理项目IDs:', configuredItemIds)
      
    // 可选择的护理项目
    console.log('护理项目API响应:', availableResponse)
    // 检查响应结构 - 可能是直接返回分页对象，也可能是包装在data中
    const allItems = availableResponse?.records || availableResponse?.data?.records || []
    console.log('所有护理项目:', allItems)
    availableItems.value = allItems
      .filter((item: CareItem) => !configuredItemIds.includes(item.id))
      .map((item: CareItem) => ({
        ...item,
        label: `${item.itemName} - ¥${item.price}`,
        disabled: false
      }))
    console.log('可选择的护理项目:', availableItems.value)

    selectedItemIds.value = []
  } catch (error) {
    console.error('加载配置数据失败:', error)
    ElMessage.error('加载配置数据失败')
  } finally {
    configLoading.value = false
  }
}

// 移除项目
const handleRemoveItem = async (careItem: CareItem) => {
  if (!currentLevel.value) return

  try {
    await ElMessageBox.confirm(
      `确定要移除护理项目"${careItem.itemName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await careApi.careLevel.removeItem(currentLevel.value.id, careItem.id)
    ElMessage.success('移除成功')
    await loadConfigData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('移除项目失败:', error)
      ElMessage.error('移除项目失败')
    }
  }
}

// 保存配置
const handleSaveConfig = async () => {
  if (!currentLevel.value || selectedItemIds.value.length === 0) {
    return
  }

  configSubmitting.value = true
  try {
    await careApi.careLevel.addItems({
      careLevelId: currentLevel.value.id,
      careItemIds: selectedItemIds.value
    })

    ElMessage.success('配置保存成功')
    await loadConfigData()
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败')
  } finally {
    configSubmitting.value = false
  }
}

// 过滤方法
const filterMethod = (query: string, item: any) => {
  const searchText = query.toLowerCase()
  return item.itemName.toLowerCase().includes(searchText) ||
         item.itemCode.toLowerCase().includes(searchText) ||
         item.description.toLowerCase().includes(searchText)
}

// 生命周期
onMounted(() => {
  fetchLevelList()
})
</script>

<style scoped>
.care-level {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

.page-header-card {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(8px);
}

.page-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-header h2 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 22px;
  font-weight: 600;
}

.search-card {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(8px);
  padding-bottom: 2px;
}

.button-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.table-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(8px);
  padding-top: 8px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

/* 护理项目配置样式 */
.config-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.level-info {
  margin-bottom: 10px;
}

.configured-items h3,
.add-items h3 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.item-selector {
  width: 100%;
}

:deep(.el-transfer) {
  display: flex;
  justify-content: center;
  gap: 20px;
}

:deep(.el-transfer-panel) {
  width: 250px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.transfer-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 4px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 4px;
}

.item-header {
  display: flex;
  align-items: center;
}

.item-name {
  font-weight: 600;
  color: #303133;
  font-size: 13px;
  line-height: 1.2;
  word-break: break-word;
}

.item-details {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

.item-price {
  color: #e6a23c;
  font-weight: 600;
  font-size: 11px;
}

/* 操作按钮样式优化 */
.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.action-buttons .el-button {
  margin: 0;
  min-width: 80px;
  font-size: 12px;
}

/* 对话框内容优化 */
.config-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.configured-items {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.add-items {
  background: #fff3cd;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #ffeaa7;
}

/* Transfer组件样式优化 */
:deep(.el-transfer) {
  display: flex !important;
  justify-content: center !important;
  gap: 30px !important;
}

:deep(.el-transfer-panel) {
  width: 320px !important;
  height: 400px !important;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.el-transfer-panel__header) {
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
  padding: 12px 15px;
}

:deep(.el-transfer-panel__body) {
  padding: 8px;
}

:deep(.el-transfer-panel__list) {
  height: 300px !important;
  overflow-y: auto;
}

:deep(.el-transfer-panel__item) {
  padding: 6px 8px !important;
  margin: 2px 0 !important;
  border-radius: 4px;
  transition: background-color 0.2s;
}

:deep(.el-transfer-panel__item:hover) {
  background-color: #f5f7fa;
}
</style>
