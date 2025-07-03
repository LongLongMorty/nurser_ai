<template>
  <div class="meal-calendar-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><Calendar /></el-icon>
            膳食日历
          </h1>
          <p class="page-description">固定七天菜单循环安排，每天四餐营养搭配</p>
        </div>
        
        <div class="action-section">
          <el-button type="primary" @click="loadWeekMenu" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 统计卡片 -->
      <div class="stats-cards" v-if="weekMenuData.length > 0">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-icon breakfast">
                <el-icon><Coffee /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statsData.breakfast }}</div>
                <div class="stat-label">早餐配置</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-icon lunch">
                <el-icon><Food /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statsData.lunch }}</div>
                <div class="stat-label">午餐配置</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-icon dinner">
                <el-icon><Bowl /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statsData.dinner }}</div>
                <div class="stat-label">晚餐配置</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-card">
              <div class="stat-icon snack">
                <el-icon><IceCreamSquare /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statsData.snack }}</div>
                <div class="stat-label">加餐配置</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 七天菜单表格 -->
      <div class="menu-table-container" v-if="weekMenuData.length > 0">
        <div class="table-header">
          <h3 class="table-title">
            <el-icon><Grid /></el-icon>
            七天菜单安排
          </h3>
          <div class="table-actions">
            <el-tag type="info" size="small">共 {{ weekMenuData.length }} 天</el-tag>
          </div>
        </div>

        <el-table
          :data="weekMenuData"
          border
          stripe
          class="menu-table"
          :header-cell-style="{ 
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 
            color: 'white',
            fontWeight: 'bold',
            textAlign: 'center'
          }"
          :row-style="{ height: '60px' }"
        >
          <el-table-column prop="dayName" label="星期" width="120" align="center">
            <template #default="{ row }">
              <div class="day-cell">
                <el-tag 
                  :type="getDayTagType(row.dayOfWeek)"
                  effect="dark"
                  size="large"
                >
                  {{ row.dayName }}
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="早餐" min-width="180">
            <template #default="{ row }">
              <div class="meal-cell breakfast-cell">
                <div class="meal-info">
                  <el-icon class="meal-icon"><Coffee /></el-icon>
                  <span class="meal-name">{{ row.breakfastName || '未设置' }}</span>
                </div>
                <el-button
                  type="primary"
                  size="small"
                  link
                  @click="editMeal(row, 'breakfast')"
                  class="edit-btn"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="午餐" min-width="180">
            <template #default="{ row }">
              <div class="meal-cell lunch-cell">
                <div class="meal-info">
                  <el-icon class="meal-icon"><Food /></el-icon>
                  <span class="meal-name">{{ row.lunchName || '未设置' }}</span>
                </div>
                <el-button
                  type="primary"
                  size="small"
                  link
                  @click="editMeal(row, 'lunch')"
                  class="edit-btn"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="晚餐" min-width="180">
            <template #default="{ row }">
              <div class="meal-cell dinner-cell">
                <div class="meal-info">
                  <el-icon class="meal-icon"><Bowl /></el-icon>
                  <span class="meal-name">{{ row.dinnerName || '未设置' }}</span>
                </div>
                <el-button
                  type="primary"
                  size="small"
                  link
                  @click="editMeal(row, 'dinner')"
                  class="edit-btn"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="加餐" min-width="180">
            <template #default="{ row }">
              <div class="meal-cell snack-cell">
                <div class="meal-info">
                  <el-icon class="meal-icon"><IceCreamSquare /></el-icon>
                  <span class="meal-name">{{ row.snackName || '未设置' }}</span>
                </div>
                <el-button
                  type="primary"
                  size="small"
                  link
                  @click="editMeal(row, 'snack')"
                  class="edit-btn"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="备注" min-width="150">
            <template #default="{ row }">
              <div class="notes-cell">
                <el-text 
                  class="notes-text" 
                  :type="row.specialNotes ? 'primary' : 'info'"
                  truncated
                >
                  {{ row.specialNotes || '无特殊说明' }}
                </el-text>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  type="primary"
                  size="small"
                  @click="editDayMenu(row)"
                  class="action-btn"
                >
                  <el-icon><Edit /></el-icon>
                  编辑整天
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty 
          description="暂无菜单数据" 
          :image-size="120"
        >
          <el-button type="primary" @click="loadWeekMenu">
            <el-icon><Refresh /></el-icon>
            重新加载
          </el-button>
        </el-empty>
      </div>
    </div>

    <!-- 编辑单餐对话框 -->
    <el-dialog
      v-model="showMealEditDialog"
      :title="`编辑${currentEditRow?.dayName} - ${getMealTypeName(currentMealType)}`"
      width="800px"
    >
      <div class="meal-edit-content">
        <!-- 当前菜品信息 -->
        <div class="current-meal-section" v-if="currentMealConfig">
          <h4 class="section-title">
            <el-icon><Star /></el-icon>
            当前菜品
          </h4>
          <div class="meal-config-card current">
            <div class="meal-card-header">
              <span class="meal-name">{{ currentMealConfig.mealName }}</span>
              <el-tag :type="getMealTypeColor(currentMealConfig.mealType)" size="small">
                {{ getMealTypeLabel(currentMealConfig.mealType) }}
              </el-tag>
            </div>
            <div class="meal-card-content">
              <div class="meal-details">
                <div class="detail-item" v-if="currentMealConfig.description">
                  <span class="detail-label">描述：</span>
                  <span class="detail-value">{{ currentMealConfig.description }}</span>
                </div>
                <div class="detail-item" v-if="currentMealConfig.mainFood">
                  <span class="detail-label">主食：</span>
                  <span class="detail-value">{{ formatJsonField(currentMealConfig.mainFood) }}</span>
                </div>
                <div class="detail-item" v-if="currentMealConfig.sideDishes">
                  <span class="detail-label">菜品：</span>
                  <span class="detail-value">{{ formatJsonField(currentMealConfig.sideDishes) }}</span>
                </div>
                <div class="detail-item" v-if="currentMealConfig.soup">
                  <span class="detail-label">汤品：</span>
                  <span class="detail-value">{{ currentMealConfig.soup }}</span>
                </div>
                <div class="detail-item" v-if="currentMealConfig.fruits">
                  <span class="detail-label">水果：</span>
                  <span class="detail-value">{{ currentMealConfig.fruits }}</span>
                </div>
                <div class="detail-item" v-if="currentMealConfig.estimatedCost">
                  <span class="detail-label">预估成本：</span>
                  <span class="detail-value cost">¥{{ currentMealConfig.estimatedCost }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 其他可选菜品 -->
        <div class="alternative-meals-section">
          <h4 class="section-title">
            <el-icon><Grid /></el-icon>
            可选替换菜品（{{ getCurrentMealTypeConfigs().length }}项）
          </h4>
          
          <!-- 加载状态 -->
          <div v-if="mealConfigs.length === 0" class="loading-state">
            <el-icon class="is-loading"><Loading /></el-icon>
            正在加载菜品配置...
          </div>
          
          <!-- 空状态 -->
          <div v-else-if="getCurrentMealTypeConfigs().length === 0" class="empty-state">
            <el-empty description="暂无可选的菜品配置" :image-size="80">
              <template #description>
                <div>
                  <p>当前{{ currentMealType ? getMealTypeLabel(getMealTypeFromKey(currentMealType)) : '' }}暂无可选配置</p>
                  <p style="color: #999; font-size: 12px;">
                    总配置数: {{ mealConfigs.length }}，
                    当前类型: {{ currentMealType }} → {{ currentMealType ? getMealTypeFromKey(currentMealType) : '' }}
                  </p>
                </div>
              </template>
              <el-button type="primary" @click="loadMealConfigs">
                <el-icon><Refresh /></el-icon>
                重新加载配置
              </el-button>
            </el-empty>
          </div>
          
          <!-- 菜品卡片列表 -->
          <div v-else class="meal-options-grid">
            <div
              v-for="config in getCurrentMealTypeConfigs()"
              :key="config.id"
              class="meal-config-card"
              :class="{ 
                'selected': mealEditForm.configId === config.id,
                'current': config.id === currentMealConfig?.id
              }"
              @click="selectMealConfig(config.id!)"
            >
              <div class="meal-card-header">
                <span class="meal-name">{{ config.mealName }}</span>
                <div class="card-actions">
                  <el-tag :type="getMealTypeColor(config.mealType)" size="small">
                    {{ getMealTypeLabel(config.mealType) }}
                  </el-tag>
                  <el-icon v-if="mealEditForm.configId === config.id" class="selected-icon">
                    <Check />
                  </el-icon>
                </div>
              </div>
              <div class="meal-card-content">
                <div class="meal-details">
                  <div class="detail-item" v-if="config.description">
                    <span class="detail-label">描述：</span>
                    <span class="detail-value">{{ config.description }}</span>
                  </div>
                  <div class="detail-item" v-if="config.mainFood">
                    <span class="detail-label">主食：</span>
                    <span class="detail-value">{{ formatJsonField(config.mainFood) }}</span>
                  </div>
                  <div class="detail-item" v-if="config.sideDishes">
                    <span class="detail-label">菜品：</span>
                    <span class="detail-value">{{ formatJsonField(config.sideDishes) }}</span>
                  </div>
                  <div class="detail-item" v-if="config.soup">
                    <span class="detail-label">汤品：</span>
                    <span class="detail-value">{{ config.soup }}</span>
                  </div>
                  <div class="detail-item" v-if="config.fruits">
                    <span class="detail-label">水果：</span>
                    <span class="detail-value">{{ config.fruits }}</span>
                  </div>
                  <div class="detail-item" v-if="config.estimatedCost">
                    <span class="detail-label">预估成本：</span>
                    <span class="detail-value cost">¥{{ config.estimatedCost }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showMealEditDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveMealEdit"
            :disabled="!mealEditForm.configId"
          >
            <el-icon><Check /></el-icon>
            保存更改
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑整天菜单对话框 -->
    <el-dialog
      v-model="showDayEditDialog"
      :title="`编辑${currentEditRow?.dayName}菜单`"
      width="600px"
    >
      <el-form :model="dayEditForm" label-width="80px">
        <el-form-item label="早餐">
          <el-select v-model="dayEditForm.breakfastConfigId" style="width: 100%">
            <el-option
              v-for="config in breakfastConfigs"
              :key="config.id"
              :label="config.mealName"
              :value="config.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="午餐">
          <el-select v-model="dayEditForm.lunchConfigId" style="width: 100%">
            <el-option
              v-for="config in lunchConfigs"
              :key="config.id"
              :label="config.mealName"
              :value="config.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="晚餐">
          <el-select v-model="dayEditForm.dinnerConfigId" style="width: 100%">
            <el-option
              v-for="config in dinnerConfigs"
              :key="config.id"
              :label="config.mealName"
              :value="config.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="加餐">
          <el-select v-model="dayEditForm.snackConfigId" style="width: 100%">
            <el-option
              v-for="config in snackConfigs"
              :key="config.id"
              :label="config.mealName"
              :value="config.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="dayEditForm.specialNotes"
            type="textarea"
            rows="3"
            placeholder="请输入特殊说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDayEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveDayEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, 
  Edit, 
  Calendar, 
  Grid,
  Coffee,
  Food,
  Bowl,
  IceCreamSquare,
  Star,
  Check,
  Loading
} from '@element-plus/icons-vue'
import { mealCalendarApi, mealConfigApi, type MealCalendar, type MealConfig } from '@/api/meal'

// 扩展 MealCalendar 类型
interface ExtendedMealCalendar extends MealCalendar {
  dayName?: string
}

// 响应式数据
const loading = ref(false)
const weekMenuData = ref<ExtendedMealCalendar[]>([])
const mealConfigs = ref<MealConfig[]>([])

// 对话框状态
const showMealEditDialog = ref(false)
const showDayEditDialog = ref(false)

// 当前编辑的行和餐类型
const currentEditRow = ref<ExtendedMealCalendar | null>(null)
const currentMealType = ref<string>('')

// 编辑表单
const mealEditForm = reactive({
  configId: undefined as number | undefined
})

const dayEditForm = reactive({
  breakfastConfigId: undefined as number | undefined,
  lunchConfigId: undefined as number | undefined,
  dinnerConfigId: undefined as number | undefined,
  snackConfigId: undefined as number | undefined,
  specialNotes: ''
})

// 计算属性 - 按餐类型分组的配置
const breakfastConfigs = computed(() =>
  mealConfigs.value.filter(config => config.mealType === 'BREAKFAST')
)
const lunchConfigs = computed(() =>
  mealConfigs.value.filter(config => config.mealType === 'LUNCH')
)
const dinnerConfigs = computed(() =>
  mealConfigs.value.filter(config => config.mealType === 'DINNER')
)
const snackConfigs = computed(() =>
  mealConfigs.value.filter(config => config.mealType === 'SNACK')
)

// 统计数据
const statsData = computed(() => {
  return {
    breakfast: new Set(weekMenuData.value.map(item => item.breakfastConfigId).filter(Boolean)).size,
    lunch: new Set(weekMenuData.value.map(item => item.lunchConfigId).filter(Boolean)).size,
    dinner: new Set(weekMenuData.value.map(item => item.dinnerConfigId).filter(Boolean)).size,
    snack: new Set(weekMenuData.value.map(item => item.snackConfigId).filter(Boolean)).size
  }
})

// 星期名称映射
const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']

// 获取星期标签类型
const getDayTagType = (dayOfWeek: number) => {
  const types = ['', 'success', 'primary', 'warning', 'info', 'danger', '', 'success']
  return types[dayOfWeek] || 'info'
}

// 餐类型名称映射
const getMealTypeName = (mealType: string) => {
  const names: Record<string, string> = {
    breakfast: '早餐',
    lunch: '午餐',
    dinner: '晚餐',
    snack: '加餐'
  }
  return names[mealType] || mealType
}

// 当前选中的膳食配置
const currentMealConfig = computed(() => {
  if (!currentEditRow.value || !currentMealType.value) return null
  
  const configIdField = `${currentMealType.value}ConfigId` as keyof MealCalendar
  const configId = currentEditRow.value[configIdField] as number
  
  return mealConfigs.value.find(config => config.id === configId) || null
})

// 获取当前餐类型的所有配置
const getCurrentMealTypeConfigs = () => {
  if (!currentMealType.value) {
    console.log('getCurrentMealTypeConfigs: currentMealType.value 为空')
    return []
  }
  
  const mealTypeMap: Record<string, string> = {
    breakfast: 'BREAKFAST',
    lunch: 'LUNCH',
    dinner: 'DINNER',
    snack: 'SNACK'
  }
  
  const mealType = mealTypeMap[currentMealType.value]
  console.log('getCurrentMealTypeConfigs: 当前餐类型:', currentMealType.value, '-> 映射为:', mealType)
  console.log('getCurrentMealTypeConfigs: 总配置数量:', mealConfigs.value.length)
  
  const filtered = mealConfigs.value.filter(config => config.mealType === mealType)
  console.log('getCurrentMealTypeConfigs: 筛选后配置数量:', filtered.length)
  console.log('getCurrentMealTypeConfigs: 筛选后配置:', filtered)
  
  return filtered
}

// 获取餐类型键值到后端类型的映射
const getMealTypeFromKey = (mealTypeKey: string) => {
  const mealTypeMap: Record<string, string> = {
    breakfast: 'BREAKFAST',
    lunch: 'LUNCH',
    dinner: 'DINNER',
    snack: 'SNACK'
  }
  return mealTypeMap[mealTypeKey] || mealTypeKey
}

// 获取餐类型颜色
const getMealTypeColor = (mealType: string) => {
  const colorMap: Record<string, string> = {
    'BREAKFAST': 'warning',
    'LUNCH': 'success',
    'DINNER': 'danger',
    'SNACK': 'info'
  }
  return colorMap[mealType] || 'info'
}

// 获取餐类型标签
const getMealTypeLabel = (mealType: string) => {
  const labelMap: Record<string, string> = {
    'BREAKFAST': '早餐',
    'LUNCH': '午餐',
    'DINNER': '晚餐',
    'SNACK': '加餐'
  }
  return labelMap[mealType] || mealType
}

// 格式化JSON字段
const formatJsonField = (jsonString: string) => {
  try {
    const parsed = JSON.parse(jsonString)
    if (Array.isArray(parsed)) {
      return parsed.join('、')
    }
    return parsed.toString()
  } catch {
    return jsonString
  }
}

// 选择膳食配置
const selectMealConfig = (configId: number) => {
  console.log('选择膳食配置 ID:', configId)
  const selectedConfig = mealConfigs.value.find(config => config.id === configId)
  console.log('选择的配置详情:', selectedConfig)
  
  mealEditForm.configId = configId
  console.log('mealEditForm.configId 已更新为:', mealEditForm.configId)
  
  // 给用户一个视觉反馈
  ElMessage.success(`已选择：${selectedConfig?.mealName || '未知菜品'}`)
}

// 加载周菜单数据
const loadWeekMenu = async () => {
  loading.value = true
  try {
    console.log('正在加载固定七天菜单')
    const response = await mealCalendarApi.getWeekMenu()
    console.log('后端返回数据:', response)
    console.log('response 的所有属性:', Object.keys(response))
    
    // 尝试不同的数据路径
    const data = response.data || response || []
    console.log('实际数据:', data)
    console.log('数据类型:', typeof data)
    console.log('是否为数组:', Array.isArray(data))
    console.log('数据长度:', data?.length)
    
    if (Array.isArray(data) && data.length > 0) {
      weekMenuData.value = data.map((item: MealCalendar) => ({
        ...item,
        dayName: dayNames[item.dayOfWeek || 1]
      }))
      console.log('处理后的数据:', weekMenuData.value)
      ElMessage.success(`成功加载 ${data.length} 天菜单`)
    } else {
      console.log('数据为空或格式不正确')
      weekMenuData.value = []
      ElMessage.warning('暂无菜单数据')
    }
  } catch (error) {
    console.error('加载菜单失败:', error)
    ElMessage.error('加载菜单失败: ' + (error as any).message)
  } finally {
    loading.value = false
  }
}

// 加载膳食配置
const loadMealConfigs = async () => {
  try {
    console.log('开始加载膳食配置...')
    const response = await mealConfigApi.getList({
      page: 1,
      size: 1000
      // 移除status限制，获取所有配置
    })
    console.log('膳食配置API完整响应:', response)
    console.log('响应类型:', typeof response)
    console.log('响应的所有键:', Object.keys(response))
    
    // 尝试多种可能的数据路径
    let configData: any[] = []
    
    if (response.data && response.data.records) {
      // 标准分页结构: { data: { records: [...] } }
      configData = response.data.records
      console.log('使用路径: response.data.records')
    } else if (response.data && Array.isArray(response.data)) {
      // 简单结构: { data: [...] }
      configData = response.data
      console.log('使用路径: response.data')
    } else if (Array.isArray(response)) {
      // 直接返回数组: [...]
      configData = response
      console.log('使用路径: response')
    } else if ((response as any).records) {
      // 直接包含records: { records: [...] }
      configData = (response as any).records
      console.log('使用路径: response.records')
    } else {
      console.log('未找到有效的数据路径，响应结构:', response)
    }
    
    mealConfigs.value = configData
    console.log('最终加载的膳食配置数量:', mealConfigs.value.length)
    console.log('膳食配置数据:', mealConfigs.value)
    
    if (mealConfigs.value.length > 0) {
      // 按类型统计
      const typeStats = mealConfigs.value.reduce((acc, config) => {
        acc[config.mealType] = (acc[config.mealType] || 0) + 1
        return acc
      }, {} as Record<string, number>)
      console.log('按类型统计:', typeStats)
      ElMessage.success(`成功加载 ${mealConfigs.value.length} 条膳食配置`)
    } else {
      console.log('膳食配置数据为空，添加测试数据')
      // 添加一些测试数据以便功能测试
      mealConfigs.value = [
        {
          id: 1,
          mealName: '营养早餐A',
          mealType: 'BREAKFAST',
          description: '营养均衡的早餐搭配',
          mainFood: '["小米粥", "煮鸡蛋"]',
          sideDishes: '["咸菜", "花生米"]',
          soup: '银耳莲子汤',
          fruits: '苹果',
          estimatedCost: 12.5,
          status: 1
        },
        {
          id: 2,
          mealName: '营养早餐B',
          mealType: 'BREAKFAST',
          description: '清淡易消化早餐',
          mainFood: '["白粥", "包子"]',
          sideDishes: '["榨菜丝"]',
          soup: '小米粥',
          fruits: '香蕉',
          estimatedCost: 10.0,
          status: 1
        },
        {
          id: 3,
          mealName: '营养午餐A',
          mealType: 'LUNCH',
          description: '荤素搭配午餐',
          mainFood: '["米饭"]',
          sideDishes: '["红烧肉", "青菜豆腐", "土豆丝"]',
          soup: '紫菜蛋花汤',
          fruits: '橙子',
          estimatedCost: 25.0,
          status: 1
        },
        {
          id: 4,
          mealName: '营养晚餐A',
          mealType: 'DINNER',
          description: '清淡营养晚餐',
          mainFood: '["蒸蛋羹", "小馒头"]',
          sideDishes: '["清蒸鱼", "菠菜", "凉拌黄瓜"]',
          soup: '冬瓜汤',
          fruits: '梨',
          estimatedCost: 20.0,
          status: 1
        }
      ]
      console.log('已添加测试数据:', mealConfigs.value.length, '条')
      ElMessage.info(`已添加 ${mealConfigs.value.length} 条测试膳食配置`)
    }
  } catch (error) {
    console.error('加载膳食配置失败:', error)
    ElMessage.error('加载膳食配置失败: ' + (error as any)?.message)
  }
}

// 编辑单餐
const editMeal = async (row: ExtendedMealCalendar, mealType: string) => {
  currentEditRow.value = row
  currentMealType.value = mealType

  // 确保膳食配置已加载
  if (mealConfigs.value.length === 0) {
    console.log('膳食配置为空，正在重新加载...')
    await loadMealConfigs()
  }

  console.log('编辑单餐 - 当前餐类型:', mealType)
  console.log('编辑单餐 - 总配置数量:', mealConfigs.value.length)
  console.log('编辑单餐 - 可用配置:', getCurrentMealTypeConfigs().length)

  // 设置当前值
  const configIdField = `${mealType}ConfigId` as keyof MealCalendar
  mealEditForm.configId = row[configIdField] as number || undefined

  console.log('编辑单餐 - 原始配置ID字段:', configIdField)
  console.log('编辑单餐 - 原始配置ID值:', row[configIdField])
  console.log('编辑单餐 - 当前选中的配置ID:', mealEditForm.configId)

  // 如果当前没有配置ID，默认选择第一个同类型的配置
  if (!mealEditForm.configId) {
    const availableConfigs = getCurrentMealTypeConfigs()
    if (availableConfigs.length > 0) {
      mealEditForm.configId = availableConfigs[0].id
      console.log('编辑单餐 - 默认选择第一个配置ID:', mealEditForm.configId)
    }
  }

  showMealEditDialog.value = true
}

// 保存单餐编辑
const saveMealEdit = async () => {
  console.log('开始保存单餐编辑...')
  console.log('currentEditRow.value:', currentEditRow.value)
  console.log('currentMealType.value:', currentMealType.value)
  console.log('mealEditForm.configId:', mealEditForm.configId)
  
  if (!currentEditRow.value || !currentMealType.value) {
    console.log('保存失败：缺少必要数据')
    ElMessage.error('保存失败：缺少必要数据')
    return
  }

  if (!mealEditForm.configId) {
    console.log('保存失败：未选择菜品配置')
    ElMessage.error('请先选择要替换的菜品配置')
    return
  }

  try {
    // 确保ID存在
    if (!currentEditRow.value.id) {
      console.log('保存失败：缺少记录ID')
      ElMessage.error('保存失败：缺少记录ID')
      return
    }

    const updateData: MealCalendar = {
      id: currentEditRow.value.id,
      weekName: currentEditRow.value.weekName,
      dayOfWeek: currentEditRow.value.dayOfWeek,
      breakfastConfigId: currentEditRow.value.breakfastConfigId,
      lunchConfigId: currentEditRow.value.lunchConfigId,
      dinnerConfigId: currentEditRow.value.dinnerConfigId,
      snackConfigId: currentEditRow.value.snackConfigId,
      specialNotes: currentEditRow.value.specialNotes,
      isActive: currentEditRow.value.isActive,
      // 更新对应的餐次配置ID
      [`${currentMealType.value}ConfigId`]: mealEditForm.configId
    }

    console.log('准备发送的更新数据:', updateData)
    console.log('要更新的字段:', `${currentMealType.value}ConfigId`, '值:', mealEditForm.configId)

    const response = await mealCalendarApi.updateDayMenu(updateData)
    console.log('保存响应:', response)
    console.log('保存响应类型:', typeof response)
    console.log('保存响应status:', response?.status)
    
    // 处理不同的响应格式
    const responseValue = response as any
    const isSuccess = responseValue === true || 
                     response?.status === 200 || 
                     response?.data !== undefined ||
                     responseValue?.code === 200 ||
                     responseValue?.success === true
    
    console.log('保存成功判断:', isSuccess)
    
    if (isSuccess) {
      ElMessage.success('保存成功')
      showMealEditDialog.value = false
      await loadWeekMenu() // 重新加载数据
    } else {
      console.log('保存响应不符合预期:', response)
      ElMessage.error('保存失败：服务器响应异常')
    }
  } catch (error) {
    console.error('保存失败详细错误:', error)
    const errorMessage = (error as any)?.response?.data?.message || 
                        (error as any)?.message || 
                        '保存失败，请稍后重试'
    ElMessage.error(errorMessage)
  }
}

// 编辑整天菜单
const editDayMenu = (row: ExtendedMealCalendar) => {
  currentEditRow.value = row

  // 设置表单值
  dayEditForm.breakfastConfigId = row.breakfastConfigId || undefined
  dayEditForm.lunchConfigId = row.lunchConfigId || undefined
  dayEditForm.dinnerConfigId = row.dinnerConfigId || undefined
  dayEditForm.snackConfigId = row.snackConfigId || undefined
  dayEditForm.specialNotes = row.specialNotes || ''

  showDayEditDialog.value = true
}

// 保存整天菜单编辑
const saveDayEdit = async () => {
  if (!currentEditRow.value) return

  try {
    const updateData: MealCalendar = {
      ...currentEditRow.value,
      breakfastConfigId: dayEditForm.breakfastConfigId,
      lunchConfigId: dayEditForm.lunchConfigId,
      dinnerConfigId: dayEditForm.dinnerConfigId,
      snackConfigId: dayEditForm.snackConfigId,
      specialNotes: dayEditForm.specialNotes
    }

    const response = await mealCalendarApi.updateDayMenu(updateData)
    if (response.data) {
      ElMessage.success('保存成功')
      showDayEditDialog.value = false
      loadWeekMenu() // 重新加载数据
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

// 页面初始化
onMounted(() => {
  loadMealConfigs()
  loadWeekMenu()
})
</script>

<style scoped>
.meal-calendar-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 页面头部样式 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  color: white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  flex: 1;
}

.page-title {
  margin: 0 0 12px 0;
  font-size: 32px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 36px;
}

.page-description {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
  font-weight: 300;
}

.action-section {
  flex-shrink: 0;
}

/* 主要内容区域 */
.main-content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 统计卡片样式 */
.stats-cards {
  margin-bottom: 32px;
}

.stat-card {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card.breakfast {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
}

.stat-card.lunch {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.stat-card.dinner {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
}

.stat-card.snack {
  background: linear-gradient(135deg, #d299c2 0%, #fef9d7 100%);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.stat-content {
  flex: 1;
  color: white;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  font-weight: 500;
}

/* 表格容器样式 */
.menu-table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #e5e7eb;
}

.table-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 表格样式 */
.menu-table {
  width: 100%;
}

.menu-table :deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.menu-table :deep(.el-table__row:hover) {
  background-color: #f8fafc !important;
}

/* 表格单元格样式 */
.day-cell {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 8px 0;
}

.meal-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 8px;
  min-height: 80px;
  justify-content: center;
}

.meal-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.meal-icon {
  font-size: 18px;
  color: #6b7280;
}

.meal-name {
  font-weight: 500;
  color: #374151;
  flex: 1;
  word-break: break-word;
  line-height: 1.4;
}

.edit-btn {
  align-self: flex-start;
  font-size: 12px;
  padding: 4px 8px;
}

.notes-cell {
  padding: 12px 8px;
}

.notes-text {
  font-size: 14px;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-btn {
  font-size: 12px;
  padding: 8px 12px;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 加载和空状态样式 */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #666;
  font-size: 14px;
}

.loading-state .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.empty-state {
  padding: 20px;
}

.empty-state .el-empty {
  padding: 20px 0;
}

/* 确保加载状态的图标动画 */
.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 膳食编辑对话框样式 */
.meal-edit-content {
  max-height: 600px;
  overflow-y: auto;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.current-meal-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f0f0f0;
}

.alternative-meals-section {
  margin-bottom: 16px;
}

.meal-options-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  max-height: 400px;
  overflow-y: auto;
  padding: 8px;
}

.meal-config-card {
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 16px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.meal-config-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.meal-config-card.selected {
  border-color: #409eff;
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f4ff 100%);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
}

.meal-config-card.current {
  border-color: #67c23a;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6fffa 100%);
}

.meal-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.meal-name {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selected-icon {
  color: #409eff;
  font-size: 18px;
  font-weight: bold;
}

.meal-card-content {
  font-size: 14px;
}

.meal-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.detail-label {
  font-weight: 600;
  color: #606266;
  min-width: 50px;
  flex-shrink: 0;
}

.detail-value {
  color: #303133;
  line-height: 1.4;
  word-break: break-word;
}

.detail-value.cost {
  color: #e6a23c;
  font-weight: 600;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .stats-cards .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .meal-calendar-container {
    padding: 16px;
  }
  
  .page-header {
    padding: 24px 20px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .main-content {
    padding: 16px;
  }
  
  .meal-cell {
    min-height: 60px;
    padding: 8px 4px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-number {
    font-size: 24px;
  }
}

/* 动画效果 */
.meal-calendar-container {
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card {
  animation: slideInUp 0.6s ease-out;
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.menu-table-container {
  animation: slideInUp 0.8s ease-out;
}
</style>
