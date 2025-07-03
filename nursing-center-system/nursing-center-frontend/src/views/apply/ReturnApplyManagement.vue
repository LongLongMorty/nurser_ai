<template>  <div class="return-apply-management">
    <div class="page-header">
      <h2>
        <el-icon class="header-icon"><Back /></el-icon>
        回院申请管理
      </h2>
      <div class="header-stats">
        <el-tag size="large" effect="plain">
          共 {{ pagination.total }} 条申请
        </el-tag>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="客户姓名">
          <el-input
            v-model="searchForm.customerName"
            placeholder="请输入客户姓名"
            clearable
          />
        </el-form-item>        <el-form-item label="申请状态">
          <el-select
            v-model="searchForm.applyStatus"
            placeholder="请选择状态"
            clearable
            style="width: 180px;"
          >
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="审批通过" value="APPROVED" />
            <el-option label="审批拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="onDateRangeChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchApplies">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>    <!-- 申请列表 -->
    <div class="table-container">      <el-table
        :data="tableData"
        v-loading="loading"
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
      ><el-table-column prop="customerName" label="客户姓名" width="120" align="center">
          <template #default="{ row }">
            <div class="customer-info">
              <el-icon class="customer-icon"><User /></el-icon>
              <span class="customer-name">{{ row.customerName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="房间床位" width="140" align="center">
          <template #default="{ row }">
            <div class="bed-info">
              <div class="bed-item">
                <el-icon class="bed-icon"><House /></el-icon>
                <el-tag size="small" type="info">{{ row.roomNo }}</el-tag>
              </div>              <div class="bed-item">
                <el-icon class="bed-icon"><Operation /></el-icon>
                <el-tag size="small" type="warning">{{ row.bedNo }}</el-tag>
              </div>
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

        <el-table-column prop="returnReason" label="回院事由" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="reason-content">
              <el-icon class="reason-icon"><Document /></el-icon>
              <span class="reason-text">{{ row.returnReason }}</span>
            </div>
          </template>
        </el-table-column>
          <el-table-column label="时间信息" width="320">
          <template #default="{ row }">
            <div class="time-info">
              <div class="time-row">
                <el-icon class="time-icon"><Clock /></el-icon>
                <span class="time-label">外出时间：</span>
                <span class="time-value">{{ formatDateTime(row.outingDate) }}</span>
              </div>
              <div class="time-row">
                <el-icon class="time-icon"><Back /></el-icon>
                <span class="time-label">申请回院：</span>
                <span class="time-value">{{ formatDateTime(row.requestedReturnDate) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="applyStatus" label="申请状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="getStatusTagType(row.applyStatus)"
              effect="plain"
              size="small"
            >
              {{ row.applyStatusDesc }}
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
              <el-button link type="primary" size="small" @click="viewDetail(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button
                v-if="row.applyStatus === 'SUBMITTED'"
                link
                type="success"
                size="small"
                @click="approveApply(row)"
              >
                <el-icon><Check /></el-icon>
                审批
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="回院申请详情" width="800px">
      <div v-if="currentApply" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户姓名">{{ currentApply.customerName }}</el-descriptions-item>
          <el-descriptions-item label="房间床位">{{ currentApply.roomNo }}-{{ currentApply.bedNo }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentApply.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="getStatusTagType(currentApply.applyStatus)">
              {{ currentApply.applyStatusDesc }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="外出时间">{{ formatDateTime(currentApply.outingDate) }}</el-descriptions-item>
          <el-descriptions-item label="预计回院时间">{{ formatDateTime(currentApply.expectedReturnDate) }}</el-descriptions-item>
          <el-descriptions-item label="申请回院时间">{{ formatDateTime(currentApply.requestedReturnDate) }}</el-descriptions-item>
          <el-descriptions-item label="实际回院时间">
            {{ currentApply.actualReturnDate ? formatDateTime(currentApply.actualReturnDate) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDateTime(currentApply.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="审批人">{{ currentApply.approverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批时间">
            {{ currentApply.approveTime ? formatDateTime(currentApply.approveTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审批备注">{{ currentApply.approveRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="reason-section">
          <h4>回院事由</h4>
          <p>{{ currentApply.returnReason }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approveDialogVisible" title="审批回院申请" width="600px">
      <el-form ref="approveFormRef" :model="approveForm" :rules="approveRules" label-width="100px">
        <el-form-item label="审批结果" prop="applyStatus">
          <el-radio-group v-model="approveForm.applyStatus">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批备注" prop="approveRemark">
          <el-input
            v-model="approveForm.approveRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入审批备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApproval" :loading="approving">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  View,
  Check,
  User,
  UserFilled,
  House,
  Document,
  Clock,
  Back,
  Calendar,
  Operation
} from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { applyApi } from '@/api'
import type { ReturnApply, ReturnApplyQueryDTO, ReturnApproveDTO } from '@/api/apply'

// 响应式数据
const loading = ref(false)
const approving = ref(false)
const detailDialogVisible = ref(false)
const approveDialogVisible = ref(false)
const tableData = ref<ReturnApply[]>([])
const currentApply = ref<ReturnApply | null>(null)
const dateRange = ref<string[]>([])

// 搜索表单
const searchForm = reactive<ReturnApplyQueryDTO>({
  customerName: '',
  applyStatus: '',
  startDate: '',
  endDate: ''
})

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 审批表单
const approveForm = reactive<ReturnApproveDTO>({
  applyId: 0,
  applyStatus: '',
  approveRemark: ''
})

const approveFormRef = ref<FormInstance>()

// 审批表单验证规则
const approveRules = {
  applyStatus: [
    { required: true, message: '请选择审批结果', trigger: 'change' }
  ]
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'SUBMITTED': return 'warning'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    default: return 'info'
  }
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 日期范围改变事件
const onDateRangeChange = (dates: string[]) => {
  if (dates && dates.length === 2) {
    searchForm.startDate = dates[0]
    searchForm.endDate = dates[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
}

// 搜索申请
const searchApplies = () => {
  pagination.current = 1
  loadApplies()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    customerName: '',
    applyStatus: '',
    startDate: '',
    endDate: ''
  })
  dateRange.value = []
  pagination.current = 1
  loadApplies()
}

// 加载申请列表
const loadApplies = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      current: pagination.current,
      size: pagination.size
    }
    const response = await applyApi.getReturnApplyPage(params)

    // 检查响应结构，适配不同的返回格式
    let dataSource = response.data || response

    if (dataSource && dataSource.records !== undefined) {
      tableData.value = dataSource.records || []
      pagination.total = dataSource.total || 0
    } else {
      tableData.value = []
      pagination.total = 0
      ElMessage.warning('暂无数据')
    }
  } catch (error) {
    console.error('加载回院申请列表失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = async (row: ReturnApply) => {
  try {
    const detail = await applyApi.getReturnApplyDetail(row.id)
    currentApply.value = detail
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取申请详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 审批申请
const approveApply = (row: ReturnApply) => {
  currentApply.value = row
  approveForm.applyId = row.id
  approveForm.applyStatus = ''
  approveForm.approveRemark = ''
  approveDialogVisible.value = true
}

// 提交审批
const submitApproval = async () => {
  if (!approveFormRef.value) return

  try {
    await approveFormRef.value.validate()
    approving.value = true

    await applyApi.approveReturn(approveForm)
    ElMessage.success('审批成功')
    approveDialogVisible.value = false
    loadApplies()
  } catch (error) {
    console.error('审批失败:', error)
    ElMessage.error('审批失败')
  } finally {
    approving.value = false
  }
}

// 分页事件
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadApplies()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadApplies()
}

// 页面加载时获取数据
onMounted(() => {
  loadApplies()
})
</script>

<style scoped>
.return-apply-management {
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
  color: white;
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
  font-weight: 500;
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
.customer-info, .applicant-info, .reason-content, .create-time {
  display: flex;
  align-items: center;
  gap: 8px;
}

.customer-icon, .applicant-icon {
  color: #409eff;
  font-size: 16px;
}

.create-time-icon {
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

/* 床位信息样式 */
.bed-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: center;
}

.bed-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.bed-icon {
  font-size: 14px;
  color: #909399;
}

/* 时间信息样式 */
.time-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.time-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.time-icon {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

.time-label {
  color: #909399;
  min-width: 70px;
  font-size: 12px;
  font-weight: 500;
}

.time-value {
  color: #303133;
  font-weight: 500;
  font-size: 12px;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: center;
}

.action-buttons .el-button {
  margin: 0;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
}

.action-buttons .el-button .el-icon {
  margin-right: 4px;
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

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.12);
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 24px;
  border-bottom: none;
}

:deep(.el-dialog__title) {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 18px;
}

:deep(.el-dialog__body) {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

/* 详情内容样式 */
.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.reason-section {
  margin-top: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border: 1px solid #e4e7ed;
}

.reason-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.reason-section p {
  margin: 0;
  line-height: 1.6;
  color: #606266;
}

/* 对话框底部样式 */
.dialog-footer {
  text-align: right;
  padding: 20px 24px;
  background: #f8f9fa;
  border-top: 1px solid #e4e7ed;
  margin: 0 -24px -24px;
}

.dialog-footer .el-button {
  margin-left: 12px;
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #1890ff 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.dialog-footer .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
}

.dialog-footer .el-button:not(.el-button--primary) {
  border-color: #dcdfe6;
  color: #606266;
  transition: all 0.3s ease;
}

.dialog-footer .el-button:not(.el-button--primary):hover {
  border-color: #409eff;
  color: #409eff;
}

/* 审批表单样式 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

:deep(.el-radio-group .el-radio) {
  margin-right: 24px;
}

:deep(.el-radio__label) {
  font-weight: 500;
}

:deep(.el-textarea .el-textarea__inner) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.el-textarea .el-textarea__inner:hover) {
  border-color: #409eff;
}

:deep(.el-textarea .el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 描述列表样式优化 */
:deep(.el-descriptions) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-descriptions__header) {
  background: #f5f7fa;
  padding: 16px;
}

:deep(.el-descriptions-item__label) {
  font-weight: 600;
  color: #303133;
}

:deep(.el-descriptions-item__content) {
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .return-apply-management {
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
    flex-direction: row;
    gap: 4px;
  }

  .time-info {
    font-size: 11px;
  }

  .time-label {
    min-width: 60px;
  }
}
</style>
