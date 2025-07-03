<template>
  <div class="apply-management">
    <div class="page-header">
      <h2>
        <el-icon class="header-icon"><Document /></el-icon>
        {{ pageTitle }}
      </h2>
      <div class="header-stats" v-if="currentApplyType === 'checkout'">
        <el-tag size="large" effect="plain">
          共 {{ checkoutPagination.total }} 条申请
        </el-tag>
      </div>
      <div class="header-stats" v-else-if="currentApplyType === 'outing'">
        <el-tag size="large" effect="plain">
          共 {{ outingPagination.total }} 条申请
        </el-tag>
      </div>
    </div>

    <!-- 根据路由决定显示内容 -->
    <div v-if="currentApplyType === 'checkout'" class="apply-section">
      <div class="search-form">
        <el-form :model="checkoutSearchForm" inline>
          <el-form-item label="客户姓名">
            <el-input
              v-model="checkoutSearchForm.customerName"
              placeholder="请输入客户姓名"
              clearable
            />
          </el-form-item>          <el-form-item label="申请人">
            <el-input
              v-model="checkoutSearchForm.applicantName"
              placeholder="请输入申请人姓名"
              clearable
            />
          </el-form-item>
          <el-form-item label="申请状态">
            <el-select
              v-model="checkoutSearchForm.applyStatus"
              placeholder="请选择状态"
              clearable
              style="width: 180px;"
            >
              <el-option label="已提交" value="SUBMITTED" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item label="申请时间">
            <el-date-picker
              v-model="checkoutSearchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleCheckoutSearch">查询</el-button>
            <el-button @click="handleCheckoutReset">重置</el-button>
          </el-form-item>
        </el-form>      </div>

      <div class="table-container">
        <el-table
          :data="checkoutList"
          v-loading="checkoutLoading"
          stripe
          border
          style="width: 100%"
          :header-cell-style="{
            background: '#f5f7fa',
            color: '#303133',
            fontWeight: 'bold',
            fontSize: '14px',
            textAlign: 'center'
          }"
          :row-style="{ height: '60px' }"
          :cell-style="{ padding: '12px 8px' }"
        >
          <el-table-column prop="customerName" label="客户姓名" width="120" align="center">
            <template #default="{ row }">
              <div class="customer-info">
                <el-icon class="customer-icon"><User /></el-icon>
                <span class="customer-name">{{ row.customerName }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="applicantName" label="申请人" width="120" align="center">
            <template #default="{ row }">
              <div class="applicant-info">
                <el-icon class="applicant-icon"><UserFilled /></el-icon>
                <span>{{ row.applicantName }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="checkoutType" label="退住类型" width="120" align="center">
            <template #default="{ row }">
              <el-tag
                :type="getCheckoutTypeTagType(row.checkoutType)"
                size="small"
                effect="plain"
              >
                {{ formatCheckoutType(row.checkoutType) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="checkoutDate" label="退住时间" width="170" align="center">
            <template #default="{ row }">
              <div class="time-info">
                <el-icon class="time-icon"><Calendar /></el-icon>
                <span>{{ formatDateTime(row.checkoutDate) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="checkoutReason" label="退住原因" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="reason-content">
                <el-icon class="reason-icon"><Document /></el-icon>
                <span class="reason-text">{{ row.checkoutReason }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="applyStatus" label="申请状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="getApplyStatusType(row.applyStatus)"
                size="small"
                effect="plain"
              >
                {{ formatApplyStatus(row.applyStatus) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="createTime" label="申请时间" width="170" align="center">
            <template #default="{ row }">
              <div class="create-time">
                <el-icon class="create-time-icon"><Clock /></el-icon>
                <span>{{ formatDateTime(row.createTime) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button link type="primary" size="small" @click="handleViewCheckout(row)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button
                  v-if="row.applyStatus === 'SUBMITTED'"
                  link
                  type="success"
                  size="small"
                  @click="handleApproveCheckout(row)"
                >
                  <el-icon><Check /></el-icon>
                  审批
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="checkoutPagination.page"
            v-model:page-size="checkoutPagination.size"
            :total="checkoutPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleCheckoutSizeChange"
            @current-change="handleCheckoutCurrentChange"
          />
        </div>
      </div>
    </div>    <!-- 外出申请列表 -->
    <div v-else-if="currentApplyType === 'outing'" class="apply-section">
      <div class="search-form">
        <el-form :model="outingSearchForm" inline>
          <el-form-item label="客户姓名">
            <el-input
              v-model="outingSearchForm.customerName"
              placeholder="请输入客户姓名"
              clearable
            />
          </el-form-item>
          <el-form-item label="申请状态">
            <el-select
              v-model="outingSearchForm.applyStatus"
              placeholder="请选择状态"
              clearable
              style="width: 180px;"
            >
              <el-option label="已提交" value="SUBMITTED" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item label="外出时间">
            <el-date-picker
              v-model="outingSearchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleOutingSearch">查询</el-button>
            <el-button @click="handleOutingReset">重置</el-button>
          </el-form-item>
        </el-form>      </div>

      <div class="table-container">
        <el-table
          :data="outingList"
          v-loading="outingLoading"
          stripe
          border
          style="width: 100%"
          :header-cell-style="{
            background: '#f5f7fa',
            color: '#303133',
            fontWeight: 'bold',
            fontSize: '14px',
            textAlign: 'center'
          }"
          :row-style="{ height: '60px' }"
          :cell-style="{ padding: '12px 8px' }"
        >
          <el-table-column prop="customerName" label="客户姓名" width="120" align="center">
            <template #default="{ row }">
              <div class="customer-info">
                <el-icon class="customer-icon"><User /></el-icon>
                <span class="customer-name">{{ row.customerName }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="applicantName" label="申请人" width="120" align="center">
            <template #default="{ row }">
              <div class="applicant-info">
                <el-icon class="applicant-icon"><UserFilled /></el-icon>
                <span>{{ row.applicantName }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="outingReason" label="外出事由" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="reason-content">
                <el-icon class="reason-icon"><Document /></el-icon>
                <span class="reason-text">{{ row.outingReason }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="时间信息" width="340">
            <template #default="{ row }">
              <div class="time-info">
                <div class="time-row">
                  <el-icon class="time-icon"><Clock /></el-icon>
                  <span class="time-label">外出时间：</span>
                  <span class="time-value">{{ formatDateTime(row.outingDate) }}</span>
                </div>
                <div class="time-row">
                  <el-icon class="time-icon"><Back /></el-icon>
                  <span class="time-label">预计回院：</span>
                  <span class="time-value">{{ formatDateTime(row.expectedReturnDate) }}</span>
                </div>
                <div class="time-row" v-if="row.actualReturnDate">
                  <el-icon class="time-icon"><Check /></el-icon>
                  <span class="time-label">实际回院：</span>
                  <span class="time-value">{{ formatDateTime(row.actualReturnDate) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="applyStatus" label="申请状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="getApplyStatusType(row.applyStatus)"
                size="small"
                effect="plain"
              >
                {{ formatApplyStatus(row.applyStatus) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="createTime" label="申请时间" width="170" align="center">
            <template #default="{ row }">
              <div class="create-time">
                <el-icon class="create-time-icon"><Calendar /></el-icon>
                <span>{{ formatDateTime(row.createTime) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button link type="primary" size="small" @click="handleViewOuting(row)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button
                  v-if="row.applyStatus === 'SUBMITTED'"
                  link
                  type="success"
                  size="small"
                  @click="handleApproveOuting(row)"
                >
                  <el-icon><Check /></el-icon>
                  审批
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="outingPagination.page"
            v-model:page-size="outingPagination.size"
            :total="outingPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleOutingSizeChange"
            @current-change="handleOutingCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 退住申请详情弹窗 -->
    <el-dialog
      v-model="checkoutDetailVisible"
      title="退住申请详情"
      width="800px"
    >
      <div v-if="selectedCheckoutApply" class="apply-detail">        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户姓名">{{ selectedCheckoutApply.customerName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ selectedCheckoutApply.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="退住类型">{{ formatCheckoutType(selectedCheckoutApply.checkoutType) }}</el-descriptions-item>
          <el-descriptions-item label="退住时间">{{ selectedCheckoutApply.checkoutDate }}</el-descriptions-item>          <el-descriptions-item label="申请状态">
            <el-tag
              :type="getApplyStatusType(selectedCheckoutApply.applyStatus)"
              size="small"
              class="status-tag"
              effect="light"
            >
              {{ formatApplyStatus(selectedCheckoutApply.applyStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ selectedCheckoutApply.createTime }}</el-descriptions-item>
          <el-descriptions-item label="退住原因" :span="2">{{ selectedCheckoutApply.checkoutReason }}</el-descriptions-item>
          <el-descriptions-item v-if="selectedCheckoutApply.approverName" label="审批人">
            {{ selectedCheckoutApply.approverName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedCheckoutApply.approveTime" label="审批时间">
            {{ selectedCheckoutApply.approveTime }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedCheckoutApply.approveRemark" label="审批备注" :span="2">
            {{ selectedCheckoutApply.approveRemark }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 外出申请详情弹窗 -->
    <el-dialog
      v-model="outingDetailVisible"
      title="外出申请详情"
      width="800px"
    >
      <div v-if="selectedOutingApply" class="apply-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户姓名">{{ selectedOutingApply.customerName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ selectedOutingApply.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="外出时间">{{ selectedOutingApply.outingDate }}</el-descriptions-item>
          <el-descriptions-item label="预计回院时间">{{ selectedOutingApply.expectedReturnDate }}</el-descriptions-item>
          <el-descriptions-item label="实际回院时间">{{ selectedOutingApply.actualReturnDate || '未回院' }}</el-descriptions-item>          <el-descriptions-item label="申请状态">
            <el-tag
              :type="getApplyStatusType(selectedOutingApply.applyStatus)"
              size="small"
              class="status-tag"
              effect="light"
            >
              {{ formatApplyStatus(selectedOutingApply.applyStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ selectedOutingApply.createTime }}</el-descriptions-item>
          <el-descriptions-item label="外出事由" :span="2">{{ selectedOutingApply.outingReason }}</el-descriptions-item>
          <el-descriptions-item v-if="selectedOutingApply.approverName" label="审批人">
            {{ selectedOutingApply.approverName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedOutingApply.approveTime" label="审批时间">
            {{ selectedOutingApply.approveTime }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedOutingApply.approveRemark" label="审批备注" :span="2">
            {{ selectedOutingApply.approveRemark }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 审批弹窗 -->
    <el-dialog
      v-model="approveVisible"
      :title="approveType === 'checkout' ? '退住申请审批' : '外出申请审批'"
      width="600px"
    >
      <ApprovalDialog
        v-if="approveVisible"
        :apply-type="approveType"
        :apply-data="selectedApply"
        @success="handleApproveSuccess"
        @cancel="approveVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  View,
  Check,
  User,
  UserFilled,
  Document,
  Clock,
  Back,
  Calendar
} from '@element-plus/icons-vue'
import { applyApi, type CheckoutApply, type OutingApply } from '@/api/apply'
import ApprovalDialog from './components/ApprovalDialog.vue'

const route = useRoute()

// 根据路由确定当前申请类型
const currentApplyType = computed(() => {
  if (route.path.includes('/customer/checkout')) {
    return 'checkout'
  } else if (route.path.includes('/customer/outing')) {
    return 'outing'
  }
  return 'checkout' // 默认
})

// 页面标题
const pageTitle = computed(() => {
  return currentApplyType.value === 'checkout' ? '退住申请管理' : '外出申请管理'
})

const activeTab = ref('checkout')
const checkoutLoading = ref(false)
const outingLoading = ref(false)
const checkoutDetailVisible = ref(false)
const outingDetailVisible = ref(false)
const approveVisible = ref(false)
const approveType = ref<'checkout' | 'outing'>('checkout')

const checkoutList = ref<CheckoutApply[]>([])
const outingList = ref<OutingApply[]>([])
const selectedCheckoutApply = ref<CheckoutApply | null>(null)
const selectedOutingApply = ref<OutingApply | null>(null)
const selectedApply = ref<any>(null)

const checkoutSearchForm = reactive({
  customerName: '',
  applicantName: '',
  applyStatus: '',
  dateRange: [] as string[]
})

const outingSearchForm = reactive({
  customerName: '',
  applyStatus: '',
  dateRange: [] as string[]
})

const checkoutPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const outingPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 获取退住申请列表
const fetchCheckoutList = async () => {
  checkoutLoading.value = true
  try {    const params = {
      pageNum: checkoutPagination.page,
      pageSize: checkoutPagination.size,
      customerName: checkoutSearchForm.customerName || undefined,
      applicantName: checkoutSearchForm.applicantName || undefined,
      applyStatus: checkoutSearchForm.applyStatus || undefined,
      startDate: checkoutSearchForm.dateRange[0] || undefined,
      endDate: checkoutSearchForm.dateRange[1] || undefined
    };const response = await applyApi.getCheckoutList(params)
    checkoutList.value = response.records
    checkoutPagination.total = response.total
  } catch (error) {
    console.error('获取退住申请列表失败:', error)
    ElMessage.error('获取退住申请列表失败')
  } finally {
    checkoutLoading.value = false
  }
}

// 获取外出申请列表
const fetchOutingList = async () => {
  outingLoading.value = true
  try {    const params = {
      pageNum: outingPagination.page,
      pageSize: outingPagination.size,
      customerName: outingSearchForm.customerName || undefined,
      applyStatus: outingSearchForm.applyStatus || undefined,
      startDate: outingSearchForm.dateRange[0] || undefined,
      endDate: outingSearchForm.dateRange[1] || undefined
    };    const response = await applyApi.getOutingList(params)
    outingList.value = response.records
    outingPagination.total = response.total
  } catch (error) {
    console.error('获取外出申请列表失败:', error)
    ElMessage.error('获取外出申请列表失败')
  } finally {
    outingLoading.value = false  }
}

// 退住申请搜索
const handleCheckoutSearch = () => {
  checkoutPagination.page = 1
  fetchCheckoutList()
}

const handleCheckoutReset = () => {
  checkoutSearchForm.customerName = ''
  checkoutSearchForm.applicantName = ''
  checkoutSearchForm.applyStatus = ''
  checkoutSearchForm.dateRange = []
  handleCheckoutSearch()
}

// 外出申请搜索
const handleOutingSearch = () => {
  outingPagination.page = 1
  fetchOutingList()
}

const handleOutingReset = () => {
  outingSearchForm.customerName = ''
  outingSearchForm.applyStatus = ''
  outingSearchForm.dateRange = []
  handleOutingSearch()
}

// 查看退住申请详情
const handleViewCheckout = (row: CheckoutApply) => {
  selectedCheckoutApply.value = row
  checkoutDetailVisible.value = true
}

// 查看外出申请详情
const handleViewOuting = (row: OutingApply) => {
  selectedOutingApply.value = row
  outingDetailVisible.value = true
}

// 审批退住申请
const handleApproveCheckout = (row: CheckoutApply) => {
  selectedApply.value = row
  approveType.value = 'checkout'
  approveVisible.value = true
}

// 审批外出申请
const handleApproveOuting = (row: OutingApply) => {
  selectedApply.value = row
  approveType.value = 'outing'
  approveVisible.value = true
}

// 审批成功回调
const handleApproveSuccess = () => {
  approveVisible.value = false
  if (approveType.value === 'checkout') {
    fetchCheckoutList()
  } else {
    fetchOutingList()
  }
  ElMessage.success('审批成功')
}

// 分页处理
const handleCheckoutSizeChange = () => {
  fetchCheckoutList()
}

const handleCheckoutCurrentChange = () => {
  fetchCheckoutList()
}

const handleOutingSizeChange = () => {
  fetchOutingList()
}

const handleOutingCurrentChange = () => {
  fetchOutingList()
}

// 格式化方法
const formatCheckoutType = (type: string) => {
  const typeMap: Record<string, string> = {
    'NORMAL': '正常退住',
    'DEATH': '死亡退住',
    'RESERVE': '保留床位'
  }
  return typeMap[type] || type
}

const formatApplyStatus = (status: string) => {
  const statusMap: Record<string, string> = {
    'SUBMITTED': '已提交',
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

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 获取退住类型标签类型
const getCheckoutTypeTagType = (type: string) => {
  switch (type) {
    case 'TEMPORARY': return 'warning'
    case 'PERMANENT': return 'danger'
    default: return 'info'
  }
}

onMounted(() => {
  // 根据当前路由决定加载哪种数据
  if (currentApplyType.value === 'checkout') {
    fetchCheckoutList()
  } else if (currentApplyType.value === 'outing') {
    fetchOutingList()
  }
})
</script>

<style scoped>
.apply-management {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  color: white;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 28px;
  color: #ffffff;
}

.header-stats {
  display: flex;
  gap: 16px;
  align-items: center;
}

.header-stats .el-tag {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 20px;
}

/* 应用模块容器 */
.apply-section {
  margin-top: 24px;
}

/* 搜索表单样式 */
.search-form {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
}

.search-form .el-form-item {
  margin-bottom: 16px;
  margin-right: 24px;
}

.search-form .el-form-item__label {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.search-form .el-input {
  width: 200px;
}

.search-form .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #1890ff 100%);
  border: none;
  border-radius: 6px;
  padding: 10px 20px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.search-form .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
}

.search-form .el-button:not(.el-button--primary) {
  border-color: #dcdfe6;
  color: #606266;
  border-radius: 6px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.search-form .el-button:not(.el-button--primary):hover {
  border-color: #409eff;
  color: #409eff;
}

/* 表格容器样式 */
.table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
}

/* 表格内容样式 */
.customer-info, .applicant-info, .time-info, .reason-content, .create-time {
  display: flex;
  align-items: center;
  gap: 8px;
}

.customer-icon, .applicant-icon {
  color: #409eff;
  font-size: 16px;
}

.time-icon, .create-time-icon {
  color: #67c23a;
  font-size: 16px;
}

.reason-icon {
  color: #e6a23c;
  font-size: 16px;
  flex-shrink: 0;
}

.customer-name {
  font-weight: 600;
  color: #303133;
}

.reason-text {
  line-height: 1.4;
  word-break: break-word;
}

/* 外出申请时间信息特殊样式 */
.time-info .time-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  font-size: 12px;
}

.time-info .time-row:last-child {
  margin-bottom: 0;
}

.time-info .time-label {
  font-weight: 500;
  color: #606266;
  min-width: 70px;
}

.time-info .time-value {
  color: #303133;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-buttons .el-button {
  margin: 0;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  transition: all 0.3s ease;
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
}

/* 分页容器样式 */
.pagination-container {
  padding: 20px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
  display: flex;
  justify-content: flex-end;
}

/* 表格行样式 */
:deep(.el-table .el-table__row:hover) {
  background-color: #f8f9ff !important;
}

:deep(.el-table .el-table__row) {
  transition: background-color 0.3s ease;
}

:deep(.el-table .el-table__header) {
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
}

/* 状态标签样式 */
:deep(.el-tag) {
  border-radius: 16px;
  font-weight: 500;
  padding: 4px 12px;
  font-size: 12px;
}

:deep(.el-tag.el-tag--success) {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67c23a;
  border-color: rgba(103, 194, 58, 0.3);
}

:deep(.el-tag.el-tag--warning) {
  background-color: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
  border-color: rgba(230, 162, 60, 0.3);
}

:deep(.el-tag.el-tag--danger) {
  background-color: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
  border-color: rgba(245, 108, 108, 0.3);
}

:deep(.el-tag.el-tag--info) {
  background-color: rgba(144, 147, 153, 0.1);
  color: #909399;
  border-color: rgba(144, 147, 153, 0.3);
}

/* 分页组件样式 */
:deep(.el-pagination) {
  justify-content: flex-end;
}

:deep(.el-pagination .el-pager li) {
  border-radius: 6px;
  margin: 0 2px;
  transition: all 0.3s ease;
}

:deep(.el-pagination .el-pager li:hover) {
  background-color: #409eff;
  color: white;
}

:deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #409eff 0%, #1890ff 100%);
  color: white;
}

:deep(.el-pagination .btn-prev, .el-pagination .btn-next) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.el-pagination .btn-prev:hover, .el-pagination .btn-next:hover) {
  background-color: #409eff;
  color: white;
}

/* 表单元素样式优化 */
:deep(.el-input__wrapper) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff;
}

:deep(.el-select .el-select__wrapper) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.el-date-editor) {
  border-radius: 6px;
}

/* 弹窗样式 */
.apply-detail {
  padding: 20px 0;
}

/* 状态标签在详情中的样式 */
.status-tag {
  min-width: 72px;
  text-align: center;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 12px;
}

:deep(.el-tag.el-tag--success.status-tag) {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67c23a;
  border-color: rgba(103, 194, 58, 0.2);
}

:deep(.el-tag.el-tag--warning.status-tag) {
  background-color: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
  border-color: rgba(230, 162, 60, 0.2);
}

:deep(.el-tag.el-tag--danger.status-tag) {
  background-color: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
  border-color: rgba(245, 108, 108, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .apply-management {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .search-form .el-form-item {
    margin-right: 0;
    width: 100%;
  }

  .search-form .el-input {
    width: 100%;
  }
    .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
