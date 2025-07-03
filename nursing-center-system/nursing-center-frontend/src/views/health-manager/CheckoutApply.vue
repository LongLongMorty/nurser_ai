<template>
  <div class="checkout-apply">
    <!-- 页面头部 -->
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>退住申请</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>健康管家</el-breadcrumb-item>
          <el-breadcrumb-item>退住申请</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>

    <!-- 客户列表 -->
    <el-card class="search-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>我的客户列表</span>
          <el-tooltip content="只显示您管理的客户" placement="top">
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
          <el-form-item label="客户状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" style="width: 150px" clearable>
              <el-option label="全部" value="" />
              <el-option label="在住" value="1" />
              <el-option label="外出" value="0" />
            </el-select>
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

      <!-- 客户列表 -->
      <el-table
        :data="customerList"
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
        <el-table-column label="床位信息" width="150" align="center">
          <template #default="{ row }">
            {{ getBedInfo(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="入住时间" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.checkInDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="contractExpireDate" label="合同到期" width="120" align="center">
          <template #default="{ row }">
            {{ formatDate(row.contractExpireDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="careLevelName" label="护理等级" width="100" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '在住' : '外出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="danger" 
              :icon="DocumentRemove"
              @click="showApplyDialog(row)"
              style="border-radius: 6px;"
            >
              申请退住
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
    </el-card>

    <!-- 退住申请对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      :title="`${selectedCustomer?.customerName || ''} - 退住申请`"
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
        
        <el-form-item label="当前床位">
          <el-input :model-value="getBedInfo(selectedCustomer)" disabled />
        </el-form-item>

        <el-form-item label="入住时间">
          <el-input :model-value="formatDate(selectedCustomer?.checkInDate)" disabled />
        </el-form-item>

        <el-form-item label="退住类型" prop="checkoutType">
          <el-select v-model="applyForm.checkoutType" placeholder="请选择退住类型" style="width: 100%">
            <el-option label="正常退住" value="NORMAL" />
            <el-option label="死亡退住" value="DEATH" />
            <el-option label="保留床位" value="RESERVE" />
          </el-select>
        </el-form-item>

        <el-form-item label="退住时间" prop="checkoutDate">
          <el-date-picker
            v-model="applyForm.checkoutDate"
            type="date"
            placeholder="选择退住时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="退住原因" prop="checkoutReason">
          <el-input
            v-model="applyForm.checkoutReason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明退住原因"
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
      title="退住申请记录"
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
                @change="loadCheckoutRecords"
                style="width: 280px"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="recordsQueryForm.status" placeholder="请选择状态" @change="loadCheckoutRecords" style="width: 120px">
                <el-option label="全部" value="" />
                <el-option label="待审批" value="SUBMITTED" />
                <el-option label="已通过" value="APPROVED" />
                <el-option label="已拒绝" value="REJECTED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadCheckoutRecords" :loading="recordsLoading">
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
          :data="checkoutRecords"
          v-loading="recordsLoading"
          stripe
          border
          style="width: 100%"
          :header-cell-style="{ 'background-color': '#f8f9fa', 'color': '#333', 'font-weight': '600' }"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="customerName" label="客户姓名" width="100" align="center" />
          <el-table-column label="退住类型" width="100" align="center">
            <template #default="{ row }">
              {{ getCheckoutTypeText(row.checkoutType) }}
            </template>
          </el-table-column>
          <el-table-column prop="checkoutDate" label="退住时间" width="120" align="center" />
          <el-table-column prop="checkoutReason" label="退住原因" min-width="200" show-overflow-tooltip />
          <el-table-column label="申请状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.applyStatus)">
                {{ getStatusText(row.applyStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="150" align="center">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="approverName" label="审批人" width="100" align="center" />
          <el-table-column prop="approveTime" label="审批时间" width="150" align="center">
            <template #default="{ row }">
              {{ row.approveTime ? formatDateTime(row.approveTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="approveRemark" label="审批备注" min-width="150" show-overflow-tooltip />
        </el-table>

        <!-- 申请记录分页 -->
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
import { Search, Refresh, DocumentRemove, InfoFilled } from '@element-plus/icons-vue'
import { healthManagerApi, type Customer } from '@/api/healthManager'
import { getMyCheckoutApplies, type CheckoutApply, type CheckoutApplyDTO, type CheckoutApplyQueryDTO } from '@/api/apply'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const recordsLoading = ref(false)
const total = ref(0)
const customerList = ref<Customer[]>([])
const selectedCustomer = ref<Customer | null>(null)

// 对话框状态
const applyDialogVisible = ref(false)
const recordsDialogVisible = ref(false)

// 申请记录相关状态
const checkoutRecords = ref<CheckoutApply[]>([])
const recordsTotal = ref(0)
const recordsCurrentPage = ref(1)
const recordsPageSize = ref(10)

const searchForm = reactive({
  customerName: '',
  status: '',
  page: 1,
  size: 10
})

const applyForm = reactive({
  checkoutType: '',
  checkoutDate: '',
  checkoutReason: ''
})

const recordsQueryForm = reactive({
  dateRange: [],
  status: ''
})

const applyFormRef = ref<FormInstance>()

const applyFormRules: FormRules = {
  checkoutType: [
    { required: true, message: '请选择退住类型', trigger: 'change' }
  ],
  checkoutDate: [
    { required: true, message: '请选择退住时间', trigger: 'change' }
  ],
  checkoutReason: [
    { required: true, message: '请输入退住原因', trigger: 'blur' },
    { min: 10, message: '退住原因至少10个字符', trigger: 'blur' }
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
    
    console.log('客户列表响应:', response)
    
    let customerData: Customer[] = []
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
        customerData = response as Customer[]
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
  searchForm.status = ''
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
  applyForm.checkoutType = ''
  applyForm.checkoutDate = ''
  applyForm.checkoutReason = ''
  applyFormRef.value?.clearValidate()
}

// 提交退住申请
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
    const data: CheckoutApplyDTO = {
      customerId: selectedCustomer.value.id,
      checkoutType: applyForm.checkoutType,
      checkoutDate: applyForm.checkoutDate,
      checkoutReason: applyForm.checkoutReason
    }
    
    // 调用健康管家端的退住申请接口
    await request.post('/api/health-manager-portal/checkout-apply', data)
    
    ElMessage.success('退住申请提交成功')
    applyDialogVisible.value = false
    resetApplyForm()
    // 刷新客户列表
    searchCustomers()
  } catch (error) {
    console.error('提交申请失败:', error)
    ElMessage.error('提交申请失败')
  } finally {
    submitLoading.value = false
  }
}

// 显示申请记录对话框
const showRecordsDialog = () => {
  recordsDialogVisible.value = true
  resetRecordsQuery()
  loadCheckoutRecords()
}

// 加载退住申请记录
const loadCheckoutRecords = async () => {
  recordsLoading.value = true
  try {
    const params: CheckoutApplyQueryDTO = {
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
    
    console.log('退住申请记录查询参数:', params)
    
    const response = await getMyCheckoutApplies(params)
    
    let recordData: any[] = []
    let totalCount = 0
    
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
    
    checkoutRecords.value = recordData
    recordsTotal.value = totalCount
  } catch (error) {
    console.error('查询退住申请记录失败:', error)
    ElMessage.error('查询退住申请记录失败')
    checkoutRecords.value = []
    recordsTotal.value = 0
  } finally {
    recordsLoading.value = false
  }
}

// 重置申请记录查询条件
const resetRecordsQuery = () => {
  recordsQueryForm.dateRange = []
  recordsQueryForm.status = ''
  recordsCurrentPage.value = 1
  recordsPageSize.value = 10
}

// 申请记录分页处理
const handleRecordsSizeChange = (size: number) => {
  recordsPageSize.value = size
  recordsCurrentPage.value = 1
  loadCheckoutRecords()
}

const handleRecordsPageChange = (page: number) => {
  recordsCurrentPage.value = page
  loadCheckoutRecords()
}

// 工具方法
const getBedInfo = (customer: Customer | null) => {
  if (!customer) return '-'
  return customer.buildingName && customer.roomNo && customer.bedNo 
    ? `${customer.buildingName}-${customer.roomNo}-${customer.bedNo}`
    : '-'
}

const formatDate = (date: string | undefined) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (dateTime: string | undefined) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getCheckoutTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'NORMAL': '正常退住',
    'DEATH': '死亡退住',
    'RESERVE': '保留床位'
  }
  return typeMap[type] || type
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'SUBMITTED': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'SUBMITTED': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 页面挂载时查询数据
onMounted(() => {
  searchCustomers()
})
</script>

<style scoped>
.checkout-apply {
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

.page-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.search-card {
  margin-bottom: 20px;
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
  padding: 20px 0;
}

.records-dialog-content {
  max-height: 600px;
}

.records-toolbar {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}

:deep(.el-table__header) {
  background-color: #f8f9fa;
}

:deep(.el-table__row:hover > td) {
  background-color: #f0f9ff;
}

/* 对话框样式优化 */
:deep(.el-dialog__header) {
  padding: 20px 20px 10px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px 20px;
  border-top: 1px solid #ebeef5;
}

/* 表单样式优化 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-button) {
  border-radius: 6px;
  font-weight: 500;
}

/* 标签样式优化 */
:deep(.el-tag) {
  border-radius: 4px;
  font-weight: 500;
}

/* 卡片样式优化 */
:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafbfc;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
