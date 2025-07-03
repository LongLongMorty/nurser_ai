<template>
  <div class="care-record">
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>护理记录管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>护理管理</el-breadcrumb-item>
          <el-breadcrumb-item>护理记录管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="4">
            <el-form-item label="客户姓名">
              <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="房间号">
              <el-input v-model="searchForm.roomNo" placeholder="请输入房间号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="护理类型">
              <el-select v-model="searchForm.careType" placeholder="请选择护理类型" clearable style="width: 100%">
                <el-option label="日常护理" value="DAILY" />
                <el-option label="专业护理" value="PROFESSIONAL" />
                <el-option label="医疗护理" value="MEDICAL" />
                <el-option label="康复护理" value="REHABILITATION" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="护理人员">
              <el-select v-model="searchForm.nurseId" placeholder="请选择护理人员" clearable filterable style="width: 100%">
                <el-option
                  v-for="nurse in nurseOptions"
                  :key="nurse.id"
                  :label="nurse.name"
                  :value="nurse.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="记录日期">
              <el-date-picker
                v-model="searchForm.recordDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item>
              <div class="button-group">
                <el-button type="primary" @click="handleSearch">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>                <el-button @click="handleReset">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>    <!-- 数据表格 -->
    <el-card class="table-card" shadow="always">
      <el-table :data="recordList" v-loading="loading" stripe border>
        <el-table-column prop="customerName" label="客户姓名" width="120" align="center" />
        <el-table-column prop="itemName" label="护理项目" width="150" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.itemName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="healthManagerName" label="护理人员" width="120" align="center" />
        <el-table-column prop="careTime" label="护理时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.careTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="careQuantity" label="护理数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.careQuantity }}次</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="careContent" label="护理内容" show-overflow-tooltip min-width="200" align="center" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip min-width="150" align="center" />        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchRecordList"
          @current-change="fetchRecordList"
        />
      </div>
    </el-card>    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="护理记录详情"
      width="700px">      <div class="record-detail" v-if="viewData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户姓名">{{ viewData.customerName }}</el-descriptions-item>
          <el-descriptions-item label="护理项目">{{ viewData.itemName }}</el-descriptions-item>
          <el-descriptions-item label="护理人员">{{ viewData.healthManagerName }}</el-descriptions-item>
          <el-descriptions-item label="护理时间">{{ formatDateTime(viewData.careTime || '') }}</el-descriptions-item>
          <el-descriptions-item label="护理数量">{{ viewData.careQuantity }}次</el-descriptions-item>
          <el-descriptions-item label="护理内容" :span="2">
            {{ viewData.careContent || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ viewData.remark || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handlePrint(viewData)">
            <el-icon><Printer /></el-icon>
            打印
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View } from '@element-plus/icons-vue'
import { careApi, type CareRecord, type CareRecordQueryParams } from '@/api/care'
import { systemApi, type User } from '@/api/system'

// 选项类型定义
interface NurseOption {
  id: number
  name: string
}

// 扩展CareRecord类型以包含前端需要的字段
interface ExtendedCareRecord extends CareRecord {
  roomNo?: string
  bedNo?: string
  recordTime?: string
  duration?: number
  urgencyLevel?: string
  careItems?: string[]
  recordContent?: string
  customerReaction?: string
  createTime?: string
}

// 响应式数据
const loading = ref(false)
const viewDialogVisible = ref(false)

// 搜索表单
const searchForm = reactive({
  customerName: '',
  roomNo: '',
  careType: '',
  nurseId: '',
  recordDateRange: []
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表格数据
const recordList = ref<ExtendedCareRecord[]>([])
const viewData = ref<ExtendedCareRecord | null>(null)

// 选项数据
const nurseOptions = ref<NurseOption[]>([])

// 方法
const fetchRecordList = async () => {
  loading.value = true
  try {
    // 构建查询参数，过滤空值
    const params: any = {
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    
    // 只添加非空的搜索参数
    if (searchForm.customerName) {
      params.customerName = searchForm.customerName
    }
    if (searchForm.roomNo) {
      params.roomNo = searchForm.roomNo
    }
    if (searchForm.careType) {
      params.careType = searchForm.careType
    }
    if (searchForm.nurseId) {
      params.nurseId = searchForm.nurseId
    }
    if (searchForm.recordDateRange?.[0]) {
      // 使用后端期望的日期时间格式：yyyy-MM-dd HH:mm:ss
      params.startTime = searchForm.recordDateRange[0] + ' 00:00:00'
    }
    if (searchForm.recordDateRange?.[1]) {
      // 使用后端期望的日期时间格式：yyyy-MM-dd HH:mm:ss
      params.endTime = searchForm.recordDateRange[1] + ' 23:59:59'
    }
    
    console.log('查询护理记录参数:', params)
    const response = await careApi.careRecord.list(params)
    console.log('护理记录查询响应:', response)

    // 响应拦截器已处理Result包装，返回的是IPage数据结构
    recordList.value = (response as any).records || []
    pagination.total = (response as any).total || 0

    console.log('护理记录列表:', recordList.value)
    console.log('总数:', pagination.total)
  } catch (error) {
    console.error('获取护理记录列表失败:', error)
    ElMessage.error('获取护理记录列表失败')
  } finally {
    loading.value = false
  }
}

const fetchOptions = async () => {
  try {
    // 获取护理人员列表（从系统用户中筛选健康管理员角色，作为护理人员）
    const nursesResponse = await systemApi.getUsers({
      pageNum: 1,
      pageSize: 100,
      role: 'HEALTH_MANAGER' // 使用健康管理员角色作为护理人员
    })
    nurseOptions.value = (nursesResponse.data?.records || []).map((user: User) => ({
      id: user.id,
      name: user.realName || user.username
    }))
  } catch (error) {
    console.error('获取选项数据失败:', error)
    ElMessage.error('获取选项数据失败')
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchRecordList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    customerName: '',
    roomNo: '',
    careType: '',
    nurseId: '',
    recordDateRange: []
  })
  pagination.page = 1
  fetchRecordList()
}

// 查看详情
const handleView = (row: any) => {
  viewData.value = row
  viewDialogVisible.value = true
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

const formatCareType = (type: string) => {
  const typeMap: Record<string, string> = {
    'DAILY': '日常护理',
    'PROFESSIONAL': '专业护理',
    'MEDICAL': '医疗护理',
    'REHABILITATION': '康复护理'
  }
  return typeMap[type] || type
}

const getCareTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'DAILY': '',
    'PROFESSIONAL': 'success',
    'MEDICAL': 'warning',
    'REHABILITATION': 'info'
  }
  return colorMap[type] || ''
}

const formatUrgency = (level: string) => {
  const levelMap: Record<string, string> = {
    'LOW': '低',
    'NORMAL': '正常',
    'HIGH': '高',
    'URGENT': '紧急'
  }
  return levelMap[level] || level
}

const getUrgencyColor = (level: string) => {
  const colorMap: Record<string, string> = {
    'LOW': 'info',
    'NORMAL': 'success',
    'HIGH': 'warning',
    'URGENT': 'danger'
  }
  return colorMap[level] || ''
}

// 生命周期
onMounted(() => {
  fetchOptions()
  fetchRecordList()
})
</script>

<style scoped>
.care-record {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

.page-header-card {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(8px);
}

.page-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-header h2 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 22px;
  font-weight: 600;
}

.search-card {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(8px);
  padding-bottom: 2px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0 18px;
}

.button-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-btn {
  margin-right: 8px;
}
.add-btn {
  margin-left: 8px;
}

.table-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  border: none;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(8px);
  padding-top: 8px;
}

.care-item-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}

.record-detail {
  max-height: 500px;
  overflow-y: auto;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
:deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
}
:deep(.el-checkbox) {
  margin-right: 20px;
  margin-bottom: 8px;
}
</style>
