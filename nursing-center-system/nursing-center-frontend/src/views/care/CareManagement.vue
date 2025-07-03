<template>
  <div class="care-management">
    <div class="page-header">
      <h2>护理管理</h2>
    </div>

    <el-tabs v-model="activeTab" class="care-tabs">
      <!-- 护理等级管理 -->
      <el-tab-pane label="护理等级" name="level">
        <div class="tab-content">
          <div class="section-header">
            <el-button type="primary" @click="handleAddLevel">
              <el-icon><Plus /></el-icon>
              新增等级
            </el-button>
          </div>

          <el-table :data="careLevelList" v-loading="levelLoading" stripe>
            <el-table-column prop="levelName" label="等级名称" />
            <el-table-column prop="levelCode" label="等级编码" />
            <el-table-column prop="baseFee" label="基础费用(元/月)">
              <template #default="{ row }">
                ¥{{ row.baseFee.toLocaleString() }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="handleViewLevel(row)">查看</el-button>
                <el-button size="small" type="primary" @click="handleEditLevel(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteLevel(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="levelPagination.page"
              v-model:page-size="levelPagination.size"
              :total="levelPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchCareLevels"
              @current-change="fetchCareLevels"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 护理项目管理 -->
      <el-tab-pane label="护理项目" name="item">
        <div class="tab-content">
          <div class="section-header">
            <div class="search-bar">
              <el-form :model="itemSearchForm" inline>
                <el-form-item label="项目名称">
                  <el-input v-model="itemSearchForm.itemName" placeholder="请输入项目名称" clearable />
                </el-form-item>
                <el-form-item label="护理类型">
                  <el-select v-model="itemSearchForm.careType" placeholder="请选择护理类型" clearable>
                    <el-option label="日常护理" value="DAILY" />
                    <el-option label="专业护理" value="PROFESSIONAL" />
                    <el-option label="医疗护理" value="MEDICAL" />
                    <el-option label="康复护理" value="REHABILITATION" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearchItems">查询</el-button>
                  <el-button @click="handleResetItemSearch">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            <el-button type="primary" @click="handleAddItem">
              <el-icon><Plus /></el-icon>
              新增项目
            </el-button>
          </div>

          <el-table :data="careItemList" v-loading="itemLoading" stripe>
            <el-table-column prop="itemName" label="项目名称" />
            <el-table-column prop="itemCode" label="项目编码" />
            <el-table-column prop="careType" label="护理类型">
              <template #default="{ row }">
                {{ formatCareType(row.careType) }}
              </template>
            </el-table-column>
            <el-table-column prop="unitPrice" label="单价(元)">
              <template #default="{ row }">
                ¥{{ row.unitPrice.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="handleViewItem(row)">查看</el-button>
                <el-button size="small" type="primary" @click="handleEditItem(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteItem(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="itemPagination.page"
              v-model:page-size="itemPagination.size"
              :total="itemPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchCareItems"
              @current-change="fetchCareItems"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 客户护理配置 -->
      <el-tab-pane label="客户护理配置" name="config">
        <div class="tab-content">
          <div class="section-header">
            <div class="search-bar">
              <el-form :model="configSearchForm" inline>
                <el-form-item label="客户姓名">
                  <el-input v-model="configSearchForm.customerName" placeholder="请输入客户姓名" clearable />
                </el-form-item>
                <el-form-item label="护理等级">
                  <el-select v-model="configSearchForm.careLevelId" placeholder="请选择护理等级" clearable>
                    <el-option 
                      v-for="level in careLevelOptions"
                      :key="level.id"
                      :label="level.levelName"
                      :value="level.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearchConfigs">查询</el-button>
                  <el-button @click="handleResetConfigSearch">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            <el-button type="primary" @click="handleAddConfig">
              <el-icon><Plus /></el-icon>
              新增配置
            </el-button>
          </div>

          <el-table :data="customerCareList" v-loading="configLoading" stripe>
            <el-table-column prop="customerName" label="客户姓名" />
            <el-table-column prop="careLevelName" label="护理等级" />
            <el-table-column prop="effectiveDate" label="生效日期" />
            <el-table-column label="护理项目">
              <template #default="{ row }">
                <el-tag 
                  v-for="item in row.careItems.slice(0, 3)" 
                  :key="item.id"
                  size="small"
                  class="mr-1"
                >
                  {{ item.itemName }}
                </el-tag>
                <span v-if="row.careItems.length > 3" class="text-gray-500">
                  等{{ row.careItems.length }}项
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="handleViewConfig(row)">查看</el-button>
                <el-button size="small" type="primary" @click="handleEditConfig(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteConfig(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="configPagination.page"
              v-model:page-size="configPagination.size"
              :total="configPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchCustomerCares"
              @current-change="fetchCustomerCares"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 护理等级编辑对话框 -->
    <CareLevelDialog
      v-model="levelDialogVisible"
      :mode="levelDialogMode"
      :data="selectedLevel"
      @success="handleLevelSuccess"
    />

    <!-- 护理项目编辑对话框 -->
    <CareItemDialog
      v-model="itemDialogVisible"
      :mode="itemDialogMode"
      :data="selectedItem"
      @success="handleItemSuccess"
    />

    <!-- 客户护理配置对话框 -->
    <CustomerCareDialog
      v-model="configDialogVisible"
      :mode="configDialogMode"
      :data="selectedConfig"
      :care-levels="careLevelOptions"
      @success="handleConfigSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { careApi, type CareLevel, type CareItem, type CustomerCareConfig } from '@/api/care'
import CareLevelDialog from './components/CareLevelDialog.vue'
import CareItemDialog from './components/CareItemDialog.vue'
import CustomerCareDialog from './components/CustomerCareDialog.vue'

// 当前激活的标签页
const activeTab = ref('level')

// 护理等级相关
const careLevelList = ref<CareLevel[]>([])
const careLevelOptions = ref<CareLevel[]>([])
const levelLoading = ref(false)
const levelPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 护理项目相关
const careItemList = ref<CareItem[]>([])
const itemLoading = ref(false)
const itemPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const itemSearchForm = reactive({
  itemName: '',
  careType: ''
})

// 客户护理配置相关
const customerCareList = ref<CustomerCareConfig[]>([])
const configLoading = ref(false)
const configPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const configSearchForm = reactive({
  customerName: '',
  careLevelId: undefined as number | undefined
})

// 对话框相关
const levelDialogVisible = ref(false)
const levelDialogMode = ref<'add' | 'edit' | 'view'>('add')
const selectedLevel = ref<CareLevel | null>(null)

const itemDialogVisible = ref(false)
const itemDialogMode = ref<'add' | 'edit' | 'view'>('add')
const selectedItem = ref<CareItem | null>(null)

const configDialogVisible = ref(false)
const configDialogMode = ref<'add' | 'edit' | 'view'>('add')
const selectedConfig = ref<CustomerCareConfig | null>(null)

// 获取护理等级列表
const fetchCareLevels = async () => {
  try {
    levelLoading.value = true
    const response = await careApi.careLevel.list({
      page: levelPagination.page,
      size: levelPagination.size
    })
    
    if (response.data) {
      careLevelList.value = response.data.records
      levelPagination.total = response.data.total
    }
  } catch (error) {
    ElMessage.error('获取护理等级列表失败')
  } finally {
    levelLoading.value = false
  }
}

// 获取护理项目列表
const fetchCareItems = async () => {
  try {
    itemLoading.value = true
    const response = await careApi.careItem.list({
      page: itemPagination.page,
      size: itemPagination.size,
      ...itemSearchForm
    })
    
    if (response.data) {
      careItemList.value = response.data.records
      itemPagination.total = response.data.total
    }
  } catch (error) {
    ElMessage.error('获取护理项目列表失败')
  } finally {
    itemLoading.value = false
  }
}

// 获取客户护理配置列表
const fetchCustomerCares = async () => {
  try {
    configLoading.value = true
    const response = await careApi.getCustomerCare.list({
      page: configPagination.page,
      size: configPagination.size,
      ...configSearchForm
    })
    
    if (response.data) {
      customerCareList.value = response.data.records
      configPagination.total = response.data.total
    }
  } catch (error) {
    ElMessage.error('获取客户护理配置列表失败')
  } finally {
    configLoading.value = false
  }
}

// 获取护理等级选项
const fetchCareLevelOptions = async () => {
  try {
    const response = await careApi.getCareLevel.getAll()
    careLevelOptions.value = response || []
  } catch (error) {
    console.error('获取护理等级选项失败:', error)
  }
}

// 护理等级操作
const handleAddLevel = () => {
  selectedLevel.value = null
  levelDialogMode.value = 'add'
  levelDialogVisible.value = true
}

const handleViewLevel = (level: CareLevel) => {
  selectedLevel.value = level
  levelDialogMode.value = 'view'
  levelDialogVisible.value = true
}

const handleEditLevel = (level: CareLevel) => {
  selectedLevel.value = level
  levelDialogMode.value = 'edit'
  levelDialogVisible.value = true
}

const handleDeleteLevel = async (level: CareLevel) => {
  try {
    await ElMessageBox.confirm(`确定要删除护理等级"${level.levelName}"吗？`, '确认删除', {
      type: 'warning'
    })
    
    await careApi.getCareLevel.delete(level.id)
    ElMessage.success('删除成功')
    fetchCareLevels()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleLevelSuccess = () => {
  fetchCareLevels()
  fetchCareLevelOptions()
}

// 护理项目操作
const handleAddItem = () => {
  selectedItem.value = null
  itemDialogMode.value = 'add'
  itemDialogVisible.value = true
}

const handleViewItem = (item: CareItem) => {
  selectedItem.value = item
  itemDialogMode.value = 'view'
  itemDialogVisible.value = true
}

const handleEditItem = (item: CareItem) => {
  selectedItem.value = item
  itemDialogMode.value = 'edit'
  itemDialogVisible.value = true
}

const handleDeleteItem = async (item: CareItem) => {
  try {
    await ElMessageBox.confirm(`确定要删除护理项目"${item.itemName}"吗？`, '确认删除', {
      type: 'warning'
    })
    
    await careApi.getCareItem.delete(item.id)
    ElMessage.success('删除成功')
    fetchCareItems()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleItemSuccess = () => {
  fetchCareItems()
}

const handleSearchItems = () => {
  itemPagination.page = 1
  fetchCareItems()
}

const handleResetItemSearch = () => {
  Object.assign(itemSearchForm, {
    itemName: '',
    careType: ''
  })
  itemPagination.page = 1
  fetchCareItems()
}

// 客户护理配置操作
const handleAddConfig = () => {
  selectedConfig.value = null
  configDialogMode.value = 'add'
  configDialogVisible.value = true
}

const handleViewConfig = (config: CustomerCareConfig) => {
  selectedConfig.value = config
  configDialogMode.value = 'view'
  configDialogVisible.value = true
}

const handleEditConfig = (config: CustomerCareConfig) => {
  selectedConfig.value = config
  configDialogMode.value = 'edit'
  configDialogVisible.value = true
}

const handleDeleteConfig = async (config: CustomerCareConfig) => {
  try {
    await ElMessageBox.confirm(`确定要删除客户"${config.customerName}"的护理配置吗？`, '确认删除', {
      type: 'warning'
    })
    
    await careApi.getCustomerCare.delete(config.id)
    ElMessage.success('删除成功')
    fetchCustomerCares()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleConfigSuccess = () => {
  fetchCustomerCares()
}

const handleSearchConfigs = () => {
  configPagination.page = 1
  fetchCustomerCares()
}

const handleResetConfigSearch = () => {
  Object.assign(configSearchForm, {
    customerName: '',
    careLevelId: undefined
  })
  configPagination.page = 1
  fetchCustomerCares()
}

// 工具函数
const formatCareType = (type: string) => {
  const typeMap: Record<string, string> = {
    'DAILY': '日常护理',
    'PROFESSIONAL': '专业护理',
    'MEDICAL': '医疗护理',
    'REHABILITATION': '康复护理'
  }
  return typeMap[type] || type
}

// 初始化
onMounted(() => {
  fetchCareLevels()
  fetchCareItems()
  fetchCustomerCares()
  fetchCareLevelOptions()
})
</script>

<style scoped>
.care-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.care-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.tab-content {
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  flex: 1;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.mr-1 {
  margin-right: 4px;
}

.text-gray-500 {
  color: #909399;
  font-size: 12px;
}
</style>
