import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, type LoginRequest, type UserInfo, type LoginResponse } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const loading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const hasRole = computed(() => (role: string) => {
    return userInfo.value?.roles?.includes(role) || userInfo.value?.role === role || false
  })

  // 登录
  const login = async (loginData: LoginRequest) => {
    loading.value = true
    try {
      const response = await loginApi(loginData)
      token.value = response.token
      
      // 转换后端数据格式为前端格式
      const user: UserInfo = {
        id: response.userId,
        username: response.username,
        name: response.realName || response.username,
        realName: response.realName,
        role: response.role,
        roles: [response.role] // 转换为数组格式
      }
      
      userInfo.value = user
      
      // 保存到localStorage
      localStorage.setItem('token', response.token)
      localStorage.setItem('userInfo', JSON.stringify(user))
      
      return response
    } catch (error) {
      throw error
    } finally {
      loading.value = false
    }
  }
  // 获取用户信息（从localStorage恢复）
  const fetchUserInfo = async () => {
    if (!token.value) return
    
    // 从localStorage恢复用户信息，因为登录时已经保存了完整信息
    const savedUserInfo = localStorage.getItem('userInfo')
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
        return userInfo.value
      } catch (error) {
        console.error('解析用户信息失败:', error)
        logout()
        throw error
      }
    } else {
      // 如果没有保存的用户信息，说明token无效，清除登录状态
      logout()
      throw new Error('用户信息不存在，请重新登录')
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 初始化用户信息
  const initUserInfo = () => {
    const savedUserInfo = localStorage.getItem('userInfo')
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        logout()
      }
    }
  }

  return {
    token,
    userInfo,
    loading,
    isLoggedIn,
    hasRole,
    login,
    logout,
    fetchUserInfo,
    initUserInfo
  }
})
