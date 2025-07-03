<template>
  <div class="customer-list">
    <div class="page-header">
      <h2>客户列表</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增客户
      </el-button>
    </div>
    
    <div class="search-section">      <el-form :model="searchForm" inline>
        <el-form-item label="客户姓名">
          <el-input
            v-model="searchForm.customerName"
            placeholder="请输入客户姓名"
            clearable
          />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input
            v-model="searchForm.idCard"
            placeholder="请输入身份证号"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
      <div class="table-section">
      <el-table :data="customerList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="customerName" label="姓名" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ formatGender(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="guardianPhone" label="监护人电话" />
        <el-table-column prop="guardianName" label="监护人姓名" />
        <el-table-column prop="checkInDate" label="入住时间" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { customerApi } from '@/api'
import type { Customer } from '@/api/customer'

const router = useRouter()

const loading = ref(false)
const customerList = ref<Customer[]>([])

const searchForm = reactive({
  customerName: '',
  idCard: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const fetchCustomerList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1, // 后端分页从0开始
      size: pagination.size,
      customerName: searchForm.customerName || undefined,
      idCard: searchForm.idCard || undefined
    }
    const response = await customerApi.getList(params)
    // 响应拦截器已经返回了 data.data，所以直接使用 response
    customerList.value = response.records
    pagination.total = response.total
  } catch (error) {
    console.error('获取客户列表失败:', error)
    ElMessage.error('获取客户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchCustomerList()
}

const handleReset = () => {
  searchForm.customerName = ''
  searchForm.idCard = ''
  handleSearch()
}

const handleAdd = () => {
  router.push('/customer/add')
}

const handleView = (row: Customer) => {
  router.push(`/customer/detail/${row.id}`)
}

const handleEdit = (row: Customer) => {
  router.push(`/customer/edit/${row.id}`)
}

const handleDelete = async (row: Customer) => {
  try {    await ElMessageBox.confirm(`确认删除客户 ${row.customerName} 吗？`, '确认删除', {
      type: 'warning'
    })
    
    if (!row.id) {
      ElMessage.error('客户ID不存在')
      return
    }
    
    await customerApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchCustomerList()
  } catch (error: any) {
    if (error.action !== 'cancel') {
      console.error('删除客户失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSizeChange = () => {
  fetchCustomerList()
}

const handleCurrentChange = () => {
  fetchCustomerList()
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

onMounted(() => {
  fetchCustomerList()
})
</script>

<style scoped>
.customer-list {
  background: white;
  padding: 20px;
  border-radius: 8px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 6px;
}

.table-section {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
