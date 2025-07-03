import request from '@/utils/request'

// 仪表板相关API接口

// 统计数据接口
export interface DashboardStats {
  totalCustomers: number
  totalBeds: number
  occupiedBeds: number
  availableBeds: number
  outingCustomers: number
  pendingApplies: number
  todayCheckIns: number
  todayCheckOuts: number
  onlineManagers: number
}

// 床位占用趋势
export interface BedOccupancyTrend {
  date: string
  occupiedBeds: number
  totalBeds: number
  occupancyRate: number
}

// 年龄分布
export interface AgeDistribution {
  ageGroup: string
  count: number
  percentage: number
}

// 护理级别分布
export interface CareLevelDistribution {
  levelName: string
  count: number
  color?: string
}

// 最近活动
export interface RecentActivity {
  type: string
  content: string
  time: string
}

// 床位统计
export interface BedStatistics {
  totalBeds: number
  occupiedBeds: number
  availableBeds: number
  maintenanceBeds: number
  occupancyRate: number
  floorStats: {
    floorNo: number
    totalBeds: number
    occupiedBeds: number
    availableBeds: number
  }[]
}

// 床位信息
export interface BedInfo {
  id: number
  bedNo: string
  roomId: number
  roomNo: string
  floorNo: number
  status: string
  customerId?: number
  customerName?: string
}

// 在住客户详细信息
export interface CustomerDetail {
  id: number
  name: string
  gender: string
  age: number
  phone: string
  emergencyContact: string
  emergencyPhone: string
  roomNo: string
  bedNo: string
  checkInDate: string
  careLevel: string
  healthStatus: string
}

// 在线管家详细信息
export interface ManagerDetail {
  id: number
  realName: string
  username: string
  phone: string
  email: string
  department: string
  position: string
  onlineStatus: string
  lastLoginTime: string
  managedCustomers: number
  workload: string
}

// 仪表板API
export const dashboardApi = {
  // 获取统计数据
  getStats: (): Promise<DashboardStats> => {
    return request({
      url: '/api/dashboard/stats',
      method: 'get'
    })
  },

  // 获取床位占用趋势数据
  getBedOccupancyTrend: (days: number = 30): Promise<BedOccupancyTrend[]> => {
    return request({
      url: '/api/dashboard/bed-occupancy-trend',
      method: 'get',
      params: { days }
    })
  },

  // 获取客户年龄分布数据
  getAgeDistribution: (): Promise<AgeDistribution[]> => {
    return request({
      url: '/api/dashboard/customer-age-distribution',
      method: 'get'
    })
  },

  // 获取护理级别分布数据
  getCareLevelDistribution: (): Promise<CareLevelDistribution[]> => {
    return request({
      url: '/api/dashboard/care-level-distribution',
      method: 'get'
    })
  },

  // 获取最近活动数据
  getRecentActivities: (limit: number = 10): Promise<RecentActivity[]> => {
    return request({
      url: '/api/dashboard/recent-activities',
      method: 'get',
      params: { limit }
    })
  },

  // 获取楼层入住统计
  getFloorOccupancyStats: (): Promise<any[]> => {
    return request({
      url: '/api/dashboard/floor-occupancy-stats',
      method: 'get'
    })
  },

  // 获取床位统计信息
  getBedStatistics: (): Promise<BedStatistics> => {
    return request({
      url: '/api/admin/bed/statistics',
      method: 'get'
    })
  },

  // 根据楼层获取床位信息
  getBedsByFloor: (floorNo: number): Promise<BedInfo[]> => {
    return request({
      url: `/api/admin/bed/floor/${floorNo}`,
      method: 'get'
    })
  },

  // 获取房间可用床位
  getAvailableBedsByRoom: (roomId: number): Promise<BedInfo[]> => {
    return request({
      url: `/api/admin/bed/available/room/${roomId}`,
      method: 'get'
    })
  },

  // 获取在住客户详细列表
  getCustomerDetails: (): Promise<CustomerDetail[]> => {
    return request({
      url: '/api/dashboard/customer-details',
      method: 'get'
    })
  },

  // 获取在线管家详细列表
  getOnlineManagerDetails: (): Promise<ManagerDetail[]> => {
    return request({
      url: '/api/dashboard/online-manager-details',
      method: 'get'
    })
  }
}
