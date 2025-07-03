// 通用类型定义

// 分页数据结构
export interface PageData<T> {
  records: T[]
  content: T[]  // 兼容Spring Boot分页结构
  total: number
  totalElements: number  // 兼容Spring Boot分页结构
  size: number
  current: number
  pages: number
}

// 分页响应接口（包装在ApiResponse中）
export interface PageResponse<T> extends ApiResponse<PageData<T>> {}

// 通用查询参数
export interface BaseQueryParams {
  page?: number
  current?: number
  size?: number
  keyword?: string
}

// API响应基础结构
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 选项接口
export interface Option {
  label: string
  value: string | number
}

// 客户接口 - 与后端DTO保持一致
export interface Customer {
  id?: number
  customerName: string
  name?: string  // 兼容属性，等同于customerName
  age?: number
  gender: 'MALE' | 'FEMALE'
  idCard: string
  birthDate?: string
  bloodType?: string
  guardianName?: string
  guardianPhone?: string
  buildingId?: number
  roomId?: number
  bedId?: number
  checkInDate?: string
  contractExpireDate?: string
  careLevelId?: number
  healthManagerId?: number
  customerType?: 'SELF_CARE' | 'CARE'
  status?: number
  // 关联信息（用于展示）
  buildingName?: string
  roomNo?: string
  bedNo?: string
  careLevelName?: string
  healthManagerName?: string
  createdAt?: string
  updatedAt?: string
}

// 客户创建/更新DTO
export interface CustomerDTO {
  id?: number
  customerName: string
  age?: number
  gender: 'MALE' | 'FEMALE'
  idCard: string
  birthDate?: string
  bloodType?: string
  guardianName?: string
  guardianPhone?: string
  buildingId?: number
  roomId?: number
  bedId?: number
  checkInDate?: string
  contractExpireDate?: string
  careLevelId?: number
  healthManagerId?: number
  customerType?: 'SELF_CARE' | 'CARE'
  status?: number
}

// 分页查询参数
export interface CustomerQueryParams {
  page?: number
  size?: number
  customerName?: string
  idCard?: string
  customerType?: 'SELF_CARE' | 'CARE'
  status?: number | string  // 支持字符串和数字类型
}

// 房间接口
export interface Room {
  id: number
  roomNo: string
  floorNo: number
  buildingId: number
  bedCount: number
  availableBeds: number
  status: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE'
  createdAt?: string
  updatedAt?: string
  // 关联信息
  buildingName?: string
  beds?: Bed[]
}

// 楼栋接口
export interface Building {
  id: number
  buildingName: string
  floorCount: number
  status: 'ACTIVE' | 'INACTIVE'
  description?: string
  createdAt?: string
  updatedAt?: string
}

// 床位接口
export interface Bed {
  id: number
  bedNo: string
  roomId: number
  status: 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE'
  createdAt?: string
  updatedAt?: string
  // 关联信息
  roomNo?: string
  buildingName?: string
}

// 系统用户接口
export interface SysUser {
  id: number
  username: string
  realName: string
  role: string
  phone?: string
  status: number // 0-禁用 1-启用
  createTime: string
  updateTime?: string
}

// 系统用户DTO
export interface SysUserDTO {
  id?: number
  username: string
  realName: string
  role: string
  phone?: string
  password?: string
  status?: number
}

// 用户查询参数
export interface UserQueryParams {
  pageNum: number
  pageSize: number
  username?: string
  realName?: string
  phone?: string
  role?: string
  status?: number
}

// 楼层信息（用于分组显示）
export interface FloorInfo {
  floorNo: number
  rooms: Room[]
}
