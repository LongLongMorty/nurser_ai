<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑健康管理师' : '新增健康管理师'"
    width="600px"
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      label-position="right"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="管理师姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入管理师姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工号" prop="employeeId">
            <el-input v-model="form.employeeId" placeholder="请输入工号" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入联系电话" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="专业资质" prop="qualification">
            <el-select v-model="form.qualification" placeholder="请选择专业资质" style="width: 100%">
              <el-option label="护理师" value="NURSE" />
              <el-option label="康复师" value="THERAPIST" />
              <el-option label="营养师" value="NUTRITIONIST" />
              <el-option label="心理咨询师" value="PSYCHOLOGIST" />
              <el-option label="医师" value="DOCTOR" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工作年限" prop="experience">
            <el-input-number 
              v-model="form.experience" 
              :min="0" 
              :max="50" 
              placeholder="工作年限"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="部门" prop="department">
            <el-select v-model="form.department" placeholder="请选择部门" style="width: 100%">
              <el-option label="护理部" value="NURSING" />
              <el-option label="康复部" value="REHABILITATION" />
              <el-option label="营养部" value="NUTRITION" />
              <el-option label="心理健康部" value="PSYCHOLOGY" />
              <el-option label="医疗部" value="MEDICAL" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
              <el-option label="在职" value="ACTIVE" />
              <el-option label="休假" value="LEAVE" />
              <el-option label="停职" value="SUSPENDED" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="专长领域" prop="specialties">
        <el-select 
          v-model="form.specialties" 
          multiple 
          placeholder="请选择专长领域" 
          style="width: 100%"
        >
          <el-option label="慢性病管理" value="CHRONIC_DISEASE" />
          <el-option label="康复护理" value="REHABILITATION" />
          <el-option label="心理健康" value="MENTAL_HEALTH" />
          <el-option label="营养指导" value="NUTRITION" />
          <el-option label="药物管理" value="MEDICATION" />
          <el-option label="健康教育" value="HEALTH_EDUCATION" />
        </el-select>
      </el-form-item>

      <el-form-item label="简介" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入管理师简介"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createHealthManager, updateHealthManager, type HealthManager, type HealthManagerDTO } from '@/api/healthManager'

interface Props {
  visible: boolean
  data?: HealthManager | null
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  data: undefined
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const isEdit = computed(() => !!props.data?.id)

const form = reactive<HealthManagerDTO>({
  realName: '',
  name: '',
  employeeId: '',
  phone: '',
  email: '',
  qualification: '',
  workExperience: 0,
  experience: 0,
  department: '',
  specialties: [],
  certifications: [],
  maxCustomers: 50,
  status: 'ACTIVE',
  description: ''
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入管理师姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  employeeId: [
    { required: true, message: '请输入工号', trigger: 'blur' },
    { pattern: /^[A-Z0-9]{4,10}$/, message: '工号格式为4-10位大写字母或数字', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  qualification: [
    { required: true, message: '请选择专业资质', trigger: 'change' }
  ],
  department: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      resetForm()
      if (props.data) {
        Object.assign(form, props.data)
      }
    }
  }
)

const resetForm = () => {
  Object.assign(form, {
    name: '',
    employeeId: '',
    phone: '',
    email: '',
    qualification: '',
    experience: 0,
    department: '',
    specialties: [],
    status: 'ACTIVE',
    description: ''
  })
  formRef.value?.clearValidate()
}

const handleClose = () => {
  emit('update:visible', false)
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    loading.value = true

    if (isEdit.value) {
      await updateHealthManager(props.data!.id, form)
      ElMessage.success('健康管理师更新成功')
    } else {
      await createHealthManager(form)
      ElMessage.success('健康管理师创建成功')
    }

    emit('success')
    emit('update:visible', false)
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
