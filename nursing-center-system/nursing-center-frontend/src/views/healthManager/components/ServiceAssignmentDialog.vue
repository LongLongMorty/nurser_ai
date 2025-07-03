<template>
  <el-dialog
    :model-value="visible"
    title="服务分配"
    width="800px"
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      label-position="right"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="健康管理师" prop="healthManagerId">
            <el-select 
              v-model="form.healthManagerId" 
              placeholder="请选择健康管理师" 
              style="width: 100%"
              @change="handleManagerChange"
            >              <el-option
                v-for="manager in healthManagers"
                :key="manager.id"
                :label="`${manager.realName || manager.name} (${manager.employeeId})`"
                :value="manager.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="服务对象" prop="customerId">
            <el-select 
              v-model="form.customerId" 
              placeholder="请选择服务对象" 
              style="width: 100%"
              filterable
              @change="handleCustomerChange"
            >              <el-option
                v-for="customer in customers"
                :key="customer.id"
                :label="`${customer.customerName} - ${customer.roomNo || '未分配床位'}`"
                :value="customer.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">        <el-col :span="12">
          <el-form-item label="服务类型" prop="serviceType">
            <el-select v-model="form.serviceType" placeholder="请选择服务类型" style="width: 100%">
              <el-option label="日常护理" value="DAILY_CARE" />
              <el-option label="康复训练" value="REHABILITATION" />
              <el-option label="健康监测" value="HEALTH_MONITORING" />
              <el-option label="心理疏导" value="PSYCHOLOGICAL_SUPPORT" />
              <el-option label="营养指导" value="NUTRITION_GUIDANCE" />
              <el-option label="用药管理" value="MEDICATION_MANAGEMENT" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="服务内容" prop="serviceContent">
        <el-input
          v-model="form.serviceContent"
          type="textarea"
          :rows="3"
          placeholder="请详细描述服务内容和要求"
        />
      </el-form-item>

      <el-form-item label="特殊要求" prop="specialRequirements">
        <el-input
          v-model="form.specialRequirements"
          type="textarea"
          :rows="2"
          placeholder="请输入特殊要求或注意事项"
        />
      </el-form-item>

      <!-- 管理师信息显示 -->
      <el-card v-if="selectedManager" class="manager-info" shadow="never">
        <template #header>
          <span>管理师信息</span>
        </template>        <el-descriptions :column="2" size="small">
          <el-descriptions-item label="姓名">{{ selectedManager.realName || selectedManager.name }}</el-descriptions-item>
          <el-descriptions-item label="工号">{{ selectedManager.employeeId }}</el-descriptions-item>
          <el-descriptions-item label="专业资质">{{ selectedManager.qualification }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ selectedManager.department }}</el-descriptions-item>
          <el-descriptions-item label="当前负责">{{ selectedManager.assignedCount || selectedManager.currentCustomers || 0 }} 位客户</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 客户信息显示 -->
      <el-card v-if="selectedCustomer" class="customer-info" shadow="never">
        <template #header>
          <span>客户信息</span>
        </template>        <el-descriptions :column="2" size="small">
          <el-descriptions-item label="姓名">{{ selectedCustomer.customerName }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ selectedCustomer.age }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ selectedCustomer.roomNo || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="护理等级">{{ selectedCustomer.careLevelName || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="健康状况">{{ selectedCustomer.status === 1 ? '正常' : '需要关注' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          确认分配
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { 
  getHealthManagers, 
  createServiceAssignment, 
  type HealthManager, 
  type ServiceAssignmentDTO 
} from '@/api/healthManager'
import { getCustomers, type Customer } from '@/api/customer'

interface Props {
  visible: boolean
  data?: any
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  data: undefined
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const healthManagers = ref<HealthManager[]>([])
const customers = ref<Customer[]>([])

const form = reactive<ServiceAssignmentDTO>({
  healthManagerId: '',
  customerId: '',
  serviceType: '',
  startTime: '',
  endTime: '',
  serviceContent: '',
  specialRequirements: ''
})

const selectedManager = computed(() => {
  return healthManagers.value.find(m => m.id === Number(form.healthManagerId))
})

const selectedCustomer = computed(() => {
  return customers.value.find((c: any) => c.id === Number(form.customerId))
})

const rules: FormRules = {
  healthManagerId: [
    { required: true, message: '请选择健康管理师', trigger: 'change' }
  ],
  customerId: [
    { required: true, message: '请选择服务对象', trigger: 'change' }
  ],
  serviceType: [
    { required: true, message: '请选择服务类型', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  serviceContent: [
    { required: true, message: '请输入服务内容', trigger: 'blur' },
    { min: 10, max: 500, message: '服务内容长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadHealthManagers()
  loadCustomers()
})

const loadHealthManagers = async () => {  try {
    const response = await getHealthManagers({ status: '1' })
    // response 是 PageResponse<HealthManager> 类型，包含 data.records
    healthManagers.value = response.data?.records || []
  } catch (error) {
    console.error('加载健康管理师失败:', error)
  }
}

const loadCustomers = async () => {
  try {
    const response = await getCustomers()
    // getCustomers 返回 Customer[] 数组
    customers.value = response || []
  } catch (error) {
    console.error('加载客户列表失败:', error)
  }
}

const handleManagerChange = (managerId: string) => {
  // 可以在这里加载管理师的当前工作负载等信息
  console.log('选择的管理师:', managerId)
}

const handleCustomerChange = (customerId: string) => {
  // 可以在这里加载客户的详细信息
  console.log('选择的客户:', customerId)
}

const resetForm = () => {
  Object.assign(form, {
    healthManagerId: '',
    customerId: '',
    serviceType: '',
    startTime: '',
    endTime: '',
    serviceContent: '',
    specialRequirements: ''
  })
  formRef.value?.clearValidate()
}

const handleClose = () => {
  emit('update:visible', false)
  resetForm()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    // 验证时间
    if (new Date(form.endTime) <= new Date(form.startTime)) {
      ElMessage.error('结束时间必须晚于开始时间')
      return
    }

    loading.value = true

    await createServiceAssignment(form)
    ElMessage.success('服务分配成功')

    emit('success')
    emit('update:visible', false)
  } catch (error) {
    console.error('分配失败:', error)
    ElMessage.error('分配失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.manager-info,
.customer-info {
  margin-top: 20px;
}

.manager-info :deep(.el-card__header),
.customer-info :deep(.el-card__header) {
  padding: 10px 20px;
  background-color: #f5f7fa;
}

.manager-info :deep(.el-card__body),
.customer-info :deep(.el-card__body) {
  padding: 15px 20px;
}
</style>
