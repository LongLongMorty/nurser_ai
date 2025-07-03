<template>
  <div class="user-management">
    <!-- 页面头部 -->
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>用户管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>系统管理</el-breadcrumb-item>
          <el-breadcrumb-item>用户管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>

    <!-- 查询区域 -->
    <el-card class="search-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>用户查询</span>
          <el-button type="primary" @click="showAddDialog" :icon="Plus">
            新增用户
          </el-button>
        </div>
      </template>

      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="用户名">
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户名"
              style="width: 200px"
              clearable
            />
          </el-form-item>
          <el-form-item label="真实姓名">
            <el-input
              v-model="searchForm.realName"
              placeholder="请输入真实姓名"
              style="width: 200px"
              clearable
            />
          </el-form-item>
          <el-form-item label="角色">
            <el-select
              v-model="searchForm.role"
              placeholder="请选择角色"
              style="width: 150px"
              clearable
            >
              <el-option label="管理员" value="ADMIN" />
              <el-option label="健康管家" value="HEALTH_MANAGER" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              style="width: 120px"
              clearable
            >
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 用户列表 -->
      <el-table
        :data="userList"
        v-loading="loading"
        stripe
        border
        style="width: 100%">
        <el-table-column prop="username" label="用户名" width="150" align="center" />
        <el-table-column prop="realName" label="真实姓名" width="120" align="center" />
        <el-table-column prop="phone" label="手机号" width="150" align="center" />
        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button
                type="warning"
                size="small"
                @click="handleResetPassword(row)">
                重置密码
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
                :disabled="row.username === 'admin'">
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="searchForm.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      :close-on-click-modal="false">
      <el-form
        :model="userForm"
        :rules="userRules"
        ref="userFormRef"
        label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="userForm.username"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="userForm.realName"
            placeholder="请输入真实姓名"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="userForm.phone"
            placeholder="请输入手机号"
            @blur="handlePhoneBlur"
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select
            v-model="userForm.role"
            placeholder="请选择角色"
            style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="健康管家" value="HEALTH_MANAGER" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="默认为手机号后6位"
            show-password
            readonly
          />
          <div class="form-tip">
            <el-text type="info" size="small">
              密码默认设置为手机号后6位
            </el-text>
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleConfirm"
          :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { systemApi } from '@/api/system'
import type { SysUser } from '@/api/system'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

// 分页当前页（前端显示用，从1开始）
const currentPage = ref(1)

// 表单数据
const searchForm = reactive({
  username: '',
  realName: '',
  role: '',
  status: '',
  page: 0, // 后端分页从0开始
  size: 10
})

const userForm = reactive({
  id: 0,
  username: '',
  realName: '',
  phone: '',
  role: '',
  password: '',
  status: 1
})

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { max: 20, message: '姓名长度不能超过 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 数据列表
const userList = ref<SysUser[]>([])
const total = ref(0)

// 表单引用
const userFormRef = ref()

// 计算属性
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑用户' : '新增用户'
})

// 方法
const handleSearch = async () => {
  searchForm.page = 0
  currentPage.value = 1
  await fetchUserList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    realName: '',
    role: '',
    status: '',
    page: 0,
    size: 10
  })
  currentPage.value = 1
  fetchUserList()
}

const handleSizeChange = (val: number) => {
  searchForm.size = val
  searchForm.page = 0
  currentPage.value = 1
  fetchUserList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  searchForm.page = val - 1 // 前端显示从1开始，后端从0开始
  fetchUserList()
}

// 获取用户列表
const fetchUserList = async () => {
  try {
    loading.value = true
    console.log('查询参数:', searchForm)
      // 构造查询参数
    const queryParams = {
      page: searchForm.page,
      size: searchForm.size,
      username: searchForm.username || undefined,
      realName: searchForm.realName || undefined,
      role: searchForm.role || undefined,
      status: searchForm.status !== '' ? Number(searchForm.status) : undefined
    }

    const response = await systemApi.user.list(queryParams)
    console.log('用户列表响应:', response)    // 处理响应数据 - 使用any类型处理不同的响应结构
    const pageData: any = response.data || response
    
    if (pageData && (pageData.records || pageData.content)) {
      userList.value = pageData.records || pageData.content || []
      total.value = pageData.total || pageData.totalElements || 0
      currentPage.value = (pageData.current || 1) // 更新当前页
    } else {
      userList.value = []
      total.value = 0
      currentPage.value = 1
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 显示新增对话框
const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (user: SysUser) => {
  isEdit.value = true
  Object.assign(userForm, {
    id: user.id,
    username: user.username,
    realName: user.realName,
    phone: user.phone,
    role: user.role,
    status: user.status
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(userForm, {
    id: 0,
    username: '',
    realName: '',
    phone: '',
    role: '',
    password: '',
    status: 1
  })
  userFormRef.value?.clearValidate()
}

// 手机号失去焦点事件，自动设置密码
const handlePhoneBlur = () => {
  if (!isEdit.value && userForm.phone && userForm.phone.length >= 6) {
    userForm.password = userForm.phone.slice(-6)
  }
}

// 确认提交
const handleConfirm = async () => {
  try {
    await userFormRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      await systemApi.user.update(userForm)
      ElMessage.success('用户更新成功')
    } else {
      await systemApi.user.create(userForm)
      ElMessage.success('用户创建成功')
    }

    dialogVisible.value = false
    await fetchUserList()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 状态切换
const handleStatusChange = async (user: SysUser) => {
  try {
    await systemApi.user.updateStatus(user.id, user.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
    // 恢复原状态
    user.status = user.status === 1 ? 0 : 1
  }
}

// 重置密码
const handleResetPassword = async (user: SysUser) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 ${user.realName} 的密码吗？密码将重置为手机号后6位。`,
      '确认重置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await systemApi.user.resetPassword(user.id)
    ElMessage.success('密码重置成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('密码重置失败:', error)
      ElMessage.error('密码重置失败')
    }
  }
}

// 删除用户
const handleDelete = async (user: SysUser) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.realName} 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await systemApi.user.delete(user.id)
    ElMessage.success('用户删除成功')
    await fetchUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }
}

// 工具方法
const getRoleType = (role: string) => {
  return role === 'ADMIN' ? 'danger' : 'primary'
}

const getRoleText = (role: string) => {
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'HEALTH_MANAGER': '健康管家'
  }
  return roleMap[role] || '未知'
}

const formatDateTime = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-management {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

.page-header-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-weight: 600;
}

.search-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.form-tip {
  margin-top: 5px;
}

:deep(.el-table) {
  background: rgba(255, 255, 255, 0.9);
}

:deep(.el-table th) {
  background: rgba(64, 158, 255, 0.1);
}

:deep(.el-pagination) {
  background: rgba(255, 255, 255, 0.9);
  padding: 15px;
  border-radius: 8px;
}

:deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}
</style>
