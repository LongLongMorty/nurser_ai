<template>
  <div class="customer-add">
    <div class="page-header">
      <h2>新增客户</h2>
      <el-button @click="handleBack">返回列表</el-button>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      class="customer-form"
    >      <el-card title="基本信息">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户姓名" prop="customerName">
              <el-input v-model="form.customerName" placeholder="请输入客户姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别">
                <el-option label="男" value="MALE" />
                <el-option label="女" value="FEMALE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="0" :max="120" placeholder="请输入年龄" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="form.birthDate"
                type="date"
                placeholder="请选择出生日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="血型" prop="bloodType">
              <el-select v-model="form.bloodType" placeholder="请选择血型">
                <el-option label="A型" value="A" />
                <el-option label="B型" value="B" />
                <el-option label="AB型" value="AB" />
                <el-option label="O型" value="O" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <el-card title="监护人信息" style="margin-top: 20px;">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="监护人姓名" prop="guardianName">
              <el-input v-model="form.guardianName" placeholder="请输入监护人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="监护人电话" prop="guardianPhone">
              <el-input v-model="form.guardianPhone" placeholder="请输入监护人电话" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <el-card title="床位信息" style="margin-top: 20px;">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="楼栋" prop="buildingId">
              <el-select v-model="form.buildingId" placeholder="请选择楼栋">
                <el-option label="1号楼" :value="1" />
                <el-option label="2号楼" :value="2" />
                <el-option label="3号楼" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="房间" prop="roomId">
              <el-select v-model="form.roomId" placeholder="请选择房间">
                <el-option label="101" :value="1" />
                <el-option label="102" :value="2" />
                <el-option label="103" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="床位" prop="bedId">
              <el-select v-model="form.bedId" placeholder="请选择床位">
                <el-option label="A床" :value="1" />
                <el-option label="B床" :value="2" />
                <el-option label="C床" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <el-card title="入住信息" style="margin-top: 20px;">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入住时间" prop="checkInDate">
              <el-date-picker
                v-model="form.checkInDate"
                type="date"
                placeholder="请选择入住时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同到期时间" prop="contractExpireDate">
              <el-date-picker
                v-model="form.contractExpireDate"
                type="date"
                placeholder="请选择合同到期时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户类型" prop="customerType">
              <el-select v-model="form.customerType" placeholder="请选择客户类型">
                <el-option label="自理老人" value="SELF_CARE" />
                <el-option label="护理老人" value="CARE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="护理级别" prop="careLevelId">
              <el-select v-model="form.careLevelId" placeholder="请选择护理级别">
                <el-option label="一级护理" :value="1" />
                <el-option label="二级护理" :value="2" />
                <el-option label="三级护理" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <div class="form-actions">
        <el-button @click="handleBack">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          保存
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { customerApi, type CustomerDTO } from '@/api'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<CustomerDTO>({
  customerName: '',
  gender: 'MALE',  // 默认值为有效的枚举值
  age: undefined,
  birthDate: '',
  idCard: '',
  bloodType: '',
  guardianName: '',
  guardianPhone: '',
  buildingId: undefined,
  roomId: undefined,
  bedId: undefined,
  checkInDate: '',
  contractExpireDate: '',
  customerType: 'SELF_CARE',  // 默认值为有效的枚举值
  careLevelId: undefined,
  healthManagerId: undefined,
  status: 1
})

const rules: FormRules = {
  customerName: [
    { required: true, message: '请输入客户姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  birthDate: [
    { required: false, message: '请选择出生日期', trigger: 'change' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|([23]\d))\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  buildingId: [
    { required: true, message: '请选择楼栋', trigger: 'change' }
  ],
  roomId: [
    { required: true, message: '请选择房间', trigger: 'change' }
  ],
  bedId: [
    { required: true, message: '请选择床位', trigger: 'change' }
  ],
  checkInDate: [
    { required: true, message: '请选择入住时间', trigger: 'change' }
  ],
  contractExpireDate: [
    { required: true, message: '请选择合同到期时间', trigger: 'change' }
  ]
}

const handleBack = () => {
  router.back()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      // 调用创建客户的API
      await customerApi.create(form)

      ElMessage.success('客户创建成功')
      router.push('/customer/list')
    } catch (error) {
      ElMessage.error('创建失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.customer-add {
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

.customer-form {
  max-width: 800px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e6e6e6;
}
</style>
