<template>
  <div class="dashboard">
    <!-- 页面头部 -->
    <div class="header-section">
      <el-card class="header-card" shadow="never">
        <div class="header-content">
          <div class="welcome-section">
            <h1 class="dashboard-title">护理中心数据总览</h1>
            <p class="welcome-text">欢迎回来，{{ userStore.userInfo?.realName || '管理员' }}</p>
            <p class="date-time">{{ currentDateTime }}</p>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="refreshAllData" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
            <el-button type="warning" @click="showAlerts" v-if="realTimeData.alerts > 0">
              <el-icon><Bell /></el-icon>
              {{ realTimeData.alerts }} 条预警
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 快速统计卡片 -->
      <div class="quick-stats-grid">
        <div
          class="stat-card"
          v-for="(stat, index) in quickStats"
          :key="index"
          :class="{ 'clickable': stat.clickable }"
          @click="handleStatCardClick(stat)">
          <div class="stat-icon-wrapper" :style="{ backgroundColor: stat.color }">
            <el-icon class="stat-icon" :color="stat.iconColor"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-trend" v-if="stat.trend !== undefined">
            <el-icon :color="stat.trend > 0 ? '#67C23A' : '#F56C6C'">
              <component :is="stat.trend > 0 ? 'TrendCharts' : 'TrendCharts'" />
            </el-icon>
            <span :class="stat.trend > 0 ? 'trend-up' : 'trend-down'">
              {{ Math.abs(stat.trend) }}%
            </span>
          </div>
          <div class="click-hint" v-if="stat.clickable">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="charts-section">
        <!-- 左侧图表区域 -->
        <div class="charts-left">
          <!-- 客户年龄分布卡片 -->
          <el-card class="chart-card modern-card age-distribution-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><PieChart /></el-icon>
                  <span class="header-title">客户年龄分布</span>
                </div>
                <el-button type="text" @click="refreshChartData" class="refresh-btn">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
            </template>
            <div class="age-distribution-content">
              <div class="chart-wrapper">
                <div ref="ageDistributionChart" class="age-chart-container"></div>
              </div>
              <div class="age-stats-panel">
                <div class="stats-header">
                  <h4>详细统计</h4>
                  <small style="color: #666;">共{{ ageGroupStats.length }}个年龄段</small>
                </div>
                <div class="age-groups">
                  <div class="age-group-item" v-for="(item, index) in ageGroupStats" :key="index">
                    <div class="age-indicator" :style="{ backgroundColor: item.color }"></div>
                    <div class="age-info">
                      <div class="age-label">{{ item.name }}</div>
                      <div class="age-value">{{ item.value }}人</div>
                      <div class="age-percentage">{{ item.percentage }}%</div>
                    </div>
                  </div>
                </div>
                <div class="total-info">
                  <div class="total-count">
                    <span class="total-label">总计</span>
                    <span class="total-number">{{ totalCustomersCount }}人</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 楼层入住情况 -->
          <el-card class="chart-card modern-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><DataBoard /></el-icon>
                  <span class="header-title">楼层入住情况</span>
                </div>
              </div>
            </template>
            <div ref="bedUsageChart" class="chart-container"></div>
          </el-card>

          <!-- 护理级别分布 -->
          <el-card class="chart-card modern-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><Grid /></el-icon>
                  <span class="header-title">护理级别分布</span>
                </div>
              </div>
            </template>
            <div ref="careLevelChart" class="chart-container"></div>
          </el-card>
        </div>

        <!-- 右侧区域 -->
        <div class="charts-right">
          <!-- 护理服务趋势 -->
          <el-card class="chart-card modern-card large-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><TrendCharts /></el-icon>
                  <span class="header-title">护理服务趋势</span>
                </div>
                <el-select v-model="serviceTimeRange" size="small" class="time-selector" @change="serviceTimeRangeChange">
                  <el-option label="近7天" value="7days" />
                  <el-option label="近1个月" value="30days" />
                </el-select>
              </div>
            </template>
            <div ref="serviceTrendChart" class="chart-container large-chart"></div>
          </el-card>

          <!-- 健康管家工作量 -->
          <el-card class="chart-card modern-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><UserFilled /></el-icon>
                  <span class="header-title">健康管家工作量</span>
                </div>
              </div>
            </template>
            <div ref="healthManagerChart" class="chart-container"></div>
          </el-card>

          <!-- 实时数据面板 -->
          <el-card class="chart-card modern-card info-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon class="header-icon"><Monitor /></el-icon>
                  <span class="header-title">实时数据</span>
                </div>
                <el-tag size="small" type="success" effect="dark">实时</el-tag>
              </div>
            </template>
            <div class="info-grid">
              <div class="info-item" v-for="(item, index) in realTimeDataList" :key="index">
                <div class="info-icon" :style="{ backgroundColor: item.color }">
                  <el-icon :color="item.iconColor"><component :is="item.icon" /></el-icon>
                </div>
                <div class="info-content">
                  <div class="info-value" :class="{ 'alert': item.isAlert }">{{ item.value }}</div>
                  <div class="info-label">{{ item.label }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 预警弹窗 -->
    <el-dialog
      title="系统预警"
      v-model="alertDialogVisible"
      width="600px">
      <el-table :data="alertList" style="width: 100%">
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlertType(row.type)" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="预警信息" />
        <el-table-column prop="time" label="时间" width="150" />
      </el-table>
    </el-dialog>

    <!-- 在住客户详情弹窗 -->
    <el-dialog
      title="在住客户详情"
      v-model="customerDialogVisible"
      width="900px"
      :loading="dialogLoading">
      <el-table
        :data="customerList"
        style="width: 100%"
        max-height="500"
        stripe>
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <el-tag :type="row.gender === '男' ? 'primary' : 'success'" size="small">
              {{ row.gender }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="roomNo" label="房间号" width="100" />
        <el-table-column prop="bedNo" label="床位号" width="100" />
        <el-table-column prop="careLevel" label="护理级别" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.careLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="healthStatus" label="健康状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.healthStatus === '良好' ? 'success' :
                     row.healthStatus === '一般' ? 'warning' : 'danger'"
              size="small">
              {{ row.healthStatus }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 在线管家详情弹窗 -->
    <el-dialog
      title="在线管家详情"
      v-model="managerDialogVisible"
      width="900px"
      :loading="dialogLoading">
      <el-table
        :data="managerList"
        style="width: 100%"
        max-height="500"
        stripe>
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="managedCustomers" label="管理客户数" width="120">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.managedCustomers }}人</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workload" label="工作状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.workload === '轻松' ? 'success' :
                     row.workload === '正常' ? 'primary' : 'warning'"
              size="small">
              {{ row.workload }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="150" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  User, House, Service, PieChart, DataBoard, TrendCharts,
  Grid, UserFilled, Monitor, Bell, Refresh, ArrowUp, ArrowDown, ArrowRight
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { dashboardApi, type CustomerDetail, type ManagerDetail } from '@/api/dashboard'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const serviceTimeRange = ref('7days')
const alertDialogVisible = ref(false)
const customerDialogVisible = ref(false)
const managerDialogVisible = ref(false)
const dialogLoading = ref(false)
const currentDateTime = ref('')
const loading = ref(false)
const ageGroupStats = ref<Array<{name: string, value: number, percentage: number, color: string}>>([])
const totalCustomersCount = ref(0)
const customerList = ref<CustomerDetail[]>([])
const managerList = ref<ManagerDetail[]>([])

// 图表实例
const chartInstances = reactive({
  ageDistribution: null as echarts.ECharts | null,
  bedUsage: null as echarts.ECharts | null,
  serviceTrend: null as echarts.ECharts | null,
  careLevel: null as echarts.ECharts | null,
  healthManager: null as echarts.ECharts | null
})

// 图表容器引用
const ageDistributionChart = ref<HTMLElement>()
const bedUsageChart = ref<HTMLElement>()
const serviceTrendChart = ref<HTMLElement>()
const careLevelChart = ref<HTMLElement>()
const healthManagerChart = ref<HTMLElement>()

// 概览统计数据
const overviewStats = reactive({
  totalCustomers: 0,
  occupancyRate: 0,
  activeServices: 0
})

// 实时数据
const realTimeData = reactive({
  todayCheckin: 0,
  todayCheckout: 0,
  pendingApplications: 0,
  alerts: 0,
  todayServices: 0,
  onlineManagers: 0
})

// 快速统计数据
const quickStats = computed(() => [
  {
    icon: User,
    value: overviewStats.totalCustomers,
    label: '在住客户',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    iconColor:  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    clickable: true,
    type: 'customers'
  },
  {
    icon: House,
    value: `${overviewStats.occupancyRate}%`,
    label: '床位占用率',
    color: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%)',
    iconColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    trend: 5.2, // 模拟趋势数据，床位占用率上升5.2%
    clickable: false
  },
  {
    icon: Service,
    value: overviewStats.activeServices,
    label: '活跃服务',
    color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
    iconColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    clickable: false
  },
  {
    icon: UserFilled,
    value: realTimeData.onlineManagers,
    label: '在线管家',
    color: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    iconColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    clickable: true,
    type: 'managers'
  }
])

// 实时数据列表
const realTimeDataList = computed(() => [
  {
    icon: User,
    value: realTimeData.todayCheckin,
    label: '今日新入住',
    color: '#e3f2fd',
    iconColor: '#2196f3',
    isAlert: false
  },
  {
    icon: House,
    value: realTimeData.todayCheckout,
    label: '今日退住',
    color: '#f3e5f5',
    iconColor: '#9c27b0',
    isAlert: false
  },
  {
    icon: Service,
    value: realTimeData.pendingApplications,
    label: '待处理申请',
    color: '#fff3e0',
    iconColor: '#ff9800',
    isAlert: realTimeData.pendingApplications > 5
  },
  {
    icon: Bell,
    value: realTimeData.alerts,
    label: '预警提醒',
    color: '#ffebee',
    iconColor: '#f44336',
    isAlert: realTimeData.alerts > 0
  },
  {
    icon: Monitor,
    value: realTimeData.todayServices,
    label: '今日服务次数',
    color: '#e8f5e8',
    iconColor: '#4caf50',
    isAlert: false
  },
  {
    icon: UserFilled,
    value: realTimeData.onlineManagers,
    label: '在线健康管家',
    color: '#f1f8e9',
    iconColor: '#8bc34a',
    isAlert: false
  }
])

// 预警列表
const alertList = ref([

  { type: '服务到期', message: '1位客户护理服务即将到期', time: '09:15' },
  { type: '欠费提醒', message: '1位客户存在欠费情况', time: '08:45' }
])

// 时间更新
const updateDateTime = () => {
  const now = new Date()
  currentDateTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    weekday: 'long'
  })
}

// 定时器
let timeTimer: number | null = null

// 显示预警
const showAlerts = () => {
  alertDialogVisible.value = true
}

// 获取预警类型
const getAlertType = (type: string) => {
  const typeMap: Record<string, string> = {
    '床位预警': 'warning',
    '服务到期': 'danger',
    '欠费提醒': 'danger'
  }
  return typeMap[type] || 'info'
}

// 刷新图表数据
const refreshChartData = () => {
  ElMessage.success('数据已刷新')
  loadChartData()
}

// 刷新所有数据
const refreshAllData = () => {
  ElMessage.success('正在刷新所有数据...')
  loadOverviewStats()
  loadRealTimeData()
  loadChartData()
}

// 处理统计卡片点击
const handleStatCardClick = async (stat: any) => {
  if (!stat.clickable) return

  if (stat.type === 'customers') {
    await showCustomerDetails()
  } else if (stat.type === 'managers') {
    await showManagerDetails()
  }
}

// 显示客户详情
const showCustomerDetails = async () => {
  try {
    dialogLoading.value = true
    customerDialogVisible.value = true
    customerList.value = await dashboardApi.getCustomerDetails()
  } catch (error) {
    console.error('加载客户详情失败:', error)
    ElMessage.error('加载客户详情失败')
    customerDialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

// 显示管家详情
const showManagerDetails = async () => {
  try {
    dialogLoading.value = true
    managerDialogVisible.value = true
    managerList.value = await dashboardApi.getOnlineManagerDetails()
  } catch (error) {
    console.error('加载管家详情失败:', error)
    ElMessage.error('加载管家详情失败')
    managerDialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

// 加载概览统计数据
const loadOverviewStats = async () => {
  try {
    const stats = await dashboardApi.getStats()
    overviewStats.totalCustomers = stats.totalCustomers
    overviewStats.occupancyRate = Math.round((stats.occupiedBeds / stats.totalBeds) * 100)
    overviewStats.activeServices = stats.totalCustomers * 2 // 假设每个客户平均2个服务
  } catch (error) {
    console.error('加载概览数据失败:', error)
    ElMessage.error('加载概览数据失败')
  }
}

// 加载实时数据
const loadRealTimeData = async () => {
  try {
    const stats = await dashboardApi.getStats()
    realTimeData.todayCheckin = stats.todayCheckIns
    realTimeData.todayCheckout = stats.todayCheckOuts
    realTimeData.pendingApplications = stats.pendingApplies
    realTimeData.alerts = alertList.value.length // 使用预警列表的实际数量
    realTimeData.todayServices = stats.totalCustomers // 假设每个客户今日都有服务
    realTimeData.onlineManagers = stats.onlineManagers // 使用真实的在线健康管家数量
  } catch (error) {
    console.error('加载实时数据失败:', error)
    ElMessage.error('加载实时数据失败')
  }
}

// 初始化图表
const initCharts = () => {
  // 客户年龄分布图
  if (ageDistributionChart.value) {
    chartInstances.ageDistribution = echarts.init(ageDistributionChart.value)
  }

  // 床位使用情况图
  if (bedUsageChart.value) {
    chartInstances.bedUsage = echarts.init(bedUsageChart.value)
  }

  // 护理服务趋势图
  if (serviceTrendChart.value) {
    chartInstances.serviceTrend = echarts.init(serviceTrendChart.value)
  }

  // 护理级别分布图
  if (careLevelChart.value) {
    chartInstances.careLevel = echarts.init(careLevelChart.value)
  }
  // 健康管家工作量图
  if (healthManagerChart.value) {
    chartInstances.healthManager = echarts.init(healthManagerChart.value)
  }
}

// 加载图表数据
const loadChartData = async () => {
  try {
    // 客户年龄分布 - 使用真实数据
    const ageDistribution = await dashboardApi.getAgeDistribution()

    const ageData = ageDistribution.map(item => ({
      value: item.count,
      name: item.ageGroup
    }))

    // 填充年龄分布统计数据
    totalCustomersCount.value = ageDistribution.reduce((sum, item) => sum + item.count, 0)
    const ageColors = ['#5470C6', '#91CC75', '#FAC858', '#EE6666', '#73C0DE', '#3BA272']

    // 确保显示所有6个年龄段，如果后端没有返回某个年龄段，就添加0值
    const allAgeGroups = ['65-70岁', '70-75岁', '75-80岁', '80-85岁', '85-90岁', '90岁以上']
    ageGroupStats.value = allAgeGroups.map((ageGroup, index) => {
      const foundItem = ageDistribution.find(item => item.ageGroup === ageGroup)
      const count = foundItem ? foundItem.count : 0
      return {
        name: ageGroup,
        value: count,
        percentage: totalCustomersCount.value > 0 ? Math.round((count / totalCustomersCount.value) * 100) : 0,
        color: ageColors[index] || '#5470C6'
      }
    })

    // 饼图也使用完整的6个年龄段数据
    const chartData = allAgeGroups.map((ageGroup, index) => {
      const foundItem = ageDistribution.find(item => item.ageGroup === ageGroup)
      const count = foundItem ? foundItem.count : 0
      return {
        value: count,
        name: ageGroup,
        itemStyle: {
          color: ageColors[index] || '#5470C6'
        }
      }
    })

    chartInstances.ageDistribution?.setOption({
      title: { text: '', left: 'center' },
      tooltip: {
        trigger: 'item',
        formatter: function(params: any) {
          return `${params.name}<br/>${params.value}人 (${params.percent}%)`
        }
      },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
        labelLine: { show: false },
        // 显示所有数据，包括0值
        minShowLabelAngle: 0,
        data: chartData
      }]
    })

    // 护理级别分布 - 使用真实数据
    const careLevelDistribution = await dashboardApi.getCareLevelDistribution()
    const careLevelData = careLevelDistribution.map(item => ({
      value: item.count,
      name: item.levelName,
      itemStyle: { color: item.color || '#5470C6' }
    }))

    chartInstances.careLevel?.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '70%',
        data: careLevelData,
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
      }]
    })    // 楼层入住情况 - 使用真实数据
    const floorStats = await dashboardApi.getFloorOccupancyStats()
    const floorNames = floorStats.map(item => item.floorName)
    const occupancyRates = floorStats.map(item => item.occupancyRate)

    chartInstances.bedUsage?.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: function(params: any) {
          const index = params[0].dataIndex
          const floorData = floorStats[index]
          return `${floorData.floorName}<br/>
                  占用率: ${floorData.occupancyRate}%<br/>
                  总床位: ${floorData.totalBeds}<br/>
                  已占用: ${floorData.occupiedBeds}<br/>
                  可用: ${floorData.availableBeds}`
        }
      },
      xAxis: { type: 'category', data: floorNames },
      yAxis: { type: 'value', max: 100, name: '占用率(%)' },
      series: [{
        data: occupancyRates,
        type: 'bar',
        itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ]) },
        barWidth: '60%'
      }]
    })

    // 护理服务趋势 - 使用模拟数据，支持7天和30天切换
    const serviceTrendData = getServiceTrendData(serviceTimeRange.value)

    chartInstances.serviceTrend?.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: function(params: any) {
          let result = `${params[0].name}<br/>`
          params.forEach((param: any) => {
            result += `${param.marker}${param.seriesName}: ${param.value}次<br/>`
          })
          return result
        }
      },
      legend: {
        data: ['基础护理', '专业护理', '生活护理'],
        top: 20
      },
      grid: {
        top: 60,
        bottom: 40,
        left: 40,
        right: 40
      },
      xAxis: {
        type: 'category',
        data: serviceTrendData.dates,
        axisLabel: {
          fontSize: 11,
          color: '#666'
        }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 10,
        name: '服务次数',
        nameTextStyle: {
          color: '#666',
          fontSize: 12
        },
        axisLabel: {
          color: '#666'
        },
        splitLine: {
          lineStyle: {
            color: '#f0f0f0'
          }
        }
      },
      series: [
        {
          name: '基础护理',
          type: 'line',
          data: serviceTrendData.basicCare,
          smooth: true,
          itemStyle: { color: '#5470C6' },
          lineStyle: { width: 3 },
          symbol: 'circle',
          symbolSize: 6
        },
        {
          name: '专业护理',
          type: 'line',
          data: serviceTrendData.professionalCare,
          smooth: true,
          itemStyle: { color: '#91CC75' },
          lineStyle: { width: 3 },
          symbol: 'circle',
          symbolSize: 6
        },
        {
          name: '生活护理',
          type: 'line',
          data: serviceTrendData.lifeCare,
          smooth: true,
          itemStyle: { color: '#FAC858' },
          lineStyle: { width: 3 },
          symbol: 'circle',
          symbolSize: 6
        }
      ]
    })

    // 健康管家工作量 - 使用模拟数据，只显示4个指定护士
    chartInstances.healthManager?.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'value', boundaryGap: [0, 0.01] },
      yAxis: {
        type: 'category',
        data: ['张护士', '李护士', '王护士', '刘护士']
      },
      series: [{
        type: 'bar',
        data: [18, 23, 20, 25],
        itemStyle: { color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#ffecd2' },
          { offset: 1, color: '#fcb69f' }
        ]) }
      }]
    })
  } catch (error) {
    console.error('加载图表数据失败:', error)
    ElMessage.error('加载图表数据失败')
  }
}

// 生成护理服务趋势数据
const getServiceTrendData = (timeRange: string) => {
  const now = new Date()
  let dates: string[] = []
  let basicCare: number[] = []
  let professionalCare: number[] = []
  let lifeCare: number[] = []

  if (timeRange === '7days') {
    // 近7天数据
    for (let i = 6; i >= 0; i--) {
      const date = new Date(now)
      date.setDate(date.getDate() - i)
      dates.push(`${date.getMonth() + 1}/${date.getDate()}`)
    }
    // 模拟7天的数据，范围0-10
    basicCare = [6, 8, 5, 9, 7, 6, 8]
    professionalCare = [4, 6, 3, 7, 5, 4, 6]
    lifeCare = [5, 7, 4, 8, 6, 5, 7]
  } else if (timeRange === '30days') {
    // 近30天数据，每5天一个数据点
    for (let i = 25; i >= 0; i -= 5) {
      const date = new Date(now)
      date.setDate(date.getDate() - i)
      dates.push(`${date.getMonth() + 1}/${date.getDate()}`)
    }
    // 模拟30天的数据，范围0-10
    basicCare = [7, 6, 8, 9, 7, 8]
    professionalCare = [5, 4, 6, 7, 5, 6]
    lifeCare = [6, 5, 7, 8, 6, 7]
  }

  return {
    dates,
    basicCare,
    professionalCare,
    lifeCare
  }
}

// 窗口大小变化时重新调整图表
const handleResize = () => {
  Object.values(chartInstances).forEach(chart => {
    if (chart) {
      chart.resize()
    }
  })
}

// 组件挂载
onMounted(() => {
  updateDateTime()
  timeTimer = setInterval(updateDateTime, 60000) // 每分钟更新时间

  // 延迟初始化图表，确保DOM已渲染
  setTimeout(() => {
    initCharts()
    loadOverviewStats()
    loadRealTimeData()
    loadChartData()
  }, 100)

  window.addEventListener('resize', handleResize)
})

// 组件卸载
onUnmounted(() => {
  if (timeTimer) {
    clearInterval(timeTimer)
  }

  // 销毁图表实例
  Object.values(chartInstances).forEach(chart => {
    if (chart) {
      chart.dispose()
    }
  })
  window.removeEventListener('resize', handleResize)
})

// 监听时间范围变化
const serviceTimeRangeChange = () => {
  // 重新加载护理服务趋势图表数据
  const serviceTrendData = getServiceTrendData(serviceTimeRange.value)

  chartInstances.serviceTrend?.setOption({
    xAxis: {
      data: serviceTrendData.dates
    },
    series: [
      {
        name: '基础护理',
        data: serviceTrendData.basicCare
      },
      {
        name: '专业护理',
        data: serviceTrendData.professionalCare
      },
      {
        name: '生活护理',
        data: serviceTrendData.lifeCare
      }
    ]
  })
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  position: relative;
  overflow-x: hidden;
}

.dashboard::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  pointer-events: none;
}

/* 头部区域 */
.header-section {
  margin-bottom: 30px;
}

.header-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  margin-bottom: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.welcome-section .dashboard-title {
  margin: 0 0 10px 0;
  color: #ffffff;
  font-size: 32px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.welcome-text {
  margin: 0 0 5px 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 18px;
  font-weight: 500;
}

.date-time {
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.header-actions .el-button {
  border-radius: 12px;
  padding: 12px 20px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.header-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* 快速统计卡片 */
.quick-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-card.clickable:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 15px 45px rgba(0, 0, 0, 0.2);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.1) 50%, transparent 70%);
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.stat-card:hover::before {
  transform: translateX(100%);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.click-hint {
  position: absolute;
  top: 10px;
  right: 10px;
  color: #409EFF;
  opacity: 0.7;
  transition: all 0.3s ease;
}

.stat-card.clickable:hover .click-hint {
  opacity: 1;
  transform: translateX(2px);
}

.stat-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.stat-icon {
  font-size: 28px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
}

.trend-up {
  color: #67C23A;
}

.trend-down {
  color: #F56C6C;
}

/* 主要内容区域 */
.main-content {
  position: relative;
  z-index: 1;
}

.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.charts-left,
.charts-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 图表卡片 */
.modern-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  overflow: hidden;
}

.modern-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.large-card {
  min-height: 450px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 20px;
  color: #409EFF;
}

.header-title {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.refresh-btn {
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: rgba(64, 158, 255, 0.1);
  transform: rotate(180deg);
}

.time-selector {
  width: 120px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.large-chart {
  height: 380px;
}

/* 实时数据网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.info-item:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateY(-2px);
}

.info-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-content {
  flex: 1;
}

.info-value {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  line-height: 1;
  margin-bottom: 2px;
}

.info-value.alert {
  color: #f56c6c;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

.info-label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .charts-section {
    grid-template-columns: 1fr;
  }

  .charts-left,
  .charts-right {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
  }

  .large-card {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 15px;
  }

  .header-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .quick-stats-grid {
    grid-template-columns: 1fr;
  }

  .charts-left,
  .charts-right {
    grid-template-columns: 1fr;
  }

  .large-card {
    grid-column: span 1;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .welcome-section .dashboard-title {
    font-size: 24px;
  }

  .stat-card {
    padding: 20px;
  }
}

/* Element Plus 组件样式覆盖 */
:deep(.el-card__header) {
  background: rgba(64, 158, 255, 0.05);
  border-bottom: 1px solid rgba(64, 158, 255, 0.1);
  padding: 20px;
}

:deep(.el-card__body) {
  padding: 24px;
}

:deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
}

:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-button:hover) {
  transform: translateY(-1px);
}

:deep(.el-tag) {
  border-radius: 6px;
  font-weight: 500;
}

/* 客户年龄分布卡片样式 */
.age-distribution-card {
  min-height: 420px;
}

.age-distribution-content {
  display: flex;
  gap: 24px;
  height: 320px;
}

.chart-wrapper {
  flex: 1;
  min-width: 0;
}

.age-chart-container {
  width: 100%;
  height: 100%;
}

.age-stats-panel {
  width: 200px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 16px;
  padding: 20px 16px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  backdrop-filter: blur(10px);
  max-height: 400px;
  overflow-y: auto;
}

.stats-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(64, 158, 255, 0.1);
}

.stats-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #334155;
  text-align: center;
}

.age-groups {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
  min-height: 240px; /* 确保有足够高度显示6个项目 */
}

.age-group-item {
  display: flex !important;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 1px solid rgba(226, 232, 240, 0.4);
  visibility: visible !important;
  opacity: 1 !important;
}

.age-group-item:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.age-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.age-info {
  flex: 1;
  min-width: 0;
}

.age-label {
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  margin-bottom: 2px;
}

.age-value {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.age-percentage {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.total-info {
  padding-top: 16px;
  border-top: 2px solid rgba(64, 158, 255, 0.1);
}

.total-count {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.05));
  border-radius: 12px;
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.total-label {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.total-number {
  font-size: 18px;
  font-weight: 700;
  color: #409EFF;
}

/* 响应式优化 */
@media (max-width: 1200px) {
  .age-distribution-content {
    flex-direction: column;
    height: auto;
  }

  .age-stats-panel {
    width: 100%;
    margin-top: 16px;
  }

  .age-groups {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .age-distribution-card {
    min-height: auto;
  }

  .age-stats-panel {
    padding: 16px 12px;
  }

  .age-groups {
    grid-template-columns: 1fr;
  }
}
</style>
