<template>
  <div class="checkin-registration">
    <div class="page-header">
      <h2>
        <el-icon class="header-icon"><User /></el-icon>
        入住登记
      </h2>
      <el-button @click="handleBack">返回客户列表</el-button>
    </div>

    <!-- 查询客户列表 -->
    <el-card class="search-card" title="客户信息查询">
      <el-form :model="searchForm" inline>
        <el-form-item label="客户姓名">
          <el-input
            v-model="searchForm.customerName"
            placeholder="请输入客户姓名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>        <el-form-item label="老人类型">
          <el-select v-model="searchForm.customerType" placeholder="请选择老人类型" clearable style="width: 120px;">
            <el-option label="自理老人" value="SELF_CARE" />
            <el-option label="护理老人" value="CARE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleShowRegisterForm">新增入住登记</el-button>
        </el-form-item>
      </el-form>
    </el-card>    <!-- 客户列表 -->
    <el-card class="table-card" title="客户列表">
      <el-table :data="customerList" v-loading="loading" stripe>
        <el-table-column prop="customerName" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ formatGender(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="guardianName" label="家属姓名" width="120" />
        <el-table-column prop="guardianPhone" label="联系电话" width="140" />
        <el-table-column prop="customerType" label="老人类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.customerType === 'SELF_CARE' ? 'success' : 'warning'" size="small">
              {{ formatCustomerType(row.customerType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === 0"
              size="small"
              type="primary"
              @click="handleCheckIn(row)"
            >
              入住登记
            </el-button>
            <el-button
              v-if="row.status === 1"
              size="small"
              type="warning"
              @click="handleEdit(row)"
            >
              修改
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchCustomerList"
          @current-change="fetchCustomerList"
        />
      </div>
    </el-card>

    <!-- 入住登记对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增入住登记' : dialogMode === 'edit' ? '修改客户信息' : '入住登记'"
      width="1000px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="checkin-form"
      >
        <!-- 基本信息 -->
        <el-card title="基本信息" class="form-card">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="客户姓名" prop="customerName">
                <el-input v-model="form.customerName" placeholder="请输入客户姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-select v-model="form.gender" placeholder="请选择性别">
                  <el-option label="男" value="MALE" />
                  <el-option label="女" value="FEMALE" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="出生日期" prop="birthDate">
                <el-date-picker
                  v-model="form.birthDate"
                  type="date"
                  placeholder="请选择出生日期"
                  style="width: 100%"
                  @change="calculateAge"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="年龄" prop="age">
                <el-input-number
                  v-model="form.age"
                  :min="0"
                  :max="120"
                  placeholder="自动计算"
                  disabled
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="form.idCard" placeholder="请输入身份证号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="血型" prop="bloodType">
                <el-select v-model="form.bloodType" placeholder="请选择血型">
                  <el-option label="A型" value="A" />
                  <el-option label="B型" value="B" />
                  <el-option label="AB型" value="AB" />
                  <el-option label="O型" value="O" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- 家属信息 -->
        <el-card title="家属联系信息" class="form-card">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="家属姓名" prop="guardianName">
                <el-input v-model="form.guardianName" placeholder="请输入家属姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="guardianPhone">
                <el-input v-model="form.guardianPhone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>        <!-- 床位信息 -->
        <el-card title="床位分配" class="form-card">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="楼栋" prop="buildingId">
                <el-input
                  value="606号楼"
                  disabled
                  placeholder="固定为606号楼"
                />
                <input type="hidden" v-model="form.buildingId" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="房间" prop="roomId">
                <el-select
                  v-model="form.roomId"
                  placeholder="请选择房间"
                  @change="handleRoomChange"
                  filterable
                >
                  <el-option-group
                    v-for="floor in groupedRooms"
                    :key="floor.floorNo"
                    :label="`${floor.floorNo}F`"
                  >
                    <el-option
                      v-for="room in floor.rooms"
                      :key="room.id"
                      :label="`${room.roomNo}室`"
                      :value="room.id"
                    />
                  </el-option-group>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="床位" prop="bedId">
                <el-select
                  v-model="form.bedId"
                  placeholder="请选择床位"
                  :disabled="!form.roomId"
                >
                  <el-option
                    v-for="bed in availableBeds"
                    :key="bed.id"
                    :label="`${bed.bedNo}床`"
                    :value="bed.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>        <!-- 入住信息 -->
        <el-card title="入住信息" class="form-card">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="入住时间" prop="checkInDate">
                <el-date-picker
                  v-model="form.checkInDate"
                  type="date"
                  placeholder="请选择入住时间"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="合同到期时间" prop="contractExpireDate">
                <el-date-picker
                  v-model="form.contractExpireDate"
                  type="date"
                  placeholder="请选择合同到期时间"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

        </el-card>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确认入住登记
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { customerApi, buildingApi, roomApi, bedApi } from '@/api'
import type { Customer } from '@/api/types'
import { User } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref<FormInstance>()

// 数据状态
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'checkin' | 'edit'>('add')

// 搜索表单
const searchForm = reactive({
  customerName: '',
  customerType: '' as 'SELF_CARE' | 'CARE' | ''
})

// 客户列表
const customerList = ref<Customer[]>([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 选项数据
const buildingOptions = ref<any[]>([])
const roomOptions = ref<any[]>([])
const bedOptions = ref<any[]>([])

// 入住登记表单
const form = reactive({
  id: undefined as number | undefined,
  customerName: '',
  gender: '' as 'MALE' | 'FEMALE' | '',
  birthDate: '',
  age: undefined as number | undefined,
  idCard: '',
  bloodType: '',
  guardianName: '',
  guardianPhone: '',
  buildingId: undefined as number | undefined,
  roomId: undefined as number | undefined,
  bedId: undefined as number | undefined,
  checkInDate: '',
  contractExpireDate: '',
  customerType: 'SELF_CARE' as 'SELF_CARE' | 'CARE',
  status: 1
})

// 表单验证规则
const rules: FormRules = {
  customerName: [
    { required: true, message: '请输入客户姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  birthDate: [
    { required: true, message: '请选择出生日期', trigger: 'change' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  guardianName: [
    { required: true, message: '请输入家属姓名', trigger: 'blur' }
  ],
  guardianPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
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
  ],  contractExpireDate: [
    { required: true, message: '请选择合同到期时间', trigger: 'change' },
    { validator: validateContractDate, trigger: 'change' }
  ]
}

// 计算属性
const groupedRooms = computed(() => {
  console.log('计算groupedRooms，roomOptions.value:', roomOptions.value) // 调试日志
  const grouped = roomOptions.value.reduce((acc: { floorNo: number; rooms: any[] }[], room: any) => {
    const floor = acc.find((f: { floorNo: number }) => f.floorNo === room.floorNo)
    if (floor) {
      floor.rooms.push(room)
    } else {
      acc.push({
        floorNo: room.floorNo,
        rooms: [room]
      })
    }
    return acc
  }, [] as { floorNo: number; rooms: any[] }[])

  const sorted = grouped.sort((a: { floorNo: number }, b: { floorNo: number }) => a.floorNo - b.floorNo)
  console.log('计算得到的groupedRooms:', sorted) // 调试日志
  return sorted
})

const availableBeds = computed(() => {
  return bedOptions.value.filter(bed =>
    bed.roomId === form.roomId && bed.bedStatus === 'AVAILABLE'
  )
})

// 验证函数
function validateContractDate(rule: any, value: any, callback: any) {
  if (value && form.checkInDate) {
    const contractDate = new Date(value)
    const checkInDate = new Date(form.checkInDate)
    if (contractDate <= checkInDate) {
      callback(new Error('合同到期时间不能小于或等于入住时间'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

// 方法
const fetchCustomerList = async () => {
  try {
    loading.value = true
    const params = {
      current: pagination.page, // 前端和后端都从1开始
      size: pagination.size,
      customerName: searchForm.customerName || undefined,
      customerType: (searchForm.customerType as 'SELF_CARE' | 'CARE' | undefined) || undefined
    }

    console.log('分页请求参数:', params) // 调试日志

    const response = await customerApi.getList(params)
    console.log('分页响应数据:', response) // 调试日志

    // 检查响应数据结构
    if (response && typeof response === 'object') {
      if (response.records && Array.isArray(response.records)) {
        // 标准分页响应格式
        customerList.value = response.records
        pagination.total = response.total || 0
      } else if (Array.isArray(response)) {
        // 如果直接返回数组，则手动处理分页
        const startIndex = (pagination.page - 1) * pagination.size
        const endIndex = startIndex + pagination.size
        customerList.value = response.slice(startIndex, endIndex)
        pagination.total = response.length
      } else {
        console.warn('未知的响应数据格式:', response)
        customerList.value = []
        pagination.total = 0
      }
    } else {
      customerList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取客户列表失败:', error)
    ElMessage.error('获取客户列表失败')
    customerList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const fetchBuildingOptions = async () => {
  try {
    const response = await buildingApi.getEnabledBuildings()
    buildingOptions.value = response.data || []
  } catch (error) {
    console.error('获取楼栋选项失败:', error)
  }
}

const fetchGroupedRooms = async (buildingId: number) => {
  try {
    console.log('开始获取房间数据，楼栋ID:', buildingId) // 调试日志
    const response = await roomApi.getRoomsGroupedByFloor(buildingId)
    console.log('房间API响应:', response) // 调试日志
    console.log('response类型:', typeof response) // 调试日志
    console.log('response.data:', response.data) // 调试日志

    // 直接使用response，因为API响应显示它就是一个数组
    if (Array.isArray(response)) {
      roomOptions.value = response
      console.log('使用response作为房间数据')
    } else if (response && response.data && Array.isArray(response.data)) {
      roomOptions.value = response.data
      console.log('使用response.data作为房间数据')
    } else {
      console.warn('未知的响应格式，设置为空数组')
      roomOptions.value = []
    }

    console.log('设置后的房间选项:', roomOptions.value) // 调试日志
    console.log('房间选项长度:', roomOptions.value.length) // 调试日志
  } catch (error) {
    console.error('获取分组房间选项失败:', error)
    ElMessage.error('获取房间信息失败')
    roomOptions.value = []
  }
}

const fetchRoomOptions = async (buildingId?: number) => {
  try {
    if (buildingId) {
      const response = await roomApi.getRoomsByBuildingId(buildingId)
      roomOptions.value = response.data || []
    } else {
      const response = await roomApi.getEnabledRooms()
      roomOptions.value = response.data || []
    }
  } catch (error) {
    console.error('获取房间选项失败:', error)
  }
}

const fetchBedOptions = async (roomId?: number) => {
  try {
    if (roomId) {
      console.log('开始获取床位数据，房间ID:', roomId) // 调试日志
      const response = await bedApi.getAvailableBedsByRoom(roomId)
      console.log('床位API响应:', response) // 调试日志
      console.log('response类型:', typeof response) // 调试日志

      // 检查响应数据结构并设置正确的数据
      if (Array.isArray(response)) {
        bedOptions.value = response
        console.log('使用response作为床位数据')
      } else if (response && response.data && Array.isArray(response.data)) {
        bedOptions.value = response.data
        console.log('使用response.data作为床位数据')
      } else {
        console.warn('未知的床位响应格式，设置为空数组')
        bedOptions.value = []
      }

      console.log('设置后的床位选项:', bedOptions.value) // 调试日志
      console.log('床位选项长度:', bedOptions.value.length) // 调试日志
    }
  } catch (error) {
    console.error('获取床位选项失败:', error)
    bedOptions.value = []
  }
}

const calculateAge = () => {
  if (form.birthDate) {
    const birth = new Date(form.birthDate)
    const today = new Date()
    let age = today.getFullYear() - birth.getFullYear()
    const monthDiff = today.getMonth() - birth.getMonth()
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--
    }
    form.age = age
  }
}

const handleBuildingChange = () => {
  form.roomId = undefined
  form.bedId = undefined
  roomOptions.value = []
  bedOptions.value = []
  if (form.buildingId) {
    fetchGroupedRooms(form.buildingId)
  }
}

const handleRoomChange = () => {
  form.bedId = undefined
  bedOptions.value = []
  if (form.roomId) {
    fetchBedOptions(form.roomId)
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchCustomerList()
}

const handleReset = () => {
  searchForm.customerName = ''
  searchForm.customerType = ''
  handleSearch()
}

const handleShowRegisterForm = () => {
  resetForm()
  dialogMode.value = 'add'
  dialogVisible.value = true
  // 确保加载房间数据
  fetchGroupedRooms(1) // 硬编码加载606号楼的房间数据
}

const handleCheckIn = (customer: Customer) => {
  Object.assign(form, {
    id: customer.id,
    customerName: customer.customerName,
    gender: customer.gender,
    birthDate: customer.birthDate,
    age: customer.age,
    idCard: customer.idCard,
    bloodType: customer.bloodType,
    guardianName: customer.guardianName,
    guardianPhone: customer.guardianPhone,
    buildingId: 1, // 硬编码设置为606号楼的ID
    roomId: undefined,
    bedId: undefined,
    checkInDate: '',
    contractExpireDate: '',
    customerType: customer.customerType || 'SELF_CARE',
    status: 1
  })
  dialogMode.value = 'checkin'
  dialogVisible.value = true

  // 自动加载606号楼的房间列表
  fetchGroupedRooms(1)
}

const handleView = (customer: Customer) => {
  router.push(`/customer/detail/${customer.id}`)
}

const handleDelete = async (customer: Customer) => {
  try {
    await ElMessageBox.confirm(
      `确认删除客户 ${customer.customerName} 吗？删除后将释放其床位。`,
      '确认删除',
      { type: 'warning' }
    )

    if (customer.id) {
      await customerApi.delete(customer.id)
      ElMessage.success('删除成功')
      fetchCustomerList()
    } else {
      ElMessage.error('客户ID不存在')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleEdit = async (customer: Customer) => {
  try {
    // 获取客户详细信息
    const customerDetail = await customerApi.getById(customer.id!)

    // 设置表单数据
    Object.assign(form, {
      id: customerDetail.id,
      customerName: customerDetail.customerName,
      gender: customerDetail.gender,
      birthDate: customerDetail.birthDate,
      age: customerDetail.age,
      idCard: customerDetail.idCard,
      bloodType: customerDetail.bloodType,
      guardianName: customerDetail.guardianName,
      guardianPhone: customerDetail.guardianPhone,
      buildingId: customerDetail.buildingId || 1, // 如果没有则默认为606号楼
      roomId: customerDetail.roomId,
      bedId: customerDetail.bedId,
      checkInDate: customerDetail.checkInDate,
      contractExpireDate: customerDetail.contractExpireDate,
      customerType: customerDetail.customerType,
      status: customerDetail.status
    })

    // 加载相关数据
    const buildingId = customerDetail.buildingId || 1
    await fetchGroupedRooms(buildingId)
    if (customerDetail.roomId) {
      await fetchBedOptions(customerDetail.roomId)
    }

    dialogMode.value = 'edit'
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取客户信息失败')
  }
}

const handleBack = () => {
  router.push('/customer/list')
}

const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    customerName: '',
    gender: '',
    birthDate: '',
    age: undefined,
    idCard: '',
    bloodType: '',
    guardianName: '',
    guardianPhone: '',
    buildingId: 1, // 硬编码设置为606号楼的ID
    roomId: undefined,
    bedId: undefined,
    checkInDate: '',
    contractExpireDate: '',
    customerType: 'SELF_CARE',
    status: 1
  })
  formRef.value?.clearValidate()

  // 自动加载606号楼的房间列表
  fetchGroupedRooms(1)
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {    const valid = await formRef.value.validate()
    if (!valid) return

    submitting.value = true

    if (dialogMode.value === 'add') {
      // 新增入住登记
      const genderValue = form.gender || 'MALE'
      const formDataToSubmit = {
        ...form,
        gender: genderValue as 'MALE' | 'FEMALE',
        customerType: 'SELF_CARE' as 'SELF_CARE' | 'CARE' // 新增入住登记默认为自理老人
      }
      await customerApi.create(formDataToSubmit)
      ElMessage.success('入住登记成功')
    } else if (dialogMode.value === 'checkin') {
      // 为已有客户办理入住登记
      await customerApi.checkIn(form.id!, {
        buildingId: form.buildingId!,
        roomId: form.roomId!,
        bedId: form.bedId!,
        checkInDate: form.checkInDate,
        contractExpireDate: form.contractExpireDate
      })
      ElMessage.success('入住登记成功')
    } else if (dialogMode.value === 'edit') {
      // 修改客户信息
      const genderValue = form.gender || 'MALE'
      const formDataToSubmit = {
        ...form,
        gender: genderValue as 'MALE' | 'FEMALE',
        customerType: form.customerType as 'SELF_CARE' | 'CARE'
      }
      await customerApi.update(formDataToSubmit)
      ElMessage.success('客户信息修改成功')
    }

    dialogVisible.value = false
    fetchCustomerList()
  } catch (error) {
    ElMessage.error(dialogMode.value === 'edit' ? '修改失败' : '入住登记失败')
  } finally {
    submitting.value = false
  }
}

// 格式化函数
const formatGender = (gender: string) => {
  return gender === 'MALE' ? '男' : '女'
}

const formatCustomerType = (type: string) => {
  const typeMap: Record<string, string> = {
    'SELF_CARE': '自理老人',
    'CARE': '护理老人'
  }
  return typeMap[type] || type
}

const formatStatus = (status: number) => {
  return status === 1 ? '在住' : '未入住'
}

const getStatusType = (status: number) => {
  return status === 1 ? 'success' : 'info'
}

// 初始化
onMounted(() => {
  fetchCustomerList()
  fetchBuildingOptions()
})
</script>

<style scoped>
.checkin-registration {
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

.page-header .el-button {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.page-header .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 255, 255, 0.2);
}

/* 卡片样式 */
.search-card, .table-card {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
}

:deep(.search-card .el-card__header),
:deep(.table-card .el-card__header) {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

:deep(.search-card .el-card__body),
:deep(.table-card .el-card__body) {
  padding: 24px;
}

/* 搜索表单样式 */
.search-card .el-form-item {
  margin-bottom: 16px;
  margin-right: 24px;
}

.search-card .el-form-item__label {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.search-card .el-input {
  width: 200px;
}

.search-card .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #1890ff 100%);
  border: none;
  border-radius: 6px;
  padding: 10px 20px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.search-card .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
}

.search-card .el-button--success {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  border-radius: 6px;
  padding: 10px 20px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
  transition: all 0.3s ease;
}

.search-card .el-button--success:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.4);
}

.search-card .el-button:not(.el-button--primary):not(.el-button--success) {
  border-color: #dcdfe6;
  color: #606266;
  border-radius: 6px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.search-card .el-button:not(.el-button--primary):not(.el-button--success):hover {
  border-color: #409eff;
  color: #409eff;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table .el-table__header) {
  background: #f5f7fa;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}

:deep(.el-table .el-table__header th) {
  background: #f5f7fa !important;
  color: #303133;
  font-weight: bold;
  font-size: 14px;
  text-align: center;
  padding: 12px 8px;
}

:deep(.el-table .el-table__row) {
  transition: background-color 0.3s ease;
}

:deep(.el-table .el-table__row:hover) {
  background-color: #f8f9ff !important;
}

:deep(.el-table .el-table__row td) {
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
}

/* 操作按钮样式 */
:deep(.el-table .el-button) {
  margin: 0 2px;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-table .el-button:hover) {
  transform: translateY(-1px);
}

:deep(.el-table .el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #1890ff 100%);
  border: none;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

:deep(.el-table .el-button--warning) {
  background: linear-gradient(135deg, #e6a23c 0%, #f7ba2a 100%);
  border: none;
  box-shadow: 0 2px 6px rgba(230, 162, 60, 0.3);
}

:deep(.el-table .el-button--danger) {
  background: linear-gradient(135deg, #f56c6c 0%, #ff7875 100%);
  border: none;
  box-shadow: 0 2px 6px rgba(245, 108, 108, 0.3);
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

/* 分页样式 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  padding: 20px 0;
}

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

/* 表单卡片样式 */
.checkin-form .form-card {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
}

:deep(.checkin-form .form-card .el-card__header) {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  color: #303133;
  font-weight: 600;
  font-size: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.checkin-form .form-card .el-card__body) {
  padding: 20px;
}

/* 表单项样式 */
:deep(.checkin-form .el-form-item__label) {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

:deep(.checkin-form .el-input__wrapper) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.checkin-form .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff;
}

:deep(.checkin-form .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff;
}

:deep(.checkin-form .el-select .el-select__wrapper) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.checkin-form .el-date-editor) {
  border-radius: 6px;
}

:deep(.checkin-form .el-input-number) {
  border-radius: 6px;
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

/* 表单元素样式优化 */
:deep(.el-select-dropdown) {
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

:deep(.el-option) {
  transition: all 0.3s ease;
}

:deep(.el-option:hover) {
  background-color: #f5f7fa;
}

:deep(.el-option.is-selected) {
  background-color: #409eff;
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .checkin-registration {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .search-card .el-form-item {
    margin-right: 0;
    width: 100%;
  }

  .search-card .el-input {
    width: 100%;
  }

  :deep(.el-dialog) {
    width: 95% !important;
    margin: 5vh auto !important;
  }

  :deep(.checkin-form .el-col) {
    margin-bottom: 16px;
  }
}
</style>
