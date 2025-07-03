<template>
  <div class="meal-config-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>膳食配置管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            新增配置
          </el-button>
        </div>
      </template>

      <!-- 搜索筛选区域 -->
      <div class="search-area">
        <el-form :model="queryForm" inline class="search-form">
          <el-form-item label="膳食名称">
            <el-input
              v-model="queryForm.mealName"
              placeholder="请输入膳食名称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="餐次类型">
            <el-select v-model="queryForm.mealType" placeholder="请选择餐次" clearable style="width: 150px">
              <el-option label="早餐" value="BREAKFAST" />
              <el-option label="午餐" value="LUNCH" />
              <el-option label="晚餐" value="DINNER" />
              <el-option label="加餐" value="SNACK" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 120px">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchData">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 批量操作区域 -->
      <div class="batch-actions" v-if="selectedIds.length > 0">
        <el-alert
          :title="`已选择 ${selectedIds.length} 项`"
          type="info"
          show-icon
          :closable="false"
        >
          <template #default>
            <el-button type="success" size="small" @click="batchEnable">批量启用</el-button>
            <el-button type="warning" size="small" @click="batchDisable">批量禁用</el-button>
            <el-button type="danger" size="small" @click="batchDelete">批量删除</el-button>
          </template>
        </el-alert>
      </div>

     
      <el-table
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="mealName" label="膳食名称" min-width="150" />
        <el-table-column prop="mealType" label="餐次类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getMealTypeTagType(row.mealType)">
              {{ getMealTypeName(row.mealType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="estimatedCost" label="预估成本" width="100">
          <template #default="{ row }">
            ¥{{ row.estimatedCost }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showDetailDialog(row)">
              详情
            </el-button>
            <el-button type="warning" size="small" @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteItem(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageInfo.current"
          v-model:page-size="pageInfo.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pageInfo.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="膳食名称" prop="mealName">
              <el-input v-model="formData.mealName" placeholder="请输入膳食名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="餐次类型" prop="mealType">
              <el-select v-model="formData.mealType" placeholder="请选择餐次类型" style="width: 100%">
                <el-option label="早餐" value="BREAKFAST" />
                <el-option label="午餐" value="LUNCH" />
                <el-option label="晚餐" value="DINNER" />
                <el-option label="加餐" value="SNACK" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="膳食描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入膳食描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="主食" prop="mainFood">
              <el-input
                v-model="formData.mainFood"
                type="textarea"
                :rows="2"
                placeholder="请输入主食内容，如：米饭、面条等"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜品" prop="sideDishes">
              <el-input
                v-model="formData.sideDishes"
                type="textarea"
                :rows="2"
                placeholder="请输入菜品内容"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="汤品">
              <el-input
                v-model="formData.soup"
                placeholder="请输入汤品"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="水果">
              <el-input
                v-model="formData.fruits"
                placeholder="请输入水果"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="制作方法" prop="cookingMethod">
          <el-input
            v-model="formData.cookingMethod"
            type="textarea"
            :rows="3"
            placeholder="请输入制作方法"
          />
        </el-form-item>

        <el-form-item label="营养信息" prop="nutritionInfo">
          <el-input
            v-model="formData.nutritionInfo"
            type="textarea"
            :rows="2"
            placeholder="请输入营养信息，如：热量、蛋白质、脂肪、碳水化合物等"
          />
        </el-form-item>

        <el-form-item label="适宜人群" prop="suitableCrowd">
          <el-input
            v-model="formData.suitableCrowd"
            type="textarea"
            :rows="2"
            placeholder="请输入适宜人群，如：所有人群、糖尿病患者等"
          />
        </el-form-item>

        <el-form-item label="饮食禁忌" prop="dietaryRestrictions">
          <el-input
            v-model="formData.dietaryRestrictions"
            type="textarea"
            :rows="2"
            placeholder="请输入饮食禁忌信息"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预估成本" prop="estimatedCost">
              <el-input-number
                v-model="formData.estimatedCost"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="膳食配置详情" width="800px">
      <div class="detail-content" v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="膳食名称">{{ detailData.mealName }}</el-descriptions-item>
          <el-descriptions-item label="餐次类型">
            <el-tag :type="getMealTypeTagType(detailData.mealType)">
              {{ getMealTypeName(detailData.mealType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预估成本">¥{{ detailData.estimatedCost }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailData.status === 1 ? 'success' : 'danger'" size="small">
              {{ detailData.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
          <el-descriptions-item label="创建者">{{ detailData.creatorName }}</el-descriptions-item>
          <el-descriptions-item label="膳食描述" :span="2">{{ detailData.description }}</el-descriptions-item>
          <el-descriptions-item label="主食" :span="2">{{ detailData.mainFood }}</el-descriptions-item>
          <el-descriptions-item label="菜品" :span="2">{{ detailData.sideDishes }}</el-descriptions-item>
          <el-descriptions-item label="汤品">{{ detailData.soup }}</el-descriptions-item>
          <el-descriptions-item label="水果">{{ detailData.fruits }}</el-descriptions-item>
          <el-descriptions-item label="制作方法" :span="2">{{ detailData.cookingMethod }}</el-descriptions-item>
          <el-descriptions-item label="营养信息" :span="2">{{ detailData.nutritionInfo }}</el-descriptions-item>
          <el-descriptions-item label="适宜人群" :span="2">{{ detailData.suitableCrowd }}</el-descriptions-item>
          <el-descriptions-item label="饮食禁忌" :span="2">{{ detailData.dietaryRestrictions }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElForm } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { mealConfigApi } from '@/api/meal'

// 表格数据
const loading = ref(false)
const tableData = ref<any[]>([])
const selectedIds = ref<number[]>([])

// 分页信息
const pageInfo = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 查询表单
const queryForm = reactive({
  mealName: '',
  mealType: '',
  status: undefined as number | undefined
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const detailDialogVisible = ref(false)
const detailData = ref<any>(null)

// 表单
const formRef = ref<InstanceType<typeof ElForm>>()
const formData = reactive({
  id: null,
  mealName: '',
  mealType: '',
  description: '',
  mainFood: '',
  sideDishes: '',
  soup: '',
  fruits: '',
  cookingMethod: '',
  nutritionInfo: '',
  suitableCrowd: '',
  dietaryRestrictions: '',
  estimatedCost: 0,
  status: 1
})

const formRules = {
  mealName: [{ required: true, message: '请输入膳食名称', trigger: 'blur' }],
  mealType: [{ required: true, message: '请选择餐次类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入膳食描述', trigger: 'blur' }],
  estimatedCost: [{ required: true, message: '请输入预估成本', trigger: 'blur' }]
}

// 页面加载时获取数据
onMounted(() => {
  fetchData()
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      page: pageInfo.current,
      size: pageInfo.size
    }

    try {
      const response = await mealConfigApi.getList(params)
      console.log('API响应:', response) // 调试用
      console.log('响应类型:', typeof response) // 调试用
      console.log('响应数据:', response.data || response) // 调试用

      // 根据实际响应结构处理数据
      let data = response

      // 如果响应包装在data中
      if (response.data) {
        data = response.data
      }

      // 如果有code字段，说明是统一响应格式
      if (data.code === 200 && data.data) {
        data = data.data
      }

      // 设置表格数据和分页信息
      if (data.records && Array.isArray(data.records)) {
        tableData.value = data.records
        pageInfo.total = data.total || 0
        pageInfo.current = data.current || 1
        pageInfo.size = data.size || 10
        console.log('设置表格数据:', tableData.value.length, '条记录')
        console.log('第一条记录:', tableData.value[0])
        console.log('tableData.value:', tableData.value)
      } else if (Array.isArray(data)) {
        // 如果直接返回数组
        tableData.value = data
        pageInfo.total = data.length
        console.log('设置表格数据（数组）:', tableData.value.length, '条记录')
        console.log('第一条记录:', tableData.value[0])
      } else {
        console.warn('未识别的数据格式:', data)
        tableData.value = []
        pageInfo.total = 0
      }
    } catch (apiError) {
      // 如果API调用失败，使用模拟数据
      console.warn('API调用失败，使用模拟数据:', apiError)

      // 模拟数据
      const mockData = [
        {
          id: 1,
          mealName: '营养粥套餐',
          mealType: 'BREAKFAST',
          description: '小米粥配咸菜和鸡蛋',
          mainFood: '小米粥',
          sideDishes: '咸菜',
          soup: '',
          fruits: '',
          cookingMethod: '煮粥，水煮蛋',
          nutritionInfo: '蛋白质:12g,碳水化合物:45g,脂肪:8g,热量:280kcal',
          suitableCrowd: '所有人群',
          dietaryRestrictions: '',
          estimatedCost: 8.50,
          status: 1,
          creatorName: '营养师李医生',
          createTime: '2025-06-22 10:00:00'
        },
        {
          id: 2,
          mealName: '红烧肉套餐',
          mealType: 'LUNCH',
          description: '经典红烧肉配米饭和蔬菜',
          mainFood: '米饭',
          sideDishes: '红烧肉、青菜',
          soup: '紫菜蛋花汤',
          fruits: '',
          cookingMethod: '红烧，蒸煮，炒制',
          nutritionInfo: '蛋白质:25g,碳水化合物:60g,脂肪:20g,热量:520kcal',
          suitableCrowd: '一般人群',
          dietaryRestrictions: '高血压患者慎食',
          estimatedCost: 18.00,
          status: 1,
          creatorName: '营养师李医生',
          createTime: '2025-06-22 11:00:00'
        }
      ]

      tableData.value = mockData
      pageInfo.total = mockData.length
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
    tableData.value = []
    pageInfo.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const searchData = () => {
  pageInfo.current = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(queryForm, {
    mealName: '',
    mealType: '',
    status: undefined
  })
  searchData()
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map((item: any) => item.id)
}

// 显示新增对话框
const showAddDialog = () => {
  dialogTitle.value = '新增膳食配置'
  resetForm()
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row: any) => {
  dialogTitle.value = '编辑膳食配置'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 显示详情对话框
const showDetailDialog = (row: any) => {
  detailData.value = row
  detailDialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    mealName: '',
    mealType: '',
    description: '',
    mainFood: '',
    sideDishes: '',
    soup: '',
    fruits: '',
    cookingMethod: '',
    nutritionInfo: '',
    suitableCrowd: '',
    dietaryRestrictions: '',
    estimatedCost: 0,
    status: 1
  })
  formRef.value?.clearValidate()
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    if (formData.id) {
      await mealConfigApi.update(formData.id, formData as any)
      ElMessage.success('更新成功')
    } else {
      await mealConfigApi.create(formData as any)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除单项
const deleteItem = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除膳食配置"${row.mealName}"吗？`, '提示', {
      type: 'warning'
    })

    await mealConfigApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量启用
const batchEnable = async () => {
  try {
    await mealConfigApi.batchToggleStatus(selectedIds.value)
    ElMessage.success('批量启用成功')
    fetchData()
  } catch (error) {
    ElMessage.error('批量启用失败')
  }
}

// 批量禁用
const batchDisable = async () => {
  try {
    await mealConfigApi.batchToggleStatus(selectedIds.value)
    ElMessage.success('批量禁用成功')
    fetchData()
  } catch (error) {
    ElMessage.error('批量禁用失败')
  }
}

// 批量删除
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 项吗？`, '提示', {
      type: 'warning'
    })

    await mealConfigApi.batchDelete(selectedIds.value)
    ElMessage.success('批量删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 辅助方法
const getMealTypeName = (type: string) => {
  const map: Record<string, string> = {
    BREAKFAST: '早餐',
    LUNCH: '午餐',
    DINNER: '晚餐',
    SNACK: '加餐'
  }
  return map[type] || type
}

const getMealTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    BREAKFAST: 'success',
    LUNCH: 'primary',
    DINNER: 'warning',
    SNACK: 'info'
  }
  return map[type] || 'default'
}
</script>

<style scoped>
.meal-config-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 20px;
}

.search-form {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.batch-actions {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.detail-content {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
