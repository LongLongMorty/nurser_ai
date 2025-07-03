<template>
  <div class="customer-detail">
    <div class="page-header">
      <h2>客户详情</h2>
      <div class="header-actions">
        <el-button @click="handleGoBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-button type="primary" @click="handleEdit">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="detail-content">
      <el-card class="basic-info-card" shadow="never">
        <template #header>          <div class="card-header">
            <span>基本信息</span>
            <el-tag :type="getStatusType(customerDetail.status || 0)">
              {{ formatStatus(customerDetail.status || 0) }}
            </el-tag>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>姓名：</label>
              <span>{{ customerDetail.customerName }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>性别：</label>
              <span>{{ formatGender(customerDetail.gender) }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>年龄：</label>
              <span>{{ customerDetail.age }}岁</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>身份证号：</label>
              <span>{{ customerDetail.idCard }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>出生日期：</label>
              <span>{{ customerDetail.birthDate }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>血型：</label>
              <span>{{ customerDetail.bloodType || '未填写' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>监护人姓名：</label>
              <span>{{ customerDetail.guardianName || '未填写' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>监护人电话：</label>
              <span>{{ customerDetail.guardianPhone || '未填写' }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-card class="residence-info-card" shadow="never">
        <template #header>
          <span>居住信息</span>
        </template>

        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>楼栋：</label>
              <span>{{ customerDetail.buildingName }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>房间：</label>
              <span>{{ customerDetail.roomNo }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>床位：</label>
              <span>{{ customerDetail.bedNo }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>入住时间：</label>
              <span>{{ customerDetail.checkInDate }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>合同到期时间：</label>
              <span>{{ customerDetail.contractExpireDate }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-card class="service-info-card" shadow="never">
        <template #header>
          <span>服务信息</span>
        </template>

        <el-row :gutter="20">
          <el-col :span="8">
            <div class="info-item">
              <label>客户类型：</label>
              <span>{{ formatCustomerType(customerDetail.customerType) }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>护理级别：</label>
              <span>{{ customerDetail.careLevelName || '未设置' }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="info-item">
              <label>健康管家：</label>
              <span>{{ customerDetail.healthManagerName || '未分配' }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-card class="record-info-card" shadow="never">
        <template #header>
          <span>记录信息</span>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <label>创建时间：</label>
              <span>{{ customerDetail.createdAt }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>更新时间：</label>
              <span>{{ customerDetail.updatedAt || '未更新' }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 护理记录 -->
      <el-card class="care-records-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>护理记录</span>
            <el-button size="small" type="primary" @click="viewCareRecords">
              查看全部记录
            </el-button>
          </div>
        </template>

        <el-table :data="recentCareRecords" stripe>
          <el-table-column prop="recordDate" label="日期" width="120" />
          <el-table-column prop="careItemName" label="护理项目" />
          <el-table-column prop="nurseStaff" label="护理人员" width="120" />
          <el-table-column prop="careResult" label="护理结果" />
          <el-table-column prop="remarks" label="备注" show-overflow-tooltip />
        </el-table>

        <div v-if="recentCareRecords.length === 0" class="empty-data">
          暂无护理记录
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Edit } from '@element-plus/icons-vue'
import { customerApi } from '@/api/customer'
import type { Customer } from '@/api/customer'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const customerDetail = ref<Customer>({} as Customer)
const recentCareRecords = ref([])

// 获取客户详情
const fetchCustomerDetail = async () => {
  const customerId = Number(route.params.id)
  if (!customerId) {
    ElMessage.error('客户ID无效')
    return
  }

  loading.value = true
  try {
    customerDetail.value = await customerApi.getById(customerId)
    // TODO: 获取最近的护理记录
    // recentCareRecords.value = await careApi.getRecentRecords(customerId)
  } catch (error: any) {
    console.error('获取客户详情失败:', error)
    ElMessage.error('获取客户详情失败')
  } finally {
    loading.value = false
  }
}

// 返回列表
const handleGoBack = () => {
  router.push('/customer/list')
}

// 编辑客户
const handleEdit = () => {
  router.push(`/customer/edit/${customerDetail.value.id}`)
}

// 查看护理记录
const viewCareRecords = () => {
  router.push(`/care/record?customerId=${customerDetail.value.id}`)
}

// 格式化性别显示
const formatGender = (gender: string) => {
  const genderMap: Record<string, string> = {
    'MALE': '男',
    'FEMALE': '女'
  }
  return genderMap[gender] || gender
}

// 格式化状态显示
const formatStatus = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '已退住',
    1: '在住'
  }
  return statusMap[status] || '未知'
}

// 获取状态标签类型
const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'info',
    1: 'success'
  }
  return typeMap[status] || 'info'
}

// 格式化客户类型
const formatCustomerType = (type?: string) => {
  const typeMap: Record<string, string> = {
    'SELF_CARE': '自理老人',
    'CARE': '护理老人'
  }
  return typeMap[type || ''] || '未设置'
}

onMounted(() => {
  fetchCustomerDetail()
})
</script>

<style scoped>
.customer-detail {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
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

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.info-item label {
  font-weight: 500;
  color: #666;
  min-width: 120px;
  margin-right: 10px;
}

.info-item span {
  color: #333;
  flex: 1;
}

.empty-data {
  text-align: center;
  color: #999;
  padding: 40px 0;
}

:deep(.el-card__header) {
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
