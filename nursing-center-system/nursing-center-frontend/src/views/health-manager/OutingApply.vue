<template>
  <div class="outing-apply">    <!-- 页面头部 -->
    <el-card class="page-header-card" shadow="never">
      <div class="page-header"><h2>外出申请</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>健康管家</el-breadcrumb-item>
          <el-breadcrumb-item>外出申请</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>    <!-- 客户选择 -->
    <el-card class="search-card" shadow="always">
      <template #header>
        <div class="card-header"><span>选择客户</span></div>
      </template>
      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="客户姓名">
            <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" style="width: 200px"
                      clearable/>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchCustomers" :loading="loading">
              <el-icon>
                <Search/>
              </el-icon>
              查询
            </el-button>
            <el-button @click="resetSearch">
              <el-icon>
                <Refresh/>
              </el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>      <!-- 客户列表 -->
      <el-table :data="customerList" v-loading="loading" stripe border style="width: 100%"
                :header-cell-style="{ 'background-color': '#f8f9fa', 'color': '#333', 'font-weight': '600' }">
        <el-table-column prop="customerName" label="客户姓名" width="120" align="center"
                         show-overflow-tooltip/>
        <el-table-column prop="idCard" label="身份证号" width="160" align="center"
                         show-overflow-tooltip/>
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === 'MALE' ? 'primary' : 'danger'" size="small">
              {{ row.gender === 'MALE' ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" align="center"/>
        <el-table-column prop="roomNo" label="房间号" width="90" align="center"/>
        <el-table-column prop="bedNo" label="床位号" width="90" align="center"/>
        <el-table-column prop="careLevelName" label="护理等级" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getCareTypeTag(row.careLevelName)" size="small"> {{
                row.careLevelName
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" :icon="Plus" @click="showApplyDialog(row)"
                       style="border-radius: 6px; margin-right: 8px;"> 申请外出
            </el-button>
            <el-button size="small" type="success" :icon="Document" @click="showRecordsDialog(row)"
                       style="border-radius: 6px;"> 外出记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="searchForm.page" v-model:page-size="searchForm.size"
                       :total="total" :page-sizes="[10, 20, 50, 100]"
                       layout="total, sizes, prev, pager, next, jumper"
                       @size-change="searchCustomers" @current-change="searchCustomers"/>
      </div>
    </el-card>    <!-- 外出申请对话框 -->
    <el-dialog v-model="applyDialogVisible"
               :title="`${selectedCustomer?.customerName || ''} - 外出申请`" width="600px"
               :close-on-click-modal="false">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyFormRules" label-width="120px"
               v-loading="submitLoading">
        <el-form-item label="客户姓名">
          <el-input :model-value="selectedCustomer?.customerName" disabled/>
        </el-form-item>
        
        <el-form-item label="申请时间">
          <el-input :model-value="formatDateTime(new Date().toISOString())" disabled/>
        </el-form-item>
        
        <el-form-item label="外出事由" prop="outingReason">
          <el-select v-model="applyForm.outingReason" placeholder="请选择外出事由" style="width: 100%">
            <el-option label="就医" value="MEDICAL"/>
            <el-option label="探亲" value="VISIT_FAMILY"/>
            <el-option label="购物" value="SHOPPING"/>
            <el-option label="散步" value="WALK"/>
            <el-option label="其他" value="OTHER"/>
          </el-select>
        </el-form-item>
        <el-form-item label="外出时间" prop="outingDate">
          <el-date-picker v-model="applyForm.outingDate" type="datetime" placeholder="选择外出时间"
                          format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss"
                          style="width: 100%"/>
        </el-form-item>
        <el-form-item label="预计回院时间" prop="expectedReturnDate">
          <el-date-picker v-model="applyForm.expectedReturnDate" type="datetime"
                          placeholder="选择预计回院时间" format="YYYY-MM-DD HH:mm"
                          value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%"/>
        </el-form-item>
      </el-form>
      <template #footer><span class="dialog-footer">          <el-button
        @click="applyDialogVisible = false">取消</el-button>          <el-button type="primary"
                                                                               @click="submitApply"
                                                                               :loading="submitLoading">            提交申请          </el-button>        </span>
      </template>
    </el-dialog>    <!-- 外出记录对话框 -->
    <el-dialog v-model="recordsDialogVisible"
               :title="`${selectedRecordCustomer?.customerName || ''} - 外出记录`" width="1200px"
               :close-on-click-modal="false">
      <div class="records-dialog-content">
        <div class="records-toolbar">
          <el-form :model="recordsQueryForm" inline>
            <el-form-item label="申请时间">
              <el-date-picker v-model="recordsQueryForm.dateRange" type="daterange"
                              range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期"
                              format="YYYY-MM-DD" value-format="YYYY-MM-DD"
                              @change="loadOutingRecords" style="width: 280px"/>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="recordsQueryForm.status" placeholder="请选择状态"
                         @change="loadOutingRecords" style="width: 120px">
                <el-option label="全部" value=""/>
                <el-option label="已提交" value="SUBMITTED" />
                <el-option label="已通过" value="APPROVED"/>
                <el-option label="已拒绝" value="REJECTED"/>
                <el-option label="已完成" value="COMPLETED"/>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadOutingRecords" :loading="recordsLoading">
                <el-icon>
                  <Search/>
                </el-icon>
                查询
              </el-button>
              <el-button @click="resetRecordsQuery">
                <el-icon>
                  <Refresh/>
                </el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="outingRecords" v-loading="recordsLoading" stripe border style="width: 100%"
                  :header-cell-style="{ 'background-color': '#f8f9fa', 'color': '#333', 'font-weight': '600' }">
          <el-table-column type="index" label="序号" width="60" align="center"/>
          <el-table-column label="外出事由" width="100" align="center">
            <template #default="{ row }"> {{ getReasonText(row.outingReason) }}</template>
          </el-table-column>
          <el-table-column prop="outingDate" label="外出时间" width="150" align="center">
            <template #default="{ row }"> {{ formatDateTime(row.outingDate) }}</template>
          </el-table-column>
          <el-table-column prop="expectedReturnDate" label="预计回院时间" width="150" align="center">
            <template #default="{ row }"> {{ formatDateTime(row.expectedReturnDate) }}</template>
          </el-table-column>
          <el-table-column prop="actualReturnDate" label="实际回院时间" width="150" align="center">
            <template #default="{ row }">
              {{ row.actualReturnDate ? formatDateTime(row.actualReturnDate) : '未回院' }}
            </template>
          </el-table-column>
          <el-table-column prop="applyStatus" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getApplyStatusType(row.applyStatus)">
                {{ getApplyStatusText(row.applyStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.applyStatus === 'APPROVED' && !row.actualReturnDate"
                         type="success" size="small" @click="registerReturn(row)"> 登记回院
              </el-button>
              <span v-else-if="row.actualReturnDate">已回院</span> <span
              v-else>{{ getApplyStatusText(row.applyStatus) }}</span></template>
          </el-table-column>
        </el-table>        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination v-model:current-page="recordsCurrentPage"
                         v-model:page-size="recordsPageSize" :total="recordsTotal"
                         :page-sizes="[10, 20, 50, 100]"
                         layout="total, sizes, prev, pager, next, jumper"
                         @size-change="handleRecordsSizeChange"
                         @current-change="handleRecordsPageChange"/>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus, Document } from '@element-plus/icons-vue'
import { healthManagerApi, type Customer } from '@/api/healthManager'
import {
  submitOutingApply,
  getMyOutingApplies,
  registerReturn as apiRegisterReturn,
  type OutingApply,
  type OutingApplyDTO,
  type OutingReturnDTO
} from '@/api/apply'
import { useUserStore } from '@/stores/user'

const loading = ref(false)
const submitLoading = ref(false)
const recordsLoading = ref(false)
const total = ref(0)
const customerList = ref<Customer[]>([])
const selectedCustomer = ref<Customer | null>(null)
const selectedRecordCustomer = ref<Customer | null>(null)

// 对话框状态
const applyDialogVisible = ref(false)
const recordsDialogVisible = ref(false)

// 外出记录相关状态
const outingRecords = ref<OutingApply[]>([])
const recordsTotal = ref(0)
const recordsCurrentPage = ref(1)
const recordsPageSize = ref(10)

const searchForm = reactive({
  customerName: '',
  page: 1,
  size: 10
})

const applyForm = reactive({
  outingReason: '',
  outingDate: '',
  expectedReturnDate: ''
})

const recordsQueryForm = reactive({
  dateRange: [] as string[],
  status: ''
})

const applyFormRef = ref<FormInstance>()

const applyFormRules: FormRules = {
  outingReason: [
    { required: true, message: '请选择外出事由', trigger: 'change' }
  ],
  outingDate: [
    { required: true, message: '请选择外出时间', trigger: 'change' }
  ],
  expectedReturnDate: [
    { required: true, message: '请选择预计回院时间', trigger: 'change' }
  ]
}

const userStore = useUserStore()

// 查询客户列表
const searchCustomers = async () => {
  loading.value = true
  try {
    const response = await healthManagerApi.getCustomerList({
      current: searchForm.page,
      size: searchForm.size,
      customerName: searchForm.customerName
    })
    
    let customerData: any[] = []
    let totalCount = 0
    
    // 处理分页响应
    if (response && typeof response === 'object') {
      if ('data' in response && Array.isArray((response as any).data)) {
        customerData = (response as any).data
        totalCount = (response as any).total || customerData.length
      } else if ('records' in response && Array.isArray((response as any).records)) {
        customerData = (response as any).records
        totalCount = (response as any).total || customerData.length
      } else if (Array.isArray(response)) {
        customerData = response as any[]
        totalCount = customerData.length
      }
    }
    
    customerList.value = customerData
    total.value = totalCount
  } catch (error) {
    console.error('查询客户列表失败:', error)
    ElMessage.error('查询客户列表失败')
    customerList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.customerName = ''
  searchForm.page = 1
  searchCustomers()
}

// 显示申请对话框
const showApplyDialog = (customer: Customer) => {
  selectedCustomer.value = customer
  resetApplyForm()
  applyDialogVisible.value = true
}

// 重置申请表单
const resetApplyForm = () => {
  applyForm.outingReason = ''
  applyForm.outingDate = ''
  applyForm.expectedReturnDate = ''
  applyFormRef.value?.clearValidate()
}

// 提交外出申请
const submitApply = async () => {
  if (!selectedCustomer.value) {
    ElMessage.warning('请先选择客户')
    return
  }
  
  if (!applyFormRef.value) return
  
  const valid = await applyFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitLoading.value = true
  try {
    const data: OutingApplyDTO = {
      customerId: selectedCustomer.value.id,
      outingReason: applyForm.outingReason,
      outingDate: applyForm.outingDate,
      expectedReturnDate: applyForm.expectedReturnDate
    }
    
    await submitOutingApply(data)
    ElMessage.success('外出申请提交成功')
    applyDialogVisible.value = false
    resetApplyForm()
  } catch (error) {
    console.error('提交申请失败:', error)
    ElMessage.error('提交申请失败')
  } finally {
    submitLoading.value = false
  }
}

// 显示记录对话框
const showRecordsDialog = async (customer: Customer) => {
  selectedRecordCustomer.value = customer
  recordsDialogVisible.value = true
  await loadOutingRecords()
}

// 加载外出记录
const loadOutingRecords = async () => {
  if (!selectedRecordCustomer.value) return
  
  recordsLoading.value = true
  try {
    const params: any = {
      customerId: selectedRecordCustomer.value.id,
      current: recordsCurrentPage.value,
      size: recordsPageSize.value
    }
    
    // 添加日期范围查询
    if (recordsQueryForm.dateRange && Array.isArray(recordsQueryForm.dateRange) && recordsQueryForm.dateRange.length === 2) {
      params.startDate = recordsQueryForm.dateRange[0]
      params.endDate = recordsQueryForm.dateRange[1]
    }
    
    // 添加状态查询
    if (recordsQueryForm.status) {
      params.status = recordsQueryForm.status
    }
    
    console.log('外出记录查询参数:', params)
    
    const response = await getMyOutingApplies(params)
    
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
    
    outingRecords.value = recordData
    recordsTotal.value = totalCount
    
    console.log('处理后的外出记录:', outingRecords.value)
  } catch (error) {
    console.error('加载外出记录失败:', error)
    ElMessage.error('加载外出记录失败')
    outingRecords.value = []
    recordsTotal.value = 0
  } finally {
    recordsLoading.value = false
  }
}

// 重置记录查询
const resetRecordsQuery = () => {
  recordsQueryForm.dateRange = []
  recordsQueryForm.status = ''
  recordsCurrentPage.value = 1
  loadOutingRecords()
}

// 记录分页变化
const handleRecordsPageChange = (page: number) => {
  recordsCurrentPage.value = page
  loadOutingRecords()
}

const handleRecordsSizeChange = (size: number) => {
  recordsPageSize.value = size
  recordsCurrentPage.value = 1
  loadOutingRecords()
}

// 登记回院
const registerReturn = async (record: OutingApply) => {
  try {
    await ElMessageBox.confirm('确定要登记该客户回院吗？', '确认操作', {
      type: 'warning'
    })
    
    const data: OutingReturnDTO = {
      applyId: record.id,
      actualReturnTime: new Date().toISOString()
    }
    
    await apiRegisterReturn(data)
    ElMessage.success('回院登记成功')
    loadOutingRecords() // 刷新记录列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('回院登记失败:', error)
      ElMessage.error('回院登记失败')
    }
  }
}

// 工具方法
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getReasonText = (reason: string) => {
  const reasonMap: Record<string, string> = {
    'MEDICAL': '就医',
    'VISIT_FAMILY': '探亲',
    'SHOPPING': '购物',
    'WALK': '散步',
    'OTHER': '其他'
  }
  return reasonMap[reason] || reason
}

const getApplyStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'SUBMITTED': '已提交',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'COMPLETED': '已完成'
  }
  return statusMap[status] || status
}

const getApplyStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'SUBMITTED': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'COMPLETED': 'info'
  }
  return typeMap[status] || 'info'
}

const getCareTypeTag = (careLevelName: string) => {
  if (!careLevelName) return 'info'
  if (careLevelName.includes('一级')) return 'danger'
  if (careLevelName.includes('二级')) return 'warning'
  if (careLevelName.includes('三级')) return 'primary'
  return 'success'
}

// 初始化
onMounted(() => {
  searchCustomers()
})
</script>
<style scoped>.outing-apply {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-weight: 600;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.records-dialog-content {
  padding: 10px 0;
}

.records-toolbar {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 全局样式优化 */
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
}</style>
