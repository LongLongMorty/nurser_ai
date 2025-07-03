import request from '@/utils/request'

// 膳食日历API
export interface MealCalendarDTO {
  id?: number
  weekName: string
  dayOfWeek: number
  dayOfWeekName?: string
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

export interface MealCalendarQueryDTO {
  weekName?: string
  dayOfWeek?: number
  isActive?: number
  creatorId?: number
  creatorName?: string
}

/**
 * 分页查询膳食日历
 */
export function getMealCalendarPage(params: {
  page: number
  size: number
  query?: MealCalendarQueryDTO
}) {
  return request({
    url: '/meal-calendar/page',
    method: 'get',
    params
  })
}

/**
 * 获取完整的一周菜单
 */
export function getWeekMenu(weekName: string) {
  return request({
    url: '/meal-calendar/week-menu',
    method: 'get',
    params: { weekName }
  })
}

/**
 * 获取指定星期几的菜单
 */
export function getByWeekAndDay(weekName: string, dayOfWeek: number) {
  return request({
    url: '/meal-calendar/week-day',
    method: 'get',
    params: { weekName, dayOfWeek }
  })
}

/**
 * 获取所有周菜单名称
 */
export function getAllWeekNames() {
  return request({
    url: '/meal-calendar/week-names',
    method: 'get'
  })
}

/**
 * 获取当前启用的周菜单
 */
export function getCurrentActiveWeekMenu() {
  return request({
    url: '/api/meal-calendar/current-active',
    method: 'get'
  })
}

/**
 * 创建新的周菜单
 */
export function createWeekMenu(weekName: string) {
  return request({
    url: '/meal-calendar/create-week',
    method: 'post',
    data: { weekName }
  })
}

/**
 * 更新某一天的膳食配置
 */
export function updateDayMeal(data: MealCalendarDTO) {
  return request({
    url: '/meal-calendar/update-day',
    method: 'put',
    data
  })
}

/**
 * 批量更新一周菜单
 */
export function batchUpdateWeekMeal(weekName: string, data: MealCalendarDTO[]) {
  return request({
    url: '/meal-calendar/batch-update',
    method: 'put',
    data: {
      weekName,
      updates: data
    }
  })
}
  return request({
    url: `/api/meal-calendar/week-menu/${weekName}/batch`,
    method: 'put',
    data
  })
}

/**
 * 复制周菜单
 */
export function copyWeekMenu(sourceWeekName: string, targetWeekName: string) {
  return request({
    url: '/meal-calendar/copy-week',
    method: 'post',
    data: {
      sourceWeekName,
      targetWeekName
    }
  })
}

/**
 * 删除周菜单
 */
export function deleteWeekMenu(weekName: string) {
  return request({
    url: '/meal-calendar/delete-week',
    method: 'delete',
    params: { weekName }
  })
}

/**
 * 根据ID获取详情
 */
export function getMealCalendarById(id: number) {
  return request({
    url: `/meal-calendar/${id}`,
    method: 'get'
  })
}
