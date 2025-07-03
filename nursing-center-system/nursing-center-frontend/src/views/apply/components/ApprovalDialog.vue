<template>
  <div class="approval-dialog">
    <div class="apply-info">
      <h4>申请信息</h4>
      <el-descriptions :column="2" size="small" border>
        <el-descriptions-item label="客户姓名">{{ applyData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ applyData.applicantName }}</el-descriptions-item>
        <el-descriptions-item v-if="applyType === 'checkout'" label="退住类型">
          {{ formatCheckoutType((applyData as CheckoutApply).checkoutType) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="applyType === 'checkout'" label="退住时间">
          {{ (applyData as CheckoutApply).checkoutDate }}
        </el-descriptions-item>
        <el-descriptions-item v-if="applyType === 'outing'" label="外出时间">
          {{ (applyData as OutingApply).outingDate }}
        </el-descriptions-item>
        <el-descriptions-item v-if="applyType === 'outing'" label="预计回院时间">
          {{ (applyData as OutingApply).expectedReturnDate }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ applyData.createTime }}</el-descriptions-item>
        <el-descriptions-item 
          :label="applyType === 'checkout' ? '退住原因' : '外出事由'" 
          :span="2"
        >
          {{ applyType === 'checkout' ? (applyData as CheckoutApply).checkoutReason : (applyData as OutingApply).outingReason }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <el-form :model="approvalForm" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="审批结果" prop="approveResult">
        <el-radio-group v-model="approvalForm.approveResult">
          <el-radio value="APPROVED">通过</el-radio>
          <el-radio value="REJECTED">拒绝</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="审批备注" prop="approveRemark">
        <el-input
          v-model="approvalForm.approveRemark"
          type="textarea"
          :rows="4"
          placeholder="请输入审批备注"
        />
      </el-form-item>

      <!-- 退住申请特殊处理 -->
      <div v-if="applyType === 'checkout' && approvalForm.approveResult === 'APPROVED'" class="checkout-settings">
        <el-form-item label="床位处理">
          <el-radio-group v-model="approvalForm.bedAction">
            <el-radio value="RELEASE">释放床位</el-radio>
            <el-radio value="RESERVE">保留床位</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="approvalForm.bedAction === 'RESERVE'" label="保留期限">
          <el-date-picker
            v-model="approvalForm.reserveEndDate"
            type="date"
            placeholder="请选择保留截止日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="费用结算">
          <el-checkbox v-model="approvalForm.settleAccounts">是否结算费用</el-checkbox>
        </el-form-item>
      </div>

      <!-- 外出申请特殊处理 -->
      <div v-if="applyType === 'outing' && approvalForm.approveResult === 'APPROVED'" class="outing-settings">
        <el-form-item label="床位状态">
          <el-radio-group v-model="approvalForm.bedStatusAction">
            <el-radio value="OUT">标记为外出</el-radio>
            <el-radio value="KEEP">保持当前状态</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="提醒设置">
          <el-checkbox v-model="approvalForm.enableReminder">开启回院提醒</el-checkbox>
        </el-form-item>

        <el-form-item v-if="approvalForm.enableReminder" label="提醒时间">
          <el-time-picker
            v-model="approvalForm.reminderTime"
            placeholder="请选择提醒时间"
            format="HH:mm"
            value-format="HH:mm"
          />
        </el-form-item>
      </div>
    </el-form>

    <div class="approval-preview" v-if="approvalForm.approveResult">
      <el-alert
        :title="getPreviewTitle()"
        :type="approvalForm.approveResult === 'APPROVED' ? 'success' : 'warning'"
        :closable="false"
        show-icon
      >
        <template #default>
          <div class="preview-content">
            <p>{{ getPreviewContent() }}</p>
            <div v-if="approvalForm.approveResult === 'APPROVED'" class="action-list">
              <h5>执行操作：</h5>
              <ul>
                <li v-for="action in getActionList()" :key="action">{{ action }}</li>
              </ul>
            </div>
          </div>
        </template>
      </el-alert>
    </div>

    <div class="dialog-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="loading">
        确认审批
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applyApi, type CheckoutApply, type OutingApply } from '@/api/apply'

interface Props {
  applyType: 'checkout' | 'outing'
  applyData: CheckoutApply | OutingApply
}

const props = defineProps<Props>()
const emit = defineEmits<{
  success: []
  cancel: []
}>()

const loading = ref(false)
const formRef = ref()

const approvalForm = reactive({
  approveResult: '',
  approveRemark: '',
  // 退住申请相关
  bedAction: 'RELEASE',
  reserveEndDate: '',
  settleAccounts: false,
  // 外出申请相关
  bedStatusAction: 'OUT',
  enableReminder: false,
  reminderTime: ''
})

const rules = {
  approveResult: [
    { required: true, message: '请选择审批结果', trigger: 'change' }
  ],
  approveRemark: [
    { required: true, message: '请输入审批备注', trigger: 'blur' }
  ]
}

// 预览标题
const getPreviewTitle = () => {
  const actionName = props.applyType === 'checkout' ? '退住申请' : '外出申请'
  const resultName = approvalForm.approveResult === 'APPROVED' ? '通过' : '拒绝'
  return `${actionName}审批${resultName}`
}

// 预览内容
const getPreviewContent = () => {
  if (approvalForm.approveResult === 'APPROVED') {
    return props.applyType === 'checkout' ? '审批通过后，将执行以下操作：' : '审批通过后，将执行以下操作：'
  } else {
    return '申请将被拒绝，客户和申请人将收到通知。'
  }
}

// 获取操作列表
const getActionList = () => {
  const actions: string[] = []
  
  if (props.applyType === 'checkout') {
    if (approvalForm.bedAction === 'RELEASE') {
      actions.push('释放客户床位')
    } else {
      actions.push(`保留床位至 ${approvalForm.reserveEndDate}`)
    }
    
    if (approvalForm.settleAccounts) {
      actions.push('进行费用结算')
    }
    
    actions.push('更新客户状态为已退住')
  } else {
    if (approvalForm.bedStatusAction === 'OUT') {
      actions.push('标记床位状态为外出')
    }
    
    if (approvalForm.enableReminder) {
      actions.push(`设置回院提醒时间：${approvalForm.reminderTime}`)
    }
    
    actions.push('更新客户外出状态')
  }
  
  actions.push('发送通知给客户和申请人')
  
  return actions
}

// 格式化退住类型
const formatCheckoutType = (type: string) => {
  const typeMap: Record<string, string> = {
    'NORMAL': '正常退住',
    'TRANSFER': '转院',
    'DEATH': '身故',
    'OTHER': '其他'
  }
  return typeMap[type] || type
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 确认审批
const handleConfirm = async () => {
  if (!formRef.value) return
  
  try {
    // 调试输出 - 验证前
    console.log('验证前的表单数据:', approvalForm)
    console.log('申请数据:', props.applyData)
    
    await formRef.value.validate()
    console.log('表单验证成功')
    
    await ElMessageBox.confirm(
      `确认${approvalForm.approveResult === 'APPROVED' ? '通过' : '拒绝'}此申请吗？`,
      '确认审批',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    
    // 构造审批数据
    const approvalData = {
      applyId: props.applyData.id,
      applyStatus: approvalForm.approveResult,
      approveRemark: approvalForm.approveRemark
    }
    
    // 调试输出 - 发送前
    console.log('即将发送的审批数据:', approvalData)
    
    // 验证必需字段
    if (!approvalData.applyId) {
      throw new Error('申请ID不存在')
    }
    if (!approvalData.applyStatus) {
      throw new Error('审批状态不存在')
    }
    
    // 调用对应的审批接口
    if (props.applyType === 'checkout') {
      await applyApi.approveCheckout(approvalData)
    } else {
      await applyApi.approveOuting(approvalData)
    }
    
    ElMessage.success('审批成功')
    emit('success')
  } catch (error: any) {
    console.error('审批过程中发生错误:', error)
    if (error.action !== 'cancel') {
      ElMessage.error(`审批失败: ${error.message || '未知错误'}`)
    }
  } finally {
    loading.value = false
  }
}


</script>

<style scoped>
.approval-dialog {
  max-width: 800px;
}

.apply-info {
  margin-bottom: 20px;
}

.apply-info h4 {
  margin-bottom: 12px;
  font-weight: 600;
  color: #303133;
}

.checkout-settings,
.outing-settings {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  margin: 16px 0;
}

.approval-preview {
  margin: 20px 0;
}

.preview-content h5 {
  margin: 8px 0 4px 0;
  font-weight: 600;
}

.action-list ul {
  margin: 8px 0 0 20px;
  padding: 0;
}

.action-list li {
  margin: 4px 0;
  color: #606266;
}

.dialog-footer {
  text-align: right;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
