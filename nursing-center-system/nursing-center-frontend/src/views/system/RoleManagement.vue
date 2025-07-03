<template>
  <div class="role-management">
    <!-- 页面头部 -->
    <el-card class="page-header-card" shadow="never">
      <div class="page-header">
        <h2>角色管理</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>系统管理</el-breadcrumb-item>
          <el-breadcrumb-item>角色管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </el-card>

    <!-- 查询区域 -->
    <el-card class="search-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>角色查询</span>
          <el-button type="primary" @click="showAddDialog" :icon="Plus">
            新增角色
          </el-button>
        </div>
      </template>

      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="角色名称">
            <el-input
              v-model="searchForm.keyword"
              placeholder="请输入角色名称"
              style="width: 200px"
              clearable
            />
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

      <!-- 角色列表 -->
      <el-table
        :data="roleList"
        v-loading="loading"
        stripe
        border
        style="width: 100%">
        <el-table-column prop="name" label="角色名称" width="150" align="center" />
        <el-table-column prop="description" label="角色描述" min-width="200" align="center" />
        <el-table-column label="权限" min-width="300" align="center">
          <template #default="{ row }">
            <div class="permissions-tags">
              <el-tag
                v-for="permission in row.permissions"
                :key="permission"
                size="small"
                type="primary"
                class="permission-tag">
                {{ getPermissionText(permission) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
                :disabled="row.name === 'ADMIN'">
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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
      width="600px"
      :close-on-click-modal="false">
      <el-form
        :model="roleForm"
        :rules="roleRules"
        ref="roleFormRef"
        label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input
            v-model="roleForm.name"
            placeholder="请输入角色名称"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
        <el-form-item label="权限设置" prop="permissions">
          <el-checkbox-group v-model="roleForm.permissions">
            <div class="permission-group" v-for="group in permissionGroups" :key="group.name">
              <h4>{{ group.name }}</h4>
              <el-checkbox
                v-for="permission in group.permissions"
                :key="permission.value"
                :label="permission.value">
                {{ permission.label }}
              </el-checkbox>
            </div>
          </el-checkbox-group>
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
import type { Role } from '@/api/system'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

// 分页当前页（前端显示用，从1开始）
const currentPage = ref(1)

// 表单数据
const searchForm = reactive({
  keyword: '',
  page: 0,
  size: 10
})

const roleForm = reactive({
  id: 0,
  name: '',
  description: '',
  permissions: [] as string[]
})

// 表单验证规则
const roleRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述长度不能超过 200 个字符', trigger: 'blur' }
  ],
  permissions: [
    { required: true, message: '请选择至少一个权限', trigger: 'change' }
  ]
}

// 权限分组
const permissionGroups = [
  {
    name: '用户管理',
    permissions: [
      { label: '查看用户', value: 'user:view' },
      { label: '新增用户', value: 'user:add' },
      { label: '编辑用户', value: 'user:edit' },
      { label: '删除用户', value: 'user:delete' },
      { label: '重置密码', value: 'user:reset' }
    ]
  },
  {
    name: '角色管理',
    permissions: [
      { label: '查看角色', value: 'role:view' },
      { label: '新增角色', value: 'role:add' },
      { label: '编辑角色', value: 'role:edit' },
      { label: '删除角色', value: 'role:delete' }
    ]
  },
  {
    name: '客户管理',
    permissions: [
      { label: '查看客户', value: 'customer:view' },
      { label: '新增客户', value: 'customer:add' },
      { label: '编辑客户', value: 'customer:edit' },
      { label: '删除客户', value: 'customer:delete' }
    ]
  },
  {
    name: '健康管家',
    permissions: [
      { label: '查看健康管家', value: 'health_manager:view' },
      { label: '分配客户', value: 'health_manager:assign' },
      { label: '服务监控', value: 'health_manager:monitor' }
    ]
  },
  {
    name: '护理服务',
    permissions: [
      { label: '查看护理服务', value: 'care:view' },
      { label: '护理服务管理', value: 'care:manage' },
      { label: '护理记录', value: 'care:record' }
    ]
  },
  {
    name: '基础数据',
    permissions: [
      { label: '护理级别管理', value: 'care_level:manage' },
      { label: '护理项目管理', value: 'care_item:manage' },
      { label: '楼栋房间管理', value: 'building:manage' }
    ]
  }
]

// 数据列表
const roleList = ref<Role[]>([])
const total = ref(0)

// 表单引用
const roleFormRef = ref()

// 计算属性
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑角色' : '新增角色'
})

// 方法
const handleSearch = async () => {
  searchForm.page = 0
  currentPage.value = 1
  await fetchRoleList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    page: 0,
    size: 10
  })
  currentPage.value = 1
  fetchRoleList()
}

const handleSizeChange = (val: number) => {
  searchForm.size = val
  searchForm.page = 0
  currentPage.value = 1
  fetchRoleList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  searchForm.page = val - 1
  fetchRoleList()
}

// 获取角色列表
const fetchRoleList = async () => {
  try {
    loading.value = true
    console.log('查询参数:', searchForm)
    
    const response = await systemApi.getRoles()
    console.log('角色列表响应:', response)
    
    // 处理响应数据 - 新接口直接返回数组
    roleList.value = response || []
    total.value = roleList.value.length
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
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

// 编辑角色
const handleEdit = (role: Role) => {
  isEdit.value = true
  Object.assign(roleForm, {
    id: role.id,
    name: role.name,
    description: role.description,
    permissions: [...(role.permissions || [])]
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(roleForm, {
    id: 0,
    name: '',
    description: '',
    permissions: []
  })
  roleFormRef.value?.clearValidate()
}

// 确认提交
const handleConfirm = async () => {
  try {
    await roleFormRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      await systemApi.updateRole(roleForm)
      ElMessage.success('角色更新成功')
    } else {
      await systemApi.createRole(roleForm)
      ElMessage.success('角色创建成功')
    }

    dialogVisible.value = false
    await fetchRoleList()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 删除角色
const handleDelete = async (role: Role) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色 ${role.name} 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await systemApi.deleteRole(role.id)
    ElMessage.success('角色删除成功')
    await fetchRoleList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除角色失败:', error)
      ElMessage.error('删除角色失败')
    }
  }
}

// 工具方法
const getPermissionText = (permission: string) => {
  for (const group of permissionGroups) {
    const found = group.permissions.find(p => p.value === permission)
    if (found) {
      return found.label
    }
  }
  return permission
}

const formatDateTime = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchRoleList()
})
</script>

<style scoped>
.role-management {
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

.permissions-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.permission-tag {
  margin: 2px;
}

.permission-group {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
}

.permission-group h4 {
  margin: 0 0 10px 0;
  color: #409eff;
  font-weight: 600;
}

.permission-group .el-checkbox {
  margin-right: 20px;
  margin-bottom: 10px;
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
