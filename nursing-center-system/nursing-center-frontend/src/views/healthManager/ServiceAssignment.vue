<template>
  <div class="service-assignment">
    <!-- 页面头部 -->
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>服务对象设置</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>健康管家</el-breadcrumb-item>
          <el-breadcrumb-item>服务对象设置</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>

    <!-- 健康管家列表 -->
    <el-card class="manager-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>健康管家列表</span>
          <div class="search-area">
            <el-input
              v-model="managerSearchKeyword"
              placeholder="请输入管家姓名"
              style="width: 300px"
              clearable
              @input="handleManagerSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <el-table
        :data="filteredManagers"
        v-loading="managersLoading"
        stripe
        border
        style="width: 100%">
        <el-table-column prop="realName" label="管家姓名" width="150" align="center" />
        <el-table-column prop="phone" label="联系电话" width="150" align="center" />
        <el-table-column prop="totalCustomers" label="服务客户数" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.totalCustomers > 5 ? 'warning' : 'success'">
              {{ row.totalCustomers }}人
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入职时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createTime) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              :disabled="!isManagerAvailable(row)"
              @click="openAssignDialog(row)">
              设置服务对象
            </el-button>
            <el-button
              size="small"
              type="info"
              @click="viewManagerCustomers(row)">
              查看客户
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 服务对象分配对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="设置服务对象"
      width="80%"
      :before-close="handleCloseAssignDialog">
      <div v-if="selectedManager" class="assign-dialog-content">
        <div class="manager-info">
          <h3>{{ selectedManager.realName }} - 服务对象设置</h3>
          <p>当前服务客户：{{ selectedManager.totalCustomers || 0 }} 人</p>
        </div>

        <el-row :gutter="20">
          <!-- 无管家客户列表 -->
          <el-col :span="12">
            <el-card class="customer-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <span>无管家客户</span>
                  <el-input
                    v-model="customerSearchKeyword"
                    placeholder="请输入客户姓名"
                    style="width: 250px"
                    clearable
                    @input="handleCustomerSearch"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                </div>
              </template>

              <el-table
                :data="filteredCustomers"
                v-loading="customersLoading"
                stripe
                border
                max-height="400"
                @selection-change="handleCustomerSelection">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="customerName" label="客户姓名" width="120" align="center" />
                <el-table-column prop="age" label="年龄" width="80" align="center" />
                <el-table-column label="床位信息" width="150" align="center">
                  <template #default="{ row }">
                    {{ row.bedInfo || '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="checkInDate" label="入住时间" show-overflow-tooltip align="center">
                  <template #default="{ row }">
                    {{ formatDate(row.checkInDate) }}
                  </template>
                </el-table-column>
              </el-table>

              <div class="assignment-actions">
                <el-button
                  type="primary"
                  :disabled="selectedCustomers.length === 0"
                  @click="handleAssignCustomers">
                  <el-icon><Right /></el-icon>
                  分配给 {{ selectedManager.realName }}
                </el-button>
              </div>
            </el-card>
          </el-col>

          <!-- 当前管家的服务客户列表 -->
          <el-col :span="12">
            <el-card class="managed-customer-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <span>{{ selectedManager.realName }} 的服务客户</span>
                  <el-tag type="info">共 {{ managedCustomers.length }} 人</el-tag>
                </div>
              </template>

              <el-table
                :data="managedCustomers"
                v-loading="managedCustomersLoading"
                stripe
                border
                max-height="400">          <el-table-column prop="customerName" label="客户姓名" width="120" align="center" />
          <el-table-column prop="age" label="年龄" width="80" align="center" />
          <el-table-column label="床位信息" width="150" align="center">
            <template #default="{ row }">
              {{ row.bedInfo || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="assignDate" label="分配时间" show-overflow-tooltip align="center">
            <template #default="{ row }">
              {{ formatDate(row.serviceStartDate || row.assignDate) }}
            </template>
          </el-table-column>
                <el-table-column label="操作" width="100" align="center">
                  <template #default="{ row }">
                    <el-button
                      size="small"
                      type="danger"
                      @click="handleRemoveCustomer(row)">
                      移除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- 客户查看对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="客户列表"
      width="70%">
      <div v-if="viewManager" class="view-dialog-content">
        <h3>{{ viewManager.realName }} - 服务客户列表</h3>
        <el-table
          :data="viewCustomers"
          v-loading="viewCustomersLoading"
          stripe
          border>
          <el-table-column prop="customerName" label="客户姓名" width="150" align="center" />
          <el-table-column prop="age" label="年龄" width="100" align="center" />
          <el-table-column label="床位信息" width="180" align="center">
            <template #default="{ row }">
              {{ row.bedInfo || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="分配时间" align="center">
            <template #default="{ row }">
              {{ formatDate(row.serviceStartDate || row.assignDate) }}
            </template>
          </el-table-column>
          <el-table-column label="服务状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getServiceStatusType(row.serviceStatus)">
                {{ getServiceStatusText(row.serviceStatus) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Right } from '@element-plus/icons-vue'
import { healthManagerApi } from '@/api/healthManager'

// 健康管家接口定义（与后端返回数据匹配）
interface ExtendedHealthManager {
  id: number
  username: string
  realName: string
  phone: string
  role: string
  status: number  // 后端返回的是数字类型
  totalCustomers: number
  activeCustomers: number
  department?: string
  createTime?: string  // 入职时间
  specialties?: string[]
}

// 客户接口扩展
interface AssignedCustomer {
  id?: number
  customerId?: number
  customerName?: string
  age?: number
  gender?: string
  roomNumber?: string
  checkInDate?: string
  assignDate?: string
  assignTime?: string
  serviceStatus?: string
}

// 响应式数据
const managersLoading = ref(false)
const customersLoading = ref(false)
const managedCustomersLoading = ref(false)
const viewCustomersLoading = ref(false)

const managerSearchKeyword = ref('')
const customerSearchKeyword = ref('')
const searchTimeout = ref<number | null>(null)

const selectedManager = ref<ExtendedHealthManager | null>(null)
const selectedCustomers = ref<AssignedCustomer[]>([])
const viewManager = ref<ExtendedHealthManager | null>(null)

// 对话框状态
const assignDialogVisible = ref(false)
const viewDialogVisible = ref(false)

// 数据列表
const healthManagers = ref<ExtendedHealthManager[]>([])
const unassignedCustomers = ref<AssignedCustomer[]>([])
const managedCustomers = ref<AssignedCustomer[]>([])
const viewCustomers = ref<AssignedCustomer[]>([])

// 计算属性
const filteredManagers = computed(() => {
  if (!managerSearchKeyword.value) {
    return healthManagers.value
  }
  return healthManagers.value.filter(manager =>
    manager.realName?.includes(managerSearchKeyword.value) ||
    manager.username?.includes(managerSearchKeyword.value)
  )
})

const filteredCustomers = computed(() => {
  if (!customerSearchKeyword.value) {
    return unassignedCustomers.value
  }
  return unassignedCustomers.value.filter(customer =>
    customer.customerName?.includes(customerSearchKeyword.value)
  )
})

// 辅助方法
const getStatusType = (status: number) => {
  switch (status) {
    case 1:
      return 'success'
    case 0:
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 1:
      return '在职'
    case 0:
      return '离职'
    default:
      return '未知'
  }
}

const isManagerAvailable = (manager: ExtendedHealthManager) => {
  return manager.status === 1 &&  // 1表示活跃状态
    (manager.totalCustomers || 0) < 10  // 假设最大客户数为10
}

const getServiceStatusType = (status: number) => {
  return status === 1 ? 'success' : 'warning'
}

const getServiceStatusText = (status: number) => {
  return status === 1 ? '服务中' : '暂停'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN')
  } catch {
    return dateStr
  }
}

// 生命周期
onMounted(() => {
  fetchHealthManagers()
})

// 获取健康管家列表
const fetchHealthManagers = async () => {
  managersLoading.value = true
  try {
    const response = await healthManagerApi.getHealthManager.list({
      page: 1,
      size: 100
    })

    console.log('完整响应数据:', response)
    console.log('第一个管家数据:', (response as any).records?.[0])

    // response 就是 IPage 对象，直接从 records 中获取数据
    const managerList = (response as any).records || []
    console.log('解析后的管理员列表:', managerList)

    healthManagers.value = managerList.map((manager: any) => {
      console.log('单个管家原始数据:', manager)
      console.log('原始createTime字段:', manager.createTime)
      console.log('原始create_time字段:', manager.create_time)

      const result = {
        id: manager.id,
        username: manager.username,
        realName: manager.realName || manager.real_name,  // 优先使用驼峰格式
        phone: manager.phone,
        role: manager.role,
        status: manager.status,
        totalCustomers: manager.totalCustomers || manager.total_customers || 0,  // 优先使用驼峰格式
        activeCustomers: manager.activeCustomers || manager.active_customers || 0,  // 优先使用驼峰格式
        department: '健康管家',
        createTime: manager.createTime || manager.create_time || manager.created_at,  // 多种格式尝试
        specialties: []
      }
      console.log('转换后的管家数据:', result)
      console.log('最终createTime:', result.createTime)
      return result
    })
    console.log('最终健康管家列表:', healthManagers.value)
  } catch (error) {
    console.error('获取健康管家列表失败:', error)
    ElMessage.error('获取健康管家列表失败，请检查网络连接')
    healthManagers.value = []
  } finally {
    managersLoading.value = false
  }
}

// 获取无管家客户列表
const fetchUnassignedCustomers = async () => {
  if (!selectedManager.value) return

  customersLoading.value = true
  try {
    const response = await healthManagerApi.getUnassignedCustomers({
      customerName: customerSearchKeyword.value
    })
    unassignedCustomers.value = response.map(customer => ({
      ...customer,
      customerName: customer.customerName || customer.name,
      age: customer.age || 0,
      bedInfo: customer.bedInfo || '-',
      checkInDate: customer.checkInDate || customer.entryDate || '-'
    }))
    console.log('无管家客户列表:', unassignedCustomers.value)
  } catch (error) {
    console.error('获取无管家客户列表失败:', error)
    ElMessage.error('获取无管家客户列表失败，请重试')
    unassignedCustomers.value = []
  } finally {
    customersLoading.value = false
  }
}

// 获取指定管家的服务客户列表
const fetchManagedCustomers = async (managerId: number) => {
  managedCustomersLoading.value = true
  try {
    const response = await healthManagerApi.getAssignedCustomers(managerId)
    managedCustomers.value = response.map(customer => ({
      ...customer,
      customerName: customer.customerName || customer.name,
      age: customer.age || 0,
      bedInfo: customer.bedInfo || '-',
      serviceStartDate: customer.serviceStartDate || customer.assignDate || customer.assignTime,
      assignDate: customer.serviceStartDate || customer.assignDate || customer.assignTime
    }))
    console.log('管家服务客户列表:', managedCustomers.value)
  } catch (error) {
    console.error('获取管家服务客户列表失败:', error)
    ElMessage.error('获取管家服务客户列表失败，请重试')
    managedCustomers.value = []
  } finally {
    managedCustomersLoading.value = false
  }
}

// 事件处理方法
const handleManagerSearch = () => {
  // 使用计算属性，无需额外处理
}

const handleCustomerSearch = () => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  searchTimeout.value = setTimeout(() => {
    if (selectedManager.value) {
      fetchUnassignedCustomers()
    }
  }, 300)
}

const handleCustomerSelection = (customers: AssignedCustomer[]) => {
  selectedCustomers.value = customers
}

// 打开分配对话框
const openAssignDialog = (manager: ExtendedHealthManager) => {
  selectedManager.value = manager
  selectedCustomers.value = []
  assignDialogVisible.value = true
  fetchUnassignedCustomers()
  fetchManagedCustomers(manager.id)
}

// 关闭分配对话框
const handleCloseAssignDialog = () => {
  assignDialogVisible.value = false
  selectedManager.value = null
  selectedCustomers.value = []
  customerSearchKeyword.value = ''
  // 刷新主列表数据
  fetchHealthManagers()
}

// 查看管家客户
const viewManagerCustomers = async (manager: ExtendedHealthManager) => {
  console.log('查看管家客户 - 管家信息:', manager)
  viewManager.value = manager
  viewDialogVisible.value = true
  viewCustomersLoading.value = true

  try {
    console.log('请求API: /api/admin/health-manager/' + manager.id + '/customers')
    const response = await healthManagerApi.getAssignedCustomers(manager.id)
    console.log('获取客户列表响应:', response)
    console.log('响应数据类型:', typeof response)
    console.log('响应是否为数组:', Array.isArray(response))

    if (Array.isArray(response)) {
      viewCustomers.value = response.map(customer => ({
        ...customer,
        customerName: customer.customerName || customer.name,
        serviceStatus: customer.serviceStatus,
        serviceStartDate: customer.serviceStartDate || customer.assignDate || customer.assignTime,
        assignDate: customer.serviceStartDate || customer.assignDate || customer.assignTime
      }))
    } else {
      console.log('响应不是数组格式，原始数据:', response)
      viewCustomers.value = []
    }

    console.log('最终客户列表:', viewCustomers.value)
  } catch (error) {
    console.error('获取客户列表失败:', error)
    ElMessage.error('获取客户列表失败')
    viewCustomers.value = []
  } finally {
    viewCustomersLoading.value = false
  }
}

// 分配客户
const handleAssignCustomers = async () => {
  if (!selectedManager.value || selectedCustomers.value.length === 0) return

  try {
    await ElMessageBox.confirm(
      `确定要将 ${selectedCustomers.value.length} 位客户分配给 ${selectedManager.value.realName} 管理吗？`,
      '确认分配',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const customerIds = selectedCustomers.value
      .map(customer => customer.id || customer.customerId)
      .filter((id): id is number => id !== undefined)

    if (customerIds.length === 0) {
      ElMessage.error('选中的客户ID无效')
      return
    }

    await healthManagerApi.batchAssignCustomers({
      healthManagerId: selectedManager.value.id,
      customerIds
    })

    // 重新获取数据
    await Promise.all([
      fetchUnassignedCustomers(),
      fetchManagedCustomers(selectedManager.value.id)
    ])

    selectedCustomers.value = []
    ElMessage.success(`成功为 ${selectedManager.value.realName} 分配了 ${customerIds.length} 位客户`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('分配客户失败:', error)
      const errorMessage = error instanceof Error ? error.message : '请重试'
      ElMessage.error(`分配客户失败：${errorMessage}`)
    }
  }
}

// 移除客户
const handleRemoveCustomer = async (customer: AssignedCustomer) => {
  if (!selectedManager.value) return

  console.log('开始移除客户，客户信息:', customer)
  console.log('当前选中的健康管家:', selectedManager.value)

  try {
    await ElMessageBox.confirm(
      `确定要将客户 ${customer.customerName} 从 ${selectedManager.value.realName} 的服务列表中移除吗？`,
      '确认移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const customerId = customer.id || customer.customerId
    if (!customerId) {
      ElMessage.error('客户ID无效')
      console.error('客户ID无效:', customer)
      return
    }

    console.log('准备调用移除API，参数:', {
      healthManagerId: selectedManager.value.id,
      customerId
    })

    await healthManagerApi.removeCustomerAssignment({
      healthManagerId: selectedManager.value.id,
      customerId
    })

    console.log('移除API调用成功')

    // 重新获取数据
    await Promise.all([
      fetchUnassignedCustomers(),
      fetchManagedCustomers(selectedManager.value.id)
    ])

    ElMessage.success(`成功将客户 ${customer.customerName} 从 ${selectedManager.value.realName} 的服务列表中移除`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除客户失败:', error)
      const errorMessage = error instanceof Error ? error.message : '请重试'
      ElMessage.error(`移除客户失败：${errorMessage}`)
    }
  }
}
</script>

<style scoped>
.service-assignment {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

.page-header-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-weight: 600;
}

.manager-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 对话框样式 */
.assign-dialog-content {
  padding: 10px 0;
}

.manager-info {
  margin-bottom: 20px;
  padding: 15px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 8px;
}

.manager-info h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.manager-info p {
  margin: 0;
  color: #666;
}

.customer-card,
.managed-customer-card {
  border: 1px solid #e4e7ed;
}

.assignment-actions {
  text-align: center;
  margin-top: 15px;
  padding: 15px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 8px;
}

.view-dialog-content h3 {
  margin-bottom: 20px;
  color: #333;
}

/* 表格样式优化 */
:deep(.el-table) {
  background: transparent;
}

:deep(.el-table th.el-table__cell) {
  background: rgba(64, 158, 255, 0.1);
  color: #333;
}

:deep(.el-table .el-table__row) {
  background: rgba(255, 255, 255, 0.8);
}

:deep(.el-table .el-table__row:hover) {
  background: rgba(64, 158, 255, 0.1);
}

/* 对话框内表格样式 */
:deep(.el-dialog .el-table) {
  background: #fff;
}

:deep(.el-dialog .el-table .el-table__row) {
  background: #fff;
}

:deep(.el-dialog .el-table .el-table__row:hover) {
  background: #f5f7fa;
}
</style>
