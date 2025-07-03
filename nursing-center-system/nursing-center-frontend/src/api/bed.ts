import request from '@/utils/request'
import type { ApiResponse, PageResponse } from './types'

// 床位状态枚举
export enum BedStatus {
  AVAILABLE = 'AVAILABLE', // 空闲
  OCCUPIED = 'OCCUPIED',   // 有人
  OUT = 'OUT'             // 外出
}

// 床位类型枚举
export enum BedType {
  STANDARD = 'STANDARD',   // 标准床
  CARE = 'CARE'           // 护理床
}

// 床位信息接口
export interface BedInfo {
  id: number
  bedNo: string
  bedType: BedType
  bedStatus: BedStatus
  roomId: number
  roomNo: string
  roomName: string
  floorNo: number
  buildingId: number
  buildingNo: string
  buildingName: string
  customerName?: string
  customerId?: number
}

// 床位统计信息
export interface BedStatistics {
  totalBeds: number
  availableBeds: number
  occupiedBeds: number
  outBeds: number
  occupancyRate: number
  byFloor: FloorStatistics[]
  byType: TypeStatistics[]
}

export interface FloorStatistics {
  floorNo: number
  totalBeds: number
  availableBeds: number
  occupiedBeds: number
  outBeds: number
}

export interface TypeStatistics {
  bedType: BedType
  totalBeds: number
  availableBeds: number
  occupiedBeds: number
}

// 床位详细信息
export interface BedDetail {
  id: number
  bedNo: string
  bedType: BedType
  bedStatus: BedStatus
  room: {
    id: number
    roomNo: string
    roomName: string
    floorNo: number
  }
  building: {
    id: number
    buildingNo: string
    buildingName: string
  }
  currentCustomer?: {
    id: number
    customerName: string
    age: number
    gender: string
    checkInDate: string
    healthManagerName: string
  }
  usageHistory: {
    customerId: number
    customerName: string
    startDate: string
    endDate: string
    duration: number
  }[]
}

// 床位图表数据
export interface BedDiagram {
  floorNo: number
  rooms: RoomDiagram[]
}

export interface RoomDiagram {
  roomId: number
  roomNo: string
  roomName: string
  roomType: string
  beds: BedDiagramItem[]
}

export interface BedDiagramItem {
  bedId: number
  bedNo: string
  bedType: BedType
  bedStatus: BedStatus
  customerName?: string
  customerId?: number
  careLevel?: string
  healthManagerName?: string
}

// 床位使用记录
export interface BedUsage {
  id: number
  // 客户信息 - 扁平化结构，与后端BedUsageDTO保持一致
  customerId: number
  customerName: string
  age: number
  gender: string
  idCard: string
  
  // 床位位置信息 - 扁平化结构，与后端BedUsageDTO保持一致
  bedId: number
  bedNo: string
  roomNo: string
  buildingNo: string
  floorNo: number
  fullLocation: string
  
  // 时间和状态信息
  startDate: string
  endDate: string
  duration: number
  usageStatus: 'USING' | 'HISTORY'
}

// 床位交换DTO
export interface BedSwap {
  sourceCustomerId: number
  sourceBedId: number
  targetCustomerId?: number
  targetBedId: number
  swapReason: string
  swapDate: string
  remark?: string
}

// 房间信息接口
export interface RoomInfo {
  id: number
  roomNo: string
  roomName: string
  floorNo: number
  buildingId: number
  buildingNo: string
  buildingName: string
  roomType: string
}

// 床位管理API
export const bedApi = {  // 获取床位统计
  getStatistics(): Promise<ApiResponse<BedStatistics>> {
    return request({
      url: '/api/admin/bed/statistics',
      method: 'get'
    })
  },
  // 获取指定楼层的床位
  getBedsByFloor(floorNo: number): Promise<ApiResponse<BedInfo[]>> {
    return request({
      url: `/api/admin/bed/floor/${floorNo}`,
      method: 'get'
    })
  },
  // 获取房间内可用床位
  getAvailableBedsByRoom(roomId: number): Promise<ApiResponse<BedInfo[]>> {
    return request({
      url: `/api/admin/bed/available/room/${roomId}`,
      method: 'get'
    })
  },
  // 获取床位详细信息
  getBedDetail(bedId: number): Promise<ApiResponse<BedDetail>> {
    return request({
      url: `/api/admin/bed/detail/${bedId}`,
      method: 'get'
    })
  },  // 获取床位图表数据
  getBedDiagram(): Promise<ApiResponse<BedDiagram[]>> {
    return request({
      url: '/api/admin/bed/diagrams/grouped',
      method: 'get'
    })
  },

  // 获取床位使用历史
  getBedUsageHistory(params: {
    bedId?: number
    customerId?: number
    page?: number
    size?: number
  }): Promise<ApiResponse<BedUsage[]>> {
    return request({
      url: '/api/admin/bed/usage/history',
      method: 'get',
      params
    })
  },
  // 床位交换
  swapBeds(data: BedSwap): Promise<ApiResponse<string>> {
    return request({
      url: '/api/admin/bed/swap',
      method: 'post',
      data
    })
  },  // 获取可交换的床位列表
  getSwappableBeds(customerId: number): Promise<ApiResponse<BedInfo[]>> {
    return request({
      url: `/api/admin/bed/swappable/${customerId}`,
      method: 'get'
    })
  }
}

// 床位使用查询参数接口
export interface BedUsageQuery {
  pageNum?: number
  pageSize?: number
  customerName?: string
  startDateFrom?: string
  startDateTo?: string
  usageStatus?: 'USING' | 'HISTORY'
  bedNo?: string
  roomNo?: string
  floorNo?: number
}

// 床位使用管理API
export const bedUsageApi = {
  // 分页查询床位使用记录
  getBedUsagePage(params: BedUsageQuery): Promise<ApiResponse<PageResponse<BedUsage>>> {
    return request({
      url: '/api/admin/bed-usage/page',
      method: 'get',
      params
    })
  },

  // 根据ID查询床位使用记录详情
  getBedUsageById(id: number): Promise<ApiResponse<BedUsage>> {
    return request({
      url: `/api/admin/bed-usage/${id}`,
      method: 'get'
    })
  },

  // 更新床位使用记录的结束时间
  updateEndDate(id: number, endDate: string): Promise<ApiResponse<string>> {
    return request({
      url: `/api/admin/bed-usage/${id}/end-date`,
      method: 'put',
      data: null,
      params: { endDate }
    })
  },

  // 床位调换
  swapBed(data: BedSwap): Promise<ApiResponse<string>> {
    return request({
      url: '/api/admin/bed-usage/swap',
      method: 'post',
      data
    })
  },

  // 获取所有房间列表（用于床位调换）
  getRoomsForSwap(): Promise<ApiResponse<RoomInfo[]>> {
    return request({
      url: '/api/admin/bed-usage/swap/rooms',
      method: 'get'
    })
  },

  // 根据房间ID获取空闲床位列表（用于床位调换）
  getAvailableBedsForSwap(roomId: number): Promise<ApiResponse<BedInfo[]>> {
    return request({
      url: `/api/admin/bed-usage/swap/available-beds/${roomId}`,
      method: 'get'
    })
  },

  // 按楼层获取房间列表（用于床位调换）
  getRoomsByFloorForSwap(floorNo: number): Promise<ApiResponse<RoomInfo[]>> {
    return request({
      url: `/api/admin/bed-usage/swap/rooms/floor/${floorNo}`,
      method: 'get'
    })
  }
}
