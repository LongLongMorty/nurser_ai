<template>
  <div class="return-apply">
    <!-- 页面头部 -->
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>回院申请</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>健康管家</el-breadcrumb-item>
          <el-breadcrumb-item>回院申请</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>

    <!-- 外出客户列表 -->
    <el-card class="search-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>外出客户列表</span>
          <el-tooltip content="只显示您管理的外出客户" placement="top">
            <el-icon><InfoFilled /></el-icon>
          </el-tooltip>
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
            <el-button type="primary" @click="searchOutingCustomers" :loading="loading">
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

      <!-- 外出客户列表 -->
      <el-table
        :data="outingCustomerList"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        :header-cell-style="{ 'background-color': '#f8f9fa', 'color': '#333', 'font-weight': '600' }"
      >
        <el-table-column prop="customerName" label="客户姓名" width="120" align="center" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === 'MALE' ? 'primary' : 'danger'" size="small">
              {{ row.gender === 'MALE' ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" align="center" />
        <el-table-column prop="bedInfo" label="床位信息" width="150" align="center" />        <el-table-column prop="outingTime" label="外出时间" width="150" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.outingTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="expectedReturnTime" label="预计回院时间" width="150" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.expectedReturnTime) }}
          </template>
        </el-table-column>
        <el-table-column label="外出状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="warning">外出中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary" 
              :icon="Plus"
              @click="showApplyDialog(row)"
              style="border-radius: 6px; margin-right: 8px;"
            >
              申请回院
            </el-button>
            <el-button 
              size="small" 
              type="success" 
              :icon="Document"
              @click="showRecordsDialog(row)"
              style="border-radius: 6px;"
            >
              申请记录
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
          @size-change="searchOutingCustomers"
          @current-change="searchOutingCustomers"
        />
      </div>
    </el-card>

    <!-- 回院申请对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      :title="`${selectedCustomer?.customerName || ''} - 回院申请`"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="applyFormRef"
        :model="applyForm"
        :rules="applyFormRules"
        label-width="120px"
        v-loading="submitLoading"
      >
        <el-form-item label="客户姓名">
          <el-input :model-value="selectedCustomer?.customerName" disabled />
        </el-form-item>
        
        <el-form-item label="外出申请ID">
          <el-input :model-value="selectedCustomer?.outingApplyId" disabled />
        </el-form-item>

        <el-form-item label="当前床位">
          <el-input :model-value="selectedCustomer?.bedInfo" disabled />
        </el-form-item>

        <el-form-item label="申请时间">
          <el-input :model-value="formatDateTime(new Date().toISOString())" disabled />
        </el-form-item>
        
        <el-form-item label="回院事由" prop="returnReason">
          <el-select v-model="applyForm.returnReason" placeholder="请选择回院事由" style="width: 100%">
            <el-option label="按期回院" value="SCHEDULED_RETURN" />
            <el-option label="提前回院" value="EARLY_RETURN" />
            <el-option label="身体不适" value="HEALTH_ISSUE" />
            <el-option label="家属要求" value="FAMILY_REQUEST" />
            <el-option label="紧急情况" value="EMERGENCY" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="申请回院时间" prop="requestedReturnDate">
          <el-date-picker
            v-model="applyForm.requestedReturnDate"
            type="datetime"
            placeholder="选择申请回院时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注说明" prop="remarks">
          <el-input
            v-model="applyForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注说明（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="applyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApply" :loading="submitLoading">
            提交申请
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 申请记录对话框 -->
    <el-dialog
      v-model="recordsDialogVisible"
      :title="`${selectedRecordCustomer?.customerName || ''} - 回院申请记录`"
      width="1200px"
      :close-on-click-modal="false"
    >
      <div class="records-dialog-content">
        <div class="records-toolbar">
          <el-form :model="recordsQueryForm" inline>
            <el-form-item label="申请时间">
              <el-date-picker
                v-model="recordsQueryForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                @change="loadReturnRecords"
                style="width: 280px"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="recordsQueryForm.status" placeholder="请选择状态" @change="loadReturnRecords" style="width: 120px">
                <el-option label="全部" value="" />
                <el-option label="待审批" value="SUBMITTED" />
                <el-option label="已通过" value="APPROVED" />
                <el-option label="已拒绝" value="REJECTED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadReturnRecords" :loading="recordsLoading">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetRecordsQuery">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table
          :data="returnRecords"
          v-loading="recordsLoading"
          stripe
          border
          style="width: 100%"
          :header-cell-style="{ 'background-color': '#f8f9fa', 'color': '#333', 'font-weight': '600' }"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="回院事由" width="120" align="center">
            <template #default="{ row }">
              {{ getReasonText(row.returnReason) }}
            </template>
          </el-table-column>
          <el-table-column prop="requestedReturnDate" label="申请回院时间" width="150" align="center">
            <template #default="{ row }">
              {{ formatDateTime(row.requestedReturnDate) }}
            </template>
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
          <el-table-column prop="approverName" label="审批人" width="100" align="center" />
          <el-table-column prop="approveTime" label="审批时间" width="150" align="center">
            <template #default="{ row }">
              {{ row.approveTime ? formatDateTime(row.approveTime) : '—' }}
            </template>
          </el-table-column>
          <el-table-column prop="approveRemark" label="审批备注" min-width="200" align="center" show-overflow-tooltip />
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
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus, Document, InfoFilled } from '@element-plus/icons-vue'
import { healthManagerApi, type Customer } from '@/api/healthManager'
import { submitReturnApply, getMyReturnApplies, type ReturnApply, type ReturnApplyDTO, type ReturnApplyQueryDTO } from '@/api/apply'
import { useUserStore } from '@/stores/user'

// 外出客户接口（与后端OutingCustomerDTO保持一致）
interface OutingCustomer {
  customerId: number // 与后端OutingCustomerDTO一致
  customerName: string
  age?: number
  gender?: string
  idCard?: string
  customerType?: string
  bedInfo: string
  checkInDate?: string
  contractExpireDate?: string
  healthManagerId?: number
  healthManagerName?: string
  outingApplyId: number // 外出申请ID
  outingTime: string // 外出时间（与后端字段名一致）
  expectedReturnTime: string // 预计回院时间（与后端字段名一致）
  outingReason?: string // 外出事由
}

const loading = ref(false)
const submitLoading = ref(false)
const recordsLoading = ref(false)
const total = ref(0)
const outingCustomerList = ref<OutingCustomer[]>([])
const selectedCustomer = ref<OutingCustomer | null>(null)
const selectedRecordCustomer = ref<OutingCustomer | null>(null)

// 对话框状态
const applyDialogVisible = ref(false)
const recordsDialogVisible = ref(false)

// 申请记录相关状态
const returnRecords = ref<ReturnApply[]>([])
const recordsTotal = ref(0)
const recordsCurrentPage = ref(1)
const recordsPageSize = ref(10)

const searchForm = reactive({
  customerName: '',
  page: 1,
  size: 10
})

const applyForm = reactive({
  returnReason: '',
  requestedReturnDate: '',
  remarks: ''
})

const recordsQueryForm = reactive({
  dateRange: [],
  status: ''
})

const applyFormRef = ref<FormInstance>()

const applyFormRules: FormRules = {
  returnReason: [
    { required: true, message: '请选择回院事由', trigger: 'change' }
  ],
  requestedReturnDate: [
    { required: true, message: '请选择申请回院时间', trigger: 'change' }
  ]
}

const userStore = useUserStore()

// 查询外出客户
const searchOutingCustomers = async () => {
  loading.value = true
  try {
    // 这里调用API获取当前健康管家管理的外出客户
    const response = await healthManagerApi.getOutingCustomers({
      current: searchForm.page,
      size: searchForm.size,
      customerName: searchForm.customerName
    })
    
    console.log('外出客户响应:', response)
    
    let customerData: OutingCustomer[] = []
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
        customerData = response as OutingCustomer[]
        totalCount = customerData.length
      }
    }
    
    outingCustomerList.value = customerData
    total.value = totalCount
  } catch (error) {
    console.error('查询外出客户失败:', error)
    ElMessage.error('查询外出客户失败')
    outingCustomerList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.customerName = ''
  searchForm.page = 1
  searchOutingCustomers()
}

// 显示申请对话框
const showApplyDialog = (customer: OutingCustomer) => {
  selectedCustomer.value = customer
  resetApplyForm()
  applyDialogVisible.value = true
}

// 重置申请表单
const resetApplyForm = () => {
  applyForm.returnReason = ''
  applyForm.requestedReturnDate = ''
  applyForm.remarks = ''
  applyFormRef.value?.clearValidate()
}

// 提交回院申请
const submitApply = async () => {
  if (!selectedCustomer.value) {
    ElMessage.warning('请先选择客户')
    return
  }
  
  if (!applyFormRef.value) return
  
  const valid = await applyFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitLoading.value = true
  try {    const data: ReturnApplyDTO = {
      customerId: selectedCustomer.value.customerId,
      outingApplyId: selectedCustomer.value.outingApplyId,
      returnReason: applyForm.returnReason,
      requestedReturnDate: applyForm.requestedReturnDate,
      remarks: applyForm.remarks
    }
    
    await submitReturnApply(data)
    ElMessage.success('回院申请提交成功')
    applyDialogVisible.value = false
    resetApplyForm()
    // 刷新外出客户列表
    searchOutingCustomers()
  } catch (error) {
    console.error('提交申请失败:', error)
    ElMessage.error('提交申请失败')
  } finally {
    submitLoading.value = false
  }
}

// 显示记录对话框
const showRecordsDialog = async (customer: OutingCustomer) => {
  selectedRecordCustomer.value = customer
  recordsDialogVisible.value = true
  await loadReturnRecords()
}

// 加载回院申请记录
const loadReturnRecords = async () => {
  if (!selectedRecordCustomer.value) return
  
  recordsLoading.value = true
  try {    const params: ReturnApplyQueryDTO = {
      current: recordsCurrentPage.value,
      size: recordsPageSize.value,
      customerName: selectedRecordCustomer.value.customerName // 通过客户姓名过滤
    }
    
    // 添加日期范围查询
    if (recordsQueryForm.dateRange && Array.isArray(recordsQueryForm.dateRange) && recordsQueryForm.dateRange.length === 2) {
      params.startDate = recordsQueryForm.dateRange[0]
      params.endDate = recordsQueryForm.dateRange[1]
    }
    
    // 添加状态查询
    if (recordsQueryForm.status) {
      params.applyStatus = recordsQueryForm.status
    }
    
    console.log('回院申请记录查询参数:', params)
    
    const response = await getMyReturnApplies(params)
    
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
    
    returnRecords.value = recordData
    recordsTotal.value = totalCount
    
    console.log('处理后的回院申请记录:', returnRecords.value)
  } catch (error) {
    console.error('加载回院申请记录失败:', error)
    ElMessage.error('加载回院申请记录失败')
    returnRecords.value = []
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
  loadReturnRecords()
}

// 记录分页变化
const handleRecordsPageChange = (page: number) => {
  recordsCurrentPage.value = page
  loadReturnRecords()
}

const handleRecordsSizeChange = (size: number) => {
  recordsPageSize.value = size
  recordsCurrentPage.value = 1
  loadReturnRecords()
}

// 工具方法
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getReasonText = (reason: string) => {
  const reasonMap: Record<string, string> = {
    'SCHEDULED_RETURN': '按期回院',
    'EARLY_RETURN': '提前回院',
    'HEALTH_ISSUE': '身体不适',
    'FAMILY_REQUEST': '家属要求',
    'EMERGENCY': '紧急情况',
    'OTHER': '其他'
  }
  return reasonMap[reason] || reason
}

const getApplyStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'SUBMITTED': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

const getApplyStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'SUBMITTED': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 初始化
onMounted(() => {
  searchOutingCustomers()
})
</script>

<style scoped>
.return-apply {
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
  color: #1f2937;
  font-size: 24px;
  font-weight: 600;
}

.search-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #374151;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.records-dialog-content {
  padding: 10px 0;
}

.records-toolbar {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

/* 表格样式优化 */
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
}
</style>
