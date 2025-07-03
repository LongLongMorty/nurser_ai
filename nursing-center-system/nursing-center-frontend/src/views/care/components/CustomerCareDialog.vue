<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="800px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      :disabled="mode === 'view'"
    >
      <el-form-item label="客户" prop="customerId">
        <el-select
          v-model="formData.customerId"
          placeholder="请选择客户"
          style="width: 100%"
          filterable
          @change="handleCustomerChange"
        >          <el-option
            v-for="customer in customerOptions"
            :key="customer.id"
            :label="customer.customerName || customer.name"
            :value="customer.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="护理等级" prop="careLevelId">
        <el-select
          v-model="formData.careLevelId"
          placeholder="请选择护理等级"
          style="width: 100%"
          @change="handleCareLevelChange"
        >
          <el-option
            v-for="level in careLevels"
            :key="level.id"
            :label="`${level.levelName} (¥${level.baseFee}/月)`"
            :value="level.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="护理项目" prop="careItemIds">
        <div class="care-items-section">
          <div class="item-filter">
            <el-select
              v-model="careTypeFilter"
              placeholder="按类型筛选"
              clearable
              @change="handleCareTypeFilter"
              style="width: 200px; margin-bottom: 10px;"
            >
              <el-option label="日常护理" value="DAILY" />
              <el-option label="专业护理" value="PROFESSIONAL" />
              <el-option label="医疗护理" value="MEDICAL" />
              <el-option label="康复护理" value="REHABILITATION" />
            </el-select>
          </div>
          
          <div class="items-grid">
            <el-checkbox-group v-model="formData.careItemIds" @change="calculateTotalCost">
              <div v-for="type in careItemsByType" :key="type.type" class="item-type-group">
                <h4 class="type-title">{{ formatCareType(type.type) }}</h4>
                <div class="items-list">
                  <el-checkbox
                    v-for="item in type.items"
                    :key="item.id"
                    :value="item.id"
                    class="care-item-checkbox"
                  >
                    <div class="item-info">
                      <span class="item-name">{{ item.itemName }}</span>
                      <span class="item-price">¥{{ item.unitPrice.toFixed(2) }}</span>
                    </div>
                    <div class="item-desc">{{ item.description }}</div>
                  </el-checkbox>
                </div>
              </div>
            </el-checkbox-group>
          </div>

          <div class="cost-summary">
            <div class="cost-item">
              <span>护理等级费用:</span>
              <span class="cost-value">¥{{ selectedLevelFee.toFixed(2) }}/月</span>
            </div>
            <div class="cost-item">
              <span>护理项目费用:</span>
              <span class="cost-value">¥{{ selectedItemsCost.toFixed(2) }}/月</span>
            </div>
            <div class="cost-item total">
              <span>总费用:</span>
              <span class="cost-value">¥{{ totalCost.toFixed(2) }}/月</span>
            </div>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="特殊要求">
        <el-input
          v-model="formData.specialRequirements"
          type="textarea"
          :rows="3"
          placeholder="请输入特殊护理要求（可选）"
        />
      </el-form-item>

      <el-form-item label="生效日期" prop="effectiveDate">
        <el-date-picker
          v-model="formData.effectiveDate"
          type="date"
          placeholder="请选择生效日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="状态" prop="status" v-if="mode !== 'add'">
        <el-radio-group v-model="formData.status">
          <el-radio value="ACTIVE">启用</el-radio>
          <el-radio value="INACTIVE">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button v-if="mode !== 'view'" type="primary" @click="handleSubmit" :loading="loading">
          {{ mode === 'add' ? '新增' : '保存' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { careApi, type CustomerCareConfig, type CustomerCareConfigDTO, type CareLevel, type CareItem } from '@/api/care'
import { customerApi, type Customer } from '@/api/customer'

interface Props {
  modelValue: boolean
  mode: 'add' | 'edit' | 'view'
  data?: CustomerCareConfig | null
  careLevels: CareLevel[]
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

interface CareItemsByType {
  type: string
  items: CareItem[]
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const customerOptions = ref<Customer[]>([])
const careItems = ref<CareItem[]>([])
const careTypeFilter = ref('')

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const dialogTitle = computed(() => {
  const titleMap = {
    add: '新增客户护理配置',
    edit: '编辑客户护理配置',
    view: '查看客户护理配置'
  }
  return titleMap[props.mode]
})

const formData = reactive<CustomerCareConfigDTO>({
  customerId: 0,
  careLevelId: 0,
  careItemIds: [],
  specialRequirements: '',
  effectiveDate: '',
  status: 'ACTIVE'
})

const formRules: FormRules = {
  customerId: [
    { required: true, message: '请选择客户', trigger: 'change' }
  ],
  careLevelId: [
    { required: true, message: '请选择护理等级', trigger: 'change' }
  ],
  careItemIds: [
    { required: true, type: 'array', min: 1, message: '请选择至少一个护理项目', trigger: 'change' }
  ],
  effectiveDate: [
    { required: true, message: '请选择生效日期', trigger: 'change' }
  ]
}

// 计算属性
const selectedLevelFee = computed(() => {
  const level = props.careLevels.find(l => l.id === formData.careLevelId)
  return level ? level.baseFee : 0
})

const selectedItemsCost = computed(() => {
  return careItems.value
    .filter(item => formData.careItemIds.includes(item.id))
    .reduce((sum, item) => sum + item.unitPrice, 0)
})

const totalCost = computed(() => {
  return selectedLevelFee.value + selectedItemsCost.value
})

const careItemsByType = computed(() => {
  const filtered = careTypeFilter.value 
    ? careItems.value.filter(item => item.careType === careTypeFilter.value)
    : careItems.value

  const grouped = filtered.reduce((acc, item) => {
    if (!acc[item.careType]) {
      acc[item.careType] = []
    }
    acc[item.careType].push(item)
    return acc
  }, {} as Record<string, CareItem[]>)

  return Object.entries(grouped).map(([type, items]) => ({
    type,
    items: items.sort((a, b) => a.sort - b.sort)
  }))
})

// 获取客户列表
const fetchCustomers = async () => {
  try {    const response = await customerApi.getCustomerPage({
      page: 1,
      size: 1000,
      status: 1  // 使用数字而不是字符串
    })
    if (response.data) {
      customerOptions.value = response.data.records
    }
  } catch (error) {
    console.error('获取客户列表失败:', error)
  }
}

// 获取护理项目列表
const fetchCareItems = async () => {
  try {
    const response = await careApi.careItem.list({
      page: 1,
      size: 1000,
      status: 'ACTIVE'
    })
    if (response.data) {
      careItems.value = response.data.records
    }
  } catch (error) {
    console.error('获取护理项目列表失败:', error)
  }
}

// 处理客户变更
const handleCustomerChange = (customerId: number) => {
  // 可以在这里加载客户特定的护理要求
}

// 处理护理等级变更
const handleCareLevelChange = (levelId: number) => {
  // 可以根据等级推荐护理项目
}

// 处理护理类型筛选
const handleCareTypeFilter = () => {
  // 筛选逻辑已在计算属性中处理
}

// 计算总费用
const calculateTotalCost = () => {
  // 费用计算已在计算属性中处理
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    customerId: 0,
    careLevelId: 0,
    careItemIds: [],
    specialRequirements: '',
    effectiveDate: '',
    status: 'ACTIVE'
  })
  careTypeFilter.value = ''
  formRef.value?.clearValidate()
}

// 填充表单数据
const fillFormData = (data: CustomerCareConfig) => {
  Object.assign(formData, {
    id: data.id,
    customerId: data.customerId,
    careLevelId: data.careLevelId,
    careItemIds: data.careItems.map(item => item.id),
    specialRequirements: data.specialRequirements || '',
    effectiveDate: data.effectiveDate,
    status: data.status
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    if (props.mode === 'add') {
      await careApi.getCustomerCare.create(formData)
      ElMessage.success('新增成功')
    } else {
      await careApi.getCustomerCare.update(formData)
      ElMessage.success('保存成功')
    }

    emit('success')
    handleClose()
  } catch (error) {
    if (error !== false) { // 不是表单验证错误
      ElMessage.error(props.mode === 'add' ? '新增失败' : '保存失败')
    }
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  resetForm()
}

// 工具函数
const formatCareType = (type: string) => {
  const typeMap: Record<string, string> = {
    'DAILY': '日常护理',
    'PROFESSIONAL': '专业护理',
    'MEDICAL': '医疗护理',
    'REHABILITATION': '康复护理'
  }
  return typeMap[type] || type
}

// 监听数据变化
watch(
  () => props.data,
  (newData) => {
    if (newData && props.mode !== 'add') {
      fillFormData(newData)
    } else {
      resetForm()
    }
  },
  { immediate: true }
)

watch(
  () => props.modelValue,
  (newVisible) => {
    if (newVisible) {
      if (props.data && props.mode !== 'add') {
        fillFormData(props.data)
      } else {
        resetForm()
      }
    }
  }
)

// 初始化
onMounted(() => {
  fetchCustomers()
  fetchCareItems()
})
</script>

<style scoped>
.care-items-section {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background-color: #fafafa;
}

.item-filter {
  margin-bottom: 15px;
}

.item-type-group {
  margin-bottom: 20px;
}

.type-title {
  margin: 0 0 10px 0;
  color: #409eff;
  font-size: 14px;
  font-weight: 500;
}

.items-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 10px;
  margin-left: 20px;
}

.care-item-checkbox {
  width: 100%;
  margin-right: 0;
}

.care-item-checkbox :deep(.el-checkbox__label) {
  width: 100%;
  line-height: 1.4;
}

.item-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.item-name {
  color: #303133;
}

.item-price {
  color: #f56c6c;
  font-size: 12px;
}

.item-desc {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.cost-summary {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.cost-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.cost-item.total {
  border-top: 1px solid #e4e7ed;
  padding-top: 8px;
  font-weight: 500;
  font-size: 16px;
}

.cost-value {
  color: #f56c6c;
  font-weight: 500;
}

.dialog-footer {
  text-align: right;
}
</style>
