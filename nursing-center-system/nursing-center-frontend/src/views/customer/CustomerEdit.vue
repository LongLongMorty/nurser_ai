<template>
  <div class="customer-edit">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑客户' : '新增客户' }}</h2>
      <div class="header-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          保存
        </el-button>
      </div>
    </div>

    <div class="form-content">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        label-position="left"
      >
        <el-card class="form-card" shadow="never">
          <template #header>
            <span>基本信息</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="姓名" prop="customerName">
                <el-input v-model="formData.customerName" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="formData.gender">
                  <el-radio value="MALE">男</el-radio>
                  <el-radio value="FEMALE">女</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="出生日期" prop="birthDate">
                <el-date-picker
                  v-model="formData.birthDate"
                  type="date"
                  placeholder="请选择出生日期"
                  style="width: 100%"
                  @change="calculateAge"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="年龄">
                <el-input v-model.number="formData.age" readonly placeholder="根据出生日期自动计算" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="血型">
                <el-select v-model="formData.bloodType" placeholder="请选择血型" clearable>
                  <el-option label="A型" value="A" />
                  <el-option label="B型" value="B" />
                  <el-option label="AB型" value="AB" />
                  <el-option label="O型" value="O" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="监护人姓名">
                <el-input v-model="formData.guardianName" placeholder="请输入监护人姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="监护人电话">
                <el-input v-model="formData.guardianPhone" placeholder="请输入监护人电话" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <el-card class="form-card" shadow="never">
          <template #header>
            <span>居住信息</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="楼栋" prop="buildingId">
                <el-select 
                  v-model="formData.buildingId" 
                  placeholder="请选择楼栋"
                  @change="onBuildingChange"
                >
                  <el-option
                    v-for="building in buildingOptions"
                    :key="building.id"
                    :label="building.buildingName"
                    :value="building.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="房间" prop="roomId">
                <el-select 
                  v-model="formData.roomId" 
                  placeholder="请选择房间"
                  @change="onRoomChange"
                  :disabled="!formData.buildingId"
                >
                  <el-option
                    v-for="room in roomOptions"
                    :key="room.id"
                    :label="room.roomNo"
                    :value="room.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="床位" prop="bedId">
                <el-select 
                  v-model="formData.bedId" 
                  placeholder="请选择床位"
                  :disabled="!formData.roomId"
                >
                  <el-option
                    v-for="bed in bedOptions"
                    :key="bed.id"
                    :label="bed.bedNo"
                    :value="bed.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="入住时间" prop="checkInDate">
                <el-date-picker
                  v-model="formData.checkInDate"
                  type="date"
                  placeholder="请选择入住时间"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="合同到期时间" prop="contractExpireDate">
                <el-date-picker
                  v-model="formData.contractExpireDate"
                  type="date"
                  placeholder="请选择合同到期时间"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <el-card class="form-card" shadow="never">
          <template #header>
            <span>服务信息</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="客户类型">
                <el-radio-group v-model="formData.customerType">
                  <el-radio value="SELF_CARE">自理老人</el-radio>
                  <el-radio value="CARE">护理老人</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="护理级别">
                <el-select v-model="formData.careLevelId" placeholder="请选择护理级别" clearable>
                  <el-option
                    v-for="level in careLevelOptions"
                    :key="level.id"
                    :label="level.levelName"
                    :value="level.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="健康管家">
                <el-select v-model="formData.healthManagerId" placeholder="请选择健康管家" clearable>
                  <el-option
                    v-for="manager in healthManagerOptions"
                    :key="manager.id"
                    :label="manager.realName"
                    :value="manager.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { customerApi, type CustomerDTO } from '@/api/customer'
import { systemApi, type CareLevel } from '@/api/system'
import { healthManagerApi, type HealthManager } from '@/api/healthManager'
import { buildingApi, type Building } from '@/api/building'
import { roomApi, type Room } from '@/api/room'
import { bedApi, type BedInfo } from '@/api/bed'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 判断是否为编辑模式
const isEdit = computed(() => !!route.params.id)

// 表单数据
const formData = reactive<CustomerDTO>({
  customerName: '',
  gender: 'MALE',
  idCard: '',
  birthDate: '',
  age: 0,
  bloodType: '',
  guardianName: '',
  guardianPhone: '',
  buildingId: 0,
  roomId: 0,
  bedId: 0,
  checkInDate: '',
  contractExpireDate: '',
  customerType: 'SELF_CARE',
  careLevelId: undefined,
  healthManagerId: undefined,
  status: 1
})

// 选项数据
const buildingOptions = ref<Building[]>([])
const roomOptions = ref<Room[]>([])
const bedOptions = ref<BedInfo[]>([])
const careLevelOptions = ref<CareLevel[]>([])
const healthManagerOptions = ref<HealthManager[]>([])

// 表单验证规则
const formRules: FormRules = {
  customerName: [
    { required: true, message: '请输入客户姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  buildingId: [
    { required: true, message: '请选择楼栋', trigger: 'change' }
  ],
  roomId: [
    { required: true, message: '请选择房间', trigger: 'change' }
  ],
  bedId: [
    { required: true, message: '请选择床位', trigger: 'change' }
  ],
  checkInDate: [
    { required: true, message: '请选择入住时间', trigger: 'change' }
  ],
  contractExpireDate: [
    { required: true, message: '请选择合同到期时间', trigger: 'change' }
  ]
}

// 根据出生日期计算年龄
const calculateAge = () => {
  if (formData.birthDate) {
    const birthDate = new Date(formData.birthDate)
    const today = new Date()
    let age = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--
    }
    
    formData.age = age
  }
}

// 楼栋变化时重置房间和床位
const onBuildingChange = () => {
  formData.roomId = 0
  formData.bedId = 0
  roomOptions.value = []
  bedOptions.value = []
  if (formData.buildingId) {
    loadRooms()
  }
}

// 房间变化时重置床位
const onRoomChange = () => {
  formData.bedId = 0
  bedOptions.value = []
  if (formData.roomId) {
    loadBeds()
  }
}

// 加载楼栋列表
const loadBuildings = async () => {
  try {
    const response = await buildingApi.getEnabledBuildings()
    buildingOptions.value = response.data || []
  } catch (error) {
    console.error('加载楼栋列表失败:', error)
    ElMessage.error('加载楼栋列表失败')
  }
}

// 加载房间列表
const loadRooms = async () => {
  try {
    if (!formData.buildingId) return
    const response = await roomApi.getRoomsByBuildingId(formData.buildingId)
    roomOptions.value = response.data || []
  } catch (error) {
    console.error('加载房间列表失败:', error)
    ElMessage.error('加载房间列表失败')
  }
}

// 加载床位列表
const loadBeds = async () => {
  try {
    if (!formData.roomId) return
    const response = await bedApi.getAvailableBedsByRoom(formData.roomId)
    bedOptions.value = response.data || []
  } catch (error) {
    console.error('加载床位列表失败:', error)
    ElMessage.error('加载床位列表失败')
  }
}

// 加载护理级别选项
const loadCareLevels = async () => {
  try {
    const response = await systemApi.getCareLevels({ page: 1, size: 100 })
    careLevelOptions.value = response.data.records
  } catch (error) {
    console.error('加载护理级别失败:', error)
  }
}

// 加载健康管家选项
const loadHealthManagers = async () => {
  try {
    const response = await healthManagerApi.getHealthManager.list({ page: 1, size: 100 })
    healthManagerOptions.value = response.data.records
  } catch (error) {
    console.error('加载健康管家失败:', error)
  }
}

// 加载客户详情（编辑模式）
const loadCustomerDetail = async () => {
  const customerId = Number(route.params.id)
  if (!customerId) return
  
  try {
    const customer = await customerApi.getById(customerId)
    Object.assign(formData, customer)
    
    // 加载对应的房间和床位选项
    if (customer.buildingId) {
      await loadRooms()
      if (customer.roomId) {
        await loadBeds()
      }
    }
  } catch (error: any) {
    console.error('加载客户详情失败:', error)
    ElMessage.error('加载客户详情失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    if (isEdit.value) {
      await customerApi.update(formData)
      ElMessage.success('客户信息更新成功')
    } else {
      await customerApi.create(formData)
      ElMessage.success('客户创建成功')
    }
    
    router.push('/customer/list')
  } catch (error: any) {
    console.error('保存客户失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitLoading.value = false
  }
}

// 取消操作
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确认取消？未保存的数据将丢失。', '确认', {
      type: 'warning'
    })
    router.push('/customer/list')
  } catch (error) {
    // 用户取消
  }
}

// 初始化
onMounted(async () => {
  await Promise.all([
    loadBuildings(),
    loadCareLevels(),
    loadHealthManagers()
  ])
  
  if (isEdit.value) {
    await loadCustomerDetail()
  }
})
</script>

<style scoped>
.customer-edit {
  background: white;
  padding: 20px;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-card {
  margin-bottom: 20px;
}

:deep(.el-card__header) {
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>
