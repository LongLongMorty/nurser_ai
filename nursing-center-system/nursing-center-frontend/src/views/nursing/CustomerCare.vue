<template>
  <div class="customer-care">
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>客户护理设置</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>护理管理</el-breadcrumb-item>
          <el-breadcrumb-item>客户护理设置</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 功能说明 -->

    </el-card>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="客户姓名">
              <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="护理级别">
              <el-select v-model="searchForm.careLevelId" placeholder="请选择护理级别" clearable style="width: 100%">
                <el-option label="未设置" :value="null" />
                <el-option
                  v-for="level in careLevelOptions"
                  :key="level.id"
                  :label="level.levelName"
                  :value="level.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item>
              <div class="button-group">
                <el-button type="primary" @click="handleSearch">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="handleReset">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="always">
      <el-table :data="customerList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="客户ID" width="80" />
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="careLevelName" label="护理级别" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.careLevelName" type="success">{{ row.careLevelName }}</el-tag>
            <el-tag v-else type="info">未设置</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="careItemCount" label="护理项目数" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.careItemCount > 0" type="primary">{{ row.careItemCount }}项</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewCustomer(row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button
              v-if="!row.careLevelId"
              size="small"
              type="primary"
              @click="handleSetCareLevel(row)">
              <el-icon><Setting /></el-icon>
              设置护理级别
            </el-button>
            <el-button
              v-if="row.careLevelId"
              size="small"
              type="warning"
              @click="handleRemoveCareLevel(row)">
              <el-icon><Delete /></el-icon>
              移除护理级别
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchCustomerList"
          @current-change="fetchCustomerList"
        />
      </div>
    </el-card>

    <!-- 设置护理级别对话框 -->
    <el-dialog
      v-model="careSettingDialogVisible"
      title="设置护理级别"
      width="900px"
      :close-on-click-modal="false">

      <div class="care-setting-content">
        <div class="customer-info">
          <el-alert
            :title="`正在为客户 ${currentCustomer?.customerName} 设置护理级别`"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 20px" />
        </div>

        <el-form ref="careFormRef" :model="careFormData" :rules="careFormRules" label-width="120px">
          <el-form-item label="护理级别" prop="careLevelId">
            <el-select
              v-model="careFormData.careLevelId"
              placeholder="请选择护理级别"
              style="width: 300px"
              @change="handleCareLevelChange">
              <el-option
                v-for="level in careLevelOptions"
                :key="level.id"
                :label="level.levelName"
                :value="level.id" />
            </el-select>
          </el-form-item>

          <!-- 护理项目展示 -->
          <el-form-item label="包含护理项目" v-if="levelCareItems.length > 0">
            <div class="care-items-display">
              <el-row :gutter="10">                <el-col v-for="item in levelCareItems" :key="item.id" :span="12">
                  <div class="care-item-card">
                    <div class="item-info">
                      <span class="item-name">{{ item.careItem.itemName }}</span>
                      <span class="item-price">¥{{ item.careItem.price }}/次</span>
                    </div>
                    <div class="item-details">
                      <span class="item-cycle">{{ item.careItem.executeCycle }}天周期</span>
                      <span class="item-times">{{ item.careItem.executeTimes }}次</span>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-form-item>

          <el-form-item label="购买设置">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="购买数量" prop="purchaseQuantity" label-width="80px">
                  <el-input-number
                    v-model="careFormData.purchaseQuantity"
                    :min="1"
                    :max="100"
                    style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="服务到期日" prop="expireDate" label-width="80px">
                  <el-date-picker
                    v-model="careFormData.expireDate"
                    type="date"
                    placeholder="选择到期日期"
                    style="width: 100%"
                    :disabled-date="disabledDate" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="预计费用" label-width="80px">
                  <el-input
                    :value="formatCurrency(calculateTotalCost())"
                    readonly
                    style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="careSettingDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmSetCareLevel" :loading="submitting">确定设置</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 客户详情对话框 -->
    <el-dialog
      v-model="customerDetailDialogVisible"
      title="客户护理详情"
      width="1000px">

      <div class="customer-detail-content" v-if="customerDetail">
        <div class="customer-basic-info">
          <h3>客户基本信息</h3>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="客户姓名">{{ customerDetail.customerName }}</el-descriptions-item>
            <el-descriptions-item label="护理级别">
              <el-tag v-if="customerDetail.careLevelName" type="success">{{ customerDetail.careLevelName }}</el-tag>
              <el-tag v-else type="info">未设置</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(customerDetail.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="customer-care-items" v-if="customerDetail.careItems && customerDetail.careItems.length > 0">
          <h3>护理项目明细</h3>
          <el-table :data="customerDetail.careItems" border>
            <el-table-column prop="careItemName" label="护理项目" width="150" />
            <el-table-column prop="unitPrice" label="单价" width="100">
              <template #default="{ row }">¥{{ (row.unitPrice || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="purchaseDate" label="购买日期" width="120" />
            <el-table-column prop="purchaseQuantity" label="购买数量" width="100" align="center" />
            <el-table-column prop="usedQuantity" label="已使用" width="100" align="center" />
            <el-table-column prop="remainingQuantity" label="剩余数量" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.remainingQuantity > 0" type="success">{{ row.remainingQuantity }}</el-tag>
                <el-tag v-else type="danger">0</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="expireDate" label="到期日期" width="120" />
            <el-table-column prop="serviceStatus" label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.serviceStatus === 'NORMAL'" type="success">正常</el-tag>
                <el-tag v-else-if="row.serviceStatus === 'EXPIRED'" type="danger">已过期</el-tag>
                <el-tag v-else type="warning">{{ row.serviceStatus }}</el-tag>
              </template>
            </el-table-column>

          </el-table>
        </div>

        <div v-else class="no-care-items">
          <el-empty description="暂无护理项目" />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="customerDetailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Setting, Delete } from '@element-plus/icons-vue'
import careApi from '@/api/care'
import type {
  CustomerCareQueryParams,
  CustomerCarePageDTO,
  CustomerCareLevelSetDTO,
  CustomerCareDetailDTO,
  CareLevel,
  CareLevelItemConfig
} from '@/api/care'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const customerList = ref<CustomerCarePageDTO[]>([])
const careLevelOptions = ref<CareLevel[]>([])
const levelCareItems = ref<CareLevelItemConfig[]>([])

// 分页数据
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive<CustomerCareQueryParams>({
  customerName: '',
  careLevelId: undefined
})

// 护理级别设置对话框
const careSettingDialogVisible = ref(false)
const currentCustomer = ref<CustomerCarePageDTO | null>(null)
const careFormRef = ref()

// 护理级别设置表单
const careFormData = reactive<CustomerCareLevelSetDTO>({
  customerId: 0,
  careLevelId: 0,
  purchaseQuantity: 1,
  expireDate: '',
  careItems: []
})

// 表单验证规则
const careFormRules = {
  careLevelId: [{ required: true, message: '请选择护理级别', trigger: 'change' }],
  purchaseQuantity: [{ required: true, message: '请输入购买数量', trigger: 'blur' }],
  expireDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }]
}

// 客户详情对话框
const customerDetailDialogVisible = ref(false)
const customerDetail = ref<CustomerCareDetailDTO | null>(null)

// 生命周期
onMounted(() => {
  fetchCustomerList()
  fetchCareLevels()
  // 默认设置3个月后到期
  careFormData.expireDate = new Date(Date.now() + 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
})

// 获取客户列表
const fetchCustomerList = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }

    console.log('客户列表查询参数:', params)
    const response = await careApi.customerCare.list(params)
    console.log('客户列表原始响应数据:', response)

    // 由于响应拦截器已经处理了 Result 结构，response 应该直接是 IPage 数据
    // 兼容不同的数据结构
    let records = []
    let total = 0

    if (response && (response as any).records) {
      // IPage 结构: { records: [], total: 0, size: 10, current: 1 }
      records = (response as any).records || []
      total = (response as any).total || 0
    } else if (response && (response as any).data && (response as any).data.records) {
      // 可能的嵌套结构
      records = (response as any).data.records || []
      total = (response as any).data.total || 0
    } else if (Array.isArray(response)) {
      // 直接数组响应
      records = response
      total = response.length
    } else {
      // 其他情况
      console.warn('未知的响应数据结构:', response)
      records = []
      total = 0
    }

    customerList.value = records
    pagination.total = total
    console.log('客户列表数据设置完成:', records.length, '条记录，总计:', total)
  } catch (error) {
    console.error('获取客户列表失败:', error)
    ElMessage.error('获取客户列表失败')
  } finally {
    loading.value = false
  }
}

// 获取护理级别选项
const fetchCareLevels = async () => {
  try {
    console.log('获取护理级别选项...')
    const response = await careApi.careLevel.getAll()
    console.log('护理级别响应数据:', response)

    // 兼容不同的响应数据结构
    let levels = []
    if (response && (response as any).data) {
      levels = (response as any).data || []
    } else if (Array.isArray(response)) {
      levels = response
    } else {
      levels = []
    }

    careLevelOptions.value = levels
    console.log('护理级别选项设置完成:', levels.length, '个级别')
  } catch (error) {
    console.error('获取护理级别失败:', error)
    ElMessage.error('获取护理级别失败')
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.pageNum = 1
  fetchCustomerList()
}

// 重置搜索
const handleReset = () => {
  searchForm.customerName = ''
  searchForm.careLevelId = undefined
  pagination.pageNum = 1
  fetchCustomerList()
}

// 查看客户详情
const handleViewCustomer = async (customer: CustomerCarePageDTO) => {
  try {
    customerDetail.value = null
    customerDetailDialogVisible.value = true

    console.log('获取客户详情:', customer.id)
    const response = await careApi.customerCare.getCustomerDetail(customer.id)
    console.log('客户详情响应数据:', response)

    // 兼容不同的响应数据结构
    let detail = null
    if (response && (response as any).data) {
      detail = (response as any).data
    } else if (response) {
      detail = response
    }

    customerDetail.value = detail
    console.log('客户详情设置完成:', detail)
  } catch (error) {
    console.error('获取客户详情失败:', error)
    ElMessage.error('获取客户详情失败')
    customerDetailDialogVisible.value = false
  }
}

// 设置护理级别
const handleSetCareLevel = (customer: CustomerCarePageDTO) => {
  currentCustomer.value = customer
  careFormData.customerId = customer.id
  careFormData.careLevelId = 0
  careFormData.purchaseQuantity = 1
  careFormData.expireDate = new Date(Date.now() + 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  careFormData.careItems = []
  levelCareItems.value = []
  careSettingDialogVisible.value = true
}

// 护理级别变化处理
const handleCareLevelChange = async (careLevelId: number) => {
  if (!careLevelId) {
    levelCareItems.value = []
    careFormData.careItems = []
    return
  }

  try {
    console.log('获取护理级别项目配置:', careLevelId)
    const response = await careApi.careLevel.getItemConfigs(careLevelId)
    console.log('护理级别项目配置响应:', response)

    // 兼容不同的响应数据结构
    let items = []
    if (response && (response as any).data) {
      items = (response as any).data || []
    } else if (Array.isArray(response)) {
      items = response
    } else {
      items = []
    }

    levelCareItems.value = items
    console.log('护理级别项目配置设置完成:', items.length, '个项目')

    // 自动设置护理项目
    careFormData.careItems = levelCareItems.value.map(item => ({
      careItemId: item.careItemId,
      quantity: careFormData.purchaseQuantity,
      expireDate: careFormData.expireDate
    }))
  } catch (error) {
    console.error('获取护理级别项目失败:', error)
    ElMessage.error('获取护理级别项目失败')
    levelCareItems.value = []
    careFormData.careItems = []
  }
}

// 确认设置护理级别
const handleConfirmSetCareLevel = async () => {
  try {
    await careFormRef.value?.validate()

    await ElMessageBox.confirm(
      `确定为客户 ${currentCustomer.value?.customerName} 设置护理级别吗？`,
      '确认设置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true

    // 更新护理项目的数量和到期日期
    careFormData.careItems = levelCareItems.value.map(item => ({
      careItemId: item.careItemId,
      quantity: careFormData.purchaseQuantity,
      expireDate: careFormData.expireDate
    }))

    await careApi.customerCare.setCareLevel(careFormData)

    ElMessage.success('护理级别设置成功')
    careSettingDialogVisible.value = false
    fetchCustomerList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置护理级别失败:', error)
      ElMessage.error('设置护理级别失败')
    }
  } finally {
    submitting.value = false
  }
}

// 移除护理级别
const handleRemoveCareLevel = async (customer: CustomerCarePageDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定移除客户 ${customer.customerName} 的护理级别吗？\n注意：这将同时移除客户的所有护理项目！`,
      '确认移除',
      {
        confirmButtonText: '确定移除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    await careApi.customerCare.removeCareLevel(customer.id)

    ElMessage.success('护理级别移除成功')
    fetchCustomerList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除护理级别失败:', error)
      ElMessage.error('移除护理级别失败')
    }
  }
}

// 计算总费用
const calculateTotalCost = (): number => {
  if (!levelCareItems.value.length || !careFormData.purchaseQuantity) {
    return 0
  }

  const totalPrice = levelCareItems.value.reduce((sum, item) => {
    return sum + (item.careItem?.price || 0)
  }, 0)

  return totalPrice * careFormData.purchaseQuantity
}

// 格式化货币
const formatCurrency = (amount: number): string => {
  return `¥${amount.toFixed(2)}`
}

// 格式化日期时间
const formatDateTime = (dateTime: string): string => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 禁用日期（不能选择今天之前的日期）
const disabledDate = (time: Date): boolean => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}
</script>

<style scoped>
.customer-care {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

.page-header-card {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-card {
  margin-bottom: 20px;
}

.button-group {
  display: flex;
  gap: 10px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.care-setting-content {
  max-height: 600px;
  overflow-y: auto;
}

.care-items-display {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
}

.care-item-card {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
}

.care-item-card:last-child {
  margin-bottom: 0;
}

.item-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.item-name {
  font-weight: bold;
  color: #303133;
}

.item-price {
  color: #f56c6c;
  font-weight: bold;
}

.item-details {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #909399;
}

.customer-detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.customer-basic-info,
.customer-care-items {
  margin-bottom: 20px;
}

.customer-basic-info h3,
.customer-care-items h3 {
  margin-bottom: 10px;
  color: #303133;
  border-bottom: 2px solid #409eff;
  padding-bottom: 5px;
}

.no-care-items {
  text-align: center;
  padding: 40px 0;
}
</style>
