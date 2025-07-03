<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
      <div class="logo">
        <span v-if="!isCollapse">东软颐养中心</span>
        <span v-else>东软</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>数据总览</span>
        </el-menu-item>        <!-- 客户管理 -->
        <el-sub-menu index="customer">          <template #title>
          <el-icon><User /></el-icon>
          <span>客户管理</span>
        </template>
          <el-menu-item index="/customer/list">客户列表</el-menu-item>
          <el-menu-item index="/customer/checkin">入住登记</el-menu-item>
          <el-menu-item index="/customer/checkout">退住登记</el-menu-item>
          <el-menu-item index="/customer/outing">外出登记</el-menu-item>
          <el-menu-item index="/customer/return">回院登记</el-menu-item>

        </el-sub-menu>        <!-- 床位管理 -->
        <el-sub-menu index="bed">
          <template #title>
            <el-icon><Grid /></el-icon>
            <span>床位管理</span>
          </template>
          <el-menu-item index="/bed/diagram">床位示意图</el-menu-item>
          <el-menu-item index="/bed/usage">床位使用管理</el-menu-item>
        </el-sub-menu>
        <!-- 护理管理 -->
        <el-sub-menu index="nursing">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>护理管理</span>
          </template>
          <el-menu-item index="/nursing/level">护理级别管理</el-menu-item>
          <el-menu-item index="/nursing/item">护理项目管理</el-menu-item>
          <el-menu-item index="/nursing/customer-care">客户护理设置</el-menu-item>
          <el-menu-item index="/nursing/record">护理记录管理</el-menu-item>
        </el-sub-menu>
        <!-- 健康管家 -->
        <el-sub-menu index="health-manager" v-if="hasRole(['ADMIN'])">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>健康管家</span>
          </template>
          <el-menu-item index="/health-manager/assign">设置服务对象</el-menu-item>
          <el-menu-item index="/health-manager/monitor">服务关注</el-menu-item>
        </el-sub-menu>

        <!-- 膳食管理 -->
        <el-sub-menu index="meal" v-if="hasRole(['ADMIN'])">
          <template #title>
            <el-icon><Bowl /></el-icon>
            <span>膳食管理</span>
          </template>
          <el-menu-item index="/meal/config">膳食配置</el-menu-item>
          <el-menu-item index="/meal/calendar">膳食日历</el-menu-item>
          <el-menu-item index="/meal/ai-recommendation">智能食谱推荐</el-menu-item>
        </el-sub-menu>

        <!-- 系统管理 -->
        <el-sub-menu index="system" v-if="hasRole(['ADMIN'])">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 主要内容区域 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-button
            :icon="isCollapse ? Expand : Fold"
            @click="toggleCollapse"
            text
          />
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.userInfo?.name || '用户' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主要内容 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  House,
  User,
  UserFilled,
  Grid,
  Monitor,
  Dish,
  Document,
  Setting,
  Fold,
  Expand,
  ArrowDown,
  DataBoard,
  Bowl
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人信息功能开发中...')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确认退出登录吗？', '提示', {
          type: 'warning'
        })
        userStore.logout()
        router.push('/login')
        ElMessage.success('已退出登录')
      } catch {
        // 用户取消
      }
      break
  }
}

// 检查用户角色权限
const hasRole = (roles: string[]) => {
  const userRole = userStore.userInfo?.role
  return userRole && roles.includes(userRole)
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #ffffff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.sidebar-menu {
  border: none;
  background: transparent;
}

/* 主菜单项（首页概览）和子菜单标题（客户管理、床位管理等）保持渐变背景白字 */
.sidebar-menu > .el-menu-item,
.sidebar-menu .el-sub-menu__title {
  color: rgba(255, 255, 255, 0.9) !important;
  background-color: transparent !important;
  margin: 0;
  border-radius: 0;
  transition: all 0.3s ease;
}

.sidebar-menu > .el-menu-item:hover,
.sidebar-menu .el-sub-menu__title:hover {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: #ffffff !important;
}

.sidebar-menu > .el-menu-item.is-active {
  background: rgba(255, 255, 255, 0.25) !important;
  color: #ffffff !important;
  font-weight: 500;
}

/* 子菜单项（客户列表、入住登记等）使用白色背景黑字 */
.sidebar-menu .el-sub-menu .el-menu-item {
  color: #333333 !important;
  background-color: #ffffff !important;
  margin: 0;
  border-radius: 0;
  padding-left: 45px !important;
  transition: all 0.3s ease;
}

.sidebar-menu .el-sub-menu .el-menu-item:hover {
  background-color: #f8f9ff !important;
  color: #667eea !important;
}

.sidebar-menu .el-sub-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: #ffffff !important;
  font-weight: 500;
}

/* 确保图标颜色正确 */
.sidebar-menu .el-icon {
  color: inherit !important;
}

/* 子菜单展开时的背景 */
.sidebar-menu .el-sub-menu.is-opened > .el-sub-menu__title {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: #ffffff !important;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
}

.header-left .el-button {
  color: rgba(255, 255, 255, 0.9);
  background: transparent;
  border: none;
  font-size: 18px;
  transition: all 0.3s ease;
}

.header-left .el-button:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 6px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.9);
  padding: 8px 15px;
  border-radius: 20px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
}

.user-info:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.2);
}

.main-content {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 60px);
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    z-index: 1000;
    height: 100vh;
  }

  .layout-container {
    padding-left: 0;
  }
}

/* 滚动条样式 */
.sidebar::-webkit-scrollbar {
  width: 4px;
}

.sidebar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}
</style>
