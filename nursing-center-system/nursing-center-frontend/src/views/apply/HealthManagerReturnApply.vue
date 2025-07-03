<template>
  <div class="health-manager-return-apply">
    <h2>回院申请</h2>
    
    <!-- 申请表单 -->
    <div class="apply-form-section">
      <el-button type="primary" @click="showApplyDialog" :disabled="!hasOutingCustomers">
        <el-icon><Plus /></el-icon>
        新增回院申请
      </el-button>
      <p v-if="!hasOutingCustomers" class="no-customers-tip">
        当前没有外出状态的客户可以申请回院
      </p>
    </div>

    <!-- 我的申请列表 -->
    <div class="my-applies-section">
      <h3>我的回院申请</h3>
      
      <!-- 搜索表单 -->
      <div class="search-form">
        <el-form :model="searchForm" inline>
          <el-form-item label="客户姓名">
            <el-input 
              v-model="searchForm.customerName" 
              placeholder="请输入客户姓名"
              clearable
            />
          </el-form-item>
          <el-form-item label="申请状态">
            <el-select v-model="searchForm.applyStatus" placeholder="请选择状态" clearable>
              <el-option label="已提交" value="SUBMITTED" />
              <el-option label="审批通过" value="APPROVED" />
              <el-option label="审批拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchApplies">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 申请列表 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="roomNo" label="房间号" width="80" />
        <el-table-column prop="bedNo" label="床位号" width="80" />
        <el-table-column prop="returnReason" label="回院事由" width="200" show-overflow-tooltip />
        <el-table-column prop="requestedReturnDate" label="申请回院时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.requestedReturnDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyStatus" label="申请状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusTagType(row.applyStatus)"
              effect="plain"
            >
              {{ row.applyStatusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 申请对话框 -->
    <el-dialog v-model="applyDialogVisible" title="回院申请" width="600px">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="120px">
        <el-form-item label="选择客户" prop="customerId">
          <el-select 
            v-model="applyForm.customerId" 
            placeholder="请选择外出客户"
            style="width: 100%"
            @change="onCustomerChange"
          >
            <el-option
              v-for="customer in outingCustomers"
              :key="customer.customerId"
              :label="`${customer.customerName} (${customer.roomNo}-${customer.bedNo})`"
              :value="customer.customerId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="回院事由" prop="returnReason">
          <el-input
            v-model="applyForm.returnReason"
            type="textarea"
            :rows="4"
            placeholder="请输入回院事由"
          />
        </el-form-item>
        <el-form-item label="申请回院时间" prop="requestedReturnDate">
          <el-date-picker
            v-model="applyForm.requestedReturnDate"
            type="datetime"
            placeholder="选择回院时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="applyForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="applyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApply" :loading="submitting">提交申请</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="回院申请详情" width="700px">
      <div v-if="currentApply" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户姓名">{{ currentApply.customerName }}</el-descriptions-item>
          <el-descriptions-item label="房间床位">{{ currentApply.roomNo }}-{{ currentApply.bedNo }}</el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="getStatusTagType(currentApply.applyStatus)">
              {{ currentApply.applyStatusDesc }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请回院时间">{{ formatDateTime(currentApply.requestedReturnDate) }}</el-descriptions-item>
          <el-descriptions-item label="外出时间">{{ formatDateTime(currentApply.outingDate) }}</el-descriptions-item>
          <el-descriptions-item label="预计回院时间">{{ formatDateTime(currentApply.expectedReturnDate) }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDateTime(currentApply.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="审批人">{{ currentApply.approverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批时间">
            {{ currentApply.approveTime ? formatDateTime(currentApply.approveTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="实际回院时间">
            {{ currentApply.actualReturnDate ? formatDateTime(currentApply.actualReturnDate) : '-' }}
          </el-descriptions-item>
        </el-descriptions>
        <div class="reason-section">
          <h4>回院事由</h4>
          <p>{{ currentApply.returnReason }}</p>
        </div>
        <div v-if="currentApply.approveRemark" class="remark-section">
          <h4>审批备注</h4>
          <p>{{ currentApply.approveRemark }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { applyApi } from '@/api'
import type { ReturnApply, ReturnApplyQueryDTO, ReturnApplyDTO, OutingApply } from '@/api/apply'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const applyDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const tableData = ref<ReturnApply[]>([])
const currentApply = ref<ReturnApply | null>(null)
const outingCustomers = ref<OutingApply[]>([])

// 搜索表单
const searchForm = reactive<ReturnApplyQueryDTO>({
  customerName: '',
  applyStatus: ''
})

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 申请表单
const applyForm = reactive<ReturnApplyDTO>({
  customerId: 0,
  outingApplyId: 0,
  returnReason: '',
  requestedReturnDate: '',
  remarks: ''
})

const applyFormRef = ref<FormInstance>()

// 申请表单验证规则
const applyRules = {
  customerId: [
    { required: true, message: '请选择客户', trigger: 'change' }
  ],
  returnReason: [
    { required: true, message: '请输入回院事由', trigger: 'blur' },
    { min: 5, max: 500, message: '回院事由长度在 5 到 500 个字符', trigger: 'blur' }
  ],
  requestedReturnDate: [
    { required: true, message: '请选择申请回院时间', trigger: 'change' }
  ]
}

// 计算属性
const hasOutingCustomers = computed(() => outingCustomers.value.length > 0)

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

// 显示申请对话框
const showApplyDialog = async () => {
  await loadOutingCustomers()
  resetApplyForm()
  applyDialogVisible.value = true
}

// 重置申请表单
const resetApplyForm = () => {
  Object.assign(applyForm, {
    customerId: 0,
    outingApplyId: 0,
    returnReason: '',
    requestedReturnDate: '',
    remarks: ''
  })
}

// 客户选择改变事件
const onCustomerChange = (customerId: number) => {
  const selectedCustomer = outingCustomers.value.find(c => c.customerId === customerId)
  if (selectedCustomer) {
    applyForm.outingApplyId = selectedCustomer.id
  }
}

// 加载外出客户列表
const loadOutingCustomers = async () => {
  try {
    // 这里应该调用获取外出客户的API，暂时使用外出申请API
    const response = await applyApi.getMyOutings({
      size: 100,
      status: 'APPROVED'
    })
    // 过滤出还未回院的客户，并解析床位信息
    outingCustomers.value = response.data.records
      .filter((apply: OutingApply) => !apply.actualReturnDate)
      .map((apply: OutingApply) => {
        // 解析bedInfo字段：格式为 "楼栋名-房间号-床位号"
        if (apply.bedInfo) {
          const parts = apply.bedInfo.split('-')
          if (parts.length >= 3) {
            apply.roomNo = parts[1] // 房间号
            apply.bedNo = parts[2]  // 床位号
          }
        }
        return apply
      })
  } catch (error) {
    console.error('加载外出客户失败:', error)
    ElMessage.error('加载外出客户失败')
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
    applyStatus: ''
  })
  pagination.current = 1
  loadApplies()
}

// 加载我的申请列表
const loadApplies = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      current: pagination.current,
      size: pagination.size
    }
    const response = await applyApi.getMyReturns(params)
    tableData.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    console.error('加载回院申请列表失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (row: ReturnApply) => {
  currentApply.value = row
  detailDialogVisible.value = true
}

// 提交申请
const submitApply = async () => {
  if (!applyFormRef.value) return
  
  try {
    await applyFormRef.value.validate()
    submitting.value = true
    
    await applyApi.submitReturn(applyForm)
    ElMessage.success('回院申请提交成功')
    applyDialogVisible.value = false
    loadApplies()
  } catch (error) {
    console.error('提交申请失败:', error)
    ElMessage.error('提交申请失败')
  } finally {
    submitting.value = false
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
  loadOutingCustomers()
})
</script>

<style scoped>
.health-manager-return-apply {
  padding: 20px;
}

.apply-form-section {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.no-customers-tip {
  margin: 10px 0 0 0;
  color: #909399;
  font-size: 14px;
}

.my-applies-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.search-form {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.reason-section,
.remark-section {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.reason-section h4,
.remark-section h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.reason-section p,
.remark-section p {
  margin: 0;
  line-height: 1.6;
  color: #606266;
}

.dialog-footer {
  text-align: right;
}
</style>
