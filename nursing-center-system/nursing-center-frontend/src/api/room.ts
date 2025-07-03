import request from '@/utils/request'
import type { ApiResponse, PageResponse } from './types'

// 房间类型枚举
export enum RoomType {
  SINGLE = 'SINGLE',     // 单人间
  DOUBLE = 'DOUBLE',     // 双人间
  MULTI = 'MULTI'        // 多人间
}

// 房间信息接口
export interface Room {
  id: number
  roomNo: string
  roomName: string
  buildingId: number
  buildingName?: string
  buildingNo?: string
  floorNo: number
  bedCount: number
  roomType: RoomType
  description?: string
  status: number  // 0-禁用 1-启用
  availableBeds?: number
  occupiedBeds?: number
  createdAt?: string
  updatedAt?: string
}

// 房间创建/更新DTO
export interface RoomDTO {
  id?: number
  roomNo: string
  roomName: string
  buildingId: number
  floorNo: number
  bedCount: number
  roomType: RoomType
  description?: string
  status?: number
}

// 房间查询DTO
export interface RoomQueryDTO {
  pageNum: number
  pageSize: number
  roomNumber?: string
  roomName?: string
  buildingId?: number
  floor?: number
  roomType?: RoomType
  status?: number
}

// 房间API
export const roomApi = {
  // 分页查询房间
  getRoomPage: (query: RoomQueryDTO): Promise<ApiResponse<PageResponse<Room>>> => {
    return request({
      url: '/api/room/page',
      method: 'post',
      data: query
    })
  },

  // 根据ID查询房间详情
  getRoomById: (id: number): Promise<ApiResponse<Room>> => {
    return request({
      url: `/api/room/${id}`,
      method: 'get'
    })
  },

  // 新增房间
  addRoom: (room: RoomDTO): Promise<ApiResponse<number>> => {
    return request({
      url: '/api/room',
      method: 'post',
      data: room
    })
  },

  // 更新房间
  updateRoom: (room: RoomDTO): Promise<ApiResponse<void>> => {
    return request({
      url: '/api/room',
      method: 'put',
      data: room
    })
  },

  // 删除房间
  deleteRoom: (id: number): Promise<ApiResponse<void>> => {
    return request({
      url: `/api/room/${id}`,
      method: 'delete'
    })
  },
  // 根据楼栋ID获取房间列表
  getRoomsByBuildingId: (buildingId: number): Promise<ApiResponse<Room[]>> => {
    return request({
      url: `/api/room/building/${buildingId}`,
      method: 'get'
    })
  },
  // 获取所有启用的房间
  getEnabledRooms: (): Promise<ApiResponse<Room[]>> => {
    return request({
      url: '/api/room/enabled',
      method: 'get'
    })
  },
  // 根据楼栋ID获取按楼层分组的房间列表
  getRoomsGroupedByFloor: (buildingId: number): Promise<ApiResponse<Room[]>> => {
    return request({
      url: `/api/room/grouped/building/${buildingId}`,
      method: 'get'
    })
  }
}

export default roomApi
