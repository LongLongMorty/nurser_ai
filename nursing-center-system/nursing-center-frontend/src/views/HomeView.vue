<template>
  <div class="home">
    <div class="welcome-section">
      <h1>欢迎使用东软颐养中心管理系统</h1>
      <p>为您提供专业的养老服务管理解决方案</p>
    </div>
    
    <div class="stats-section">
      <el-row :gutter="20" v-loading="statsLoading">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ stats.totalCustomers || 0 }}</div>
              <div class="stat-label">在住客户</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ stats.occupiedBeds || 0 }}</div>
              <div class="stat-label">已占用床位</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ stats.availableBeds || 0 }}</div>
              <div class="stat-label">可用床位</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-number">{{ stats.pendingApplies || 0 }}</div>
              <div class="stat-label">待处理申请</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div class="content-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card title="最近活动">
            <div class="recent-list" v-loading="activitiesLoading">
              <div 
                v-for="activity in recentActivities" 
                :key="activity.time"
                class="recent-item"
              >
                <span>{{ activity.content }}</span>
                <span class="time">{{ activity.time }}</span>
              </div>
              <div v-if="recentActivities.length === 0" class="empty-state">
                暂无最近活动
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card title="护理级别分布">
            <div class="care-level-chart" v-loading="distributionLoading">
              <div 
                v-for="level in careLevelDistribution" 
                :key="level.levelName"
                class="level-item"
              >
                <div class="level-info">
                  <span 
                    class="level-dot" 
                    :style="{ backgroundColor: level.color }"
                  ></span>
                  <span class="level-name">{{ level.levelName }}</span>
                </div>
                <span class="level-count">{{ level.count }}人</span>
              </div>
              <div v-if="careLevelDistribution.length === 0" class="empty-state">
                暂无数据
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { dashboardApi } from '@/api'

const userStore = useUserStore()

// 响应式数据
const statsLoading = ref(false)
const activitiesLoading = ref(false)
const distributionLoading = ref(false)

const stats = ref({
  totalCustomers: 0,
  occupiedBeds: 0,
  availableBeds: 0,
  pendingApplies: 0
})

const recentActivities = ref<Array<{
  content: string
  time: string
}>>([])

const careLevelDistribution = ref<Array<{
  levelName: string
  count: number
  color: string
}>>([])

// 获取统计数据
const loadStats = async () => {
  try {
    statsLoading.value = true
    const data = await dashboardApi.getStats()
    stats.value = data
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 使用模拟数据作为降级方案
    stats.value = {
      totalCustomers: 120,
      occupiedBeds: 118,
      availableBeds: 32,
      pendingApplies: 3
    }
  } finally {
    statsLoading.value = false
  }
}

// 获取最近活动
const loadRecentActivities = async () => {
  try {
    activitiesLoading.value = true
    const data = await dashboardApi.getRecentActivities()
    recentActivities.value = data
  } catch (error) {
    console.error('获取最近活动失败:', error)
  } finally {
    activitiesLoading.value = false
  }
}

// 获取护理级别分布
const loadCareLevelDistribution = async () => {
  try {
    distributionLoading.value = true
    const data = await dashboardApi.getCareLevelDistribution()
    // 为每个护理级别分配颜色
    const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
    careLevelDistribution.value = data.map((item, index) => ({
      ...item,
      color: colors[index % colors.length]
    }))
  } catch (error) {
    console.error('获取护理级别分布失败:', error)
  } finally {
    distributionLoading.value = false
  }
}

onMounted(() => {
  // 初始化用户信息
  userStore.initUserInfo()
  
  // 加载仪表板数据
  loadStats()
  loadRecentActivities()
  loadCareLevelDistribution()
})
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  text-align: center;
  margin-bottom: 40px;
}

.welcome-section h1 {
  color: #333;
  margin-bottom: 10px;
}

.welcome-section p {
  color: #666;
  font-size: 16px;
}

.stats-section {
  margin-bottom: 40px;
}

.stat-card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-item {
  padding: 20px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.content-section {
  margin-bottom: 40px;
}

.recent-list {
  max-height: 200px;
  overflow-y: auto;
}

.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.recent-item:last-child {
  border-bottom: none;
}

.time {
  color: #999;
  font-size: 12px;
}

.care-level-chart {
  max-height: 200px;
  overflow-y: auto;
}

.level-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.level-item:last-child {
  border-bottom: none;
}

.level-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.level-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}

.level-name {
  font-size: 14px;
  color: #333;
}

.level-count {
  font-weight: bold;
  color: #409eff;
}

.empty-state {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
}
</style>
