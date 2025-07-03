<template>
  <div class="bed-swap-dialog">
    <el-form :model="swapForm" :rules="rules" ref="formRef" label-width="100px">
      <div v-if="sourceBed" class="source-bed-info">
        <h4>源床位信息</h4>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="床位位置">
            {{ sourceBed.buildingNo }}栋{{ sourceBed.floorNo }}楼{{ sourceBed.roomNo }}室{{ sourceBed.bedNo }}床
          </el-descriptions-item>
          <el-descriptions-item label="当前客户">
            {{ sourceBed.customerName }}
          </el-descriptions-item>
        </el-descriptions>
      </div>      <el-form-item label="选择楼层" prop="selectedFloor">
        <el-select
          v-model="swapForm.selectedFloor"
          placeholder="请选择楼层"
          @change="handleFloorChange"
        >
          <el-option
            v-for="floor in floorOptions"
            :key="floor"
            :label="`${floor}楼`"
            :value="floor"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="选择房间" prop="selectedRoom" v-if="swapForm.selectedFloor">
        <el-select
          v-model="swapForm.selectedRoom"
          placeholder="请选择房间"
          @change="handleRoomChange"
        >
          <el-option
            v-for="room in roomOptions"
            :key="room.id"
            :label="`${room.roomNo} - ${room.roomName}`"
            :value="room.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="目标床位" prop="targetBedId" v-if="swapForm.selectedRoom">
        <el-select
          v-model="swapForm.targetBedId"
          placeholder="请选择空闲床位"
          @change="handleTargetBedChange"
        >
          <el-option
            v-for="bed in availableBeds"
            :key="bed.id"
            :label="`${bed.bedNo}床 (${formatBedType(bed.bedType)})`"
            :value="bed.id"
          />
        </el-select>
      </el-form-item><div v-if="targetBedInfo" class="target-bed-info">
        <h4>目标床位信息</h4>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="床位位置">
            {{ targetBedInfo.buildingNo }}栋{{ targetBedInfo.floorNo }}楼{{ targetBedInfo.roomNo }}室{{ targetBedInfo.bedNo }}床
          </el-descriptions-item>
          <el-descriptions-item label="床位类型">
            {{ formatBedType(targetBedInfo.bedType) }}
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(targetBedInfo.bedStatus)">
              {{ formatBedStatus(targetBedInfo.bedStatus) }}
            </el-tag>
          </el-descriptions-item>          <el-descriptions-item label="当前客户">
            空闲床位
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <el-form-item label="交换原因" prop="swapReason">
        <el-input
          v-model="swapForm.swapReason"
          type="textarea"
          :rows="3"
          placeholder="请输入床位交换原因"
        />
      </el-form-item>

      <el-form-item label="交换时间" prop="swapDate">
        <el-date-picker
          v-model="swapForm.swapDate"
          type="datetime"
          placeholder="请选择交换时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>

      <el-form-item label="备注">
        <el-input
          v-model="swapForm.remark"
          type="textarea"
          :rows="2"
          placeholder="请输入备注信息（可选）"
        />
      </el-form-item>      <div class="swap-preview" v-if="swapForm.targetBedId">
        <el-alert
          title="调换预览"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="preview-content">
              <div class="swap-arrow">
                <div class="bed-info">
                  <div class="bed-location">{{ sourceBed?.buildingNo }}栋{{ sourceBed?.floorNo }}楼{{ sourceBed?.roomNo }}室{{ sourceBed?.bedNo }}床</div>
                  <div class="customer-name">{{ sourceBed?.customerName }}</div>
                </div>
                <el-icon class="arrow-icon"><Right /></el-icon>
                <div class="bed-info">
                  <div class="bed-location">{{ targetBedInfo?.buildingNo }}栋{{ targetBedInfo?.floorNo }}楼{{ targetBedInfo?.roomNo }}室{{ targetBedInfo?.bedNo }}床</div>
                  <div class="customer-name">空闲床位</div>
                </div>
              </div>
              <div class="swap-note">
                客户将调换到空闲床位
              </div>
            </div>
          </template>
        </el-alert>
      </div></el-form>

    <div class="dialog-footer">
      <el-button @click="handleCancel">取消</el-button>      <el-button type="primary" @click="handleConfirm" :loading="loading">
        确认调换
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Right } from '@element-plus/icons-vue'
import { bedUsageApi, type BedInfo, type BedSwap, type RoomInfo } from '@/api/bed'

interface Props {
  sourceBed?: BedInfo | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  success: []
  cancel: []
}>()

const loading = ref(false)
const formRef = ref()
const availableBeds = ref<BedInfo[]>([])
const roomOptions = ref<RoomInfo[]>([])
const floorOptions = ref([1, 2, 3, 4, 5])

const swapForm = reactive({
  sourceCustomerId: 0,
  sourceBedId: 0,
  targetCustomerId: 0,
  targetBedId: '',
  selectedFloor: '',
  selectedRoom: '',
  swapReason: '',
  swapDate: '',
  remark: ''
})

const rules = {
  selectedFloor: [
    { required: true, message: '请选择楼层', trigger: 'change' }
  ],
  selectedRoom: [
    { required: true, message: '请选择房间', trigger: 'change' }
  ],
  targetBedId: [
    { required: true, message: '请选择目标床位', trigger: 'change' }
  ],
  swapReason: [
    { required: true, message: '请输入调换原因', trigger: 'blur' }
  ],
  swapDate: [
    { required: true, message: '请选择调换时间', trigger: 'change' }
  ]
}

// 目标床位信息
const targetBedInfo = computed(() => {
  if (!swapForm.targetBedId) return null
  return availableBeds.value.find(bed => bed.id === Number(swapForm.targetBedId))
})

// 楼层变化处理
const handleFloorChange = async (floorNo: number) => {
  try {
    console.log('选择楼层:', floorNo)
    swapForm.selectedRoom = ''
    swapForm.targetBedId = ''
    roomOptions.value = []
    availableBeds.value = []
    
    const response = await bedUsageApi.getRoomsByFloorForSwap(floorNo)
    console.log('楼层房间响应:', response)
    
    roomOptions.value = response.data || response || []
    console.log('房间选项:', roomOptions.value)
  } catch (error) {
    console.error('获取房间列表失败:', error)
    ElMessage.error('获取房间列表失败')
  }
}

// 房间变化处理
const handleRoomChange = async (roomId: number) => {
  try {
    console.log('选择房间:', roomId)
    swapForm.targetBedId = ''
    availableBeds.value = []
    
    const response = await bedUsageApi.getAvailableBedsForSwap(roomId)
    console.log('房间空闲床位响应:', response)
    
    availableBeds.value = response.data || response || []
    console.log('空闲床位:', availableBeds.value)
  } catch (error) {
    console.error('获取空闲床位失败:', error)
    ElMessage.error('获取空闲床位失败')
  }
}

// 处理目标床位变化
const handleTargetBedChange = (bedId: string) => {
  console.log('选择床位:', bedId)
}

// 确认调换
const handleConfirm = async () => {
  try {
    await formRef.value.validate()
    
    const confirmMessage = `确认要将 ${props.sourceBed?.customerName} 调换到 ${targetBedInfo.value?.buildingNo}栋${targetBedInfo.value?.floorNo}楼${targetBedInfo.value?.roomNo}室${targetBedInfo.value?.bedNo}床 吗？`

    await ElMessageBox.confirm(confirmMessage, '确认调换', {
      type: 'warning'
    })

    loading.value = true

    const swapData: BedSwap = {
      sourceCustomerId: props.sourceBed?.customerId || 0,
      sourceBedId: props.sourceBed?.id || 0,
      targetCustomerId: 0, // 调换到空床位，无目标客户
      targetBedId: Number(swapForm.targetBedId),
      swapReason: swapForm.swapReason,
      swapDate: swapForm.swapDate,
      remark: swapForm.remark
    }
    
    await bedUsageApi.swapBed(swapData)
    ElMessage.success('床位调换成功')
    emit('success')
  } catch (error: any) {
    if (error.action !== 'cancel') {
      console.error('床位调换失败:', error)
      ElMessage.error('床位调换失败')
    }
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 格式化方法
const formatBedType = (type: string) => {
  const typeMap: Record<string, string> = {
    'STANDARD': '标准床',
    'CARE': '护理床'
  }
  return typeMap[type] || type
}

const formatBedStatus = (status: string) => {
  const statusMap: Record<string, string> = {
    'AVAILABLE': '空闲',
    'OCCUPIED': '已入住',
    'OUT': '外出'
  }
  return statusMap[status] || status
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'AVAILABLE': 'success',
    'OCCUPIED': 'warning',
    'OUT': 'info'
  }
  return typeMap[status] || 'info'
}

onMounted(() => {
  if (props.sourceBed) {
    swapForm.sourceCustomerId = props.sourceBed.customerId || 0
    swapForm.sourceBedId = props.sourceBed.id
  }
  
  // 设置默认调换时间为当前时间
  swapForm.swapDate = new Date().toISOString().slice(0, 19).replace('T', ' ')
})
</script>

<style scoped>
.bed-swap-dialog {
  padding: 20px;
}

.source-bed-info,
.target-bed-info {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.source-bed-info h4,
.target-bed-info h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.target-bed-form {
  margin-bottom: 20px;
}

.swap-preview {
  margin-top: 20px;
}

.preview-content {
  padding: 10px 0;
}

.swap-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 10px;
}

.bed-info {
  flex: 1;
  text-align: center;
  padding: 10px;
  background: white;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.bed-location {
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.customer-name {
  color: #666;
  font-size: 14px;
}

.arrow-icon {
  font-size: 20px;
  color: #409EFF;
}

.swap-note {
  text-align: center;
  color: #E6A23C;
  font-size: 14px;
  font-weight: bold;
}

.dialog-footer {
  text-align: right;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}
</style>
