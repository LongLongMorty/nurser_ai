<template>
  <div class="bed-usage-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>床位使用管理</h2>
    </div>

    <!-- 搜索表单 -->
    <div class="search-section">
      <el-card>
        <el-form
          ref="searchFormRef"
          :model="searchForm"
          inline
          label-width="100px"
          class="search-form"
        >
          <el-form-item label="客户姓名">
            <el-input
              v-model="searchForm.customerName"
              placeholder="请输入客户姓名"
              clearable
              style="width: 180px"
            />
          </el-form-item>
          <el-form-item label="入住日期">
            <el-date-picker
              v-model="searchForm.startDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item label="使用状态">
            <el-select
              v-model="searchForm.usageStatus"
              placeholder="请选择状态"
              clearable
              style="width: 180px"
            >
              <el-option label="正在使用" value="USING" />
              <el-option label="历史记录" value="HISTORY" />
            </el-select>
          </el-form-item>
          <el-form-item label="床位号">
            <el-input
              v-model="searchForm.bedNo"
              placeholder="请输入床位号"
              clearable
              style="width: 120px"
            />
          </el-form-item>
          <el-form-item label="房间号">
            <el-input
              v-model="searchForm.roomNo"
              placeholder="请输入房间号"
              clearable
              style="width: 120px"
            />
          </el-form-item>
          <el-form-item label="楼层">
            <el-select
              v-model="searchForm.floorNo"
              placeholder="请选择楼层"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="floor in floorOptions"
                :key="floor"
                :label="`${floor}楼`"
                :value="floor"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>床位使用记录</span>
          </div>
        </template>

        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          border
          style="width: 100%"
          @sort-change="handleSortChange"
        >          <el-table-column prop="id" label="记录ID" width="80" />          <el-table-column label="客户姓名" width="120">
            <template #default="{ row }">
              {{ row.customerName }}
            </template>
          </el-table-column>
          <el-table-column label="客户信息" width="180">
            <template #default="{ row }">
              <div class="customer-info">
                <div>年龄：{{ row.age }}岁</div>
                <div>性别：{{ row.gender === 'MALE' ? '男' : '女' }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="床位位置" width="200">
            <template #default="{ row }">
              <div class="bed-location">
                <div>{{ row.fullLocation }}</div>
                <el-tag size="small" type="info">{{ row.bedNo }}</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="startDate" label="入住时间" width="120" sortable="custom" />
          <el-table-column prop="endDate" label="结束时间" width="120">
            <template #default="{ row }">
              <span v-if="row.endDate">{{ row.endDate }}</span>
              <el-tag v-else type="success" size="small">正在使用</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="使用天数" width="100">
            <template #default="{ row }">
              {{ row.duration }}天
            </template>
          </el-table-column>
          <el-table-column prop="usageStatus" label="状态" width="100">
            <template #default="{ row }">
              <el-tag
                :type="row.usageStatus === 'USING' ? 'success' : 'info'"
                size="small"
              >
                {{ row.usageStatus === 'USING' ? '正在使用' : '历史记录' }}
              </el-tag>
            </template>
          </el-table-column>          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.usageStatus === 'USING'"
                type="primary"
                size="small"
                @click="handleSwapBed(row)"
              >
                床位调换
              </el-button>
              <el-button
                v-if="row.usageStatus === 'USING'"
                type="warning"
                size="small"
                @click="handleEditEndDate(row)"
              >
                设置结束
              </el-button>
              <el-button
                v-else
                type="info"
                size="small"
                disabled
              >
                已结束
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 设置结束时间弹窗 -->
    <el-dialog
      v-model="endDateVisible"
      title="设置结束时间"
      width="400px"
    >
      <el-form
        ref="endDateFormRef"
        :model="endDateForm"
        :rules="endDateRules"
        label-width="100px"
      >
        <el-form-item label="客户姓名">
          <span>{{ selectedRecord?.customerName }}</span>
        </el-form-item>
        <el-form-item label="床位信息">
          <span>{{ selectedRecord?.fullLocation }}</span>
        </el-form-item>
        <el-form-item label="入住时间">
          <span>{{ selectedRecord?.startDate }}</span>
        </el-form-item>
        <el-form-item label="结束时间" prop="endDate">
          <el-date-picker
            v-model="endDateForm.endDate"
            type="date"
            placeholder="选择结束时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="endDateVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmEndDate">确定</el-button>        </span>
      </template>
    </el-dialog>

    <!-- 床位调换弹窗 -->
    <el-dialog
      v-model="swapDialogVisible"
      title="床位调换"
      width="800px"
      :close-on-click-modal="false"
    >
      <BedSwapDialog
        :source-bed="sourceBedForSwap"
        @success="handleSwapSuccess"
        @cancel="handleSwapCancel"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { bedUsageApi, type BedUsage, type BedUsageQuery, type BedInfo } from '@/api/bed'
import BedSwapDialog from './components/BedSwapDialog.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<BedUsage[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const endDateVisible = ref(false)
const selectedRecord = ref<BedUsage | null>(null)

// 床位调换相关
const swapDialogVisible = ref(false)
const sourceBedForSwap = ref<BedInfo | null>(null)

const searchForm = reactive<BedUsageQuery & { startDateRange?: string[] }>({
  customerName: '',
  startDateRange: undefined,
  usageStatus: 'USING', // 默认查询正在使用的
  bedNo: '',
  roomNo: '',
  floorNo: undefined
})

const endDateForm = reactive({
  endDate: ''
})

const floorOptions = [1, 2, 3, 4, 5] // 可根据实际情况配置

// 表单验证规则
const endDateRules = {
  endDate: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

// 获取床位使用数据
const fetchData = async () => {
  loading.value = true
  try {
    const params: BedUsageQuery = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      customerName: searchForm.customerName || undefined,
      usageStatus: searchForm.usageStatus || undefined,
      bedNo: searchForm.bedNo || undefined,
      roomNo: searchForm.roomNo || undefined,
      floorNo: searchForm.floorNo || undefined
    }    // 处理日期范围
    if (searchForm.startDateRange && searchForm.startDateRange.length === 2) {
      params.startDateFrom = searchForm.startDateRange[0]
      params.startDateTo = searchForm.startDateRange[1]
    }

    console.log('请求参数:', params)
    const response = await bedUsageApi.getBedUsagePage(params)
    console.log('API原始响应:', response)
    console.log('响应类型:', typeof response)
    console.log('是否为数组:', Array.isArray(response))
    console.log('response.data:', response.data)
    console.log('response.data类型:', typeof response.data)
    
    // 检查响应结构
    if (response && typeof response === 'object') {
      console.log('响应对象的keys:', Object.keys(response))
      if (response.data) {
        console.log('response.data的keys:', Object.keys(response.data))
        console.log('response.data.records:', response.data.records)
        console.log('response.data.total:', response.data.total)
      }
    }
      // 如果response本身就是数据，而不是包装对象
    if (Array.isArray(response)) {
      console.log('响应是数组，直接使用')
      tableData.value = response
      total.value = response.length
    } else if (response && (response.records || response.total !== undefined)) {
      // 直接使用response的records和total（MyBatis Plus分页格式）
      console.log('响应是分页对象，直接使用response.records')
      tableData.value = response.records || []
      total.value = response.total || 0
    } else if (response && response.data) {
      console.log('响应有data属性，使用response.data')
      tableData.value = response.data.records || []
      total.value = response.data.total || 0
    } else {
      console.error('未知的响应格式:', response)
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取床位使用数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    customerName: '',
    startDateRange: undefined,
    usageStatus: 'USING',
    bedNo: '',
    roomNo: '',
    floorNo: undefined
  })
  currentPage.value = 1
  fetchData()
}

// 分页变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

// 排序变化
const handleSortChange = ({ prop, order }: any) => {
  // 这里可以添加排序逻辑
  console.log('排序:', prop, order)
}

// 编辑结束时间
const handleEditEndDate = (record: BedUsage) => {
  selectedRecord.value = record
  endDateForm.endDate = ''
  endDateVisible.value = true
}

// 确认设置结束时间
const handleConfirmEndDate = async () => {
  if (!selectedRecord.value) return

  try {
    await ElMessageBox.confirm(
      '设置结束时间后，该床位使用记录将变为历史记录，确定要继续吗？',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await bedUsageApi.updateEndDate(selectedRecord.value.id, endDateForm.endDate)
    ElMessage.success('结束时间设置成功')
    endDateVisible.value = false
    fetchData() // 刷新列表
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('设置结束时间失败:', error)
      ElMessage.error('设置结束时间失败')
    }
  }
}

// 床位调换
const handleSwapBed = (record: BedUsage) => {
  console.log('床位调换 - 原始记录:', record)
  
  // 构造BedInfo对象 - 使用扁平化的数据结构
  sourceBedForSwap.value = {
    id: record.bedId,
    bedNo: record.bedNo,
    bedType: 'STANDARD', // 这里可以根据实际需要调整
    bedStatus: 'OCCUPIED',
    roomId: 0, // 如果需要可以从record中获取
    roomNo: record.roomNo,
    roomName: record.roomNo,
    floorNo: record.floorNo,
    buildingId: 1,
    buildingNo: record.buildingNo,
    buildingName: record.buildingNo + '号楼',
    customerName: record.customerName,
    customerId: record.customerId
  }
  
  console.log('床位调换 - 构造的sourceBed:', sourceBedForSwap.value)
  swapDialogVisible.value = true
}

// 床位调换成功回调
const handleSwapSuccess = () => {
  swapDialogVisible.value = false
  ElMessage.success('床位调换成功')
  fetchData() // 刷新列表
}

// 床位调换取消回调
const handleSwapCancel = () => {
  swapDialogVisible.value = false
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.bed-usage-management {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 20px;
}

.page-header h2 {
  color: white;
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

/* 搜索区域 */
.search-section {
  margin-bottom: 20px;
}

.search-form {
  padding: 20px;
}

.search-form .el-form-item {
  margin-bottom: 15px;
}

/* 表格区域 */
.table-section {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.customer-info {
  font-size: 12px;
  color: #666;
}

.customer-info div {
  margin-bottom: 2px;
}

.bed-location {
  font-size: 12px;
}

.bed-location div {
  margin-bottom: 4px;
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  border-top: 1px solid #ebeef5;
  margin-top: 20px;
}

/* 弹窗样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .bed-usage-management {
    padding: 10px;
  }
  
  .search-form {
    padding: 10px;
  }
  
  .search-form .el-form-item {
    margin-bottom: 10px;
  }
  
  .el-table {
    font-size: 12px;
  }
  
  .pagination-container {
    padding: 15px 0;
  }
}

/* 表格样式优化 */
.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-table th {
  background: #f8f9fa;
  color: #333;
  font-weight: 600;
}

.el-table td {
  padding: 12px 0;
}

.el-table .el-button {
  border-radius: 6px;
}

/* 卡片样式 */
.el-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.el-card:hover {
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

/* 按钮样式 */
.el-button {
  border-radius: 6px;
  font-weight: 500;
}

.el-button--primary {
  background: linear-gradient(45deg, #409eff, #66b3ff);
  border: none;
}

.el-button--primary:hover {
  background: linear-gradient(45deg, #66b3ff, #409eff);
}

/* 标签样式 */
.el-tag {
  border-radius: 4px;
  font-weight: 500;
}

/* 表单样式 */
.el-form-item__label {
  font-weight: 500;
  color: #333;
}

.el-input__wrapper {
  border-radius: 6px;
}

.el-select .el-input__wrapper {
  border-radius: 6px;
}

.el-date-editor .el-input__wrapper {
  border-radius: 6px;
}
</style>
