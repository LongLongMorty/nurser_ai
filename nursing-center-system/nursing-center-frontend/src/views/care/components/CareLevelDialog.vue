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
      <el-form-item label="等级名称" prop="levelName">
        <el-input v-model="formData.levelName" placeholder="请输入等级名称" />
      </el-form-item>      <el-form-item label="等级编码" prop="levelCode">
        <el-input v-model="formData.levelCode" placeholder="请输入等级编码" />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入等级描述"        />
      </el-form-item>      <el-form-item label="状态" prop="status" v-if="mode !== 'add'">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
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
import { careApi, type CareLevel, type CareLevelDTO } from '@/api/care'

interface Props {
  modelValue: boolean
  mode: 'add' | 'edit' | 'view'
  data?: CareLevel | null
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
    add: '新增护理等级',
    edit: '编辑护理等级',
    view: '查看护理等级'
  }
  return titleMap[props.mode]
})

const formData = reactive<CareLevelDTO>({
  levelName: '',
  levelCode: '',
  description: '',
  status: 1
})

const formRules: FormRules = {
  levelName: [
    { required: true, message: '请输入等级名称', trigger: 'blur' },
    { min: 2, max: 20, message: '等级名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],  levelCode: [
    { required: true, message: '请输入等级编码', trigger: 'blur' },
    { pattern: /^[A-Z0-9_]+$/, message: '等级编码只能包含大写字母、数字和下划线', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入描述', trigger: 'blur' },
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    levelName: '',
    levelCode: '',
    description: '',
    status: 1
  })
  formRef.value?.clearValidate()
}

// 填充表单数据
const fillFormData = (data: CareLevel) => {
  Object.assign(formData, {
    id: data.id,
    levelName: data.levelName,
    levelCode: data.levelCode,
    description: data.description,
    status: data.status === 'ACTIVE' ? 1 : 0
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    if (props.mode === 'add') {
      await careApi.careLevel.create(formData)
      ElMessage.success('新增成功')
    } else {
      await careApi.careLevel.update(formData)
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
