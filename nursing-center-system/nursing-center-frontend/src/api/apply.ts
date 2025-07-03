import request from '@/utils/request'
import type { PageResponse, BaseQueryParams, PageData } from './types'

// 退住申请接口
export interface CheckoutApply {
  id: number
  customerId: number
  customerName: string
  applicantId: number
  applicantName: string
  checkoutType: string
  checkoutReason: string
  checkoutDate: string
  applyStatus: string
  approverName?: string
  approveTime?: string
  approveRemark?: string
  createTime: string
  updateTime?: string
}

// 外出申请接口
export interface OutingApply {
  id: number
  customerId: number
  customerName: string
  applicantId: number
  applicantName: string
  outingReason: string
  outingDate: string
  expectedReturnDate: string
  actualReturnDate?: string
  applyStatus: string
  approverName?: string
  approveTime?: string
  approveRemark?: string
  createTime: string
  updateTime?: string
  actualOutTime?: string
  actualReturnTime?: string
  approver?: string
  approveRemarks?: string
  bedInfo?: string // 床位信息格式：楼栋名-房间号-床位号
  // 计算属性，用于兼容前端组件
  roomNo?: string
  bedNo?: string
}

// 回院申请接口
export interface ReturnApply {
  id: number
  customerId: number
  customerName: string
  outingApplyId: number
  applicantId: number
  applicantName: string
  returnReason: string
  requestedReturnDate: string
  actualReturnDate?: string
  applyStatus: string
  applyStatusDesc: string
  approverId?: number
  approverName?: string
  approveTime?: string
  approveRemark?: string
  createTime: string
  updateTime?: string
  outingDate: string
  expectedReturnDate: string
  bedNo?: string
  roomNo?: string
}

// 申请搜索参数
export interface ApplySearchParams extends BaseQueryParams {
  customerName?: string
  applyStatus?: string
  startDate?: string
  endDate?: string
  applicantId?: number
}

// 审批请求参数
export interface ApprovalRequest {
  id: number
  approveResult: string
  approveRemark: string
  // 退住申请特有字段
  bedAction?: string
  reserveEndDate?: string
  settleAccounts?: boolean
  // 外出申请特有字段
  bedStatusAction?: string
  enableReminder?: boolean
  reminderTime?: string
}

// 退住申请DTO
export interface CheckoutApplyDTO {
  customerId: number
  applicantId?: number
  checkoutType: string
  checkoutReason: string
  checkoutDate: string
}

// 退住申请查询DTO
export interface CheckoutApplyQueryDTO extends BaseQueryParams {
  customerName?: string
  applicantId?: number
  status?: string
  startDate?: string
  endDate?: string
}

// 退住申请审批DTO
export interface CheckoutApproveDTO {
  applyId: number
  applyStatus: string // 应该是 'APPROVED' 或 'REJECTED'
  approveRemark?: string
}

// 外出申请DTO
export interface OutingApplyDTO {
  customerId: number
  outingReason: string
  outingDate: string
  expectedReturnDate: string
}

// 外出申请查询DTO
export interface OutingApplyQueryDTO extends BaseQueryParams {
  customerName?: string
  applicantName?: string
  status?: string
  startDate?: string
  endDate?: string
  applicantId?: number
}

// 外出申请审批DTO
export interface OutingApproveDTO {
  applyId: number
  applyStatus: string // 应该是 'APPROVED' 或 'REJECTED'
  approveRemark?: string
}

// 外出回院登记DTO
export interface OutingReturnDTO {
  applyId: number
  actualReturnTime: string
  returnRemarks?: string
}

// 回院申请DTO
export interface ReturnApplyDTO {
  customerId: number
  outingApplyId: number
  applicantId?: number
  returnReason: string
  requestedReturnDate: string
  remarks?: string
}

// 回院申请查询DTO
export interface ReturnApplyQueryDTO extends BaseQueryParams {
  customerName?: string
  applicantId?: number
  applyStatus?: string
  startDate?: string
  endDate?: string
}

// 回院申请审批DTO
export interface ReturnApproveDTO {
  applyId: number
  applyStatus: string // 应该是 'APPROVED' 或 'REJECTED'
  approveRemark?: string
}

// === 退住申请管理（管理员视角） ===

// 分页查询退住申请
export const getCheckoutApplyPage = (query: CheckoutApplyQueryDTO): Promise<PageData<CheckoutApply>> => {
  return request({
    url: '/api/admin/checkout-apply/page',
    method: 'get',
    params: query
  })
}

// 根据ID获取退住申请详情
export const getCheckoutApplyById = (id: number) => {
  return request({
    url: `/api/admin/checkout-apply/${id}`,
    method: 'get'
  })
}

// 审批退住申请
export const approveCheckoutApply = (data: CheckoutApproveDTO) => {
  return request({
    url: '/api/admin/checkout-apply/approve',
    method: 'post',
    data
  })
}

// 删除退住申请
export const deleteCheckoutApply = (id: number) => {
  return request({
    url: `/api/admin/checkout-apply/${id}`,
    method: 'delete'
  })
}

// === 外出申请管理（管理员视角） ===

// 分页查询外出申请
export const getOutingApplyPage = (query: OutingApplyQueryDTO): Promise<PageData<OutingApply>> => {
  return request({
    url: '/api/admin/outing-apply/page',
    method: 'get',
    params: query
  })
}

// 根据ID获取外出申请详情
export const getOutingApplyById = (id: number) => {
  return request({
    url: `/api/admin/outing-apply/${id}`,
    method: 'get'
  })
}

// 审批外出申请
export const approveOutingApply = (data: OutingApproveDTO) => {
  return request({
    url: '/api/admin/outing-apply/approve',
    method: 'post',
    data
  })
}

// 回院登记（管理员视角）
export const registerReturnForAdmin = (data: OutingReturnDTO) => {
  return request({
    url: '/api/admin/outing-apply/return',
    method: 'post',
    data
  })
}

// 删除外出申请
export const deleteOutingApply = (id: number) => {
  return request({
    url: `/api/admin/outing-apply/${id}`,
    method: 'delete'
  })
}

// ===== 回院申请 API =====

// 提交回院申请
export const submitReturnApply = (data: ReturnApplyDTO): Promise<number> => {
  return request({
    url: '/api/health-manager-portal/return-apply',
    method: 'post',
    data
  })
}

// 查询我的回院申请列表
export const getMyReturnApplies = (params: ReturnApplyQueryDTO): Promise<PageResponse<ReturnApply>> => {
  return request({
    url: '/api/health-manager/apply/return/my',
    method: 'get',
    params
  })
}

// 检查客户是否可以申请回院
export const canApplyReturn = (customerId: number): Promise<boolean> => {
  return request({
    url: `/api/health-manager/apply/return/can-apply/${customerId}`,
    method: 'get'
  })
}

// 管理员查询回院申请列表
export const getReturnApplyPage = (params: ReturnApplyQueryDTO): Promise<PageResponse<ReturnApply>> => {
  return request({
    url: '/api/admin/return-apply/page',
    method: 'get',
    params
  })
}

// 查询回院申请详情
export const getReturnApplyDetail = (id: number): Promise<ReturnApply> => {
  return request({
    url: `/api/admin/return-apply/${id}`,
    method: 'get'
  })
}

// 审批回院申请
export const approveReturnApply = (data: ReturnApproveDTO): Promise<void> => {
  return request({
    url: '/api/admin/return-apply/approve',
    method: 'post',
    data
  })
}

// 管理员检查客户是否可以申请回院
export const adminCanApplyReturn = (customerId: number): Promise<boolean> => {
  return request({
    url: `/api/admin/return-apply/check-can-apply/${customerId}`,
    method: 'get'
  })
}

// === 健康管家申请相关接口 ===

// 提交退住申请（健康管家）
export const submitCheckoutApply = (data: CheckoutApplyDTO) => {
  return request({
    url: '/api/health-manager-portal/checkout-apply',
    method: 'post',
    data
  })
}

// 获取我的退住申请（健康管家）
export const getMyCheckoutApplies = (query: CheckoutApplyQueryDTO) => {
  return request({
    url: '/api/health-manager/apply/checkout/my',
    method: 'get',
    params: query
  })
}

// 提交外出申请（健康管家）
export const submitOutingApply = (data: OutingApplyDTO) => {
  return request({
    url: '/api/health-manager/apply/outing',
    method: 'post',
    data
  })
}

// 获取我的外出申请（健康管家）
export const getMyOutingApplies = (query: OutingApplyQueryDTO) => {
  return request({
    url: '/api/health-manager/apply/outing/my',
    method: 'get',
    params: query
  })
}

// 回院登记（健康管家）
export const registerReturn = (data: OutingReturnDTO) => {
  return request({
    url: '/api/health-manager/apply/outing/return',
    method: 'post',
    data
  })
}

// 申请管理API对象
export const applyApi = {
  // 退住申请相关
  getCheckoutList: getCheckoutApplyPage,
  getCheckoutById: getCheckoutApplyById,
  approveCheckout: approveCheckoutApply,
  deleteCheckout: deleteCheckoutApply,
  
  // 外出申请相关  
  getOutingList: getOutingApplyPage,
  getOutingById: getOutingApplyById,
  approveOuting: approveOutingApply,
  deleteOuting: deleteOutingApply,
    // 健康管家申请相关
  submitCheckout: submitCheckoutApply,
  getMyCheckouts: getMyCheckoutApplies,
  submitOuting: submitOutingApply,
  getMyOutings: getMyOutingApplies,
  registerReturn: registerReturn,
  
  // 回院申请相关
  submitReturn: submitReturnApply,
  getMyReturns: getMyReturnApplies,
  canApplyReturn: canApplyReturn,
  
  // 管理员回院申请相关
  getReturnApplyPage: getReturnApplyPage,
  getReturnApplyDetail: getReturnApplyDetail,
  approveReturn: approveReturnApply,
  adminCanApplyReturn: adminCanApplyReturn
}