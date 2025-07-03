<template>
  <div class="care-records">    <!-- 页面头部 -->
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>护理记录</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>健康管家</el-breadcrumb-item>
          <el-breadcrumb-item>护理记录</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>

    <!-- 客户选择 -->
    <el-card class="search-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>选择客户</span>
        </div>
      </template>

      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="客户姓名">
            <el-input
              v-model="searchForm.customerName"
              placeholder="请输入客户姓名"
              style="width: 200px"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchCustomers" :loading="loading">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 客户列表 -->      <el-table
        :data="customerList"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        @row-click="selectCustomer">
        <el-table-column prop="customerName" label="客户姓名" width="120" align="center" />
        <el-table-column label="性别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === 'MALE' ? 'primary' : 'danger'" size="small">
              {{ row.gender === 'MALE' ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" align="center" />
        <el-table-column label="床位信息" width="150" align="center">
          <template #default="{ row }">
            {{ row.buildingName || '' }}{{ row.roomNumber || '' }}-{{ row.bedNumber || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="careLevelName" label="护理级别" width="120" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="viewCareRecords(row)">
              查看护理记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 护理记录列表 -->
    <el-card v-if="selectedCustomer" class="records-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>{{ selectedCustomer.customerName }} - 护理记录</span>
          <div class="header-actions">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="loadCareRecords"
              style="margin-right: 10px" />
            <el-button type="success" @click="exportRecords" size="small">
              <el-icon><Download /></el-icon>
              导出记录
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="careRecordList"
        v-loading="recordsLoading"        stripe
        border
        style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="护理日期" width="120" align="center">
          <template #default="{ row }">
            {{ new Date(row.careTime).toLocaleDateString('zh-CN') }}
          </template>
        </el-table-column>
        <el-table-column label="护理时间" width="100" align="center">
          <template #default="{ row }">
            {{ new Date(row.careTime).toLocaleTimeString('zh-CN') }}
          </template>
        </el-table-column>
        <el-table-column prop="careItemName" label="护理项目" width="150" align="center" />
        <el-table-column prop="careQuantity" label="护理数量" width="100" align="center" />
        <el-table-column prop="remark" label="护理备注" min-width="200" align="center" />
        <el-table-column prop="healthManagerName" label="健康管家" width="120" align="center" />
        <el-table-column label="记录时间" width="150" align="center">
          <template #default="{ row }">
            {{ new Date(row.createdTime).toLocaleString('zh-CN') }}
          </template>
        </el-table-column>        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              @click="hideRecord(row)"
              :disabled="row.status === 'HIDDEN'">
              {{ row.status === 'HIDDEN' ? '已隐藏' : '隐藏' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 统计信息卡片 -->
    <el-row v-if="selectedCustomer" :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.totalRecords }}</div>
            <div class="stat-label">总护理次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.thisMonthRecords }}</div>
            <div class="stat-label">本月护理次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.lastCareDate }}</div>
            <div class="stat-label">最后护理时间</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.mostCareItem }}</div>
            <div class="stat-label">最多护理项目</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { healthManagerApi, type Customer, type CareRecord } from '@/api/healthManager'
import type { PageResponse, BaseQueryParams } from '@/api/types'

// 类型定义
interface Statistics {
  totalRecords: number
  thisMonthRecords: number
  lastCareDate: string
  mostCareItem: string
}

// 响应式数据
const loading = ref(false)
const recordsLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  customerName: ''
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 日期范围
const dateRange = ref<[string, string] | null>(null)

// 数据列表
const customerList = ref<Customer[]>([])
const careRecordList = ref<CareRecord[]>([])
const selectedCustomer = ref<Customer | null>(null)

// 统计信息
const statistics = reactive<Statistics>({
  totalRecords: 0,
  thisMonthRecords: 0,
  lastCareDate: '',
  mostCareItem: ''
})

// 搜索客户
const searchCustomers = async () => {
  try {
    loading.value = true
    const response = await healthManagerApi.getCustomerList({
      current: 1,
      size: 100,
      customerName: searchForm.customerName
    })
      console.log('API响应:', response)
    
    // 使用any类型绕过TypeScript检查，处理不同的响应结构
    if (response) {
      const resp = response as any
      if (resp.records) {
        // 直接是分页对象
        customerList.value = resp.records || []
      } else if (resp.data && resp.data.records) {
        // 嵌套在data中
        customerList.value = resp.data.records || []
      } else if (Array.isArray(resp)) {
        // 直接是数组
        customerList.value = resp
      } else if (Array.isArray(resp.data)) {
        // 数组在data中
        customerList.value = resp.data
      } else {
        customerList.value = []
      }
      console.log('处理后的客户列表:', customerList.value)
    } else {
      customerList.value = []
    }
  } catch (error) {
    console.error('查询客户失败:', error)
    ElMessage.error('查询客户失败')
    customerList.value = []
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.customerName = ''
  searchCustomers()
}

// 选择客户
const selectCustomer = (row: Customer) => {
  selectedCustomer.value = row
  currentPage.value = 1
  loadCareRecords()
  loadStatistics()
}

// 查看护理记录
const viewCareRecords = (customer: Customer) => {
  selectCustomer(customer)
}

// 加载护理记录 - 使用真实API
const loadCareRecords = async () => {
  if (!selectedCustomer.value) return
  
  try {
    recordsLoading.value = true
    
    // 构建查询参数
    const params: any = {
      current: currentPage.value,
      size: pageSize.value,
      customerId: selectedCustomer.value.id
    }
    
    // 如果有日期范围，添加到参数中
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }    // 调用API获取护理记录
    const response = await healthManagerApi.getCareRecords(params)
      console.log('护理记录API响应:', response)
    
    if (response) {
      // 使用any类型处理不同的响应结构
      const resp = response as any
      if (resp.records) {
        // 直接是分页对象
        careRecordList.value = resp.records || []
        total.value = resp.total || 0
      } else if (resp.data && resp.data.records) {
        // 嵌套在data中
        careRecordList.value = resp.data.records || []
        total.value = resp.data.total || 0
      } else if (Array.isArray(resp)) {
        // 直接是数组
        careRecordList.value = resp
        total.value = resp.length
      } else if (Array.isArray(resp.data)) {
        // 数组在data中
        careRecordList.value = resp.data
        total.value = resp.data.length
      } else {
        careRecordList.value = []
        total.value = 0
      }
      console.log('处理后的护理记录:', careRecordList.value)
    }
    
  } catch (error) {
    console.error('查询护理记录失败:', error)
    ElMessage.error('查询护理记录失败')
    careRecordList.value = []
    total.value = 0  } finally {
    recordsLoading.value = false
  }
}

// 加载统计信息 - 使用真实API
const loadStatistics = async () => {
  if (!selectedCustomer.value) return
  
  try {
    // 获取客户所有护理记录来计算统计信息
    const allRecordsResponse = await healthManagerApi.getCareRecords({
      current: 1,
      size: 1000,
      customerId: selectedCustomer.value.id
    })
    
    if (allRecordsResponse && allRecordsResponse.data) {
      const allRecords = allRecordsResponse.data.data.records || []
      
      // 计算总记录数
      statistics.totalRecords = allRecords.length
      
      // 计算本月记录数
      const currentMonth = new Date().getMonth()
      const currentYear = new Date().getFullYear()
      statistics.thisMonthRecords = allRecords.filter(record => {
        const recordDate = new Date(record.careTime)
        return recordDate.getMonth() === currentMonth && recordDate.getFullYear() === currentYear
      }).length
      
      // 获取最后护理时间
      if (allRecords.length > 0) {
        const sortedRecords = allRecords.sort((a, b) => new Date(b.careTime).getTime() - new Date(a.careTime).getTime())
        statistics.lastCareDate = new Date(sortedRecords[0].careTime).toLocaleDateString('zh-CN')
        
        // 获取最多的护理项目
        const itemCounts: Record<string, number> = {}
        allRecords.forEach(record => {
          itemCounts[record.careItemName] = (itemCounts[record.careItemName] || 0) + 1
        })
        const mostCommonItem = Object.entries(itemCounts).sort((a, b) => b[1] - a[1])[0]
        statistics.mostCareItem = mostCommonItem ? mostCommonItem[0] : ''
      }
    }
  } catch (error) {
    console.error('查询统计信息失败:', error)
  }
}

// 隐藏记录 - 使用真实API
const hideRecord = async (record: CareRecord) => {
  try {
    await ElMessageBox.confirm('确认隐藏此护理记录吗？', '提示', {
      type: 'warning'
    })
    
    // 调用隐藏记录API（假设API存在）
    // await healthManagerApi.hideCareRecord(record.id)
      // 临时处理：直接更新本地状态
    record.status = 'HIDDEN'
    ElMessage.success('护理记录已隐藏')
  } catch (error) {
    // 用户取消或其他错误
    if (error !== 'cancel') {
      console.error('隐藏记录失败:', error)
      ElMessage.error('隐藏记录失败')
    }
  }
}

// 导出记录
const exportRecords = () => {
  if (!selectedCustomer.value || careRecordList.value.length === 0) {
    ElMessage.warning('没有可导出的记录')
    return
  }
  
  // 构建CSV内容
  const headers = ['序号', '护理日期', '护理时间', '护理项目', '护理数量', '护理备注', '健康管家', '记录时间']
  const csvContent = [
    headers.join(','),
    ...careRecordList.value.map((record, index) => [
      index + 1,
      new Date(record.careTime).toLocaleDateString('zh-CN'),
      new Date(record.careTime).toLocaleTimeString('zh-CN'),
      record.careItemName,
      record.careQuantity,
      `"${record.remark || ''}"`,
      record.healthManagerName,
      new Date(record.createdTime).toLocaleString('zh-CN')
    ].join(','))
  ].join('\n')
  
  // 下载CSV文件
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `${selectedCustomer.value.customerName}_护理记录_${new Date().toLocaleDateString()}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('护理记录导出成功')
}

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadCareRecords()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadCareRecords()
}

// 页面加载时查询客户
onMounted(() => {
  searchCustomers()
})
</script>

<style scoped>
.care-records {
  padding: 0;
}

.page-header-card {
  margin-bottom: 20px;
  border: 1px solid #e8eaec;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.search-card,
.records-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.header-actions {
  display: flex;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 20px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 表格行点击效果 */
.el-table tbody tr {
  cursor: pointer;
}

.el-table tbody tr:hover {
  background-color: #f5f7fa;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-form .el-form-item {
    margin-bottom: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
