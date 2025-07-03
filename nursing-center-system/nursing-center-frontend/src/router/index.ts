import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Layout from '@/components/Layout.vue'
import HealthManagerLayout from '@/components/HealthManagerLayout.vue'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresAuth: false }
    },
    // 管理员路由
    {
      path: '/',
      component: Layout,
      meta: { requiresAuth: true, roles: ['ADMIN'] },
      children: [
        {
          path: '',
          name: 'home',
          component: HomeView
        },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue')
        },
        {
          path: '/customer',
          name: 'customer',
          children: [
            {
              path: 'list',
              name: 'customer-list',
              component: () => import('@/views/customer/CustomerList.vue')
            },
            {
              path: 'checkin',
              name: 'customer-checkin',
              component: () => import('@/views/customer/CheckInRegistration.vue')
            },
            {
              path: 'checkout',
              name: 'customer-checkout',
              component: () => import('@/views/apply/ApplyManagement.vue')
            },
            {
              path: 'outing',
              name: 'customer-outing',
              component: () => import('@/views/apply/ApplyManagement.vue')
            },
            {
              path: 'return',
              name: 'customer-return',
              component: () => import('@/views/apply/ReturnApplyManagement.vue')
            },
            {
              path: 'add',
              name: 'customer-add',
              component: () => import('@/views/customer/CustomerAdd.vue')
            },
            {
              path: 'detail/:id',
              name: 'customer-detail',
              component: () => import('@/views/customer/CustomerDetail.vue')
            },
            {
              path: 'edit/:id',
              name: 'customer-edit',
              component: () => import('@/views/customer/CustomerEdit.vue')
            },
            {
              path: 'care',
              name: 'customer-care',
              component: () => import('@/views/care/CareManagement.vue')
            }
          ]
        },
        {
          path: '/bed',
          name: 'bed',
          children: [
            {
              path: 'diagram',
              name: 'bed-diagram',
              component: () => import('@/views/bed/BedDiagram.vue')
            },
            {
              path: 'usage',
              name: 'bed-usage',
              component: () => import('@/views/bed/BedUsageManagement.vue')
            }
          ]
        },
        {
          path: '/nursing',
          name: 'nursing',
          children: [
            {
              path: 'level',
              name: 'nursing-level',
              component: () => import('@/views/nursing/CareLevel.vue')
            },
            {
              path: 'item',
              name: 'nursing-item',
              component: () => import('@/views/nursing/CareItem.vue')
            },
            {
              path: 'customer-care',
              name: 'nursing-customer-care',
              component: () => import('@/views/nursing/CustomerCare.vue')
            },
            {
              path: 'record',
              name: 'nursing-record',
              component: () => import('@/views/nursing/CareRecord.vue')
            }
          ]
        },
        {
          path: '/health-manager',
          name: 'health-manager',
          children: [
            {
              path: '',
              redirect: '/health-manager/assign'
            },
            {
              path: 'assign',
              name: 'health-manager-assign',
              component: () => import('@/views/healthManager/ServiceAssignment.vue')
            },
            {
              path: 'monitor',
              name: 'health-manager-monitor',
              component: () => import('@/views/healthManager/ServiceMonitor.vue')
            }
          ]
        },
        {
          path: '/meal',
          name: 'meal',
          children: [
            {
              path: 'config',
              name: 'meal-config',
              component: () => import('@/views/meal/ConfigView.vue')
            },
            {
              path: 'calendar',
              name: 'meal-calendar',
              component: () => import('@/views/meal/CalendarView.vue')
            },
            {
              path: 'ai-recommendation',
              name: 'meal-ai-recommendation',
              component: () => import('@/views/meal/AiRecommendationV4View.vue')
            }
          ]
        },
        {
          path: '/system',
          name: 'system',
          children: [
            {
              path: '',
              name: 'system-index',
              component: () => import('@/views/system/SystemIndex.vue')
            },
            {
              path: 'user',
              name: 'system-user',
              component: () => import('@/views/system/UserManagement.vue')
            },
            {
              path: 'role',
              name: 'system-role',
              component: () => import('@/views/system/RoleManagement.vue')
            },
            {
              path: 'care-level',
              name: 'system-care-level',
              component: () => import('@/views/system/CareLevelManagement.vue')
            },
            {
              path: 'care-item',
              name: 'system-care-item',
              component: () => import('@/views/system/CareItemManagement.vue')
            },
            {
              path: 'statistics',
              name: 'system-statistics',
              component: () => import('@/views/system/UserManagement.vue') // 暂时使用用户管理组件
            },
            {
              path: 'settings',
              name: 'system-settings',
              component: () => import('@/views/system/UserManagement.vue') // 暂时使用用户管理组件
            },
            {
              path: 'logs',
              name: 'system-logs',
              component: () => import('@/views/system/UserManagement.vue') // 暂时使用用户管理组件
            }
          ]
        }
      ]
    },
    // 健康管家路由
    {
      path: '/health-manager-portal',
      component: HealthManagerLayout,
      meta: { requiresAuth: true, roles: ['HEALTH_MANAGER'] },
      children: [
        {
          path: '',
          redirect: '/health-manager-portal/daily-care'
        },
        {
          path: 'daily-care',
          name: 'health-manager-daily-care',
          component: () => import('@/views/health-manager/DailyCare.vue')
        },
        {
          path: 'outing-apply',
          name: 'health-manager-outing-apply',
          component: () => import('@/views/health-manager/OutingApply.vue')
        },
        {
          path: 'return-apply',
          name: 'health-manager-return-apply',
          component: () => import('@/views/health-manager/ReturnApply.vue')
        },
        {
          path: 'checkout-apply',
          name: 'health-manager-checkout-apply',
          component: () => import('@/views/health-manager/CheckoutApply.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 如果store中没有用户信息但localStorage中有token，先恢复用户状态
  if (!userStore.userInfo && localStorage.getItem('token')) {
    userStore.initUserInfo()
  }

  if (to.meta.requiresAuth === false) {
    // 不需要认证的页面
    if (to.path === '/login' && userStore.isLoggedIn) {
      // 已登录用户访问登录页，根据角色重定向到对应首页
      const userRole = userStore.userInfo?.role
      if (userRole === 'HEALTH_MANAGER') {
        next('/health-manager-portal/daily-care')
      } else {
        next('/')
      }
    } else {
      next()
    }
  } else {
    // 需要认证的页面
    if (!userStore.isLoggedIn) {
      next('/login')
    } else {
      // 检查角色权限
      const userRole = userStore.userInfo?.role
      const requiredRoles = to.meta.roles as string[]

      if (requiredRoles && userRole && !requiredRoles.includes(userRole)) {
        // 用户角色不匹配，重定向到对应的首页
        if (userRole === 'HEALTH_MANAGER') {
          next('/health-manager-portal/daily-care')
        } else {
          next('/')
        }
      } else {
        // 访问根路径时，根据角色重定向
        if (to.path === '/' && userRole === 'HEALTH_MANAGER') {
          next('/health-manager-portal/daily-care')
        } else {
          next()
        }
      }
    }
  }
})

export default router
