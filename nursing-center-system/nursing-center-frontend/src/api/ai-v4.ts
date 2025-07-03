import request from '@/utils/request'

// ==================== 类型定义 ====================

// AI推荐临时表实体
export interface AiRecommendationTemp {
  id: number
  customerId?: number
  customerName?: string
  customerAge?: number
  customerGender?: string // MALE-男性，FEMALE-女性
  sessionId: string
  mealName: string
  mealType: string // BREAKFAST-早餐，LUNCH-午餐，DINNER-晚餐，SNACK-加餐
  description?: string
  mainFood?: string
  sideDishes?: string
  soup?: string
  fruits?: string
  ingredients?: string
  cookingMethod?: string
  calories?: number
  protein?: number
  fat?: number
  carbohydrate?: number
  nutritionInfo?: string
  suitableCrowd?: string
  targetGroup?: string // ELDERLY-老年人，DIABETIC-糖尿病，HYPERTENSION-高血压等
  dietaryRestrictions?: string
  estimatedCost?: number
  aiScore?: number
  aiReason?: string
  aiModel?: string
  nutritionType?: string // HIGH_PROTEIN-高蛋白，LOW_SODIUM-低钠等
  additionalRequirements?: string
  isFromHistory?: number // 0-否，1-是
  historyRefId?: number
  userId?: number
  userAge?: number
  userGender?: string
  createTime: string
}

// 个性化菜单实体
export interface AiPersonalizedMenu {
  id: number
  customerId: number
  customerName: string
  customerAge?: number
  customerGender?: string
  menuDate: string
  sessionId: string
  healthConditions?: string
  dietaryPreferences?: string
  allergies?: string

  // 早餐
  breakfastName?: string
  breakfastDescription?: string
  breakfastIngredients?: string
  breakfastCookingMethod?: string
  breakfastCalories?: number
  breakfastNutrition?: string

  // 午餐
  lunchName?: string
  lunchDescription?: string
  lunchIngredients?: string
  lunchCookingMethod?: string
  lunchCalories?: number
  lunchNutrition?: string

  // 晚餐
  dinnerName?: string
  dinnerDescription?: string
  dinnerIngredients?: string
  dinnerCookingMethod?: string
  dinnerCalories?: number
  dinnerNutrition?: string

  // 加餐
  snackName?: string
  snackDescription?: string
  snackIngredients?: string
  snackCookingMethod?: string
  snackCalories?: number
  snackNutrition?: string

  // 总计信息
  totalCalories?: number
  totalCost?: number
  aiRecommendationReason?: string
  aiRawResponse?: string
  nutritionistReview?: string

  isApproved?: number // 0-待审核，1-已审核
  approvedBy?: number
  approvedTime?: string

  createdByUserId: number
  createdByUserName?: string
  createTime: string
  updateTime?: string
}

// AI推荐历史
export interface AiRecommendationHistory {
  id: number
  mealConfigId: number
  originalAiContent?: string
  mealName: string
  mealType: string
  targetGroup?: string
  nutritionType?: string
  aiScore?: number
  userRating?: number
  adoptionDate: string
  adoptedByUserId: number
  adoptedByUserName?: string
  effectivenessScore?: number
  isRecommendedAgain?: number
  createTime: string
  updateTime?: string
}

// AI调用统计
export interface AiCallStatistics {
  totalCalls: number
  successCalls: number
  errorCalls: number
  successRate: number
  avgDuration: number
  totalTokens: number
  totalCost: number
  dailyStats: Array<{
    date: string
    callCount: number
    apiType: string
  }>
}

// ==================== 请求参数类型 ====================

// 通用推荐生成请求
export interface GenerateRecommendationRequest {
  mealType: string // 餐食类型
  targetGroup: string // 目标群体
  nutritionType: string // 营养类型
  additionalRequirements?: string // 附加要求
  caloriesRange?: number[] // 热量范围 [最小值, 最大值]
  count?: number // 生成数量，默认5个
}

// 个性化推荐生成请求
export interface PersonalizedRecommendationRequest {
  customerId: number
  customerName?: string
  customerAge?: number
  customerGender?: string
  mealType?: string
  targetGroup?: string
  nutritionType?: string
  healthConditions?: string
  dietaryPreferences?: string
  allergies?: string
  count?: number // 默认生成4个（四餐）
}

// 个性化菜单生成请求
export interface GeneratePersonalizedMenuRequest {
  customerId: number
  customerName?: string
  customerAge?: number
  customerGender?: string
  menuDate: string
  healthConditions?: string
  dietaryPreferences?: string
  allergies?: string
}

// 批量采纳请求
export interface BatchAdoptRequest {
  recommendationIds: number[]
}

// 菜单审核请求
export interface ApproveMenuRequest {
  review?: string // 审核意见
}

// ==================== API接口方法 ====================

const API_BASE = '/api/ai-recommendation'

// 生成通用AI推荐
export function generateRecommendations(data: GenerateRecommendationRequest) {
  return request<AiRecommendationTemp[]>({
    url: `${API_BASE}/generate`,
    method: 'post',
    data
  })
}

// 生成个性化AI推荐
export function generatePersonalizedRecommendations(data: PersonalizedRecommendationRequest) {
  return request<AiRecommendationTemp[]>({
    url: `${API_BASE}/personalized`,
    method: 'post',
    data
  })
}

// 生成客户一日四餐个性化菜单
export function generatePersonalizedMenu(data: GeneratePersonalizedMenuRequest) {
  return request<AiPersonalizedMenu>({
    url: `${API_BASE}/personalized-menu/generate`,
    method: 'post',
    data
  })
}

// 获取当前推荐列表
export function getCurrentRecommendations(sessionId?: string) {
  return request<AiRecommendationTemp[]>({
    url: `${API_BASE}/current`,
    method: 'get',
    params: { sessionId }
  })
}

// 采纳推荐到meal_config表
export function adoptRecommendation(recommendationId: number) {
  return request<number>({
    url: `${API_BASE}/adopt/${recommendationId}`,
    method: 'post'
  })
}

// 批量采纳推荐
export function batchAdoptRecommendations(data: BatchAdoptRequest) {
  return request<number[]>({
    url: `${API_BASE}/adopt/batch`,
    method: 'post',
    data
  })
}

// 清空当前推荐列表
export function clearCurrentRecommendations(sessionId?: string) {
  return request<void>({
    url: `${API_BASE}/clear`,
    method: 'delete',
    params: { sessionId }
  })
}

// 获取客户的个性化菜单历史
export function getCustomerPersonalizedMenus(
  customerId: number,
  startDate?: string,
  endDate?: string
) {
  return request<AiPersonalizedMenu[]>({
    url: `${API_BASE}/personalized-menu/customer/${customerId}`,
    method: 'get',
    params: { startDate, endDate }
  })
}

// 审核个性化菜单
export function approvePersonalizedMenu(menuId: number, data: ApproveMenuRequest) {
  return request<void>({
    url: `${API_BASE}/personalized-menu/${menuId}/approve`,
    method: 'post',
    data
  })
}

// 获取AI调用统计信息
export function getAiCallStatistics(startDate?: string, endDate?: string) {
  return request<AiCallStatistics>({
    url: `${API_BASE}/statistics`,
    method: 'get',
    params: { startDate, endDate }
  })
}

// 获取历史推荐记录
export function getHistoryRecommendations(
  customerId?: number,
  startDate?: string,
  endDate?: string
) {
  return request<AiRecommendationHistory[]>({
    url: `${API_BASE}/history`,
    method: 'get',
    params: { customerId, startDate, endDate }
  })
}

// 基于历史推荐生成类似菜单
export function generateFromHistory(historyId: number) {
  return request<AiRecommendationTemp[]>({
    url: `${API_BASE}/generate-from-history/${historyId}`,
    method: 'post'
  })
}

// 基于历史推荐生成个性化菜单
export function generateMenuFromHistory(historyId: number, menuDate: string) {
  return request<AiPersonalizedMenu>({
    url: `${API_BASE}/personalized-menu/generate-from-history/${historyId}`,
    method: 'post',
    data: { menuDate }
  })
}

// 基于最新历史记录生成类似推荐
export function generateFromLatestHistory(customerId?: number) {
  return request<AiRecommendationTemp[]>({
    url: `${API_BASE}/generate-from-latest-history`,
    method: 'post',
    params: { customerId }
  })
}

// ==================== 辅助常量 ====================

// 餐食类型选项
export const MEAL_TYPE_OPTIONS = [
  { label: '早餐', value: 'BREAKFAST' },
  { label: '午餐', value: 'LUNCH' },
  { label: '晚餐', value: 'DINNER' },
  { label: '加餐', value: 'SNACK' }
]

// 目标群体选项
export const TARGET_GROUP_OPTIONS = [
  { label: '糖尿病患者', value: 'DIABETIC' },
  { label: '高血压患者', value: 'HYPERTENSION' },
  { label: '心脏病患者', value: 'HEART_DISEASE' },
  { label: '肾病患者', value: 'KIDNEY_DISEASE' },
  { label: '一般人群', value: 'GENERAL' }
]

// 营养类型选项
export const NUTRITION_TYPE_OPTIONS = [
  { label: '高蛋白', value: 'HIGH_PROTEIN' },
  { label: '低钠', value: 'LOW_SODIUM' },
  { label: '低糖', value: 'LOW_SUGAR' },
  { label: '低脂', value: 'LOW_FAT' },
  { label: '高纤维', value: 'HIGH_FIBER' },
  { label: '易消化', value: 'DIGESTIBLE' },
  { label: '营养均衡', value: 'BALANCED' }
]

// 性别选项
export const GENDER_OPTIONS = [
  { label: '男性', value: 'MALE' },
  { label: '女性', value: 'FEMALE' }
]

// 审核状态选项
export const APPROVAL_STATUS_OPTIONS = [
  { label: '待审核', value: 0 },
  { label: '已审核', value: 1 }
]
