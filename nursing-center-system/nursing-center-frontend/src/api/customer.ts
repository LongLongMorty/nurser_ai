import request from '@/utils/request'
import type { PageData, Customer, CustomerDTO, CustomerQueryParams } from './types'

// 重新导出类型以解决导入问题
export type { Customer, CustomerDTO, CustomerQueryParams } from './types'

// 获取所有客户（兼容性导出）
export const getCustomers = (params?: CustomerQueryParams): Promise<Customer[]> => {
  return request({
    url: '/admin/customer/page',
    method: 'get',
    params: { ...params, pageNum: 1, pageSize: 1000 }
  }).then((response: any) => response.records || [])
}

// 客户管理API
export const customerApi = {
  // 分页查询客户列表
  getList: (params: CustomerQueryParams): Promise<PageData<Customer>> => {
    return request({
      url: '/api/admin/customer/page',
      method: 'get',
      params
    })
  },
  // 分页查询客户列表（另一个接口名称，用于兼容）
  getCustomerPage: (params: CustomerQueryParams): Promise<{ data: PageData<Customer> }> => {
    return request({
      url: '/api/admin/customer/page',
      method: 'get',
      params
    }).then((response: any) => ({ data: response }))
  },

  // 获取所有客户（用于下拉选择等场景）
  getCustomers: (params?: CustomerQueryParams): Promise<Customer[]> => {
    return request({
      url: '/api/admin/customer/page',
      method: 'get',
      params: { ...params, pageNum: 1, pageSize: 1000 }
    }).then((response: any) => response.records || [])
  },

  // 根据ID获取客户详情
  getById: (id: number): Promise<Customer> => {
    return request({
      url: `/api/admin/customer/${id}`,
      method: 'get'
    })
  },

  // 新增客户
  create: (data: CustomerDTO): Promise<Customer> => {
    return request({
      url: '/api/admin/customer',
      method: 'post',
      data
    })
  },

  // 修改客户信息
  update: (data: CustomerDTO): Promise<Customer> => {
    return request({
      url: '/api/admin/customer',
      method: 'put',
      data
    })
  },

  // 删除客户
  delete: (id: number): Promise<void> => {
    return request({
      url: `/api/admin/customer/${id}`,
      method: 'delete'
    })
  },

  // 入住登记
  checkIn: (id: number, data: {
    buildingId: number
    roomId: number
    bedId: number
    checkInDate: string
    contractExpireDate: string
  }): Promise<void> => {
    return request({
      url: `/api/admin/customer/${id}/checkin`,
      method: 'post',
      data
    })
  },

  // 批量删除客户
  batchDelete: (ids: number[]): Promise<void> => {
    return request({
      url: '/api/admin/customer/batch',
      method: 'delete',
      data: ids
    })
  },

  // 获取客户统计信息
  getStats: (): Promise<{
    total: number
    active: number
    inactive: number
    pending: number
  }> => {
    return request({
      url: '/api/admin/customer/stats',
      method: 'get'
    })
  }
}
