<template>
  <el-dialog
    :model-value="visible"
    title="处理服务监测预警"
    width="700px"
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >    <div v-if="adaptAlertData" class="alert-content">
      <!-- 预警信息 -->
      <el-card class="alert-info" shadow="never">
        <template #header>
          <div class="alert-header">
            <el-icon class="alert-icon" :class="getAlertIconClass(adaptAlertData.level)">
              <Warning v-if="adaptAlertData.level === 'HIGH'" />
              <InfoFilled v-else-if="adaptAlertData.level === 'MEDIUM'" />
              <CircleCheck v-else />
            </el-icon>
            <span class="alert-title">{{ getAlertLevelText(adaptAlertData.level) }}预警</span>
            <el-tag :type="getAlertTagType(adaptAlertData.level)" size="small">
              {{ adaptAlertData.status }}
            </el-tag>
          </div>
        </template>
        
        <el-descriptions :column="2" size="small">
          <el-descriptions-item label="客户姓名">{{ adaptAlertData.customerName }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ adaptAlertData.roomNumber }}</el-descriptions-item>
          <el-descriptions-item label="管理师">{{ adaptAlertData.healthManagerName }}</el-descriptions-item>
          <el-descriptions-item label="预警时间">{{ formatDateTime(adaptAlertData.alertTime) }}</el-descriptions-item>
          <el-descriptions-item label="服务类型">{{ adaptAlertData.serviceType }}</el-descriptions-item>
          <el-descriptions-item label="预警类型">{{ adaptAlertData.alertType }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="alert-message">
          <h4>预警描述</h4>
          <p>{{ adaptAlertData.message }}</p>
        </div>

        <div v-if="adaptAlertData.metrics" class="alert-metrics">
          <h4>相关数据</h4>
          <el-row :gutter="20">
            <el-col v-for="(value, key) in adaptAlertData.metrics" :key="key" :span="8">
              <div class="metric-item">
                <span class="metric-label">{{ getMetricLabel(key) }}</span>
                <span class="metric-value" :class="getMetricValueClass(key, value)">
                  {{ formatMetricValue(key, value) }}
                </span>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 处理表单 -->
      <el-card class="process-form" shadow="never">
        <template #header>
          <span>处理措施</span>
        </template>
        
        <el-form
          ref="formRef"
          :model="processForm"
          :rules="rules"
          label-width="120px"
        >
          <el-form-item label="处理状态" prop="status">
            <el-select v-model="processForm.status" style="width: 100%">
              <el-option label="已确认" value="CONFIRMED" />
              <el-option label="处理中" value="PROCESSING" />
              <el-option label="已解决" value="RESOLVED" />
              <el-option label="误报" value="FALSE_ALARM" />
              <el-option label="需要升级" value="ESCALATED" />
            </el-select>
          </el-form-item>

          <el-form-item label="处理人员" prop="handlerId">
            <el-select v-model="processForm.handlerId" placeholder="选择处理人员" style="width: 100%">
              <el-option
                v-for="staff in staffList"
                :key="staff.id"
                :label="`${staff.name} (${staff.department})`"
                :value="staff.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="处理措施" prop="actions">
            <el-checkbox-group v-model="processForm.actions">
              <el-checkbox label="IMMEDIATE_CARE">立即护理</el-checkbox>
              <el-checkbox label="MEDICAL_ATTENTION">医疗关注</el-checkbox>
              <el-checkbox label="FAMILY_NOTIFY">通知家属</el-checkbox>
              <el-checkbox label="ADJUST_PLAN">调整护理计划</el-checkbox>
              <el-checkbox label="EQUIPMENT_CHECK">设备检查</el-checkbox>
              <el-checkbox label="MEDICATION_REVIEW">药物复查</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="紧急程度" prop="urgency">
            <el-radio-group v-model="processForm.urgency">
              <el-radio label="LOW">低</el-radio>
              <el-radio label="MEDIUM">中</el-radio>
              <el-radio label="HIGH">高</el-radio>
              <el-radio label="CRITICAL">紧急</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="预计完成时间" prop="expectedCompletionTime">
            <el-date-picker
              v-model="processForm.expectedCompletionTime"
              type="datetime"
              placeholder="选择预计完成时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>

          <el-form-item label="处理说明" prop="notes">
            <el-input
              v-model="processForm.notes"
              type="textarea"
              :rows="4"
              placeholder="请详细描述处理过程、采取的措施和注意事项"
            />
          </el-form-item>

          <el-form-item label="后续跟进">
            <el-switch
              v-model="processForm.needFollowUp"
              active-text="需要跟进"
              inactive-text="无需跟进"
            />
          </el-form-item>

          <el-form-item v-if="processForm.needFollowUp" label="跟进时间">
            <el-date-picker
              v-model="processForm.followUpTime"
              type="datetime"
              placeholder="选择跟进时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-form>
      </el-card>      <!-- 历史处理记录 -->
      <el-card v-if="adaptAlertData.processHistory?.length" class="history-records" shadow="never">
        <template #header>
          <span>历史处理记录</span>
        </template>
        
        <el-timeline>
          <el-timeline-item
            v-for="record in adaptAlertData.processHistory"
            :key="record.id"
            :timestamp="formatDateTime(record.processTime)"
            placement="top"
          >
            <el-card shadow="never" class="history-card">
              <div class="history-header">
                <span class="handler-name">{{ record.handlerName }}</span>
                <el-tag size="small" :type="getStatusTagType(record.status)">
                  {{ record.status }}
                </el-tag>
              </div>
              <p class="history-notes">{{ record.notes }}</p>
              <div v-if="record.actions?.length" class="history-actions">
                <el-tag
                  v-for="action in record.actions"
                  :key="action"
                  size="small"
                  class="action-tag"
                >
                  {{ getActionLabel(action) }}
                </el-tag>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          :loading="loading" 
          @click="handleSubmit"
          :disabled="!processForm.status"
        >
          提交处理结果
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Warning, InfoFilled, CircleCheck } from '@element-plus/icons-vue'
import { processServiceAlert, type ServiceAlert, type ServiceMonitor, type AlertProcessDTO } from '@/api/healthManager'

interface Props {
  visible: boolean
  alertData?: ServiceMonitor | ServiceAlert | null
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  alertData: undefined
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

// 适配器函数，将ServiceMonitor转换为ServiceAlert兼容格式
const adaptAlertData = computed(() => {
  if (!props.alertData) return null
  
  const data = props.alertData
  return {
    id: String(data.id),
    customerName: data.customerName,
    roomNumber: 'roomNumber' in data ? data.roomNumber : '未知',
    healthManagerName: data.healthManagerName,
    alertTime: 'alertTime' in data ? data.alertTime : data.createTime,
    serviceType: data.serviceType,
    alertType: 'alertType' in data ? data.alertType : '服务监控',
    level: 'level' in data ? data.level : data.alertLevel,
    status: data.status,
    message: 'message' in data ? data.message : data.alertMessage || '无详细信息',
    metrics: 'metrics' in data ? data.metrics : undefined,
    processHistory: 'processHistory' in data ? data.processHistory : undefined
  } as ServiceAlert
})

// 模拟员工列表
const staffList = ref([
  { id: '1', name: '张护士长', department: '护理部' },
  { id: '2', name: '李医生', department: '医疗部' },
  { id: '3', name: '王康复师', department: '康复部' },
  { id: '4', name: '赵营养师', department: '营养部' }
])

const processForm = reactive<AlertProcessDTO>({
  alertId: '',
  status: '',
  handlerId: '',
  actions: [],
  urgency: 'MEDIUM',
  notes: '',
  expectedCompletionTime: '',
  needFollowUp: false,
  followUpTime: ''
})

const rules: FormRules = {
  status: [
    { required: true, message: '请选择处理状态', trigger: 'change' }
  ],
  handlerId: [
    { required: true, message: '请选择处理人员', trigger: 'change' }
  ],
  notes: [
    { required: true, message: '请输入处理说明', trigger: 'blur' },
    { min: 10, max: 500, message: '处理说明长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  expectedCompletionTime: [
    { required: true, message: '请选择预计完成时间', trigger: 'change' }
  ]
}

watch(
  () => props.visible,
  (visible) => {
    if (visible && adaptAlertData.value) {
      processForm.alertId = adaptAlertData.value.id
      resetForm()
    }
  }
)

const getAlertIconClass = (level: string) => {
  const classMap: Record<string, string> = {
    'HIGH': 'high-alert',
    'MEDIUM': 'medium-alert',
    'LOW': 'low-alert'
  }
  return classMap[level] || 'low-alert'
}

const getAlertLevelText = (level: string) => {
  const textMap: Record<string, string> = {
    'HIGH': '高级',
    'MEDIUM': '中级',
    'LOW': '低级'
  }
  return textMap[level] || '未知'
}

const getAlertTagType = (level: string) => {
  const typeMap: Record<string, string> = {
    'HIGH': 'danger',
    'MEDIUM': 'warning',
    'LOW': 'success'
  }
  return typeMap[level] || 'info'
}

const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'RESOLVED': 'success',
    'PROCESSING': 'warning',
    'CONFIRMED': 'info',
    'FALSE_ALARM': 'info',
    'ESCALATED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getMetricLabel = (key: string) => {
  const labelMap: Record<string, string> = {
    'heartRate': '心率',
    'bloodPressure': '血压',
    'temperature': '体温',
    'oxygenSaturation': '血氧饱和度',
    'bloodSugar': '血糖',
    'weight': '体重'
  }
  return labelMap[key] || key
}

const getMetricValueClass = (key: string, value: any) => {
  // 根据指标和数值返回样式类
  if (key === 'heartRate') {
    const rate = parseInt(value)
    if (rate > 100 || rate < 60) return 'abnormal-value'
  } else if (key === 'temperature') {
    const temp = parseFloat(value)
    if (temp > 37.5 || temp < 36) return 'abnormal-value'
  }
  return 'normal-value'
}

const formatMetricValue = (key: string, value: any) => {
  const unitMap: Record<string, string> = {
    'heartRate': ' bpm',
    'temperature': ' °C',
    'oxygenSaturation': '%',
    'bloodSugar': ' mmol/L',
    'weight': ' kg'
  }
  return value + (unitMap[key] || '')
}

const getActionLabel = (action: string) => {
  const labelMap: Record<string, string> = {
    'IMMEDIATE_CARE': '立即护理',
    'MEDICAL_ATTENTION': '医疗关注',
    'FAMILY_NOTIFY': '通知家属',
    'ADJUST_PLAN': '调整护理计划',
    'EQUIPMENT_CHECK': '设备检查',
    'MEDICATION_REVIEW': '药物复查'
  }
  return labelMap[action] || action
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

const resetForm = () => {
  Object.assign(processForm, {
    alertId: props.alertData?.id || '',
    status: '',
    handlerId: '',
    actions: [],
    urgency: 'MEDIUM',
    notes: '',
    expectedCompletionTime: '',
    needFollowUp: false,
    followUpTime: ''
  })
  formRef.value?.clearValidate()
}

const handleClose = () => {
  emit('update:visible', false)
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    // 验证跟进时间
    if (processForm.needFollowUp && !processForm.followUpTime) {
      ElMessage.error('需要跟进时请选择跟进时间')
      return
    }

    loading.value = true

    await processServiceAlert(processForm)
    ElMessage.success('预警处理成功')

    emit('success')
    emit('update:visible', false)
  } catch (error) {
    console.error('处理失败:', error)
    ElMessage.error('处理失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.alert-content {
  max-height: 70vh;
  overflow-y: auto;
}

.alert-info,
.process-form,
.history-records {
  margin-bottom: 20px;
}

.alert-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.alert-icon {
  font-size: 18px;
}

.alert-icon.high-alert {
  color: #f56c6c;
}

.alert-icon.medium-alert {
  color: #e6a23c;
}

.alert-icon.low-alert {
  color: #67c23a;
}

.alert-title {
  font-weight: bold;
  flex: 1;
}

.alert-message {
  margin-top: 15px;
}

.alert-message h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
}

.alert-message p {
  margin: 0;
  line-height: 1.6;
  color: #303133;
}

.alert-metrics {
  margin-top: 15px;
}

.alert-metrics h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.metric-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 10px;
}

.metric-label {
  font-size: 12px;
  color: #909399;
}

.metric-value {
  font-weight: bold;
}

.metric-value.normal-value {
  color: #67c23a;
}

.metric-value.abnormal-value {
  color: #f56c6c;
}

.history-card {
  margin-bottom: 10px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.handler-name {
  font-weight: bold;
  color: #303133;
}

.history-notes {
  margin: 0;
  color: #606266;
  line-height: 1.5;
}

.history-actions {
  margin-top: 8px;
}

.action-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
