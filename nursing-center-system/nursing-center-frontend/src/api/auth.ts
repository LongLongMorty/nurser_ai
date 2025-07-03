import request from '@/utils/request'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  userId: number
  username: string
  realName: string
  role: string
  expiresIn: number
}

export interface UserInfo {
  id: number
  username: string
  name: string
  realName: string
  role: string
  roles: string[]
}

// 登录
export const login = (data: LoginRequest): Promise<LoginResponse> => {
  return request<LoginResponse>({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 登出
export const logout = (): Promise<void> => {
  return request<void>({
    url: '/auth/logout',
    method: 'post'
  })
}
