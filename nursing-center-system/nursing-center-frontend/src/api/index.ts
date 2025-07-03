// API模块统一导出文件

// 认证相关
export * from './auth'

// 客户管理
export * from './customer'

// 建筑管理
export { buildingApi } from './building'

// 房间管理
export { roomApi } from './room'

// 床位管理
export { bedApi } from './bed'

// 护理管理 - 明确导出以避免命名冲突
export { 
  careApi,
  type CareRecord,
  type CareRecordDTO,
  type DailyCareDTO,
  type CarePlan,
  type CarePlanDTO,
  type CareQueryParams,
  type CustomerCareConfig,
  type CustomerCareConfigDTO,
  type CareLevelQueryParams,
  type CareItemQueryParams,
  type CustomerCareQueryParams
} from './care'

// 从care模块导出护理相关类型（避免与system模块冲突）
export {
  type CareLevel,
  type CareLevelDTO,
  type CareItem,
  type CareItemDTO
} from './care'

// 膳食管理
export * from './meal'

// 系统管理 - 明确导出以避免命名冲突
export {
  systemApi,
  type User,
  type UserDTO,
  type UserQueryDTO,
  type PasswordResetDTO,
  type Role,
  type RoleDTO,
  type QueryParams
} from './system'

// 申请管理
export * from './apply'

// 仪表板
export * from './dashboard'