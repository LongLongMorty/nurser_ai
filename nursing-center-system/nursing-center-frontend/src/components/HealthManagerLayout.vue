<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-if="!isCollapse">东软颐养中心</span>
        <span v-else>东软</span>
      </div>      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="false"
        router
        class="sidebar-menu"
      >        <!-- 日常护理 -->
        <el-menu-item index="/health-manager-portal/daily-care">
          <el-icon><UserFilled /></el-icon>
          <span>日常护理</span>
        </el-menu-item>

        <!-- 外出申请 -->
        <el-menu-item index="/health-manager-portal/outing-apply">
          <el-icon><Position /></el-icon>
          <span>外出申请</span>
        </el-menu-item>        <!-- 退住申请 -->
        <el-menu-item index="/health-manager-portal/checkout-apply">
          <el-icon><Back /></el-icon>
          <span>退住申请</span>
        </el-menu-item>

        <!-- 回院申请 -->
        <el-menu-item index="/health-manager-portal/return-apply">
          <el-icon><House /></el-icon>
          <span>回院申请</span>
        </el-menu-item>
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
              {{ userStore.userInfo?.realName || '健康管家' }}
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

      <!-- 主内容 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  UserFilled, User, Fold, Expand, ArrowDown, Document, Position, Back, House
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 处理下拉菜单命令
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

/* 主菜单项统一样式 */
.sidebar-menu > .el-menu-item {
  color: rgba(255, 255, 255, 0.9) !important;
  background-color: transparent !important;
  margin: 0;
  border-radius: 0;
  transition: all 0.3s ease;
}

.sidebar-menu > .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: #ffffff !important;
}

.sidebar-menu > .el-menu-item.is-active {
  background: rgba(255, 255, 255, 0.25) !important;
  color: #ffffff !important;
  font-weight: 500;
}

/* 确保图标颜色正确 */
.sidebar-menu .el-icon {
  margin-right: 8px;
}

.header {
  background: #ffffff;
  border-bottom: 1px solid #e8eaec;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
  color: #333;
  font-weight: 500;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-info .el-icon {
  margin: 0 4px;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

/* 暗色模式适配 */
@media (prefers-color-scheme: dark) {
  .header {
    background: #1f1f1f;
    border-bottom-color: #3a3a3a;
  }
  
  .user-info {
    color: #e8e8e8;
  }
  
  .user-info:hover {
    background-color: #2a2a2a;
  }
  
  .main-content {
    background: #141414;
  }
}
</style>
