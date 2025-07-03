import request from '@/utils/request'
import type { PageResponse, BaseQueryParams } from './types'

// 护理记录相关API接口

// 护理记录接口
export interface CareRecord {
  id: number
  customerId: number
  customerName: string
  healthManagerId?: number
  healthManagerName?: string
  careItemId: number
  careItemName: string
  careType: string
  careContent: string
  careResult?: string
  recordDate: string
  nurseStaff: string
  remarks?: string
  createdAt?: string
  updatedAt?: string
}

// 护理记录创建/更新DTO
export interface CareRecordDTO {
  id?: number
  customerId: number
  healthManagerId?: number
  careItemId: number
  careContent: string
  careResult?: string
  recordDate: string
  remarks?: string
}

// 日常护理DTO
export interface DailyCareDTO {
  customerId: number
  careItems: {
    careItemId: number
    careContent: string
    careResult?: string
    remarks?: string
  }[]
}

// 护理计划接口
export interface CarePlan {
  id: number
  customerId: number
  customerName: string
  planName: string
  planContent: string
  startDate: string
  endDate: string
  frequency: string
  status: string
  createdBy: string
  createdAt: string
  updatedAt?: string
}

// 护理计划DTO
export interface CarePlanDTO {
  id?: number
  customerId: number
  planName: string
  planContent: string
  startDate: string
  endDate: string
  frequency: string
  status?: string
}

// 查询参数
export interface CareQueryParams {
  page?: number
  size?: number
  customerName?: string
  healthManagerName?: string
  careItemName?: string
  startTime?: string
  endTime?: string
}

// 护理等级接口
export interface CareLevel {
  id: number
  levelName: string
  levelCode: string
  description: string
  status: string
  createTime: string
  updateTime?: string
}

// 护理等级DTO
export interface CareLevelDTO {
  id?: number
  levelName: string
  levelCode: string
  description?: string
  status?: number
}

// 护理等级查询参数
export interface CareLevelQueryParams extends BaseQueryParams {
  pageNum?: number
  pageSize?: number
  levelName?: string
  status?: string
}

// 护理项目接口
export interface CareItem {
  id: number
  itemName: string
  itemCode: string
  careType: string
  description: string
  price: number
  executeCycle: number
  executeTimes: number
  status: number
  createTime: string
  updateTime?: string
}

// 护理项目DTO
export interface CareItemDTO {
  id?: number
  itemName: string
  itemCode: string
  careType: string
  description?: string
  price: number
  executeCycle: number
  executeTimes: number
  status?: number
}

// 护理项目查询参数
export interface CareItemQueryParams extends BaseQueryParams {
  pageNum?: number
  pageSize?: number
  itemName?: string
  careType?: string
  status?: string
}

// 护理级别项目配置接口
export interface CareLevelItemConfig {
  id: number
  careLevelId: number
  careItemId: number
  careItem: CareItem
  createTime: string
}

// 护理级别项目配置DTO
export interface CareLevelItemConfigDTO {
  careLevelId: number
  careItemIds: number[]
}

// 客户护理设置接口 - 用于客户护理设置页面
export interface CustomerCareSetting {
  id: number
  name: string
  phone: string
  idCard: string
  age: number
  gender: string
  careLevelId?: number
  careLevelName?: string
  careItemCount: number
  effectiveDate: string
  createTime: string
  buildingName: string
  roomNo: string
  bedNo: string
  healthManagerName?: string
}

// 客户护理设置查询参数
export interface CustomerCareSettingQuery {
  pageNum?: number
  pageSize?: number
  customerName?: string
  careLevelId?: number
  status?: number
}

// 护理级别设置DTO
export interface CustomerCareLevelSetting {
  customerId: number
  careLevelId: number
  careItems?: {
    careItemId: number
    quantity: number
    expireDate?: string
  }[]
  serviceStartDate?: string
  serviceExpireDate?: string
}

// 客户护理配置接口
export interface CustomerCareConfig {
  id: number
  customerId: number
  customerName: string
  careLevelId: number
  careLevelName: string
  careItems: CareItem[]
  specialRequirements?: string
  effectiveDate: string
  status: string
  createTime: string
  updateTime?: string
}

// 客户护理配置DTO
export interface CustomerCareConfigDTO {
  id?: number
  customerId: number
  careLevelId: number
  careItemIds: number[]
  specialRequirements?: string
  effectiveDate: string
  status?: string
}

// 客户护理配置查询参数
export interface CustomerCareQueryParams extends BaseQueryParams {
  page?: number
  size?: number
  customerName?: string
  careLevelId?: number
  status?: string
}

// 护理管理API
export const careApi = {
  // 护理记录相关
  getRecords: (params: CareQueryParams): Promise<PageResponse<CareRecord>> => {
    return request({
      url: '/api/admin/care-record/page',
      method: 'get',
      params
    })
  },

  getRecordById: (id: number): Promise<CareRecord> => {
    return request({
      url: `/api/admin/care-record/${id}`,
      method: 'get'
    })
  },

  createRecord: (data: CareRecordDTO): Promise<CareRecord> => {
    return request({
      url: '/api/admin/care-record',
      method: 'post',
      data
    })
  },

  updateRecord: (data: CareRecordDTO): Promise<CareRecord> => {
    return request({
      url: '/api/admin/care-record',
      method: 'put',
      data
    })
  },

  deleteRecord: (id: number): Promise<void> => {
    return request({
      url: `/api/admin/care-record/${id}`,
      method: 'delete'
    })
  },

  // 日常护理
  addDailyCare: (data: DailyCareDTO): Promise<void> => {
    return request({
      url: '/api/admin/care-record/daily',
      method: 'post',
      data
    })
  },

  // 护理计划相关
  getPlans: (params: CareQueryParams): Promise<PageResponse<CarePlan>> => {
    return request({
      url: '/api/admin/care-plan/page',
      method: 'get',
      params
    })
  },

  getPlanById: (id: number): Promise<CarePlan> => {
    return request({
      url: `/api/admin/care-plan/${id}`,
      method: 'get'
    })
  },

  createPlan: (data: CarePlanDTO): Promise<CarePlan> => {
    return request({
      url: '/api/admin/care-plan',
      method: 'post',
      data
    })
  },

  updatePlan: (data: CarePlanDTO): Promise<CarePlan> => {
    return request({
      url: '/api/admin/care-plan',
      method: 'put',
      data
    })
  },

  deletePlan: (id: number): Promise<void> => {
    return request({
      url: `/api/admin/care-plan/${id}`,
      method: 'delete'
    })
  },

  // 护理等级相关
  getCareLevel: {
    list: (params: CareLevelQueryParams): Promise<PageResponse<CareLevel>> => {
      return request({
        url: '/api/admin/care-level/page',
        method: 'get',
        params
      })
    },

    getById: (id: number): Promise<CareLevel> => {
      return request({
        url: `/api/admin/care-level/${id}`,
        method: 'get'
      })
    },

    create: (data: CareLevelDTO): Promise<CareLevel> => {
      return request({
        url: '/api/admin/care-level',
        method: 'post',
        data
      })
    },

    update: (data: CareLevelDTO): Promise<CareLevel> => {
      return request({
        url: '/api/admin/care-level',
        method: 'put',
        data
      })
    },

    delete: (id: number): Promise<void> => {
      return request({
        url: `/api/admin/care-level/${id}`,
        method: 'delete'
      })
    },

    getAll: (): Promise<CareLevel[]> => {
      return request({
        url: '/api/admin/care-level/all',
        method: 'get'
      })
    },

    getItems: (id: number): Promise<CareItem[]> => {
      return request({
        url: `/api/admin/care-level/${id}/items`,
        method: 'get'
      })
    },

    addItems: (data: CareLevelItemConfigDTO): Promise<void> => {
      return request({
        url: '/api/admin/care-level/items',
        method: 'post',
        data
      })
    }
  },

  // 护理项目相关
  getCareItem: {
    list: (params: CareItemQueryParams): Promise<PageResponse<CareItem>> => {
      return request({
        url: '/api/admin/care-item/page',
        method: 'get',
        params
      })
    },

    getById: (id: number): Promise<CareItem> => {
      return request({
        url: `/api/admin/care-item/${id}`,
        method: 'get'
      })
    },

    create: (data: CareItemDTO): Promise<CareItem> => {
      return request({
        url: '/api/admin/care-item',
        method: 'post',
        data
      })
    },

    update: (data: CareItemDTO): Promise<CareItem> => {
      return request({
        url: '/api/admin/care-item',
        method: 'put',
        data
      })
    },

    delete: (id: number): Promise<void> => {
      return request({
        url: `/api/admin/care-item/${id}`,
        method: 'delete'
      })
    },

    updateStatus: (id: number, status: number): Promise<void> => {
      return request({
        url: `/api/admin/care-item/${id}/status`,
        method: 'put',
        data: { status }
      })
    },

    getAll: (): Promise<CareItem[]> => {
      return request({
        url: '/api/admin/care-item/all',
        method: 'get'
      })
    }
  },

  // 客户护理设置相关
  getCustomerCareSetting: {
    // 分页查询客户护理设置
    getPage: (params: CustomerCareSettingQuery): Promise<PageResponse<CustomerCareSetting>> => {
      return request({
        url: '/api/admin/customer-care/setting/page',
        method: 'get',
        params
      })
    },

    // 获取护理级别下的护理项目
    getLevelItems: (levelId: number): Promise<CareItem[]> => {
      return request({
        url: `/api/admin/care-level/${levelId}/items`,
        method: 'get'
      })
    },

    // 设置客户护理级别
    setCareLevel: (data: CustomerCareLevelSetting): Promise<string> => {
      return request({
        url: '/api/admin/customer-care/set-level',
        method: 'post',
        data
      })
    },

    // 移除客户护理级别
    removeCareLevel: (customerId: number): Promise<string> => {
      return request({
        url: `/api/admin/customer-care/remove-level/${customerId}`,
        method: 'delete'
      })
    },

    // 批量购买护理项目
    purchaseItems: (customerId: number, careItems: { careItemId: number; quantity: number; expireDate?: string }[]): Promise<string> => {
      return request({
        url: `/api/admin/customer-care/purchase/${customerId}`,
        method: 'post',
        data: careItems
      })
    }
  },

  // 客户护理配置管理
  getCustomerCare: {
    list: (params: CustomerCareQueryParams): Promise<PageResponse<CustomerCareConfig>> => {
      return request({
        url: '/api/admin/customer-care/page',
        method: 'get',
        params
      })
    },

    getById: (id: number): Promise<CustomerCareConfig> => {
      return request({
        url: `/api/admin/customer-care/${id}`,
        method: 'get'
      })
    },

    getByCustomerId: (customerId: number): Promise<CustomerCareConfig> => {
      return request({
        url: `/api/admin/customer-care/customer/${customerId}`,
        method: 'get'
      })
    },

    create: (data: CustomerCareConfigDTO): Promise<CustomerCareConfig> => {
      return request({
        url: '/api/admin/customer-care',
        method: 'post',
        data
      })
    },

    update: (data: CustomerCareConfigDTO): Promise<CustomerCareConfig> => {
      return request({
        url: '/api/admin/customer-care',
        method: 'put',
        data
      })
    },

    delete: (id: number): Promise<void> => {
      return request({
        url: `/api/admin/customer-care/${id}`,
        method: 'delete'
      })
    }
  }
}
