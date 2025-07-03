import request from '@/utils/request'

// 膳食配置相关API
export interface MealConfig {
  id?: number
  mealName: string
  mealType: string
  description?: string
  mainFood?: string
  sideDishes?: string
  soup?: string
  fruits?: string
  nutritionInfo?: string
  suitableCrowd?: string
  dietaryRestrictions?: string
  cookingMethod?: string
  estimatedCost?: number
  creatorId?: number
  creatorName?: string
  status: number
  createTime?: string
  updateTime?: string
}

export interface MealConfigQuery {
  mealName?: string
  mealType?: string
  status?: number
  page?: number
  size?: number
}

// 膳食日历相关API（周菜单结构）
export interface MealCalendar {
  id?: number
  weekName: string
  dayOfWeek: number // 1-7 (周一到周日)
  breakfastConfigId?: number
  breakfastName?: string
  lunchConfigId?: number
  lunchName?: string
  dinnerConfigId?: number
  dinnerName?: string
  snackConfigId?: number
  snackName?: string
  isActive?: number
  specialNotes?: string
  totalEstimatedCost?: number
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

export interface MealCalendarQuery {
  weekName?: string
  dayOfWeek?: number
  isActive?: number
}

// AI食谱推荐相关API
export interface AiRecommendation {
  id?: number
  dishName: string
  dishType: string
  ingredients: string
  calories: number
  protein: number
  fat: number
  carbohydrate: number
  targetGroup: string
  aiScore: number
  status: string
  cookingMethod?: string
  nutritionAnalysis?: string
  aiReason?: string
  createTime?: string
}

export interface AiRecommendationQuery {
  dishType?: string
  nutritionType?: string
  targetGroup?: string
  status?: string
  page?: number
  size?: number
}

export interface FeedbackForm {
  recommendationId: number
  rating: number
  content: string
}

// 膳食配置API
export const mealConfigApi = {
  // 获取配置列表
  getList: (params: MealConfigQuery) => {
    return request({
      url: '/api/admin/meal-config/page',
      method: 'get',
      params
    })
  },

  // 获取配置详情
  getDetail: (id: number) => {
    return request({
      url: `/api/admin/meal-config/${id}`,
      method: 'get'
    })
  },

  // 新增配置
  create: (data: MealConfig) => {
    return request({
      url: '/api/admin/meal-config',
      method: 'post',
      data
    })
  },

  // 更新配置
  update: (id: number, data: MealConfig) => {
    return request({
      url: '/api/admin/meal-config',
      method: 'put',
      data: { ...data, id }
    })
  },

  // 删除配置
  delete: (id: number) => {
    return request({
      url: `/api/admin/meal-config/${id}`,
      method: 'delete'
    })
  },

  // 批量删除
  batchDelete: (ids: number[]) => {
    return request({
      url: '/api/admin/meal-config/batch-delete',
      method: 'post',
      data: { ids }
    })
  },

  // 批量切换状态
  batchToggleStatus: (ids: number[]) => {
    return request({
      url: '/api/admin/meal-config/batch-status',
      method: 'post',
      data: { ids, status: 1 }
    })
  }
}

// 膳食日历API
export const mealCalendarApi = {
  // 获取七天菜单（固定循环）
  getWeekMenu: () => {
    return request({
      url: '/api/meal-calendar/week-menu',
      method: 'get'
    })
  },

  // 编辑单天菜单
  updateDayMenu: (data: MealCalendar) => {
    return request({
      url: '/api/meal-calendar/day',
      method: 'put',
      data
    })
  }
}

// AI食谱推荐API
export const aiRecommendationApi = {
  // 获取推荐列表
  getList: (params: AiRecommendationQuery) => {
    return request({
      url: '/api/meal/ai-recommendation/list',
      method: 'get',
      params
    })
  },

  // 获取推荐详情
  getDetail: (id: number) => {
    return request({
      url: `/api/meal/ai-recommendation/${id}`,
      method: 'get'
    })
  },

  // 生成AI推荐
  generate: (params: any) => {
    return request({
      url: '/api/meal/ai-recommendation/generate',
      method: 'post',
      data: params
    })
  },

  // 采纳推荐
  adopt: (id: number) => {
    return request({
      url: `/api/meal/ai-recommendation/${id}/adopt`,
      method: 'put'
    })
  },

  // 拒绝推荐
  reject: (id: number) => {
    return request({
      url: `/api/meal/ai-recommendation/${id}/reject`,
      method: 'put'
    })
  },

  // 批量采纳
  batchAdopt: (ids: number[]) => {
    return request({
      url: '/api/meal/ai-recommendation/batch-adopt',
      method: 'put',
      data: { ids }
    })
  },

  // 批量拒绝
  batchReject: (ids: number[]) => {
    return request({
      url: '/api/meal/ai-recommendation/batch-reject',
      method: 'put',
      data: { ids }
    })
  },

  // 提供反馈
  provideFeedback: (data: FeedbackForm) => {
    return request({
      url: '/api/meal/ai-recommendation/feedback',
      method: 'post',
      data
    })
  },

  // 获取推荐统计
  getStatistics: () => {
    return request({
      url: '/api/meal/ai-recommendation/statistics',
      method: 'get'
    })
  }
}

export default {
  mealConfigApi,
  mealCalendarApi,
  aiRecommendationApi
}
