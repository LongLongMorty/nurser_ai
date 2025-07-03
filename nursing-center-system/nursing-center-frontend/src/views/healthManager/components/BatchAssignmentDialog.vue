<template>
  <el-dialog
    :model-value="visible"
    title="批量分配服务"
    width="900px"
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <div class="batch-assignment-container">
      <!-- 分配模式选择 -->
      <el-card class="mode-selection" shadow="never">
        <template #header>
          <span>分配模式</span>
        </template>
        <el-radio-group v-model="assignmentMode" @change="handleModeChange">
          <el-radio-button label="AUTO">智能分配</el-radio-button>
          <el-radio-button label="MANUAL">手动分配</el-radio-button>
          <el-radio-button label="LOAD_BALANCE">负载均衡</el-radio-button>
        </el-radio-group>
        <div class="mode-description">
          <span v-if="assignmentMode === 'AUTO'">
            系统将根据管理师的专长、工作负载和客户需求自动分配
          </span>
          <span v-else-if="assignmentMode === 'MANUAL'">
            手动选择每个客户对应的管理师
          </span>
          <span v-else-if="assignmentMode === 'LOAD_BALANCE'">
            优先考虑管理师的工作负载均衡进行分配
          </span>
        </div>
      </el-card>

      <!-- 筛选条件 -->
      <el-card class="filter-section" shadow="never">
        <template #header>
          <span>筛选条件</span>
        </template>
        <el-form :model="filterForm" label-width="100px" :inline="true">
          <el-form-item label="护理等级">
            <el-select v-model="filterForm.careLevel" placeholder="全部" clearable>
              <el-option label="一级护理" value="LEVEL_1" />
              <el-option label="二级护理" value="LEVEL_2" />
              <el-option label="三级护理" value="LEVEL_3" />
              <el-option label="特级护理" value="SPECIAL" />
            </el-select>
          </el-form-item>
          <el-form-item label="健康状况">
            <el-select v-model="filterForm.healthStatus" placeholder="全部" clearable>
              <el-option label="良好" value="GOOD" />
              <el-option label="一般" value="FAIR" />
              <el-option label="较差" value="POOR" />
              <el-option label="危重" value="CRITICAL" />
            </el-select>
          </el-form-item>
          <el-form-item label="房间号">
            <el-input v-model="filterForm.roomNumber" placeholder="输入房间号" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadCustomers">筛选</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 客户列表 -->
      <el-card class="customer-list" shadow="never">
        <template #header>
          <div class="customer-header">
            <span>待分配客户 ({{ selectedCustomers.length }}/{{ customers.length }})</span>
            <div class="header-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click="selectAll"
                :disabled="customers.length === 0"
              >
                全选
              </el-button>
              <el-button 
                size="small" 
                @click="clearSelection"
                :disabled="selectedCustomers.length === 0"
              >
                清空
              </el-button>
            </div>
          </div>
        </template>

        <el-table
          ref="customerTableRef"
          :data="customers"
          @selection-change="handleSelectionChange"
          max-height="400"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="age" label="年龄" width="80" />
          <el-table-column prop="roomNumber" label="房间号" width="100" />
          <el-table-column prop="careLevel" label="护理等级" width="120">
            <template #default="{ row }">
              <el-tag :type="getCareLevelType(row.careLevel)">{{ row.careLevel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="healthStatus" label="健康状况" width="120">
            <template #default="{ row }">
              <el-tag :type="getHealthStatusType(row.healthStatus)">{{ row.healthStatus }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column 
            v-if="assignmentMode === 'MANUAL'" 
            label="分配管理师" 
            width="200"
          >
            <template #default="{ row }">
              <el-select 
                v-model="manualAssignments[row.id]" 
                placeholder="选择管理师"
                size="small"
                style="width: 180px"
              >
                <el-option
                  v-for="manager in availableManagers"
                  :key="manager.id"                  :label="`${manager.realName} (${manager.currentCustomers || 0})`"
                  :value="manager.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="currentManager" label="当前管理师" />
        </el-table>
      </el-card>

      <!-- 管理师工作负载 -->
      <el-card class="manager-workload" shadow="never">
        <template #header>
          <span>管理师工作负载</span>
        </template>
        <el-row :gutter="20">
          <el-col 
            v-for="manager in availableManagers" 
            :key="manager.id" 
            :span="8"
          >
            <div class="manager-card">
              <div class="manager-info">                <h4>{{ manager.realName }}</h4>
                <p>{{ manager.specialties?.join(', ') || '暂无' }} - {{ manager.status }}</p>
              </div>
              <div class="workload-progress">
                <el-progress 
                  :percentage="getWorkloadPercentage(manager)" 
                  :color="getWorkloadColor(manager)"
                  :show-text="false"
                />
                <span class="workload-text">
                  {{ manager.currentCustomers || 0 }}/{{ manager.maxCustomers || 10 }}
                </span>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 分配设置 -->
      <el-card class="assignment-settings" shadow="never">
        <template #header>
          <span>分配设置</span>
        </template>
        <el-form :model="assignmentSettings" label-width="120px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="服务类型">
                <el-select v-model="assignmentSettings.serviceType" style="width: 100%">
                  <el-option label="日常护理" value="DAILY_CARE" />
                  <el-option label="康复训练" value="REHABILITATION" />
                  <el-option label="健康监测" value="HEALTH_MONITORING" />
                  <el-option label="心理疏导" value="PSYCHOLOGICAL_SUPPORT" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="开始时间">
                <el-date-picker
                  v-model="assignmentSettings.startTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  style="width: 100%"
                  format="YYYY-MM-DD HH:mm"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="服务周期">
                <el-select v-model="assignmentSettings.duration" style="width: 100%">
                  <el-option label="1天" value="1" />
                  <el-option label="1周" value="7" />
                  <el-option label="1月" value="30" />
                  <el-option label="3月" value="90" />
                  <el-option label="长期" value="365" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          :loading="loading" 
          @click="handleBatchAssign"
          :disabled="selectedCustomers.length === 0"
        >
          批量分配 ({{ selectedCustomers.length }})
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type ElTable } from 'element-plus'
import { 
  getHealthManagers, 
  batchCreateServiceAssignments,
  type HealthManager,
  type BatchServiceAssignmentDTO 
} from '@/api/healthManager'
import { getCustomers, type Customer, type CustomerQueryParams } from '@/api/customer'

interface Props {
  visible: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

const customerTableRef = ref<InstanceType<typeof ElTable>>()
const loading = ref(false)
const assignmentMode = ref('AUTO')
const customers = ref<Customer[]>([])
const selectedCustomers = ref<Customer[]>([])
const availableManagers = ref<HealthManager[]>([])
const manualAssignments = ref<Record<string, string>>({})

const filterForm = reactive({
  careLevel: '',
  healthStatus: '',
  roomNumber: ''
})

const assignmentSettings = reactive({
  serviceType: 'DAILY_CARE',
  startTime: '',
  duration: '30'
})

onMounted(() => {
  if (props.visible) {
    loadData()
  }
})

const loadData = async () => {
  await Promise.all([
    loadCustomers(),
    loadManagers()
  ])
}

const loadCustomers = async () => {
  try {
    const params: CustomerQueryParams = {
      customerName: filterForm.roomNumber || undefined  // 暂时将roomNumber作为customerName使用
    }
    const response = await getCustomers(params)
    customers.value = response || []
  } catch (error) {
    console.error('加载客户列表失败:', error)
  }
}

const loadManagers = async () => {  try {
    const response = await getHealthManagers({ status: '1' })
    availableManagers.value = response.data?.records || []
  } catch (error) {
    console.error('加载管理师列表失败:', error)
  }
}

const handleModeChange = () => {
  // 清空手动分配
  manualAssignments.value = {}
}

const resetFilter = () => {
  Object.assign(filterForm, {
    careLevel: '',
    healthStatus: '',
    roomNumber: ''
  })
  loadCustomers()
}

const handleSelectionChange = (selection: Customer[]) => {
  selectedCustomers.value = selection
}

const selectAll = () => {
  customerTableRef.value?.toggleAllSelection()
}

const clearSelection = () => {
  customerTableRef.value?.clearSelection()
}

const getCareLevelType = (level: string) => {
  const typeMap: Record<string, string> = {
    'SPECIAL': 'danger',
    'LEVEL_1': 'warning',
    'LEVEL_2': 'primary',
    'LEVEL_3': 'success'
  }
  return typeMap[level] || 'info'
}

const getHealthStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'CRITICAL': 'danger',
    'POOR': 'warning',
    'FAIR': 'primary',
    'GOOD': 'success'
  }
  return typeMap[status] || 'info'
}

const getWorkloadPercentage = (manager: HealthManager) => {
  const assigned = manager.currentCustomers || 0
  const capacity = manager.maxCustomers || 10
  return Math.round((assigned / capacity) * 100)
}

const getWorkloadColor = (manager: HealthManager) => {
  const percentage = getWorkloadPercentage(manager)
  if (percentage >= 90) return '#f56c6c'
  if (percentage >= 70) return '#e6a23c'
  return '#67c23a'
}

const handleClose = () => {
  emit('update:visible', false)
  resetForm()
}

const resetForm = () => {
  selectedCustomers.value = []
  manualAssignments.value = {}
  Object.assign(assignmentSettings, {
    serviceType: 'DAILY_CARE',
    startTime: '',
    duration: '30'
  })
}

const handleBatchAssign = async () => {
  if (selectedCustomers.value.length === 0) {
    ElMessage.warning('请选择要分配的客户')
    return
  }
  // 验证手动分配模式下是否都选择了管理师
  if (assignmentMode.value === 'MANUAL') {
    const unassigned = selectedCustomers.value.filter((customer: Customer) => !manualAssignments.value[customer.id || 0])
    if (unassigned.length > 0) {
      ElMessage.warning(`请为所有选中的客户分配管理师`)
      return
    }
  }

  try {
    await ElMessageBox.confirm(
      `确定要为 ${selectedCustomers.value.length} 位客户批量分配服务吗？`,
      '确认批量分配',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }    )

    loading.value = true
    
    const batchData: BatchServiceAssignmentDTO = {
      mode: assignmentMode.value,
      customerIds: selectedCustomers.value.map((c: Customer) => String(c.id || 0)),
      manualAssignments: assignmentMode.value === 'MANUAL' ? manualAssignments.value : undefined,
      serviceType: assignmentSettings.serviceType,
      startTime: assignmentSettings.startTime,
      duration: parseInt(assignmentSettings.duration)
    }

    await batchCreateServiceAssignments(batchData)
    ElMessage.success(`成功为 ${selectedCustomers.value.length} 位客户分配了服务`)

    emit('success')
    emit('update:visible', false)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量分配失败:', error)
      ElMessage.error('批量分配失败，请重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.batch-assignment-container {
  max-height: 70vh;
  overflow-y: auto;
}

.mode-selection,
.filter-section,
.customer-list,
.manager-workload,
.assignment-settings {
  margin-bottom: 20px;
}

.mode-selection :deep(.el-card__body) {
  padding: 15px 20px;
}

.mode-description {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

.customer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.manager-card {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 10px;
}

.manager-info h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
}

.manager-info p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.workload-progress {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.workload-text {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
