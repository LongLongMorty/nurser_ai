<template>
  <div class="care-item">
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>护理项目管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>护理管理</el-breadcrumb-item>
          <el-breadcrumb-item>护理项目管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>


    </el-card>    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="5">
            <el-form-item label="项目名称">
              <el-input v-model="searchForm.itemName" placeholder="请输入项目名称" clearable />
            </el-form-item>
          </el-col>          <el-col :span="5">
            <el-form-item label="护理类型">
              <el-input v-model="searchForm.careType" placeholder="请输入护理类型" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%">
                <el-option label="启用" value="1" />
                <el-option label="停用" value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="10">
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
                <el-button type="primary" @click="handleAdd">
                  <el-icon><Plus /></el-icon>
                  新增项目
                </el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="always">
      <el-table :data="itemList" v-loading="loading" stripe border>        <el-table-column prop="itemName" label="项目名称" width="150" />
        <el-table-column prop="itemCode" label="项目编码" width="120" />
        <el-table-column label="执行周期" width="120" align="center">
          <template #default="{ row }">
            {{ row.executeCycle }}天
          </template>
        </el-table-column>
        <el-table-column label="执行次数" width="120" align="center">
          <template #default="{ row }">
            {{ row.executeTimes }}次
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价(元)" width="120" align="center">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="项目描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
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
          @size-change="fetchItemList"
          @current-change="fetchItemList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px">        <el-form-item label="项目名称" prop="itemName">
          <el-input
            v-model="formData.itemName"
            placeholder="请输入项目名称"
            :disabled="isViewMode" />
        </el-form-item>
        <el-form-item label="项目编码" prop="itemCode">
          <el-input
            v-model="formData.itemCode"
            placeholder="请输入项目编码"
            :disabled="isEdit || isViewMode" />
        </el-form-item>
        <el-form-item label="服务单价" prop="price">
          <el-input-number
            v-model="formData.price"
            :min="0"
            :precision="2"
            placeholder="请输入服务单价"
            :disabled="isViewMode"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="执行周期" prop="executeCycle">
          <el-input-number
            v-model="formData.executeCycle"
            :min="1"
            placeholder="执行周期(天)"
            :disabled="isViewMode"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="执行次数" prop="executeTimes">
          <el-input-number
            v-model="formData.executeTimes"
            :min="1"
            placeholder="执行次数"
            :disabled="isViewMode"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status" :disabled="isViewMode">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
            :disabled="isViewMode" />
        </el-form-item>
      </el-form>
        <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">
            {{ isViewMode ? '关闭' : '取消' }}
          </el-button>
          <el-button
            v-if="!isViewMode"
            type="primary"
            @click="handleSubmit"
            :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, View, Edit, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { careApi, type CareItem, type CareItemDTO } from '@/api/care'

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const isViewMode = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 搜索表单
const searchForm = reactive({
  itemName: '',
  careType: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表格数据
const itemList = ref<CareItem[]>([])

// 表单数据
const formData = reactive({
  id: null as number | null,
  itemName: '',
  itemCode: '',
  price: 0,
  executeCycle: 1,
  executeTimes: 1,
  status: 1,
  description: ''
})

// 表单验证规则
const formRules: FormRules = {
  itemName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ],
  itemCode: [
    { required: true, message: '请输入项目编码', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入服务单价', trigger: 'blur' }
  ],
  executeCycle: [
    { required: true, message: '请输入执行周期', trigger: 'blur' }
  ],
  executeTimes: [
    { required: true, message: '请输入执行次数', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  if (isViewMode.value) return '查看护理项目'
  return isEdit.value ? '编辑护理项目' : '新增护理项目'
})

// 方法
const fetchItemList = async () => {
  loading.value = true
  try {    const params = {
      ...searchForm,
      pageNum: pagination.page,
      pageSize: pagination.size
    }

    const response = await careApi.careItem.list(params)
    console.log('API响应数据:', response)

    // 检查响应结构并适配
    if (response && response.data) {
      // 如果是Result<IPage>结构
      const data = response.data
      if (data.records) {
        itemList.value = data.records
        pagination.total = data.total
      } else if (Array.isArray(data)) {
        // 如果直接是数组
        itemList.value = data
        pagination.total = data.length
      } else {
        // 其他情况
        itemList.value = []
        pagination.total = 0
      }    } else {
      // 如果没有data字段，可能直接是IPage对象
      if (response && (response as any).records) {
        itemList.value = (response as any).records
        pagination.total = (response as any).total
      } else {
        itemList.value = []
        pagination.total = 0
      }
    }
  } catch (error) {
    console.error('获取护理项目列表失败:', error)
    ElMessage.error('获取护理项目列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchItemList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    itemName: '',
    careType: '',
    status: ''
  })
  pagination.page = 1
  fetchItemList()
}

const handleAdd = () => {
  isEdit.value = false
  isViewMode.value = false
  resetForm()
  dialogVisible.value = true
}

const handleView = (row: CareItem) => {
  Object.assign(formData, row)
  isEdit.value = false
  isViewMode.value = true
  dialogVisible.value = true
}

const handleEdit = (row: CareItem) => {
  Object.assign(formData, row)
  isEdit.value = true
  isViewMode.value = false
  dialogVisible.value = true
}

const handleToggleStatus = async (row: CareItem) => {
  const action = row.status === 1 ? '停用' : '启用'
  const newStatus = row.status === 1 ? 0 : 1

  let confirmMessage = `确定要${action}该护理项目吗？`
  if (row.status === 1) {
    confirmMessage += '\n\n注意：停用后该项目将：\n• 从护理级别配置中移除\n• 无法被新客户选择\n• 不影响客户已有的护理服务和记录'
  }

  try {
    await ElMessageBox.confirm(
      confirmMessage,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )

    // 更新状态
    const updateData = { ...row, status: newStatus }
    await careApi.careItem.update(updateData)

    row.status = newStatus
    ElMessage.success(`${action}成功`)

    // 重新加载列表以确保数据同步
    fetchItemList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('状态切换失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const handleDelete = async (row: CareItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除护理项目"${row.itemName}"吗？\n\n注意：删除后该项目将：\n• 从系统中移除（逻辑删除）\n• 从护理级别配置中移除\n• 不影响客户已有的护理服务和记录\n\n此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )

    await careApi.careItem.delete(row.id)
    ElMessage.success('删除成功')
    fetchItemList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (valid) {
      submitForm()
    }
  })
}

const submitForm = async () => {
  submitting.value = true
  try {
    const submitData: CareItemDTO = {
      id: formData.id || undefined,
      itemName: formData.itemName,
      itemCode: formData.itemCode,
      price: formData.price,
      executeCycle: formData.executeCycle,
      executeTimes: formData.executeTimes,
      description: formData.description,
      status: formData.status
    }

    if (isEdit.value && formData.id) {
      await careApi.careItem.update(submitData)
      ElMessage.success('更新成功')
    } else {
      await careApi.careItem.create(submitData)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    fetchItemList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(`${isEdit.value ? '更新' : '创建'}失败`)
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  Object.assign(formData, {
    id: null,
    itemName: '',
    itemCode: '',
    price: 0,
    executeCycle: 1,
    executeTimes: 1,
    status: 1,
    description: ''
  })

  isViewMode.value = false

  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchItemList()
})
</script>

<style scoped>
.care-item {
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

.price-text {
  color: #e6a23c;
  font-weight: 600;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>
