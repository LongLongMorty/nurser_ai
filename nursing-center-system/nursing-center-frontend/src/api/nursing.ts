import request from '@/utils/request'
import type { PageResponse, BaseQueryParams } from './types'

// 护理级别接口
export interface NursingLevel {
  id: number
  levelName: string
  levelCode: string
  description: string
  priority: number
  dailyFee: number
  monthlyFee: number
  status: string // ACTIVE, INACTIVE
  careItems: NursingItem[]
  createTime: string
  updateTime?: string
}

// 护理级别DTO
export interface NursingLevelDTO {
  id?: number
  levelName: string
  levelCode: string
  description?: string
  priority: number
  dailyFee: number
  monthlyFee: number
  status?: string
  careItemIds?: number[]
}

// 护理项目接口
export interface NursingItem {
  id: number
  itemName: string
  itemCode: string
  category: string // BASIC, MEDICAL, SPECIAL
  description: string
  duration: number // 预计用时（分钟）
  requiredSkills: string[]
  unitPrice: number
  status: string // ACTIVE, INACTIVE
  createTime: string
  updateTime?: string
}

// 护理项目DTO
export interface NursingItemDTO {
  id?: number
  itemName: string
  itemCode: string
  category: string
  description?: string
  duration: number
  requiredSkills: string[]
  unitPrice: number
  status?: string
}

// 客户护理设置接口
export interface CustomerNursingConfig {
  id: number
  customerId: number
  customerName: string
  nursingLevelId?: number
  nursingLevelName?: string
  nursingItems: CustomerNursingItem[]
  effectiveDate: string
  expiryDate?: string
  status: string // ACTIVE, INACTIVE, EXPIRED
  notes?: string
  createTime: string
  updateTime?: string
}

// 客户护理项目
export interface CustomerNursingItem {
  id: number
  customerId: number
  nursingItemId: number
  nursingItemName: string
  frequency: string // DAILY, WEEKLY, MONTHLY
  frequencyValue: number // 频次值
  startDate: string
  endDate?: string
  status: string
  notes?: string
}

// 客户护理设置DTO
export interface CustomerNursingConfigDTO {
  id?: number
  customerId: number
  nursingLevelId?: number
  nursingItemIds?: number[]
  effectiveDate: string
  expiryDate?: string
  notes?: string
  status?: string
}

// 护理记录接口
export interface NursingRecord {
  id: number
  customerId: number
  customerName: string
  nursingItemId: number
  nursingItemName: string
  nursingDate: string
  startTime: string
  endTime?: string
  duration?: number
  nurseId: number
  nurseName: string
  status: string // PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
  result?: string
  notes?: string
  createTime: string
  updateTime?: string
}

// 护理记录DTO
export interface NursingRecordDTO {
  id?: number
  customerId: number
  nursingItemId: number
  nursingDate: string
  startTime: string
  endTime?: string
  nurseId: number
  result?: string
  notes?: string
  status?: string
}

// 查询参数接口
export interface NursingLevelQueryParams extends BaseQueryParams {
  page?: number
  size?: number
  levelName?: string
  status?: string
}

export interface NursingItemQueryParams extends BaseQueryParams {
  page?: number
  size?: number
  itemName?: string
  category?: string
  status?: string
}

export interface CustomerNursingQueryParams extends BaseQueryParams {
  page?: number
  size?: number
  customerName?: string
  nursingLevelId?: number
  status?: string
}

export interface NursingRecordQueryParams extends BaseQueryParams {
  page?: number
  size?: number
  customerId?: number
  customerName?: string
  nursingItemId?: number
  nurseId?: number
  nurseName?: string
  status?: string
  startDate?: string
  endDate?: string
}

// 护理管理API
export const nursingApi = {
  // 护理级别管理
  nursingLevel: {
    list: (params: NursingLevelQueryParams): Promise<PageResponse<NursingLevel>> => {
      return request({
        url: '/api/admin/nursing-level/page',
        method: 'get',
        params
      })
    },

    getById: (id: number): Promise<NursingLevel> => {
      return request({
        url: `/api/admin/nursing-level/${id}`,
        method: 'get'
      })
    },

    create: (data: NursingLevelDTO): Promise<NursingLevel> => {
      return request({
        url: '/api/admin/nursing-level',
        method: 'post',
        data
      })
    },

    update: (data: NursingLevelDTO): Promise<NursingLevel> => {
      return request({
        url: '/api/admin/nursing-level',
        method: 'put',
        data
      })
    },

    delete: (id: number): Promise<void> => {
      return request({
        url: `/api/admin/nursing-level/${id}`,
        method: 'delete'
      })
    },

    updateStatus: (id: number, status: string): Promise<void> => {
      return request({
        url: `/api/admin/nursing-level/${id}/status`,
        method: 'put',
        data: { status }
      })
    },

    configureItems: (levelId: number, itemIds: number[]): Promise<void> => {
      return request({
        url: `/api/admin/nursing-level/${levelId}/items`,
        method: 'put',
        data: { itemIds }
      })
    },

    getAll: (): Promise<NursingLevel[]> => {
      return request({
        url: '/api/admin/nursing-level/all',
        method: 'get'
      })
    }
  },

  // 护理项目管理
  nursingItem: {
    list: (params: NursingItemQueryParams): Promise<PageResponse<NursingItem>> => {
      return request({
        url: '/api/admin/nursing-item/page',
        method: 'get',
        params
      })
    },

    getById: (id: number): Promise<NursingItem> => {
      return request({
        url: `/api/admin/nursing-item/${id}`,
        method: 'get'
      })
    },

    create: (data: NursingItemDTO): Promise<NursingItem> => {
      return request({
        url: '/api/admin/nursing-item',
        method: 'post',
        data
      })
    },

    update: (data: NursingItemDTO): Promise<NursingItem> => {
      return request({
        url: '/api/admin/nursing-item',
        method: 'put',
        data
      })
    },

    delete: (id: number): Promise<void> => {
      return request({
        url: `/api/admin/nursing-item/${id}`,
        method: 'delete'
      })
    },

    updateStatus: (id: number, status: string): Promise<void> => {
      return request({
        url: `/api/admin/nursing-item/${id}/status`,
        method: 'put',
        data: { status }
      })
    },

    getAll: (): Promise<NursingItem[]> => {
      return request({
        url: '/api/admin/nursing-item/all',
        method: 'get'
      })
    },

    getByCategory: (category: string): Promise<NursingItem[]> => {
      return request({
        url: `/api/admin/nursing-item/category/${category}`,
        method: 'get'
      })
    }
  },

  // 客户护理设置管理
  customerNursing: {
    list: (params: CustomerNursingQueryParams): Promise<PageResponse<CustomerNursingConfig>> => {
      return request({
        url: '/api/admin/customer-nursing/page',
        method: 'get',
        params
      })
    },

    getByCustomerId: (customerId: number): Promise<CustomerNursingConfig> => {
      return request({
        url: `/api/admin/customer-nursing/customer/${customerId}`,
        method: 'get'
      })
    },

    setLevel: (customerId: number, levelId: number, effectiveDate: string, notes?: string): Promise<void> => {
      return request({
        url: `/api/admin/customer-nursing/set-level`,
        method: 'post',
        data: { customerId, levelId, effectiveDate, notes }
      })
    },

    removeLevel: (customerId: number): Promise<void> => {
      return request({
        url: `/api/admin/customer-nursing/remove-level`,
        method: 'post',
        data: { customerId }
      })
    },

    addItems: (customerId: number, itemConfigs: {
      nursingItemId: number
      frequency: string
      frequencyValue: number
      startDate: string
      endDate?: string
      notes?: string
    }[]): Promise<void> => {
      return request({
        url: `/api/admin/customer-nursing/add-items`,
        method: 'post',
        data: { customerId, itemConfigs }
      })
    },

    removeItem: (customerId: number, itemId: number): Promise<void> => {
      return request({
        url: `/api/admin/customer-nursing/remove-item`,
        method: 'post',
        data: { customerId, itemId }
      })
    },

    batchSetLevel: (customerIds: number[], levelId: number, effectiveDate: string): Promise<void> => {
      return request({
        url: `/api/admin/customer-nursing/batch-set-level`,
        method: 'post',
        data: { customerIds, levelId, effectiveDate }
      })
    }
  },

  // 护理记录管理
  nursingRecord: {
    list: (params: NursingRecordQueryParams): Promise<PageResponse<NursingRecord>> => {
      return request({
        url: '/api/admin/nursing-record/page',
        method: 'get',
        params
      })
    },

    getById: (id: number): Promise<NursingRecord> => {
      return request({
        url: `/api/admin/nursing-record/${id}`,
        method: 'get'
      })
    },

    create: (data: NursingRecordDTO): Promise<NursingRecord> => {
      return request({
        url: '/api/admin/nursing-record',
        method: 'post',
        data
      })
    },

    update: (data: NursingRecordDTO): Promise<NursingRecord> => {
      return request({
        url: '/api/admin/nursing-record',
        method: 'put',
        data
      })
    },

    delete: (id: number): Promise<void> => {
      return request({
        url: `/api/admin/nursing-record/${id}`,
        method: 'delete'
      })
    },

    getByCustomerId: (customerId: number, startDate?: string, endDate?: string): Promise<NursingRecord[]> => {
      return request({
        url: `/api/admin/nursing-record/customer/${customerId}`,
        method: 'get',
        params: { startDate, endDate }
      })
    },

    updateStatus: (id: number, status: string, result?: string, notes?: string): Promise<void> => {
      return request({
        url: `/api/admin/nursing-record/${id}/status`,
        method: 'put',
        data: { status, result, notes }
      })
    },

    batchDelete: (ids: number[]): Promise<void> => {
      return request({
        url: '/api/admin/nursing-record/batch-delete',
        method: 'post',
        data: { ids }
      })
    }
  }
}

// 导出独立函数供组件使用
export const getNursingLevels = (params?: NursingLevelQueryParams) => {
  return nursingApi.nursingLevel.list(params || {})
}

export const getNursingItems = (params?: NursingItemQueryParams) => {
  return nursingApi.nursingItem.list(params || {})
}

export const getCustomerNursingConfigs = (params?: CustomerNursingQueryParams) => {
  return nursingApi.customerNursing.list(params || {})
}

export const getNursingRecords = (params?: NursingRecordQueryParams) => {
  return nursingApi.nursingRecord.list(params || {})
}
