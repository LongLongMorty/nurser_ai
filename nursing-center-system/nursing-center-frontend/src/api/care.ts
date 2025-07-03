import request from '@/utils/request'
import type { PageResponse, BaseQueryParams } from './types'

// 护理项目接口
export interface CareItem {
  id: number
  itemName: string
  careType: string
  executeCycle: number
  executeTimes: number
  price: number
  description?: string
  status: number
  createTime: string
  updateTime?: string
}

// 护理项目DTO
export interface CareItemDTO {
  id?: number
  itemName: string
  careType: string
  executeCycle: number
  executeTimes: number
  price: number
  description?: string
  status?: number  // 保持为number类型，前端发送1/0
}

// 护理项目查询参数
export interface CareItemQueryParams extends BaseQueryParams {
  pageNum?: number
  pageSize?: number
  itemName?: string
  careType?: string
  status?: string | number  // 兼容字符串和数字
}

// 护理级别接口
export interface CareLevel {
  id: number
  levelName: string
  levelCode: string
  description: string
  status: string
  createTime: string
  updateTime?: string
}

// 护理级别DTO
export interface CareLevelDTO {
  id?: number
  levelName: string
  levelCode: string
  description?: string
  status?: string
}

// 护理级别查询参数
export interface CareLevelQueryParams extends BaseQueryParams {
  page?: number
  size?: number
  levelName?: string
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

// 客户护理设置查询参数
export interface CustomerCareQueryParams extends BaseQueryParams {
  pageNum?: number
  pageSize?: number
  customerName?: string
  careLevelId?: number
  status?: string
}

// 客户护理设置响应接口
export interface CustomerCarePageDTO {
  id: number
  customerName: string
  phone: string
  careLevelId?: number
  careLevelName?: string
  careItemCount: number
  effectiveDate?: string
  createTime: string
}

// 客户护理详情接口
export interface CustomerCareDetailDTO {
  id: number
  customerId: number
  customerName: string
  phone: string
  careLevelId?: number
  careLevelName?: string
  careItems: CustomerCareItemDetailDTO[]
  createTime: string
}

// 客户护理项目详情接口
export interface CustomerCareItemDetailDTO {
  id: number
  itemName: string
  careType: string
  price: number
  purchaseQuantity: number
  usedQuantity: number
  remainingQuantity: number
  purchaseDate: string
  expireDate: string
  serviceStatus: string
}

// 客户护理DTO（用于服务列表和可用项目）
export interface CustomerCareDTO {
  id?: number
  customerId?: number
  careItemId?: number
  purchaseDate?: string
  purchaseQuantity?: number
  usedQuantity?: number
  remainingQuantity?: number
  expireDate?: string
  serviceStatus?: string
  customerName?: string
  itemName?: string
  itemPrice?: number
  itemCode?: string
  careType?: string
  description?: string
}

// 客户护理项目购买DTO
export interface CustomerCareItemPurchaseDTO {
  careItemId: number
  quantity: number
  expireDate: string
}

// 客户护理级别设置DTO
export interface CustomerCareLevelSetDTO {
  customerId: number
  careLevelId: number
}

// 护理记录相关接口（已有的）


// 客户护理级别设置DTO
export interface CustomerCareLevelSetDTO {
  customerId: number
  careLevelId: number
  purchaseQuantity: number
  expireDate: string
  careItems: CustomerCareItemPurchaseDTO[]
}

// 客户护理项目购买DTO
export interface CustomerCareItemPurchaseDTO {
  careItemId: number
  quantity: number
  expireDate: string
}

// 客户护理详情DTO
export interface CustomerCareDetailDTO {
  id: number
  customerId: number
  customerName: string
  careLevelId?: number
  careLevelName?: string
  careItems: CustomerCareItemDetailDTO[]
  createTime: string
}

// 客户护理项目详情DTO
export interface CustomerCareItemDetailDTO {
  id: number
  careItemId: number
  careItemName: string
  unitPrice: number  // 修改为unitPrice
  purchaseDate: string
  purchaseQuantity: number
  usedQuantity: number
  remainingQuantity: number
  expireDate: string
  serviceStatus: string  // 修改为serviceStatus
}

// 护理记录接口
export interface CareRecord {
  id: number
  customerId: number
  customerName: string
  careItemId: number
  itemName: string  // 护理项目名称，对应后端的itemName字段
  healthManagerId: number
  healthManagerName: string
  careTime: string
  careQuantity: number
  careContent: string
  remark?: string
}

// 护理记录DTO
export interface CareRecordDTO {
  id?: number
  customerId: number
  careItemId: number
  healthManagerId: number
  careTime: string
  careQuantity: number
  careContent: string
  remark?: string
}

// 护理记录查询参数
export interface CareRecordQueryParams extends BaseQueryParams {
  pageNum?: number
  pageSize?: number
  customerName?: string
  careType?: string
  startTime?: string
  endTime?: string
}

// 护理管理API
export const careApi = {
  // 护理项目管理
  careItem: {
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
    toggleStatus: (id: number): Promise<void> => {
      return request({
        url: `/api/admin/care-item/${id}/toggle-status`,
        method: 'put'
      })
    }
  },

  // 护理级别管理
  careLevel: {
    list: (params: CareLevelQueryParams): Promise<PageResponse<CareLevel>> => {
      return request({
        url: '/api/admin/care-level/page',
        method: 'get',
        params
      })
    },
    getAll: (): Promise<CareLevel[]> => {
      return request({
        url: '/api/admin/care-level/all',
        method: 'get'
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
      })    },
    // 护理级别项目配置
    getItemConfigs: (careLevelId: number): Promise<CareLevelItemConfig[]> => {
      return request({
        url: `/api/admin/care-level/${careLevelId}/items`,
        method: 'get'
      })
    },
    configItems: (data: CareLevelItemConfigDTO): Promise<void> => {
      return request({
        url: '/api/admin/care-level/config-items',
        method: 'post',
        data
      })
    },
    // 移除护理级别中的护理项目
    removeItem: (careLevelId: number, careItemId: number): Promise<void> => {
      return request({
        url: `/api/admin/care-level/${careLevelId}/items/${careItemId}`,
        method: 'delete'
      })
    },
    // 批量添加护理项目到护理级别
    addItems: (data: CareLevelItemConfigDTO): Promise<void> => {
      return request({
        url: '/api/admin/care-level/config-items',
        method: 'post',
        data
      })
    }
  },
  // 客户护理设置管理
  customerCare: {
    // 分页查询客户护理设置
    list: (params: CustomerCareQueryParams): Promise<PageResponse<CustomerCarePageDTO>> => {
      return request({
        url: '/api/admin/customer-care/page',
        method: 'get',
        params
      })
    },
    // 获取客户护理详情
    getCustomerDetail: (customerId: number): Promise<CustomerCareDetailDTO> => {
      return request({
        url: `/api/admin/customer-care/customer/${customerId}`,
        method: 'get'
      })
    },
    // 设置客户护理级别
    setCareLevel: (data: CustomerCareLevelSetDTO): Promise<void> => {
      return request({
        url: '/api/admin/customer-care/set-level',
        method: 'post',
        data
      })
    },
    // 移除客户护理级别
    removeCareLevel: (customerId: number): Promise<void> => {
      return request({
        url: `/api/admin/customer-care/remove-level/${customerId}`,
        method: 'delete'
      })
    },
    // 批量购买护理项目
    purchaseItems: (customerId: number, items: CustomerCareItemPurchaseDTO[]): Promise<void> => {
      return request({
        url: `/api/admin/customer-care/purchase/${customerId}`,
        method: 'post',
        data: items
      })
    },
    // 获取客户护理服务列表
    getCustomerServices: (customerId: number): Promise<CustomerCareDTO[]> => {
      return request({
        url: `/api/admin/customer-care/services/${customerId}`,
        method: 'get'
      })
    },
    // 获取客户可购买的护理项目
    getAvailableItems: (customerId: number, itemName?: string): Promise<CustomerCareDTO[]> => {
      return request({
        url: `/api/admin/customer-care/available/${customerId}`,
        method: 'get',
        params: { itemName }
      })
    },
    // 护理服务续费
    renewService: (customerCareId: number, additionalQuantity: number, newExpireDate: string): Promise<void> => {
      return request({
        url: `/api/admin/customer-care/renew/${customerCareId}`,
        method: 'post',
        params: { additionalQuantity, newExpireDate }
      })
    },
    // 移除护理服务
    removeService: (customerCareId: number): Promise<void> => {
      return request({
        url: `/api/admin/customer-care/${customerCareId}`,
        method: 'delete'
      })
    }
  },

  // 护理记录管理
  careRecord: {
    // 分页查询护理记录
    list: (params: CareRecordQueryParams): Promise<PageResponse<CareRecord>> => {
      return request({
        url: '/api/admin/care-record/page',
        method: 'get',
        params
      })
    },
    // 获取护理记录详情
    getById: (id: number): Promise<CareRecord> => {
      return request({
        url: `/api/admin/care-record/detail/${id}`,
        method: 'get'
      })
    },
    // 创建护理记录
    create: (data: CareRecordDTO): Promise<CareRecord> => {
      return request({
        url: '/api/admin/care-record',
        method: 'post',
        data
      })
    },
    // 更新护理记录
    update: (data: CareRecordDTO): Promise<CareRecord> => {
      return request({
        url: '/api/admin/care-record',
        method: 'put',
        data
      })
    },
    // 删除护理记录
    delete: (id: number): Promise<void> => {
      return request({
        url: `/api/admin/care-record/${id}`,
        method: 'delete'
      })
    }
  }
}

export default careApi
