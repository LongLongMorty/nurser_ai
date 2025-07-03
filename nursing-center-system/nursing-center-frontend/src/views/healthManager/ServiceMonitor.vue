<template>
  <div class="service-monitor">
    <!-- 页面头部 -->
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>服务监控</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>健康管家</el-breadcrumb-item>
          <el-breadcrumb-item>服务监控</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>

    <!-- 客户查询区域 -->
    <el-card class="search-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>客户信息查询</span>
        </div>
      </template>

      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="客户姓名">
            <el-input
              v-model="searchForm.customerName"
              placeholder="请输入客户姓名"
              style="width: 200px"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 客户列表 -->
      <el-table
        :data="customerList"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        @row-click="handleCustomerSelect">
        <el-table-column prop="customerName" label="客户姓名" width="120" align="center" />
        <el-table-column prop="age" label="年龄" width="80" align="center" />
        <el-table-column label="性别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === '男' ? 'primary' : 'danger'" size="small">
              {{ row.gender || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="房间信息" width="150" align="center">
          <template #default="{ row }">
            {{ row.bedInfo || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="护理级别" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.careLevelName" type="success" size="small">
              {{ row.careLevelName }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="健康管家" width="120" align="center">
          <template #default="{ row }">
            {{ row.healthManagerName || '未分配' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="入住时间" show-overflow-tooltip align="center">
          <template #default="{ row }">
            {{ formatDate(row.checkInDate) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click.stop="handleCustomerSelect(row)">
              查看服务
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="searchForm.pageNum"
        v-model:page-size="searchForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :small="false"
        :disabled="loading"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 客户服务监控对话框 -->
    <el-dialog
      v-model="serviceDialogVisible"
      :title="`${selectedCustomer?.customerName} - 服务监控`"
      width="1200px"
      :close-on-click-modal="false">

      <div class="service-tabs">
        <el-tabs v-model="activeTab" type="card">
          <!-- 已购买的服务项目 -->
          <el-tab-pane label="已购买服务" name="purchased">
            <div class="service-actions">
              <el-button type="success" @click="showPurchaseDialog">
                <el-icon><Plus /></el-icon>
                购买新服务
              </el-button>
              <el-button type="warning" @click="handleRefreshServices">
                <el-icon><Refresh /></el-icon>
                刷新状态
              </el-button>
            </div>

            <el-table
              :data="customerServices"
              v-loading="servicesLoading"
              stripe
              border
              style="width: 100%; margin-top: 15px;">
              <el-table-column prop="itemName" label="服务项目" width="150" align="center" />

              <el-table-column label="服务状态" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.serviceStatus)" size="small">
                    {{ getStatusText(row.serviceStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="totalQuantity" label="总数量" width="100" align="center" />
              <el-table-column prop="remainingQuantity" label="剩余数量" width="100" align="center">
                <template #default="{ row }">
                  <span :class="{ 'text-danger': row.remainingQuantity <= 5 }">
                    {{ row.remainingQuantity }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="expireDate" label="到期时间" width="120" align="center">
                <template #default="{ row }">
                  <span :class="{ 'text-danger': isExpiringSoon(row.expireDate) }">
                    {{ formatDate(row.expireDate) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="purchaseDate" label="购买时间" width="120" align="center">
                <template #default="{ row }">
                  {{ formatDate(row.purchaseDate) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button-group>
                    <el-button
                      type="primary"
                      size="small"
                      @click="handleRenewService(row)">
                      续费
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      @click="handleRemoveService(row)">
                      移除
                    </el-button>
                  </el-button-group>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 服务历史记录 -->
          <el-tab-pane label="服务历史" name="history">
            <el-table
              :data="serviceHistory"
              v-loading="historyLoading"
              stripe
              border
              style="width: 100%;">
              <el-table-column prop="itemName" label="服务项目" width="150" align="center" />
              <el-table-column prop="operationType" label="操作类型" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getOperationType(row.operationType)" size="small">
                    {{ row.operationType }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="quantity" label="数量变化" width="100" align="center" />
              <el-table-column prop="operatorName" label="操作人" width="120" align="center" />
              <el-table-column prop="operateTime" label="操作时间" width="150" align="center">
                <template #default="{ row }">
                  {{ formatDateTime(row.operateTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="notes" label="备注" show-overflow-tooltip align="center" />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 续费对话框 -->
    <el-dialog
      v-model="renewDialogVisible"
      title="服务续费"
      width="500px"
      :close-on-click-modal="false">

      <el-form :model="renewForm" :rules="renewRules" ref="renewFormRef" label-width="100px">
        <el-form-item label="服务项目">
          <el-input v-model="renewForm.itemName" readonly />
        </el-form-item>
        <el-form-item label="当前数量">
          <el-input v-model="renewForm.currentQuantity" readonly />
        </el-form-item>
        <el-form-item label="新增数量" prop="addQuantity">
          <el-input-number
            v-model="renewForm.addQuantity"
            :min="1"
            :max="1000"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="到期时间" prop="expireDate">
          <el-date-picker
            v-model="renewForm.expireDate"
            type="date"
            placeholder="选择到期时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="renewForm.notes"
            type="textarea"
            rows="3"
            placeholder="请输入续费备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="renewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmRenew" :loading="renewLoading">
          确认续费
        </el-button>
      </template>
    </el-dialog>

    <!-- 购买新服务对话框 -->
    <el-dialog
      v-model="purchaseDialogVisible"
      title="购买护理服务"
      width="800px"
      :close-on-click-modal="false">

      <div class="purchase-content">
        <div class="service-selection">
          <h4>选择服务项目</h4>
          <div class="search-bar">
            <el-input
              v-model="serviceSearchKeyword"
              placeholder="请输入服务项目名称"
              style="width: 300px"
              clearable
              @input="handleServiceSearch">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <el-table
            :data="availableServices"
            v-loading="servicesSearchLoading"
            stripe
            border
            max-height="300"
            @row-click="handleServiceSelect"
            style="margin-top: 15px;">
            <el-table-column prop="itemName" label="服务名称" width="150" align="center" />
            <el-table-column prop="careType" label="服务类型" width="120" align="center" />
            <el-table-column prop="price" label="单价" width="100" align="center">
              <template #default="{ row }">
                ¥{{ row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip align="center" />
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  @click.stop="handleServiceSelect(row)"
                  :disabled="isServiceSelected(row.id)">
                  {{ isServiceSelected(row.id) ? '已选' : '选择' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="selected-services" v-if="selectedServices.length > 0">
          <h4>已选服务项目</h4>
          <el-table
            :data="selectedServices"
            stripe
            border
            style="margin-top: 15px;">
            <el-table-column prop="itemName" label="服务名称" width="150" align="center" />
            <el-table-column prop="careType" label="服务类型" width="120" align="center" />
            <el-table-column prop="price" label="单价" width="100" align="center">
              <template #default="{ row }">
                ¥{{ row.price }}
              </template>
            </el-table-column>
            <el-table-column label="数量" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="1"
                  :max="1000"
                  size="small" />
              </template>
            </el-table-column>
            <el-table-column label="到期时间" width="150" align="center">
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.expireDate"
                  type="date"
                  placeholder="选择到期时间"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row, $index }">
                <el-button
                  type="danger"
                  size="small"
                  @click="handleRemoveSelected($index)">
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="purchaseDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleConfirmPurchase"
          :loading="purchaseLoading"
          :disabled="selectedServices.length === 0">
          确认购买
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { customerApi } from '@/api/customer'
import { careApi } from '@/api/care'
import type { Customer } from '@/api/types'

// 响应式数据
const loading = ref(false)
const servicesLoading = ref(false)
const historyLoading = ref(false)
const renewLoading = ref(false)
const purchaseLoading = ref(false)
const servicesSearchLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  customerName: '',
  pageNum: 1,
  pageSize: 10
})

// 客户列表
const customerList = ref<Customer[]>([])
const total = ref(0)

// 选中的客户
const selectedCustomer = ref<Customer | null>(null)

// 对话框显示状态
const serviceDialogVisible = ref(false)
const renewDialogVisible = ref(false)
const purchaseDialogVisible = ref(false)

// 标签页
const activeTab = ref('purchased')

// 客户服务数据
const customerServices = ref<any[]>([])
const serviceHistory = ref<any[]>([])

// 续费表单
const renewFormRef = ref()
const renewForm = reactive({
  serviceId: 0,
  itemName: '',
  currentQuantity: 0,
  addQuantity: 1,
  expireDate: '',
  notes: ''
})

// 续费表单验证规则
const renewRules = {
  addQuantity: [
    { required: true, message: '请输入新增数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量至少为1', trigger: 'blur' }
  ],
  expireDate: [
    { required: true, message: '请选择到期时间', trigger: 'change' }
  ]
}

// 服务选择相关
const serviceSearchKeyword = ref('')
const availableServices = ref<any[]>([])
const selectedServices = ref<any[]>([])

// 计算属性
const isServiceSelected = computed(() => {
  return (serviceId: number) => {
    return selectedServices.value.some(service => service.id === serviceId)
  }
})

// 方法
const handleSearch = async () => {
  searchForm.pageNum = 1
  await fetchCustomerList()
}

const handleReset = () => {
  searchForm.customerName = ''
  searchForm.pageNum = 1
  fetchCustomerList()
}

const handleSizeChange = (val: number) => {
  searchForm.pageSize = val
  fetchCustomerList()
}

const handleCurrentChange = (val: number) => {
  searchForm.pageNum = val
  fetchCustomerList()
}

// 获取客户列表
const fetchCustomerList = async () => {
  try {
    loading.value = true
    const response = await customerApi.getList(searchForm)
    
    console.log('获取到的原始客户数据:', response)

    // 处理客户数据，确保房间信息正确显示
    const customers = (response.records || []).map(customer => {
      console.log('处理客户:', customer)
      
      // 构建房间信息
      let bedInfo = null
      if (customer.buildingName && customer.roomNo && customer.bedNo) {
        bedInfo = `${customer.buildingName}-${customer.roomNo}-${customer.bedNo}`
      } else if (customer.building_name && customer.room_no && customer.bed_no) {
        bedInfo = `${customer.building_name}-${customer.room_no}-${customer.bed_no}`
      }
      
      return {
        ...customer,
        // 房间信息字段映射
        bedInfo: bedInfo || customer.bedInfo || customer.roomInfo || '-',
        // 性别字段映射
        gender: customer.gender === 'MALE' || customer.gender === 1 || customer.gender === '1' ? '男' :
                customer.gender === 'FEMALE' || customer.gender === 2 || customer.gender === '2' ? '女' : 
                customer.gender || '未知',
        // 护理级别字段映射
        careLevelName: customer.careLevelName || customer.care_level_name || customer.levelName,
        // 健康管家字段映射
        healthManagerName: customer.healthManagerName || customer.health_manager_name || customer.managerName
      }
    })

    console.log('处理后的客户数据:', customers)
    customerList.value = customers
    total.value = response.total || 0
  } catch (error) {
    console.error('获取客户列表失败:', error)
    ElMessage.error('获取客户列表失败')
  } finally {
    loading.value = false
  }
}

// 选择客户，显示服务监控对话框
const handleCustomerSelect = async (customer: Customer) => {
  selectedCustomer.value = customer
  serviceDialogVisible.value = true
  activeTab.value = 'purchased'
  await Promise.all([
    fetchCustomerServices(),
    fetchServiceHistory()
  ])
}

// 获取客户服务列表
const fetchCustomerServices = async () => {
  if (!selectedCustomer.value?.id) return

  try {
    servicesLoading.value = true
    const response = await careApi.customerCare.getCustomerServices(selectedCustomer.value.id)    // 添加调试信息
    console.log('获取到的客户服务数据:', response)
    if (response && response.length > 0) {
      console.log('第一个服务的详细信息:', JSON.stringify(response[0], null, 2))
    }
    
    // 处理数据，确保字段映射正确
    const services = (response || []).map((service: any) => {
      console.log('原始服务数据:', service)
      console.log('可用的数量字段:', {
        totalQuantity: (service as any).totalQuantity,
        quantity: (service as any).quantity,
        initialQuantity: (service as any).initialQuantity,
        purchaseQuantity: (service as any).purchaseQuantity,
        originalQuantity: (service as any).originalQuantity
      })
      
      return {
        ...service,
        // 确保数量字段正确映射
        totalQuantity: (service as any).totalQuantity || (service as any).quantity || 
                      (service as any).initialQuantity || (service as any).purchaseQuantity || 
                      (service as any).originalQuantity || 0,
        remainingQuantity: (service as any).remainingQuantity || (service as any).remaining || 
                          (service as any).currentQuantity || 0,
        // 确保服务项目名称正确
        itemName: (service as any).itemName || (service as any).careItemName || 
                 (service as any).serviceName || '未知服务',
        // 确保日期格式正确
        expireDate: (service as any).expireDate || (service as any).endDate,
        purchaseDate: (service as any).purchaseDate || (service as any).createTime || 
                     (service as any).startDate
      }
    })
    
    customerServices.value = services
    console.log('处理后的服务数据:', services)
  } catch (error) {
    console.error('获取客户服务失败:', error)
    ElMessage.error('获取客户服务失败')
  } finally {
    servicesLoading.value = false
  }
}

// 获取服务历史记录
const fetchServiceHistory = async () => {
  if (!selectedCustomer.value?.customerName) return

  try {
    historyLoading.value = true
    const response = await careApi.careRecord.list({
      pageNum: 1,
      pageSize: 100,
      customerName: selectedCustomer.value.customerName
    })
    serviceHistory.value = response.data?.records || []
  } catch (error) {
    console.error('获取服务历史失败:', error)
    ElMessage.error('获取服务历史失败')
  } finally {
    historyLoading.value = false
  }
}

// 续费服务
const handleRenewService = (service: any) => {
  renewForm.serviceId = service.id
  renewForm.itemName = service.itemName
  renewForm.currentQuantity = service.remainingQuantity
  renewForm.addQuantity = 1
  renewForm.expireDate = service.expireDate
  renewForm.notes = ''
  renewDialogVisible.value = true
}

// 确认续费
const handleConfirmRenew = async () => {
  try {
    await renewFormRef.value.validate()
    renewLoading.value = true

    await careApi.customerCare.renewService(
      renewForm.serviceId,
      renewForm.addQuantity,
      renewForm.expireDate
    )

    ElMessage.success('续费成功')
    renewDialogVisible.value = false
    await fetchCustomerServices()
  } catch (error) {
    console.error('续费失败:', error)
    ElMessage.error('续费失败')
  } finally {
    renewLoading.value = false
  }
}

// 移除服务
const handleRemoveService = async (service: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除客户的"${service.itemName}"服务吗？移除后客户将不再享有该服务。`,
      '确认移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await careApi.customerCare.removeService(service.id)

    ElMessage.success('移除成功')
    await fetchCustomerServices()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除服务失败:', error)
      ElMessage.error('移除服务失败')
    }
  }
}

// 刷新服务状态
const handleRefreshServices = async () => {
  await fetchCustomerServices()
  ElMessage.success('已刷新服务状态')
}

// 显示购买对话框
const showPurchaseDialog = async () => {
  purchaseDialogVisible.value = true
  selectedServices.value = []
  await fetchAvailableServices()
}

// 获取可用服务列表（排除客户已购买的服务）
const fetchAvailableServices = async () => {
  try {
    servicesSearchLoading.value = true
    console.log('开始获取可用服务列表...')
    
    // 尝试获取护理项目列表
    const response = await careApi.careItem.list({
      pageNum: 1,
      pageSize: 1000,
      itemName: serviceSearchKeyword.value,
      status: 1
    })
    
    console.log('获取服务项目响应:', response)
    
    // 获取客户已购买的服务ID列表
    const purchasedServiceIds = customerServices.value.map(service => service.careItemId || service.itemId)
    console.log('已购买的服务ID列表:', purchasedServiceIds)
    
    // 处理响应数据结构
    let serviceList = []
    if (response && (response as any).records) {
      serviceList = (response as any).records
    } else if (response && response.data && (response.data as any).records) {
      serviceList = (response.data as any).records
    } else if (Array.isArray(response)) {
      serviceList = response
    } else {
      console.warn('未知的响应数据结构:', response)
    }
    
    console.log('解析出的服务列表:', serviceList)
    
    // 过滤掉已购买的服务
    const availableServiceList = serviceList.filter((service: any) =>
      !purchasedServiceIds.includes(service.id)
    )
    
    console.log('过滤后的可用服务列表:', availableServiceList)
    availableServices.value = availableServiceList
  } catch (error) {
    console.error('获取服务列表失败:', error)
    ElMessage.error(`获取服务列表失败: ${(error as any).message || '请重试'}`)
  } finally {
    servicesSearchLoading.value = false
  }
}

// 搜索服务
const handleServiceSearch = () => {
  fetchAvailableServices()
}

// 选择服务
const handleServiceSelect = (service: any) => {
  if (isServiceSelected.value(service.id)) return

  const today = new Date()
  const expireDate = new Date(today)
  expireDate.setMonth(expireDate.getMonth() + 1) // 默认一个月后到期

  selectedServices.value.push({
    ...service,
    quantity: 1,
    expireDate: expireDate.toISOString().split('T')[0]
  })
}

// 移除已选服务
const handleRemoveSelected = (index: number) => {
  selectedServices.value.splice(index, 1)
}

// 确认购买
const handleConfirmPurchase = async () => {
  try {
    // 验证所有必填项
    for (const service of selectedServices.value) {
      if (!service.quantity || service.quantity < 1) {
        ElMessage.error(`请设置"${service.itemName}"的数量`)
        return
      }
      if (!service.expireDate) {
        ElMessage.error(`请设置"${service.itemName}"的到期时间`)
        return
      }
    }

    if (!selectedCustomer.value?.id) {
      ElMessage.error('未选择客户')
      return
    }

    purchaseLoading.value = true

    const purchaseItems = selectedServices.value.map(service => ({
      careItemId: service.id,
      quantity: service.quantity,
      expireDate: service.expireDate
    }))

    await careApi.customerCare.purchaseItems(selectedCustomer.value.id, purchaseItems)

    ElMessage.success('购买成功')
    purchaseDialogVisible.value = false
    await fetchCustomerServices()
  } catch (error) {
    console.error('购买失败:', error)
    ElMessage.error('购买失败')
  } finally {
    purchaseLoading.value = false
  }
}

// 工具方法
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

const formatDateTime = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'NORMAL': 'success',
    'EXPIRED': 'danger',
    'ARREARS': 'warning',
    'USED_UP': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'NORMAL': '正常',
    'EXPIRED': '到期',
    'ARREARS': '欠费',
    'USED_UP': '用完'
  }
  return statusMap[status] || '未知'
}

const getOperationType = (type: string) => {
  const typeMap: Record<string, string> = {
    '购买': 'success',
    '续费': 'primary',
    '移除': 'danger',
    '使用': 'info'
  }
  return typeMap[type] || 'info'
}

const isExpiringSoon = (date: string) => {
  if (!date) return false
  const expireDate = new Date(date)
  const today = new Date()
  const diffDays = Math.ceil((expireDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  return diffDays <= 7 && diffDays >= 0
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCustomerList()
})
</script>

<style scoped>
.service-monitor {
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

.search-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.service-tabs {
  min-height: 400px;
}

.service-actions {
  margin-bottom: 15px;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
}

.purchase-content {
  max-height: 600px;
  overflow-y: auto;
}

.service-selection,
.selected-services {
  margin-bottom: 20px;
}

.service-selection h4,
.selected-services h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-weight: 600;
}

.search-bar {
  margin-bottom: 15px;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-tabs__content) {
  padding: 20px 0;
}

:deep(.el-table .el-table__cell) {
  padding: 8px 0;
}

:deep(.el-button-group .el-button) {
  margin: 0;
}
</style>
