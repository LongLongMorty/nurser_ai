<template>
  <div class="daily-care">
    <!-- 页面头部 -->
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>日常护理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>健康管家</el-breadcrumb-item>
          <el-breadcrumb-item>日常护理</el-breadcrumb-item>
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
      </div>      <!-- 客户列表 -->
      <el-table
        :data="customerList"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        @selection-change="handleCustomerSelection"
      >        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="customerName" label="客户姓名" width="100" align="center" show-overflow-tooltip />
        <el-table-column prop="idCard" label="身份证号" width="160" align="center" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === 'MALE' ? 'primary' : 'danger'" size="small">
              {{ row.gender === 'MALE' ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" align="center" />
        <el-table-column prop="guardianName" label="监护人" width="100" align="center" show-overflow-tooltip />
        <el-table-column prop="guardianPhone" label="联系电话" width="130" align="center" />
        <el-table-column prop="roomNo" label="房间号" width="90" align="center" />
        <el-table-column prop="bedNo" label="床位号" width="90" align="center" />
        <el-table-column prop="careLevelName" label="护理等级" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getCareTypeTag(row.careLevelName)" size="small">
              {{ row.careLevelName }}
            </el-tag>
          </template>
        </el-table-column>        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary" 
              :icon="Edit"
              @click="showCareItems(row)"
              style="border-radius: 6px; margin-right: 8px;"
            >
              护理项目
            </el-button>
            <el-button 
              size="small" 
              type="success" 
              :icon="Document"
              @click="showCareRecords(row)"
              style="border-radius: 6px;"
            >
              护理记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="searchForm.page"
          v-model:page-size="searchForm.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="searchCustomers"
          @current-change="searchCustomers"
        />
      </div>
    </el-card>    <!-- 护理项目对话框 -->
    <el-dialog
      v-model="careItemsDialogVisible"
      :title="`${selectedCustomer?.customerName || ''} - 护理项目`"
      width="1000px"
      :close-on-click-modal="false"
    >      <el-table
        :data="careItemList"
        v-loading="careItemsLoading"
        stripe
        border
        style="width: 100%"
        :header-cell-style="{ 'background-color': '#f8f9fa', 'color': '#333', 'font-weight': '600' }"
      >
        <el-table-column label="护理项目" width="150" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getProjectName(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="itemCode" label="项目编码" width="120" align="center" />
        <el-table-column prop="purchaseDate" label="购买日期" width="110" align="center">
          <template #default="{ row }">
            {{ formatDate(row.purchaseDate) }}
          </template>
        </el-table-column>        <el-table-column prop="purchaseQuantity" label="购买数量" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #409eff; font-weight: 600;">{{ row.purchaseQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="usedQuantity" label="已使用" width="90" align="center">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: 600;">{{ row.usedQuantity || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remainingQuantity" label="剩余数量" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #e6a23c; font-weight: 600;">{{ row.remainingQuantity }}</span>
          </template>
        </el-table-column>        <el-table-column label="使用进度" width="130" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.round(((row.usedQuantity || 0) / (row.purchaseQuantity || 1)) * 100)"
              :status="(row.usedQuantity || 0) >= (row.purchaseQuantity || 0) ? 'success' : 'primary'"
              :stroke-width="8"
              style="margin: 0 8px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="expireDate" label="到期日期" width="110" align="center">
          <template #default="{ row }">
            {{ formatDate(row.expireDate) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getProjectStatusType(row)" size="small">
              {{ getProjectStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="success"
              :disabled="(row.remainingQuantity || 0) <= 0"
              @click="showCareDialog(row)"
              style="border-radius: 4px;"
            >
              执行
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 护理记录对话框 -->
    <el-dialog
      v-model="careDialogVisible"
      title="添加护理记录"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="careFormRef"
        :model="careForm"
        :rules="careFormRules"
        label-width="120px"
      >        <el-form-item label="客户姓名">
          <el-input :value="selectedCustomer?.customerName" disabled />
        </el-form-item>        <el-form-item label="护理项目">
          <el-input :value="selectedCareItem?.itemName || selectedCareItem?.itemCode" disabled />
        </el-form-item>        <el-form-item label="护理时间" prop="careTime">
          <el-date-picker
            v-model="careForm.careTime"
            type="datetime"
            placeholder="选择护理时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item><el-form-item label="护理数量" prop="careCount">
          <el-input-number
            v-model="careForm.careCount"
            :min="1"
            :max="selectedCareItem?.remainingQuantity || 10"
            placeholder="请输入护理数量"
            style="width: 100%"
          />          <div style="font-size: 12px; color: #909399; margin-top: 4px;">
            剩余可执行次数：{{ selectedCareItem?.remainingQuantity || 0 }}
          </div>
        </el-form-item>
        <el-form-item label="护理备注" prop="remark">
          <el-input
            v-model="careForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入护理备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="careDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCareRecord" :loading="submitLoading">
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 护理记录列表对话框 -->
    <el-dialog
      v-model="careRecordsDialogVisible"
      :title="`${selectedRecordCustomer?.customerName || ''} - 护理记录`"
      width="1200px"
      :close-on-click-modal="false"
    >
      <div class="care-records-dialog">
        <!-- 查询区域 -->
        <div class="query-area">
          <el-form :model="queryForm" inline>
            <el-form-item label="护理日期">
              <el-date-picker
                v-model="queryForm.careDate"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                @change="loadCareRecords"
                style="width: 300px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadCareRecords" :loading="careRecordsLoading">
                查询
              </el-button>
              <el-button @click="resetQuery">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 护理记录表格 -->
        <el-table
          :data="careRecordList"
          v-loading="careRecordsLoading"
          stripe
          border
          style="width: 100%"
          :header-cell-style="{ 'background-color': '#f8f9fa', 'color': '#333', 'font-weight': '600' }"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="护理日期" width="120" align="center">
            <template #default="{ row }">
              {{ formatDate(row.careTime) }}
            </template>
          </el-table-column>
          <el-table-column label="护理时间" width="120" align="center">
            <template #default="{ row }">
              {{ new Date(row.careTime).toLocaleTimeString('zh-CN') }}
            </template>
          </el-table-column>
          <el-table-column label="护理项目" width="150" align="center" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.careItemName || '—' }}
            </template>
          </el-table-column>
          <el-table-column label="护理数量" width="100" align="center">
            <template #default="{ row }">
              <span style="color: #409eff; font-weight: 600;">{{ row.careQuantity }}</span>
            </template>
          </el-table-column>
          <el-table-column label="护理备注" width="200" align="center">
            <template #default="{ row }">
              {{ row.remark || '—' }}
            </template>
          </el-table-column>
          <el-table-column label="健康管家" width="120" align="center">
            <template #default="{ row }">
              {{ row.healthManagerName || '—' }}
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="recordsCurrentPage"
            v-model:page-size="recordsPageSize"
            :total="recordsTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleRecordsSizeChange"
            @current-change="handleRecordsPageChange"
          />
        </div>

        <!-- 导出按钮 -->
        <div class="export-area">
          <el-button type="success" @click="exportCareRecords">
            <el-icon><Download /></el-icon>
            导出护理记录
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Edit, Document, Download } from '@element-plus/icons-vue'
import { healthManagerApi, type Customer, type CareItem, type CareRecord } from '@/api/healthManager'
import { useUserStore } from '@/stores/user'

const loading = ref(false)
const careItemsLoading = ref(false)
const submitLoading = ref(false)
const total = ref(0)
const customerList = ref<Customer[]>([])
const careItemList = ref<CareItem[]>([])
const selectedCustomer = ref<Customer | null>(null)
const selectedCareItem = ref<CareItem | null>(null)

// 对话框状态
const careItemsDialogVisible = ref(false)
const careDialogVisible = ref(false)
const careRecordsDialogVisible = ref(false)

// 护理记录相关状态
const careRecordList = ref<CareRecord[]>([])
const careRecordsLoading = ref(false)
const recordsTotal = ref(0)
const recordsCurrentPage = ref(1)
const recordsPageSize = ref(10)
const selectedRecordCustomer = ref<Customer | null>(null)

const searchForm = reactive({
  customerName: '',
  page: 1,
  size: 10
})

const careForm = reactive({
  careTime: '',
  careCount: 1,
  remark: ''
})

const queryForm = reactive({
  careDate: []
})

const careFormRef = ref<FormInstance>()

const careFormRules: FormRules = {
  careTime: [
    { required: true, message: '请选择护理时间', trigger: 'change' }
  ],
  careCount: [
    { required: true, message: '请输入护理数量', trigger: 'blur' },
    { type: 'number', min: 1, max: 10, message: '护理数量必须在1-10之间', trigger: 'blur' }
  ]
}

const userStore = useUserStore()

const searchCustomers = async () => {
  loading.value = true
  try {
    console.log('发送请求参数:', {
      current: searchForm.page,
      size: searchForm.size,
      customerName: searchForm.customerName
    })
    
    const response = await healthManagerApi.getCustomerList({
      current: searchForm.page,
      size: searchForm.size,
      customerName: searchForm.customerName
    }).catch((error) => {
      console.error('API请求错误:', error)
      console.error('错误详情:', error.response || error.message || error)
      throw error
    })
      console.log('API响应成功:', response)
    
    if (response) {
      // 使用any类型绕过TypeScript检查，处理不同的响应结构
      const resp = response as any
      let records = []
      let totalCount = 0
      
      if (resp.records) {
        // 直接是分页对象
        records = resp.records
        totalCount = resp.total || 0
      } else if (resp.data && resp.data.records) {
        // 嵌套在data中
        records = resp.data.records
        totalCount = resp.data.total || 0
      } else if (Array.isArray(resp)) {
        // 直接是数组
        records = resp
        totalCount = resp.length
      } else if (Array.isArray(resp.data)) {
        // 数组在data中
        records = resp.data
        totalCount = resp.data.length
      }
      
      customerList.value = records
      total.value = totalCount
      console.log('设置客户列表:', customerList.value)
      console.log('设置总数:', total.value)
    } else {
      console.error('响应为空')
      throw new Error('响应为空')
    }
  } catch (error) {
    console.error('查询客户列表失败:', error)
    ElMessage.error('查询客户列表失败')
    customerList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.customerName = ''
  searchForm.page = 1
  searchCustomers()
}

const handleCustomerSelection = (selection: Customer[]) => {
  if (selection.length > 0) {
    selectedCustomer.value = selection[0]
    showCareItems(selection[0])
  }
}

const showCareItems = async (customer: Customer) => {
  selectedCustomer.value = customer
  careItemsDialogVisible.value = true
  careItemsLoading.value = true
  try {
    const response = await healthManagerApi.getCustomerCareItems(customer.id)
    
    let careData = response
    // 如果返回的是包装对象，提取data字段
    if (response && typeof response === 'object' && 'data' in response) {
      careData = response.data as CareItem[]
    }
    
    careItemList.value = Array.isArray(careData) ? careData : []
  } catch (error) {
    console.error('查询护理项目失败:', error)
    ElMessage.error('查询护理项目失败')
    careItemList.value = []
  } finally {
    careItemsLoading.value = false
  }
}

const showCareDialog = (careItem: CareItem) => {
  selectedCareItem.value = careItem
  careForm.careTime = new Date().toISOString().slice(0, 19)
  careForm.careCount = 1
  careForm.remark = ''
  careDialogVisible.value = true
}

const submitCareRecord = async () => {
  if (!careFormRef.value) return
  const valid = await careFormRef.value.validate()
  if (!valid) return
  
  if (!selectedCustomer.value || !selectedCareItem.value) {
    ElMessage.error('请选择客户和护理项目')
    return
  }
  
  // 验证剩余数量
  if ((selectedCareItem.value.remainingQuantity || 0) <= 0) {
    ElMessage.error('该护理项目剩余数量不足，无法执行护理')
    return
  }
  
  if (careForm.careCount > (selectedCareItem.value.remainingQuantity || 0)) {
    ElMessage.error(`护理数量不能超过剩余数量 ${selectedCareItem.value.remainingQuantity}`)
    return
  }
  submitLoading.value = true
  try {    const recordData = {
      customerId: selectedCustomer.value.id,
      careItemId: selectedCareItem.value.careItemId,
      careTime: careForm.careTime,
      careQuantity: careForm.careCount,
      careContent: careForm.remark || '',
      remark: careForm.remark,
      healthManagerId: userStore.userInfo?.id // 新增健康管家ID
    }
    await healthManagerApi.addCareRecord(recordData)
    ElMessage.success('护理记录添加成功')
    careDialogVisible.value = false
    // 重新加载护理项目数据
    showCareItems(selectedCustomer.value)
  } catch (error) {ElMessage.error('添加护理记录失败')
  } finally {
    submitLoading.value = false
  }
}

// 显示护理记录
const showCareRecords = async (customer: Customer) => {
  selectedRecordCustomer.value = customer
  careRecordsDialogVisible.value = true
  await loadCareRecords()
}

// 加载护理记录
const loadCareRecords = async () => {
  if (!selectedRecordCustomer.value) return
  
  careRecordsLoading.value = true
  try {
    // 构建查询参数
    const params: any = {
      customerId: selectedRecordCustomer.value.id,
      current: recordsCurrentPage.value,
      size: recordsPageSize.value
    }
    
    // 添加日期范围查询参数
    if (queryForm.careDate && Array.isArray(queryForm.careDate) && queryForm.careDate.length === 2) {
      params.startTime = queryForm.careDate[0] + ' 00:00:00'
      params.endTime = queryForm.careDate[1] + ' 23:59:59'
    }
    
    console.log('护理记录查询参数:', params)
    
    const response = await healthManagerApi.getCareRecords(params)
    
    console.log('护理记录响应:', response)
    
    let recordData: any[] = []
    let totalCount = 0
    
    // 处理分页响应
    if (response && typeof response === 'object') {
      if ('data' in response && Array.isArray((response as any).data)) {
        recordData = (response as any).data
        totalCount = (response as any).total || recordData.length
      } else if ('records' in response && Array.isArray((response as any).records)) {
        recordData = (response as any).records
        totalCount = (response as any).total || recordData.length
      } else if (Array.isArray(response)) {
        recordData = response as any[]
        totalCount = recordData.length
      }
    }
    
    careRecordList.value = recordData
    recordsTotal.value = totalCount
    
    console.log('处理后的护理记录:', careRecordList.value)
  } catch (error) {
    console.error('加载护理记录失败:', error)
    ElMessage.error('加载护理记录失败')
    careRecordList.value = []
    recordsTotal.value = 0
  } finally {
    careRecordsLoading.value = false
  }
}

// 护理记录分页变化
const handleRecordsPageChange = (page: number) => {
  recordsCurrentPage.value = page
  loadCareRecords()
}

const handleRecordsSizeChange = (size: number) => {
  recordsPageSize.value = size
  recordsCurrentPage.value = 1
  loadCareRecords()
}

// 导出护理记录
const exportCareRecords = () => {
  if (careRecordList.value.length === 0) {
    ElMessage.warning('暂无护理记录可导出')
    return
  }
  
  try {
    const headers = ['序号', '护理日期', '护理时间', '护理项目', '护理数量', '护理备注', '健康管家']
    const data = careRecordList.value.map((record, index) => [
      index + 1,
      new Date(record.careTime).toLocaleDateString('zh-CN'),
      new Date(record.careTime).toLocaleTimeString('zh-CN'),
      record.careItemName || '—',
      record.careQuantity || '—',
      record.remark || '—',
      record.healthManagerName || '—'
    ])
    
    const csvContent = [headers, ...data]
      .map(row => row.map(field => `"${field}"`).join(','))
      .join('\n')
    
    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `${selectedRecordCustomer.value?.customerName || '客户'}_护理记录_${new Date().toLocaleDateString('zh-CN')}.csv`
    link.click()
    URL.revokeObjectURL(link.href)
    
    ElMessage.success('护理记录导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 重置护理记录查询
const resetQuery = () => {
  queryForm.careDate = []
  recordsCurrentPage.value = 1
  loadCareRecords()
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// 获取护理项目名称
const getProjectName = (row: any) => {
  if (!row) return '—'
  // 使用后端返回的itemName，如果为空则使用itemCode作为兜底
  const name = row.itemName || row.itemCode || '—'
  return name
}

// 获取护理项目状态
const getProjectStatusText = (row: any) => {
  if (!row) return '—'
  // 使用后端返回的serviceStatus，如果为空则默认为NORMAL
  const status = row.serviceStatus || 'NORMAL'
  
  const statusMap: Record<string, string> = {
    'NORMAL': '正常',
    'EXPIRED': '已过期',
    'USED_UP': '已用完',
    'ARREARS': '欠费',
    'SUSPENDED': '暂停'
  }
  return statusMap[status] || status || '正常'
}
const getProjectStatusType = (row: any) => {
  if (!row) return 'info'
  const status = row.serviceStatus || 'NORMAL'
  const typeMap: Record<string, string> = {
    'NORMAL': 'success',
    'EXPIRED': 'danger',
    'USED_UP': 'warning',
    'ARREARS': 'warning',
    'SUSPENDED': 'info'
  }
  return typeMap[status] || 'info'
}

const getServiceStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'NORMAL': '正常',
    'EXPIRED': '已过期',
    'USED_UP': '已用完',
    'ARREARS': '欠费',
    'SUSPENDED': '暂停'
  }
  return statusMap[status] || status || '未知'
}

const getServiceStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'NORMAL': 'success',
    'EXPIRED': 'danger',
    'USED_UP': 'warning',
    'ARREARS': 'danger',
    'SUSPENDED': 'info'
  }
  return typeMap[status] || 'info'
}

const isExpiringSoon = (expireDate: string) => {
  if (!expireDate) return false
  const expire = new Date(expireDate)
  const now = new Date()
  const daysDiff = Math.ceil((expire.getTime() - now.getTime()) / (1000 * 3600 * 24))
  return daysDiff <= 7 && daysDiff >= 0 // 7天内到期显示红色
}

const getCareTypeTag = (careLevelName: string) => {
  if (!careLevelName) return 'info'
  if (careLevelName.includes('一级')) return 'danger'
  if (careLevelName.includes('二级')) return 'warning'  
  if (careLevelName.includes('三级')) return 'success'
  if (careLevelName.includes('特护')) return 'danger'
  return 'primary'
}

const getCareTypeText = (careType: string) => {
  const typeMap: Record<string, string> = {
    'DAILY': '日常护理',
    'MEDICAL': '医疗护理',
    'REHAB': '康复护理',
    'MENTAL': '心理护理',
    'DIET': '饮食护理'
  }
  return typeMap[careType] || careType || '未知'
}

onMounted(() => {
  searchCustomers()
})
</script>

<style scoped>
.daily-care {
  padding: 0;
}
.page-header-card {
  margin-bottom: 20px;
  border-radius: 8px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}
.search-card, .care-items-card {
  margin-bottom: 20px;
  border-radius: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #333;
}
.search-form {
  margin-bottom: 20px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
:deep(.el-table th) {
  background-color: #f8f9fa;
  color: #333;
  font-weight: 600;
  border-bottom: 2px solid #e9ecef;
}
:deep(.el-table td) {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fafbfc;
}
:deep(.el-progress-bar__outer) {
  border-radius: 10px;
  background-color: #f0f2f5;
}
:deep(.el-progress-bar__inner) {
  border-radius: 10px;
}
:deep(.el-card) {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: none;
  border-radius: 12px;
}
:deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 18px 20px;
  background-color: #fafbfc;
}
:deep(.el-card__body) {
  padding: 20px;
}
:deep(.el-button) {
  border-radius: 6px;
  font-weight: 500;
}
:deep(.el-tag) {
  border-radius: 4px;
  font-weight: 500;
}
:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}
:deep(.el-dialog__header) {
  padding: 20px 24px 10px;
  background-color: #fafbfc;
  border-bottom: 1px solid #f0f0f0;
}
:deep(.el-dialog__body) {
  padding: 20px 24px;
}
</style>
