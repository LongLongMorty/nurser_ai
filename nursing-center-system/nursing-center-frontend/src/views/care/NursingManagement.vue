<template>
  <div class="nursing-management">
    <div class="page-header">
      <h2>护理管理</h2>
    </div>

    <el-tabs v-model="activeTab" class="management-tabs">
      <!-- 护理级别管理 -->
      <el-tab-pane label="护理级别管理" name="level">
        <div class="tab-content">
          <!-- 搜索区域 -->
          <div class="search-form">
            <el-form :model="levelSearchForm" inline>
              <el-form-item label="级别名称">
                <el-input v-model="levelSearchForm.levelName" placeholder="请输入级别名称" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="levelSearchForm.status" placeholder="请选择状态" clearable>
                  <el-option label="启用" value="1" />
                  <el-option label="停用" value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearchLevels">查询</el-button>
                <el-button @click="handleResetLevelSearch">重置</el-button>
                <el-button type="primary" @click="handleAddLevel">
                  <el-icon><Plus /></el-icon>
                  新增级别
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="careLevelList" v-loading="levelLoading" stripe>
            <el-table-column prop="levelName" label="级别名称" />
            <el-table-column prop="levelCode" label="级别编码" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280">
              <template #default="{ row }">
                <el-button size="small" @click="handleViewLevel(row)">查看</el-button>
                <el-button size="small" type="primary" @click="handleEditLevel(row)">编辑</el-button>
                <el-button size="small" type="warning" @click="handleConfigureItems(row)">配置项目</el-button>
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
      <el-tab-pane label="护理项目管理" name="item">
        <div class="tab-content">
          <!-- 搜索区域 -->
          <div class="search-form">
            <el-form :model="itemSearchForm" inline>
              <el-form-item label="项目名称">
                <el-input v-model="itemSearchForm.itemName" placeholder="请输入项目名称" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="itemSearchForm.status" placeholder="请选择状态" clearable>
                  <el-option label="启用" value="1" />
                  <el-option label="停用" value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearchItems">查询</el-button>
                <el-button @click="handleResetItemSearch">重置</el-button>
                <el-button type="primary" @click="handleAddItem">
                  <el-icon><Plus /></el-icon>
                  新增项目
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="careItemList" v-loading="itemLoading" stripe>
            <el-table-column prop="itemCode" label="项目编号" width="120" />
            <el-table-column prop="itemName" label="项目名称" />
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                ¥{{ row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="executeCycle" label="执行周期(天)" width="120" />
            <el-table-column prop="executeTimes" label="执行次数" width="100" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '停用' }}
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

      <!-- 客户护理设置 -->
      <el-tab-pane label="客户护理设置" name="customer">
        <div class="tab-content">
          <!-- 客户搜索 -->
          <div class="search-form">
            <el-form :model="customerSearchForm" inline>
              <el-form-item label="客户姓名">
                <el-input v-model="customerSearchForm.customerName" placeholder="请输入客户姓名" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearchCustomers">查询</el-button>
                <el-button @click="handleResetCustomerSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="customerList" v-loading="customerLoading" stripe>
            <el-table-column prop="customerName" label="客户姓名" />
            <el-table-column prop="careLevelName" label="护理级别" />
            <el-table-column prop="roomInfo" label="房间信息" />
            <el-table-column prop="healthManagerName" label="健康管家" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="handleSetCareLevel(row)">设置护理级别</el-button>
                <el-button size="small" type="success" @click="handleAddCareItems(row)">添加护理项目</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="customerPagination.page"
              v-model:page-size="customerPagination.size"
              :total="customerPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchCustomers"
              @current-change="fetchCustomers"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 护理记录管理 -->
      <el-tab-pane label="护理记录管理" name="record">
        <div class="tab-content">
          <!-- 搜索区域 -->
          <div class="search-form">
            <el-form :model="recordSearchForm" inline>
              <el-form-item label="客户姓名">
                <el-input v-model="recordSearchForm.customerName" placeholder="请输入客户姓名" />
              </el-form-item>
              <el-form-item label="护理时间">
                <el-date-picker
                  v-model="recordSearchForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearchRecords">查询</el-button>
                <el-button @click="handleResetRecordSearch">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="recordList" v-loading="recordLoading" stripe>
            <el-table-column prop="customerName" label="客户姓名" />
            <el-table-column prop="itemName" label="护理项目" />
            <el-table-column prop="healthManagerName" label="健康管家" />
            <el-table-column prop="careTime" label="护理时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.careTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="careQuantity" label="护理数量" width="100" />
            <el-table-column prop="careContent" label="护理内容" show-overflow-tooltip />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="handleDeleteRecord(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="recordPagination.page"
              v-model:page-size="recordPagination.size"
              :total="recordPagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchRecords"
              @current-change="fetchRecords"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 护理级别对话框 -->
    <LevelDialog
      v-model:visible="levelDialogVisible"
      :mode="levelDialogMode"
      :level="selectedLevel"
      @success="handleLevelSuccess"
    />

    <!-- 护理项目对话框 -->
    <ItemDialog
      v-model:visible="itemDialogVisible"
      :mode="itemDialogMode"
      :item="selectedItem"
      @success="handleItemSuccess"
    />

    <!-- 护理级别项目配置对话框 -->
    <LevelItemsDialog
      v-model:visible="levelItemsDialogVisible"
      :level="selectedLevel"
      @success="handleLevelSuccess"
    />

    <!-- 客户护理级别设置对话框 -->
    <CustomerCareLevelDialog
      v-model:visible="careLevelDialogVisible"
      :customer="selectedCustomer"
      @success="handleCustomerSuccess"
    />

    <!-- 客户护理项目添加对话框 -->
    <CustomerCareItemsDialog
      v-model:visible="careItemsDialogVisible"
      :customer="selectedCustomer"
      @success="handleCustomerSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { careApi } from '@/api/care'
import { customerApi } from '@/api/customer'
import type { CareLevel, CareItem, Customer } from '@/api/care'
import LevelDialog from './components/LevelDialog.vue'
import ItemDialog from './components/ItemDialog.vue'
import LevelItemsDialog from './components/LevelItemsDialog.vue'
import CustomerCareLevelDialog from './components/CustomerCareLevelDialog.vue'
import CustomerCareItemsDialog from './components/CustomerCareItemsDialog.vue'

// 当前激活的标签页
const activeTab = ref('level')

// 护理级别管理
const careLevelList = ref<CareLevel[]>([])
const levelLoading = ref(false)
const levelSearchForm = reactive({
  levelName: '',
  status: ''
})
const levelPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 护理项目管理
const careItemList = ref<CareItem[]>([])
const itemLoading = ref(false)
const itemSearchForm = reactive({
  itemName: '',
  status: ''
})
const itemPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 客户管理
const customerList = ref<Customer[]>([])
const customerLoading = ref(false)
const customerSearchForm = reactive({
  customerName: ''
})
const customerPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 护理记录管理
const recordList = ref([])
const recordLoading = ref(false)
const recordSearchForm = reactive({
  customerName: '',
  dateRange: []
})
const recordPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框状态
const levelDialogVisible = ref(false)
const levelDialogMode = ref<'add' | 'edit' | 'view'>('add')
const selectedLevel = ref<CareLevel | null>(null)

const itemDialogVisible = ref(false)
const itemDialogMode = ref<'add' | 'edit' | 'view'>('add')
const selectedItem = ref<CareItem | null>(null)

const levelItemsDialogVisible = ref(false)

const careLevelDialogVisible = ref(false)
const careItemsDialogVisible = ref(false)
const selectedCustomer = ref<Customer | null>(null)

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString()
}

// 获取护理级别列表
const fetchCareLevels = async () => {
  try {
    levelLoading.value = true
    const response = await careApi.careLevel.list({
      page: levelPagination.page,
      size: levelPagination.size,
      ...levelSearchForm
    })
    
    if (response.data) {
      careLevelList.value = response.data.records
      levelPagination.total = response.data.total
    }
  } catch (error) {
    ElMessage.error('获取护理级别列表失败')
  } finally {
    levelLoading.value = false
  }
}

// 护理级别操作
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

const handleConfigureItems = (level: CareLevel) => {
  selectedLevel.value = level
  levelItemsDialogVisible.value = true
}

const handleDeleteLevel = async (level: CareLevel) => {
  try {
    await ElMessageBox.confirm(`确定要删除护理级别"${level.levelName}"吗？`, '确认删除', {
      type: 'warning'
    })
    
    await careApi.careLevel.delete(level.id)
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
}

const handleSearchLevels = () => {
  levelPagination.page = 1
  fetchCareLevels()
}

const handleResetLevelSearch = () => {
  Object.assign(levelSearchForm, {
    levelName: '',
    status: ''
  })
  levelPagination.page = 1
  fetchCareLevels()
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
    
    await careApi.careItem.delete(item.id)
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
    status: ''
  })
  itemPagination.page = 1
  fetchCareItems()
}

// 获取客户列表
const fetchCustomers = async () => {
  try {
    customerLoading.value = true
    const response = await customerApi.getList({
      page: customerPagination.page,
      size: customerPagination.size,
      ...customerSearchForm
    })
    
    if (response.data) {
      customerList.value = response.data.records
      customerPagination.total = response.data.total
    }
  } catch (error) {
    ElMessage.error('获取客户列表失败')
  } finally {
    customerLoading.value = false
  }
}

// 客户护理操作
const handleSetCareLevel = (customer: Customer) => {
  selectedCustomer.value = customer
  careLevelDialogVisible.value = true
}

const handleAddCareItems = (customer: Customer) => {
  selectedCustomer.value = customer
  careItemsDialogVisible.value = true
}

const handleCustomerSuccess = () => {
  fetchCustomers()
}

const handleSearchCustomers = () => {
  customerPagination.page = 1
  fetchCustomers()
}

const handleResetCustomerSearch = () => {
  Object.assign(customerSearchForm, {
    customerName: ''
  })
  customerPagination.page = 1
  fetchCustomers()
}

// 获取护理记录列表
const fetchRecords = async () => {
  try {
    recordLoading.value = true
    const params: any = {
      page: recordPagination.page,
      size: recordPagination.size,
      customerName: recordSearchForm.customerName || undefined
    }
    
    if (recordSearchForm.dateRange && recordSearchForm.dateRange.length === 2) {
      params.startTime = recordSearchForm.dateRange[0] + ' 00:00:00'
      params.endTime = recordSearchForm.dateRange[1] + ' 23:59:59'
    }
    
    const response = await careApi.careRecord.list(params)
    
    if (response.data) {
      recordList.value = response.data.records || response.data.content || []
      recordPagination.total = response.data.total || response.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('获取护理记录列表失败')
  } finally {
    recordLoading.value = false
  }
}

// 护理记录操作
const handleDeleteRecord = async (record: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这条护理记录吗？', '确认删除', {
      type: 'warning'
    })
    
    await careApi.careRecord.delete(record.id)
    ElMessage.success('删除成功')
    fetchRecords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSearchRecords = () => {
  recordPagination.page = 1
  fetchRecords()
}

const handleResetRecordSearch = () => {
  Object.assign(recordSearchForm, {
    customerName: '',
    dateRange: []
  })
  recordPagination.page = 1
  fetchRecords()
}

// 页面加载时初始化数据
onMounted(() => {
  fetchCareLevels()
})
</script>

<style scoped>
.nursing-management {
  background: white;
  padding: 20px;
  border-radius: 8px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.management-tabs {
  margin-top: 20px;
}

.tab-content {
  padding: 20px 0;
}

.search-form {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.search-form .el-form-item {
  margin-bottom: 0;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.el-table {
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.el-table .el-table__header {
  background-color: #fafafa;
}
</style>
