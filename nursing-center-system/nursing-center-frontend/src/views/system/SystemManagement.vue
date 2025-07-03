<template>
  <div class="system-management">
    <!-- 页面头部 -->
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>用户管理 - 基础数据维护</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>系统管理</el-breadcrumb-item>
          <el-breadcrumb-item>用户管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>

    <!-- 功能选项卡 -->
    <el-card class="main-card" shadow="always">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <!-- 用户管理 -->
        <el-tab-pane label="用户管理" name="users">
          <div class="tab-content">
            <!-- 搜索和操作区域 -->
            <div class="action-bar">
              <div class="search-section">
                <el-form :model="userSearchForm" inline size="default">
                  <el-form-item label="用户名">
                    <el-input 
                      v-model="userSearchForm.username" 
                      placeholder="请输入用户名" 
                      clearable 
                      style="width: 150px"
                      @keyup.enter="handleUserSearch"
                    />
                  </el-form-item>
                  <el-form-item label="姓名">
                    <el-input 
                      v-model="userSearchForm.realName" 
                      placeholder="请输入姓名" 
                      clearable 
                      style="width: 150px"
                      @keyup.enter="handleUserSearch"
                    />
                  </el-form-item>
                  <el-form-item label="角色">
                    <el-select 
                      v-model="userSearchForm.role" 
                      placeholder="请选择角色" 
                      clearable 
                      style="width: 120px"
                    >
                      <el-option label="管理员" value="ADMIN" />
                      <el-option label="健康管家" value="HEALTH_MANAGER" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="状态">
                    <el-select 
                      v-model="userSearchForm.status" 
                      placeholder="请选择状态" 
                      clearable 
                      style="width: 100px"
                    >
                      <el-option label="启用" :value="1" />
                      <el-option label="禁用" :value="0" />
                    </el-select>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="handleUserSearch" :loading="userLoading">
                      <el-icon><Search /></el-icon>
                      查询
                    </el-button>
                    <el-button @click="handleUserReset">
                      <el-icon><Refresh /></el-icon>
                      重置
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
              
              <div class="action-buttons">
                <el-button type="primary" @click="handleAddUser">
                  <el-icon><Plus /></el-icon>
                  新增用户
                </el-button>
              </div>
            </div>

            <!-- 用户列表 -->
            <el-table 
              :data="userList" 
              v-loading="userLoading" 
              stripe 
              border 
              style="width: 100%"
              empty-text="暂无用户数据"
            >
              <el-table-column prop="id" label="ID" width="80" align="center" />
              <el-table-column prop="username" label="用户名" width="120" align="center" />
              <el-table-column prop="realName" label="真实姓名" width="120" align="center" />
              <el-table-column prop="phone" label="手机号" width="130" align="center" />
              <el-table-column prop="role" label="角色" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="getRoleTagType(row.role)" size="small">
                    {{ getRoleName(row.role) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-switch
                    v-model="row.status"
                    :active-value="1"
                    :inactive-value="0"
                    active-text="启用"
                    inactive-text="禁用"
                    @change="handleStatusChange(row)"
                    :disabled="row.username === 'admin'"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="160" align="center">
                <template #default="{ row }">
                  {{ formatDateTime(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="280" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button-group>
                    <el-button size="small" @click="handleViewUser(row)">
                      查看
                    </el-button>
                    <el-button size="small" type="primary" @click="handleEditUser(row)">
                      编辑
                    </el-button>
                    <el-button size="small" type="warning" @click="handleResetPassword(row)">
                      重置密码
                    </el-button>
                    <el-button 
                      size="small" 
                      type="danger" 
                      :disabled="row.username === 'admin'" 
                      @click="handleDeleteUser(row)"
                    >
                      删除
                    </el-button>
                  </el-button-group>
                </template>
              </el-table-column>
            </el-table>

            <!-- 用户分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="userPagination.pageNum"
                v-model:page-size="userPagination.pageSize"
                :total="userPagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleUserSizeChange"
                @current-change="handleUserCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 角色信息 -->
        <el-tab-pane label="角色信息" name="roles">
          <div class="tab-content">
            <div class="role-summary">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-card class="role-card">
                    <div class="role-info">
                      <div class="role-icon admin">
                        <el-icon><User /></el-icon>
                      </div>
                      <div class="role-details">
                        <h3>管理员 (ADMIN)</h3>
                        <p>系统最高权限，可管理所有功能</p>
                        <div class="role-count">
                          <el-tag type="success">{{ adminCount }}人</el-tag>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card class="role-card">
                    <div class="role-info">
                      <div class="role-icon health-manager">
                        <el-icon><Avatar /></el-icon>
                      </div>
                      <div class="role-details">
                        <h3>健康管家 (HEALTH_MANAGER)</h3>
                        <p>负责客户护理服务管理</p>
                        <div class="role-count">
                          <el-tag type="primary">{{ healthManagerCount }}人</el-tag>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card class="role-card">
                    <div class="role-info">
                      <div class="role-icon total">
                        <el-icon><UserFilled /></el-icon>
                      </div>
                      <div class="role-details">
                        <h3>总计用户</h3>
                        <p>系统中所有用户数量</p>
                        <div class="role-count">
                          <el-tag type="info">{{ totalUsers }}人</el-tag>
                        </div>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>

            <!-- 角色权限说明 -->
            <el-card class="permission-card" header="角色权限说明">
              <el-table :data="rolePermissions" stripe border>
                <el-table-column prop="role" label="角色" width="150" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getRoleTagType(row.role)" size="small">
                      {{ row.roleName }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="角色描述" align="center" />
                <el-table-column prop="permissions" label="主要权限" show-overflow-tooltip align="center">
                  <template #default="{ row }">
                    <el-tag 
                      v-for="permission in row.permissions" 
                      :key="permission" 
                      size="small" 
                      style="margin: 2px"
                    >
                      {{ permission }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 用户编辑对话框 -->
    <el-dialog
      v-model="userDialogVisible"
      :title="userDialogMode === 'add' ? '新增用户' : userDialogMode === 'edit' ? '编辑用户' : '查看用户'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form 
        :model="userForm" 
        :rules="userRules" 
        ref="userFormRef" 
        label-width="100px"
        :disabled="userDialogMode === 'view'"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="userForm.username" 
            placeholder="请输入用户名"
            :disabled="userDialogMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
          <div class="form-tip">注意：密码默认为手机号后6位</div>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="健康管家" value="HEALTH_MANAGER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="userDialogMode !== 'add'">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer v-if="userDialogMode !== 'view'">
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleUserSubmit" 
          :loading="userSubmitLoading"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 密码重置对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="重置密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="password-reset-content">
        <el-alert
          title="密码重置说明"
          type="warning"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>重置后的密码将为该用户手机号的后6位</p>
            <p>用户：<strong>{{ selectedUser?.realName }}</strong></p>
            <p>手机号：<strong>{{ selectedUser?.phone }}</strong></p>
            <p>新密码：<strong>{{ getDefaultPassword(selectedUser?.phone) }}</strong></p>
          </template>
        </el-alert>
      </div>

      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleConfirmResetPassword" 
          :loading="passwordResetLoading"
        >
          确认重置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Plus, 
  User, 
  Avatar, 
  UserFilled 
} from '@element-plus/icons-vue'
import { systemApi } from '@/api/system'
import type { SysUser, UserQueryParams } from '@/api/types'

// 响应式数据
const activeTab = ref('users')
const userLoading = ref(false)
const userSubmitLoading = ref(false)
const passwordResetLoading = ref(false)

// 用户搜索表单
const userSearchForm = reactive({
  username: '',
  realName: '',
  role: '',
  status: null as number | null
})

// 用户列表和分页
const userList = ref<SysUser[]>([])
const userPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const userDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const userDialogMode = ref<'add' | 'edit' | 'view'>('add')
const selectedUser = ref<SysUser | null>(null)

// 用户表单
const userFormRef = ref()
const userForm = reactive({
  id: undefined as number | undefined,
  username: '',
  realName: '',
  phone: '',
  role: '',
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
    { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 角色权限数据
const rolePermissions = ref([
  {
    role: 'ADMIN',
    roleName: '管理员',
    description: '系统管理员，拥有系统最高权限',
    permissions: ['用户管理', '护理管理', '客户管理', '床位管理', '数据统计', '系统配置']
  },
  {
    role: 'HEALTH_MANAGER',
    roleName: '健康管家',
    description: '负责客户日常护理服务',
    permissions: ['客户护理', '护理记录', '服务监控', '个人信息']
  }
])

// 计算属性
const adminCount = computed(() => 
  userList.value.filter(user => user.role === 'ADMIN').length
)

const healthManagerCount = computed(() => 
  userList.value.filter(user => user.role === 'HEALTH_MANAGER').length
)

const totalUsers = computed(() => userList.value.length)

// 方法
const handleTabChange = (tab: any) => {
  if (tab.name === 'users') {
    fetchUserList()
  }
}

const handleUserSearch = () => {
  userPagination.pageNum = 1
  fetchUserList()
}

const handleUserReset = () => {
  Object.assign(userSearchForm, {
    username: '',
    realName: '',
    role: '',
    status: null
  })
  userPagination.pageNum = 1
  fetchUserList()
}

const handleUserSizeChange = (size: number) => {
  userPagination.pageSize = size
  fetchUserList()
}

const handleUserCurrentChange = (page: number) => {
  userPagination.pageNum = page
  fetchUserList()
}

// 获取用户列表
const fetchUserList = async () => {
  try {
    userLoading.value = true
    const params: UserQueryParams = {
      pageNum: userPagination.pageNum,
      pageSize: userPagination.pageSize,
      username: userSearchForm.username || undefined,
      realName: userSearchForm.realName || undefined,
      role: userSearchForm.role || undefined,
      status: userSearchForm.status ?? undefined
    }
    
    const response = await systemApi.user.list(params)
    userList.value = response.records || response.data?.records || []
    userPagination.total = response.total || response.data?.total || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    userLoading.value = false
  }
}

// 用户操作
const handleAddUser = () => {
  resetUserForm()
  userDialogMode.value = 'add'
  userDialogVisible.value = true
}

const handleViewUser = (user: SysUser) => {
  selectedUser.value = user
  Object.assign(userForm, user)
  userDialogMode.value = 'view'
  userDialogVisible.value = true
}

const handleEditUser = (user: SysUser) => {
  selectedUser.value = user
  Object.assign(userForm, user)
  userDialogMode.value = 'edit'
  userDialogVisible.value = true
}

const handleDeleteUser = async (user: SysUser) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${user.realName}"吗？删除后不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await systemApi.user.delete(user.id)
    ElMessage.success('删除成功')
    await fetchUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }
}

const handleStatusChange = async (user: SysUser) => {
  try {
    await systemApi.user.updateStatus(user.id, user.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
    // 恢复原状态
    user.status = user.status === 1 ? 0 : 1
  }
}

const handleResetPassword = (user: SysUser) => {
  selectedUser.value = user
  passwordDialogVisible.value = true
}

const handleConfirmResetPassword = async () => {
  if (!selectedUser.value) return

  try {
    passwordResetLoading.value = true
    const newPassword = getDefaultPassword(selectedUser.value.phone)
    await systemApi.user.resetPassword(selectedUser.value.id, newPassword)
    ElMessage.success('密码重置成功')
    passwordDialogVisible.value = false
  } catch (error) {
    console.error('重置密码失败:', error)
    ElMessage.error('重置密码失败')
  } finally {
    passwordResetLoading.value = false
  }
}

// 提交用户表单
const handleUserSubmit = async () => {
  try {
    await userFormRef.value.validate()
    userSubmitLoading.value = true

    const userData = { ...userForm }
    
    // 如果是新增用户，设置默认密码
    if (userDialogMode.value === 'add') {
      userData.password = getDefaultPassword(userData.phone)
    }

    if (userDialogMode.value === 'add') {
      await systemApi.user.create(userData)
      ElMessage.success('用户创建成功')
    } else {
      await systemApi.user.update(userData)
      ElMessage.success('用户更新成功')
    }

    userDialogVisible.value = false
    await fetchUserList()
  } catch (error) {
    console.error('保存用户失败:', error)
    ElMessage.error('保存用户失败')
  } finally {
    userSubmitLoading.value = false
  }
}

// 重置用户表单
const resetUserForm = () => {
  Object.assign(userForm, {
    id: undefined,
    username: '',
    realName: '',
    phone: '',
    role: '',
    status: 1
  })
  userFormRef.value?.resetFields()
}

// 工具方法
const getRoleName = (role: string) => {
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'HEALTH_MANAGER': '健康管家'
  }
  return roleMap[role] || '未知角色'
}

const getRoleTagType = (role: string) => {
  const typeMap: Record<string, string> = {
    'ADMIN': 'danger',
    'HEALTH_MANAGER': 'primary'
  }
  return typeMap[role] || 'info'
}

const getDefaultPassword = (phone: string) => {
  return phone ? phone.slice(-6) : '123456'
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
.system-management {
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

.main-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.tab-content {
  padding: 20px 0;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 20px;
}

.search-section {
  flex: 1;
}

.action-buttons {
  flex-shrink: 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.role-summary {
  margin-bottom: 30px;
}

.role-card {
  height: 120px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.role-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.role-info {
  display: flex;
  align-items: center;
  height: 100%;
}

.role-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  margin-right: 15px;
}

.role-icon.admin {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.role-icon.health-manager {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.role-icon.total {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.role-details h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}

.role-details p {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #666;
}

.role-count {
  margin-top: 10px;
}

.permission-card {
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.password-reset-content {
  padding: 10px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-section {
    width: 100%;
  }
  
  .action-buttons {
    width: 100%;
    text-align: right;
  }
}
</style>
