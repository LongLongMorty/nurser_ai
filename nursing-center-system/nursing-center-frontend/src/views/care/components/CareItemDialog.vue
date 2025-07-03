<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      :disabled="mode === 'view'"
    >
      <el-form-item label="项目名称" prop="itemName">
        <el-input v-model="formData.itemName" placeholder="请输入项目名称" />
      </el-form-item>

      <el-form-item label="项目编码" prop="itemCode">
        <el-input v-model="formData.itemCode" placeholder="请输入项目编码" />
      </el-form-item>

      <el-form-item label="护理类型" prop="careType">
        <el-select v-model="formData.careType" placeholder="请选择护理类型" style="width: 100%">
          <el-option label="日常护理" value="DAILY" />
          <el-option label="专业护理" value="PROFESSIONAL" />
          <el-option label="医疗护理" value="MEDICAL" />
          <el-option label="康复护理" value="REHABILITATION" />
        </el-select>
      </el-form-item>

      <el-form-item label="单价" prop="unitPrice">
        <el-input-number
          v-model="formData.unitPrice"
          :min="0"
          :step="1"
          :precision="2"
          placeholder="请输入单价"
          style="width: 100%"
        />
        <div class="help-text">单位：元</div>
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入项目描述"
        />
      </el-form-item>

      <el-form-item label="标准内容" prop="standardContent">
        <el-input
          v-model="formData.standardContent"
          type="textarea"
          :rows="4"
          placeholder="请输入标准护理内容和操作规范"
        />
      </el-form-item>

      <el-form-item label="排序" prop="sort">
        <el-input-number
          v-model="formData.sort"
          :min="0"
          :max="999"
          placeholder="请输入排序值"
          style="width: 100%"
        />
        <div class="help-text">数值越小排序越靠前</div>
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
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { careApi, type CareItem, type CareItemDTO } from '@/api/care'

interface Props {
  modelValue: boolean
  mode: 'add' | 'edit' | 'view'
  data?: CareItem | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const dialogTitle = computed(() => {
  const titleMap = {
    add: '新增护理项目',
    edit: '编辑护理项目',
    view: '查看护理项目'
  }
  return titleMap[props.mode]
})

const formData = reactive<CareItemDTO>({
  itemName: '',
  itemCode: '',
  careType: '',
  description: '',
  standardContent: '',
  unitPrice: 0,
  sort: 0,
  status: 'ACTIVE'
})

const formRules: FormRules = {
  itemName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 30, message: '项目名称长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  itemCode: [
    { required: true, message: '请输入项目编码', trigger: 'blur' },
    { pattern: /^[A-Z0-9_]+$/, message: '项目编码只能包含大写字母、数字和下划线', trigger: 'blur' }
  ],
  careType: [
    { required: true, message: '请选择护理类型', trigger: 'change' }
  ],
  unitPrice: [
    { required: true, message: '请输入单价', trigger: 'blur' },
    { type: 'number', min: 0, message: '单价不能小于0', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入描述', trigger: 'blur' },
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ],
  standardContent: [
    { required: true, message: '请输入标准内容', trigger: 'blur' },
    { max: 500, message: '标准内容不能超过500个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' },
    { type: 'number', min: 0, max: 999, message: '排序值在 0 到 999 之间', trigger: 'blur' }
  ]
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    itemName: '',
    itemCode: '',
    careType: '',
    description: '',
    standardContent: '',
    unitPrice: 0,
    sort: 0,
    status: 'ACTIVE'
  })
  formRef.value?.clearValidate()
}

// 填充表单数据
const fillFormData = (data: CareItem) => {
  Object.assign(formData, {
    id: data.id,
    itemName: data.itemName,
    itemCode: data.itemCode,
    careType: data.careType,
    description: data.description,
    standardContent: data.standardContent,
    unitPrice: data.unitPrice,
    sort: data.sort,
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
      await careApi.careItem.create(formData)
      ElMessage.success('新增成功')
    } else {
      await careApi.careItem.update(formData)
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
</script>

<style scoped>
.help-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.dialog-footer {
  text-align: right;
}
</style>
