<template>
  <div class="care-record">
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>护理记录管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>护理管理</el-breadcrumb-item>
          <el-breadcrumb-item>护理记录</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>

    <!-- 客户查询区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="customerSearchForm" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="客户姓名">
              <el-input 
                v-model="customerSearchForm.customerName" 
                placeholder="请输入客户姓名进行模糊查询" 
                clearable 
                @keyup.enter="handleCustomerSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item>
              <div class="button-group">
                <el-button type="primary" @click="handleCustomerSearch">
                  <el-icon><Search /></el-icon>
                  查询客户
                </el-button>
                <el-button @click="handleCustomerReset">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 客户列表 -->
    <el-card class="customer-list-card" shadow="always" v-if="showCustomerList">
      <div class="card-header">
        <span>客户列表</span>
        <el-tag type="info">找到 {{ customerList.length }} 位客户</el-tag>
      </div>
      <el-table :data="customerList" v-loading="customerLoading" stripe border @row-click="handleSelectCustomer">
        <el-table-column prop="id" label="客户ID" width="80" />
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="careLevelName" label="护理级别" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.careLevelName" type="success">{{ row.careLevelName }}</el-tag>
            <el-tag v-else type="info">未设置</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordCount" label="护理记录数" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.recordCount > 0" type="primary">{{ row.recordCount }}条</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastRecordTime" label="最近护理时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleSelectCustomer(row)">
              <el-icon><View /></el-icon>
              查看护理记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 护理记录列表 -->
    <el-card class="record-list-card" shadow="always" v-if="selectedCustomer">
      <div class="card-header">
        <span>护理记录 - {{ selectedCustomer.customerName }}</span>
        <div class="header-actions">
          <el-button size="small" @click="handleBackToCustomerList">
            <el-icon><ArrowLeft /></el-icon>
            返回客户列表
          </el-button>
          <el-button size="small" type="primary" @click="handleAddRecord">
            <el-icon><Plus /></el-icon>
            新增记录
          </el-button>
        </div>
      </div>      <!-- 护理记录筛选 -->
      <div class="record-filter">
        <el-form :model="recordSearchForm" inline>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="recordSearchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleRecordSearch">查询</el-button>
            <el-button @click="handleRecordReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div><el-table :data="recordList" v-loading="recordLoading" stripe border>
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="itemName" label="护理项目" width="150" />
        <el-table-column prop="careQuantity" label="护理数量" width="120" align="center">
          <template #default="{ row }">
            {{ row.careQuantity }}次
          </template>
        </el-table-column>
        <el-table-column prop="careContent" label="护理内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" width="150" show-overflow-tooltip />
        <el-table-column prop="careTime" label="护理时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.careTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="healthManagerName" label="健康管理师" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewRecord(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button size="small" type="primary" @click="handleEditRecord(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleHideRecord(row)">
              <el-icon><Delete /></el-icon>
              隐藏
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="recordPagination.pageNum"
          v-model:page-size="recordPagination.pageSize"
          :total="recordPagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchRecordList"
          @current-change="fetchRecordList"
        />
      </div>
    </el-card>

    <!-- 护理记录详情对话框 -->
    <el-dialog
      v-model="recordDetailDialogVisible"
      title="护理记录详情"
      width="800px">
        <div class="record-detail-content" v-if="currentRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户姓名">{{ selectedCustomer?.customerName }}</el-descriptions-item>
          <el-descriptions-item label="护理项目">{{ currentRecord.itemName }}</el-descriptions-item>
          <el-descriptions-item label="护理数量">{{ currentRecord.careQuantity }}次</el-descriptions-item>
          <el-descriptions-item label="护理时间">{{ formatDateTime(currentRecord.careTime) }}</el-descriptions-item>
          <el-descriptions-item label="健康管理师">{{ currentRecord.healthManagerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="护理内容" :span="2">{{ currentRecord.careContent }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRecord.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="recordDetailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, View, Edit, Delete, ArrowLeft } from '@element-plus/icons-vue'
import careApi from '@/api/care'
import type { CareRecord, CustomerCarePageDTO } from '@/api/care'

const router = useRouter()

// 响应式数据
const customerLoading = ref(false)
const recordLoading = ref(false)
const customerList = ref<CustomerCarePageDTO[]>([])
const recordList = ref<CareRecord[]>([])
const selectedCustomer = ref<CustomerCarePageDTO | null>(null)
const currentRecord = ref<CareRecord | null>(null)

// 显示状态
const showCustomerList = ref(false)
const recordDetailDialogVisible = ref(false)

// 客户搜索表单
const customerSearchForm = reactive({
  customerName: ''
})

// 护理记录搜索表单
const recordSearchForm = reactive({
  dateRange: null as [string, string] | null
})

// 护理记录分页
const recordPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 生命周期
onMounted(() => {
  // 初始化时不显示客户列表，等待用户搜索
})

// 客户搜索
const handleCustomerSearch = async () => {
  if (!customerSearchForm.customerName.trim()) {
    ElMessage.warning('请输入客户姓名')
    return
  }

  try {
    customerLoading.value = true
    const params = {
      customerName: customerSearchForm.customerName,
      pageNum: 1,
      pageSize: 100 // 获取更多客户数据
    }

    const response = await careApi.customerCare.list(params)
    console.log('客户搜索响应:', response)
      // 兼容不同的响应结构
    let customerData = []
    if (response?.data?.records) {
      customerData = response.data.records
    } else if ((response as any)?.records) {
      customerData = (response as any).records
    } else if (Array.isArray(response?.data)) {
      customerData = response.data
    } else if (Array.isArray(response)) {
      customerData = response as any
    }

    customerList.value = customerData
    showCustomerList.value = true
    selectedCustomer.value = null

    if (customerData.length === 0) {
      ElMessage.info('未找到匹配的客户')
    }
  } catch (error) {
    console.error('搜索客户失败:', error)
    ElMessage.error('搜索客户失败')
  } finally {
    customerLoading.value = false
  }
}

// 重置客户搜索
const handleCustomerReset = () => {
  customerSearchForm.customerName = ''
  customerList.value = []
  showCustomerList.value = false
  selectedCustomer.value = null
  recordList.value = []
}

// 选择客户
const handleSelectCustomer = (customer: CustomerCarePageDTO) => {
  selectedCustomer.value = customer
  recordPagination.pageNum = 1
  recordSearchForm.dateRange = null
  fetchRecordList()
}

// 返回客户列表
const handleBackToCustomerList = () => {
  selectedCustomer.value = null
  recordList.value = []
}

// 获取护理记录列表
const fetchRecordList = async () => {
  if (!selectedCustomer.value) return

  try {
    recordLoading.value = true    const params = {
      customerName: selectedCustomer.value.customerName,
      startTime: recordSearchForm.dateRange?.[0] || undefined,
      endTime: recordSearchForm.dateRange?.[1] || undefined,
      pageNum: recordPagination.pageNum,
      pageSize: recordPagination.pageSize
    }

    const response = await careApi.careRecord.list(params)
    console.log('护理记录响应:', response)    // 兼容不同的响应结构
    let recordData = []
    if (response?.data?.records) {
      recordData = response.data.records
    } else if ((response as any)?.records) {
      recordData = (response as any).records
    } else if (Array.isArray(response?.data)) {
      recordData = response.data
    } else if (Array.isArray(response)) {
      recordData = response as any
    }

    recordList.value = recordData
    recordPagination.total = response?.data?.total || (response as any)?.total || recordData.length
  } catch (error) {
    console.error('获取护理记录失败:', error)
    ElMessage.error('获取护理记录失败')
  } finally {
    recordLoading.value = false
  }
}

// 护理记录搜索
const handleRecordSearch = () => {
  recordPagination.pageNum = 1
  fetchRecordList()
}

// 重置护理记录搜索
const handleRecordReset = () => {
  recordSearchForm.dateRange = null
  recordPagination.pageNum = 1
  fetchRecordList()
}

// 新增护理记录
const handleAddRecord = () => {
  if (!selectedCustomer.value) return
  router.push({
    path: '/care/record/add',
    query: { customerId: selectedCustomer.value.id }
  })
}

// 查看护理记录详情
const handleViewRecord = (record: CareRecord) => {
  currentRecord.value = record
  recordDetailDialogVisible.value = true
}

// 编辑护理记录
const handleEditRecord = (record: CareRecord) => {
  router.push(`/care/record/edit/${record.id}`)
}

// 隐藏护理记录
const handleHideRecord = async (record: CareRecord) => {
  try {
    await ElMessageBox.confirm(
      `确定要隐藏客户 ${selectedCustomer.value?.customerName} 的这条护理记录吗？\n注意：隐藏后记录将不再显示，但数据不会被删除。`,
      '确认隐藏',
      {
        confirmButtonText: '确定隐藏',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await careApi.careRecord.delete(record.id)
    ElMessage.success('护理记录已隐藏')
    fetchRecordList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('隐藏护理记录失败:', error)
      ElMessage.error('隐藏护理记录失败')
    }
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string): string => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.care-record {
  padding: 20px;
}

.page-header-card {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-card {
  margin-bottom: 20px;
}

.button-group {
  display: flex;
  gap: 10px;
}

.customer-list-card,
.record-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
  font-weight: bold;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.record-filter {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.record-detail-content {
  max-height: 500px;
  overflow-y: auto;
}

/* 表格行点击效果 */
:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 客户列表表格特殊样式 */
.customer-list-card :deep(.el-table__row) {
  cursor: pointer;
}

.customer-list-card :deep(.el-table__row:hover) {
  background-color: #ecf5ff;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .care-record {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .record-filter :deep(.el-form--inline .el-form-item) {
    display: block;
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>
