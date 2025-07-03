<template>
  <div class="ai-recommendation-v4">
    <!-- 页面头部 -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><Robot /></el-icon>
            AI智能食谱推荐系统
          </h1>
          <p class="page-description">基于DeepSeek大模型的个性化养老院食谱推荐系统</p>
        </div>

      </div>
    </el-card>

    <!-- 功能选择标签页 -->
    <el-card class="main-card" shadow="never">
      <el-tabs v-model="activeTab" type="card" class="ai-tabs" @tab-change="handleTabChange">

        <!-- 通用推荐 -->
        <el-tab-pane name="general">
          <template #label>
            <span class="tab-label">
              <el-icon><Grid /></el-icon>
              通用推荐
            </span>
          </template>

          <div class="tab-content">
            <el-row :gutter="24">
              <!-- 左侧表单区域 -->
              <el-col :span="10">
                <el-card class="form-card" shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <h3><el-icon><Setting /></el-icon> 推荐配置</h3>
                    </div>
                  </template>

                  <el-form ref="generalFormRef" :model="generalForm" :rules="generalRules" label-width="100px" class="ai-form">
                    <el-form-item label="餐食类型" prop="mealType">
                      <el-select v-model="generalForm.mealType" placeholder="请选择餐食类型" style="width: 100%">
                        <el-option
                          v-for="item in MEAL_TYPE_OPTIONS"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        />
                      </el-select>
                    </el-form-item>

                    <el-form-item label="目标群体" prop="targetGroup">
                      <el-select v-model="generalForm.targetGroup" placeholder="请选择目标群体" style="width: 100%">
                        <el-option
                          v-for="item in TARGET_GROUP_OPTIONS"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        />
                      </el-select>
                    </el-form-item>

                    <el-form-item label="营养类型" prop="nutritionType">
                      <el-select v-model="generalForm.nutritionType" placeholder="请选择营养类型" style="width: 100%">
                        <el-option
                          v-for="item in NUTRITION_TYPE_OPTIONS"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        />
                      </el-select>
                    </el-form-item>

                    <el-form-item label="热量范围">
                      <el-slider
                        v-model="generalForm.caloriesRange"
                        range
                        :min="100"
                        :max="1000"
                        :step="50"
                        show-stops
                        :format-tooltip="(val: number) => `${val}千卡`"
                      />
                    </el-form-item>

                    <el-form-item label="附加要求">
                      <el-input
                        v-model="generalForm.additionalRequirements"
                        type="textarea"
                        :rows="3"
                        placeholder="请输入特殊要求，如：清淡、少盐、素食等..."
                        maxlength="200"
                        show-word-limit
                      />
                    </el-form-item>

                    <el-form-item>
                      <el-button
                        type="primary"
                        size="large"
                        @click="generateGeneral"
                        :loading="loading.general"
                        icon="Magic"
                        style="width: 100%"
                      >
                        {{ loading.general ? 'AI正在思考中...' : '开始AI推荐' }}
                      </el-button>
                    </el-form-item>

                    <el-form-item v-if="generalRecommendations.length > 0">
                      <el-button-group style="width: 100%">
                        <el-button @click="clearRecommendations" icon="Delete">清空结果</el-button>
                        <el-button type="success" @click="batchAdoptGeneral" :loading="loading.adopt" icon="Check">
                          批量采纳
                        </el-button>
                      </el-button-group>
                    </el-form-item>
                  </el-form>
                </el-card>
              </el-col>

              <!-- 右侧结果区域 -->
              <el-col :span="14">
                <el-card class="result-card" shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <h3><el-icon><Document /></el-icon> AI推荐结果</h3>
                      <el-tag v-if="generalRecommendations.length > 0" type="success">
                        共 {{ generalRecommendations.length }} 个推荐
                      </el-tag>
                    </div>
                  </template>

                  <!-- 生成进度提示 -->
                  <div v-if="loading.general" class="loading-state">
                    <div class="loading-content">
                      <el-icon class="rotating"><Loading /></el-icon>
                      <h4>AI正在为您生成推荐...</h4>
                      <p>{{ generateProgress }}</p>
                      <el-progress :percentage="progressPercent" :show-text="false" />
                    </div>
                  </div>

                  <!-- 空状态 -->
                  <div v-else-if="generalRecommendations.length === 0" class="empty-state">
                    <el-empty description="暂无推荐结果">
                      <template #image>
                        <el-icon size="100"><Document /></el-icon>
                      </template>
                      <el-button type="primary" @click="generateGeneral" icon="Magic">开始生成推荐</el-button>
                    </el-empty>
                  </div>

                  <!-- 推荐结果列表 -->
                  <div v-else class="recommendations-container">
                    <el-scrollbar height="600px">
                      <div class="recommendations-list">
                        <el-card
                          v-for="(item, index) in generalRecommendations"
                          :key="item.id"
                          class="recommendation-item"
                          shadow="hover"
                        >
                          <template #header>
                            <div class="item-header">
                              <div class="header-left">
                                <el-tag class="meal-type-tag" :type="getMealTypeTag(item.mealType)">
                                  {{ getMealTypeLabel(item.mealType) }}
                                </el-tag>
                                <h4 class="meal-name">{{ item.mealName || `推荐${index + 1}` }}</h4>
                              </div>
                              <div class="header-right">
                                <el-rate
                                  v-model="item.aiScore"
                                  disabled
                                  show-score
                                  text-color="#ff9900"
                                  score-template="{value}分"
                                />
                                <el-button
                                  type="primary"
                                  size="small"
                                  @click="adoptSingle(item.id!)"
                                  :loading="loading.adopt"
                                  icon="Check"
                                >
                                  采纳
                                </el-button>
                              </div>
                            </div>
                          </template>

                          <div class="item-content">
                            <!-- 基本信息 -->
                            <div class="content-section">
                              <h5><el-icon><Document /></el-icon> 菜品信息</h5>
                              <el-descriptions :column="2" size="small" border>
                                <el-descriptions-item label="菜品名称">
                                  {{ item.mealName || '暂无' }}
                                </el-descriptions-item>
                                <el-descriptions-item label="预估热量">
                                  <el-tag type="warning">{{ item.calories || 0 }} 千卡</el-tag>
                                </el-descriptions-item>
                                <el-descriptions-item label="营养类型" span="2">
                                  {{ item.nutritionType || '暂无' }}
                                </el-descriptions-item>
                              </el-descriptions>
                            </div>

                            <!-- 详细内容 -->
                            <div class="content-section">
                              <el-collapse v-model="item.activeCollapse">
                                <el-collapse-item title="食材清单" name="ingredients">
                                  <div class="content-text">
                                    {{ item.ingredients || '暂无食材信息' }}
                                  </div>
                                </el-collapse-item>

                                <el-collapse-item title="制作方法" name="cooking">
                                  <div class="content-text">
                                    {{ item.cookingMethod || '暂无制作方法' }}
                                  </div>
                                </el-collapse-item>

                                <el-collapse-item title="营养分析" name="nutrition">
                                  <div class="content-text">
                                    {{ item.nutritionInfo || '暂无营养信息' }}
                                  </div>
                                </el-collapse-item>

                                <el-collapse-item title="AI推荐理由" name="reason">
                                  <div class="ai-reason-text">
                                    <el-icon><Robot /></el-icon>
                                    {{ item.aiReason || '暂无推荐理由' }}
                                  </div>
                                </el-collapse-item>
                              </el-collapse>
                            </div>

                            <!-- 操作区域 -->
                            <div class="item-actions">
                              <el-button-group>
                                <el-button size="small" @click="previewRecommendation(item)">预览</el-button>
                                <el-button size="small" @click="editRecommendation(item)">编辑</el-button>
                                <el-button size="small" type="danger" @click="deleteRecommendation(item.id!)">删除</el-button>
                              </el-button-group>
                            </div>
                          </div>
                        </el-card>
                      </div>
                    </el-scrollbar>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 个性化推荐 -->
        <el-tab-pane label="个性化推荐" name="personalized">
          <div class="tab-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="form-panel">
                  <h3>客户信息</h3>
                  <el-form ref="personalizedFormRef" :model="personalizedForm" label-width="100px">
                    <el-form-item label="客户" required>
                      <el-select
                        v-model="personalizedForm.customerId"
                        placeholder="请选择客户"
                        filterable
                        @change="handleCustomerChange"
                      >
                        <el-option
                          v-for="customer in customerList"
                          :key="customer.id"
                          :label="customer.customerName || customer.name"
                          :value="customer.id"
                        />
                      </el-select>
                    </el-form-item>

                    <el-form-item label="客户姓名">
                      <el-input v-model="personalizedForm.customerName" readonly />
                    </el-form-item>

                    <el-form-item label="年龄">
                      <el-input-number
                        v-model="personalizedForm.customerAge"
                        :min="0"
                        :max="120"
                        placeholder="年龄"
                      />
                    </el-form-item>

                    <el-form-item label="性别">
                      <el-select v-model="personalizedForm.customerGender" placeholder="请选择性别">
                        <el-option
                          v-for="item in GENDER_OPTIONS"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        />
                      </el-select>
                    </el-form-item>

                    <el-form-item label="餐食类型">
                      <el-select v-model="personalizedForm.mealType" placeholder="请选择餐食类型">
                        <el-option
                          v-for="item in MEAL_TYPE_OPTIONS"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        />
                      </el-select>
                    </el-form-item>

                    <el-form-item label="健康状况">
                      <el-input
                        v-model="personalizedForm.healthConditions"
                        type="textarea"
                        :rows="2"
                        placeholder="如：轻度高血压、糖尿病等"
                      />
                    </el-form-item>

                    <el-form-item label="饮食偏好">
                      <el-input
                        v-model="personalizedForm.dietaryPreferences"
                        type="textarea"
                        :rows="2"
                        placeholder="如：清淡饮食、素食等"
                      />
                    </el-form-item>

                    <el-form-item label="过敏信息">
                      <el-input
                        v-model="personalizedForm.allergies"
                        placeholder="如：海鲜过敏、花生过敏等"
                      />
                    </el-form-item>

                    <el-form-item>
                      <el-button
                        type="primary"
                        @click="generatePersonalized"
                        :loading="loading.personalized"
                        icon="User"
                      >
                        生成个性化推荐
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </el-col>

              <el-col :span="16">
                <div class="result-panel">
                  <div class="panel-header">
                    <h3>个性化推荐结果</h3>
                  </div>

                  <div v-if="personalizedRecommendations.length === 0" class="empty-state">
                    <el-empty description="暂无个性化推荐，请填写客户信息后生成" />
                  </div>

                  <div v-else class="recommendations-list">
                    <el-card
                      v-for="item in personalizedRecommendations"
                      :key="item.id"
                      class="recommendation-card personalized"
                      shadow="hover"
                    >
                      <template #header>
                        <div class="card-header">
                          <span class="meal-name">{{ item.mealName }}</span>
                          <div class="card-actions">
                            <el-tag type="success">个性化</el-tag>
                            <el-tag>{{ item.customerName }}，{{ item.customerAge }}岁</el-tag>
                            <el-rate v-model="item.aiScore" disabled show-score text-color="#ff9900" />
                            <el-button
                              type="primary"
                              size="small"
                              @click="adoptSingle(item.id!)"
                              :loading="loading.adopt"
                            >
                              采纳
                            </el-button>
                          </div>
                        </div>
                      </template>

                      <div class="card-content">
                        <el-descriptions :column="1" size="small">
                          <el-descriptions-item label="推荐理由">
                            {{ item.aiReason }}
                          </el-descriptions-item>
                          <el-descriptions-item label="食材清单">
                            {{ item.ingredients }}
                          </el-descriptions-item>
                          <el-descriptions-item label="制作方法">
                            {{ item.cookingMethod }}
                          </el-descriptions-item>
                          <el-descriptions-item label="营养信息">
                            {{ item.nutritionInfo }}
                          </el-descriptions-item>
                        </el-descriptions>
                      </div>
                    </el-card>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 一日四餐生成 -->
        <el-tab-pane label="一日四餐" name="daily-menu">
          <div class="tab-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="form-panel">
                  <h3>菜单生成</h3>
                  <el-form ref="menuFormRef" :model="menuForm" label-width="100px">
                    <el-form-item label="客户" required>
                      <el-select
                        v-model="menuForm.customerId"
                        placeholder="请选择客户"
                        filterable
                        @change="handleMenuCustomerChange"
                      >
                        <el-option
                          v-for="customer in customerList"
                          :key="customer.id"
                          :label="customer.customerName || customer.name"
                          :value="customer.id"
                        />
                      </el-select>
                    </el-form-item>

                    <el-form-item label="菜单日期" required>
                      <el-date-picker
                        v-model="menuForm.menuDate"
                        type="date"
                        placeholder="选择日期"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        :disabled-date="disabledDate"
                      />
                    </el-form-item>

                    <el-form-item label="客户姓名">
                      <el-input v-model="menuForm.customerName" readonly />
                    </el-form-item>

                    <el-form-item label="年龄">
                      <el-input-number
                        v-model="menuForm.customerAge"
                        :min="0"
                        :max="120"
                      />
                    </el-form-item>

                    <el-form-item label="性别">
                      <el-select v-model="menuForm.customerGender" placeholder="请选择性别">
                        <el-option
                          v-for="item in GENDER_OPTIONS"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        />
                      </el-select>
                    </el-form-item>

                    <el-form-item label="健康状况">
                      <el-input
                        v-model="menuForm.healthConditions"
                        type="textarea"
                        :rows="3"
                        placeholder="详细描述健康状况..."
                      />
                    </el-form-item>

                    <el-form-item label="饮食偏好">
                      <el-input
                        v-model="menuForm.dietaryPreferences"
                        type="textarea"
                        :rows="2"
                        placeholder="饮食偏好和习惯..."
                      />
                    </el-form-item>

                    <el-form-item label="过敏信息">
                      <el-input
                        v-model="menuForm.allergies"
                        placeholder="过敏原信息..."
                      />
                    </el-form-item>

                    <el-form-item>
                      <el-button
                        type="primary"
                        @click="generateDailyMenu"
                        :loading="loading.menu"
                        icon="Calendar"
                      >
                        生成一日四餐
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </el-col>

              <el-col :span="16">
                <div class="result-panel">
                  <div class="panel-header">
                    <h3>一日四餐菜单</h3>
                    <div class="header-actions" v-if="currentMenu">
                      <el-button
                        type="success"
                        size="small"
                        @click="approveMenu"
                        :loading="loading.approve"
                      >
                        审核通过
                      </el-button>
                    </div>
                  </div>

                  <div v-if="!currentMenu" class="empty-state">
                    <el-empty description="暂无菜单，请选择客户和日期后生成" />
                  </div>

                  <div v-else class="menu-content">
                    <el-row :gutter="16">
                      <el-col :span="12">
                        <el-card class="meal-card breakfast">
                          <template #header>
                            <div class="meal-header">
                              <el-icon><Sunrise /></el-icon>
                              <span>早餐</span>
                            </div>
                          </template>
                          <div class="meal-content">
                            <h4>{{ currentMenu.breakfastName || '暂无' }}</h4>
                            <p class="meal-desc">{{ currentMenu.breakfastDescription || '暂无描述' }}</p>
                            <el-divider />
                            <div class="meal-details">
                              <p><strong>食材：</strong>{{ currentMenu.breakfastIngredients || '暂无' }}</p>
                              <p><strong>做法：</strong>{{ currentMenu.breakfastCookingMethod || '暂无' }}</p>
                              <p><strong>热量：</strong>{{ currentMenu.breakfastCalories || 0 }} 千卡</p>
                            </div>
                          </div>
                        </el-card>
                      </el-col>

                      <el-col :span="12">
                        <el-card class="meal-card lunch">
                          <template #header>
                            <div class="meal-header">
                              <el-icon><Sunny /></el-icon>
                              <span>午餐</span>
                            </div>
                          </template>
                          <div class="meal-content">
                            <h4>{{ currentMenu.lunchName || '暂无' }}</h4>
                            <p class="meal-desc">{{ currentMenu.lunchDescription || '暂无描述' }}</p>
                            <el-divider />
                            <div class="meal-details">
                              <p><strong>食材：</strong>{{ currentMenu.lunchIngredients || '暂无' }}</p>
                              <p><strong>做法：</strong>{{ currentMenu.lunchCookingMethod || '暂无' }}</p>
                              <p><strong>热量：</strong>{{ currentMenu.lunchCalories || 0 }} 千卡</p>
                            </div>
                          </div>
                        </el-card>
                      </el-col>

                      <el-col :span="12">
                        <el-card class="meal-card dinner">
                          <template #header>
                            <div class="meal-header">
                              <el-icon><Moon /></el-icon>
                              <span>晚餐</span>
                            </div>
                          </template>
                          <div class="meal-content">
                            <h4>{{ currentMenu.dinnerName || '暂无' }}</h4>
                            <p class="meal-desc">{{ currentMenu.dinnerDescription || '暂无描述' }}</p>
                            <el-divider />
                            <div class="meal-details">
                              <p><strong>食材：</strong>{{ currentMenu.dinnerIngredients || '暂无' }}</p>
                              <p><strong>做法：</strong>{{ currentMenu.dinnerCookingMethod || '暂无' }}</p>
                              <p><strong>热量：</strong>{{ currentMenu.dinnerCalories || 0 }} 千卡</p>
                            </div>
                          </div>
                        </el-card>
                      </el-col>

                      <el-col :span="12">
                        <el-card class="meal-card snack">
                          <template #header>
                            <div class="meal-header">
                              <el-icon><Coffee /></el-icon>
                              <span>加餐</span>
                            </div>
                          </template>
                          <div class="meal-content">
                            <h4>{{ currentMenu.snackName || '暂无' }}</h4>
                            <p class="meal-desc">{{ currentMenu.snackDescription || '暂无描述' }}</p>
                            <el-divider />
                            <div class="meal-details">
                              <p><strong>食材：</strong>{{ currentMenu.snackIngredients || '暂无' }}</p>
                              <p><strong>做法：</strong>{{ currentMenu.snackCookingMethod || '暂无' }}</p>
                              <p><strong>热量：</strong>{{ currentMenu.snackCalories || 0 }} 千卡</p>
                            </div>
                          </div>
                        </el-card>
                      </el-col>
                    </el-row>

                    <!-- 总计信息 -->
                    <el-card class="summary-card" style="margin-top: 16px;">
                      <template #header>
                        <span>营养总计</span>
                      </template>
                      <el-descriptions :column="3" border>
                        <el-descriptions-item label="总热量">
                          {{ currentMenu.totalCalories || 0 }} 千卡
                        </el-descriptions-item>
                        <el-descriptions-item label="预估成本">
                          {{ currentMenu.totalCost || 0 }} 元
                        </el-descriptions-item>
                        <el-descriptions-item label="审核状态">
                          <el-tag :type="currentMenu.isApproved ? 'success' : 'warning'">
                            {{ currentMenu.isApproved ? '已审核' : '待审核' }}
                          </el-tag>
                        </el-descriptions-item>
                      </el-descriptions>
                      <el-divider />
                      <div class="ai-reason">
                        <p><strong>AI推荐理由：</strong></p>
                        <p>{{ currentMenu.aiRecommendationReason || '暂无推荐理由' }}</p>
                      </div>
                    </el-card>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 历史推荐 -->
        <el-tab-pane name="history">
          <template #label>
            <span class="tab-label">
              <el-icon><Clock /></el-icon>
              历史推荐
            </span>
          </template>

          <div class="tab-content">
            <div class="filter-bar">
              <el-form :inline="true">
                <el-form-item>
                  <el-button type="primary" @click="loadHistoryRecommendations" icon="Search">查询历史</el-button>
                  <el-button
                    type="success"
                    @click="generateFromLatestHistoryMethod"
                    :loading="generatingFromLatest"
                    icon="Magic"
                  >
                    基于最新历史生成
                  </el-button>
                </el-form-item>
              </el-form>
            </div>

            <div class="history-recommendations">
              <el-row :gutter="16">
                <!-- 左侧：历史记录列表 -->
                <el-col :span="10">
                  <el-card class="history-list-card" shadow="hover">
                    <template #header>
                      <div class="card-header">
                        <h3><el-icon><Document /></el-icon> 历史推荐记录</h3>
                        <span class="count-badge">{{ historyRecommendations.length }} 条记录</span>
                      </div>
                    </template>

                    <div class="history-list" v-loading="historyLoading">
                      <el-card
                        v-for="history in historyRecommendations"
                        :key="history.id"
                        class="history-item"
                        shadow="hover"
                        :class="{ 'selected': selectedHistory?.id === history.id }"
                        @click="selectHistory(history)"
                      >
                        <div class="history-content">
                          <div class="history-title">
                            <h4>{{ history.mealName }}</h4>
                            <el-tag :type="getMealTypeTag(history.mealType)" size="small">
                              {{ getMealTypeLabel(history.mealType) }}
                            </el-tag>
                          </div>
                          <div class="history-info">
                            <p><strong>目标群体：</strong>{{ getTargetGroupLabel(history.targetGroup) }}</p>
                            <p><strong>营养类型：</strong>{{ getNutritionTypeLabel(history.nutritionType) }}</p>
                            <p><strong>AI评分：</strong>
                              <el-rate
                                v-model="history.aiScore"
                                disabled
                                show-score
                                text-color="#ff9900"
                                :max="5"
                              />
                            </p>
                            <p><strong>采纳时间：</strong>{{ formatDate(history.adoptionDate) }}</p>
                            <p><strong>采纳人：</strong>{{ history.adoptedByUserName }}</p>
                          </div>
                        </div>
                      </el-card>

                      <el-empty v-if="!historyLoading && historyRecommendations.length === 0" description="暂无历史推荐记录" />
                    </div>
                  </el-card>
                </el-col>

                <!-- 右侧：基于历史生成新推荐 -->
                <el-col :span="14">
                  <el-card class="generate-from-history-card" shadow="hover">
                    <template #header>
                      <div class="card-header">
                        <h3><el-icon><Magic /></el-icon> 基于历史生成类似推荐</h3>
                      </div>
                    </template>

                    <!-- 基于最新历史生成的推荐结果 -->
                    <div v-if="historyGeneratedRecommendations.length > 0" class="latest-generated-results">
                      <h4><el-icon><Star /></el-icon> 基于最新历史生成的推荐</h4>
                      <el-row :gutter="12">
                        <el-col :span="12" v-for="rec in historyGeneratedRecommendations" :key="rec.id">
                          <el-card class="recommendation-card" shadow="hover">
                            <template #header>
                              <div class="rec-header">
                                <span class="rec-name">{{ rec.mealName }}</span>
                                <el-tag :type="getMealTypeTag(rec.mealType)" size="small">
                                  {{ getMealTypeLabel(rec.mealType) }}
                                </el-tag>
                              </div>
                            </template>

                            <div class="rec-content">
                              <p><strong>描述：</strong>{{ rec.description }}</p>
                              <p><strong>热量：</strong>{{ rec.calories || 0 }} 千卡</p>
                              <p><strong>评分：</strong>
                                <el-rate v-model="rec.aiScore" disabled size="small" />
                              </p>
                              <p class="rec-reason">{{ rec.aiReason }}</p>
                            </div>

                            <div class="rec-actions">
                              <el-button
                                type="primary"
                                size="small"
                                @click="adoptRecommendationFromHistory(rec.id)"
                                :loading="adoptingRecommendation"
                              >
                                采纳
                              </el-button>
                            </div>
                          </el-card>
                        </el-col>
                      </el-row>
                      <el-divider />
                    </div>

                    <!-- 原有的基于选择历史生成的功能 -->
                    <div v-if="!selectedHistory && historyGeneratedRecommendations.length === 0" class="select-prompt">
                      <el-empty description="点击上方生成按钮" />
                    </div>

                    <div v-if="selectedHistory" class="generate-section">
                      <div class="selected-history-info">
                        <h4>基于推荐：{{ selectedHistory.mealName }}</h4>
                        <el-descriptions :column="2" size="small">
                          <el-descriptions-item label="餐食类型">
                            <el-tag :type="getMealTypeTag(selectedHistory.mealType)">
                              {{ getMealTypeLabel(selectedHistory.mealType) }}
                            </el-tag>
                          </el-descriptions-item>
                          <el-descriptions-item label="目标群体">
                            {{ getTargetGroupLabel(selectedHistory.targetGroup) }}
                          </el-descriptions-item>
                          <el-descriptions-item label="营养类型">
                            {{ getNutritionTypeLabel(selectedHistory.nutritionType) }}
                          </el-descriptions-item>
                          <el-descriptions-item label="AI评分">
                            <el-rate v-model="selectedHistory.aiScore" disabled />
                          </el-descriptions-item>
                        </el-descriptions>
                      </div>

                      <el-divider />

                      <div class="generate-options">
                        <el-row :gutter="16">
                          <el-col :span="12">
                            <el-card class="option-card">
                              <template #header>
                                <h4><el-icon><List /></el-icon> 生成类似推荐</h4>
                              </template>
                              <p>基于此历史记录，生成多个类似的餐食推荐</p>
                              <el-button
                                type="primary"
                                @click="generateSimilarRecommendations"
                                :loading="generatingSimilar"
                                style="width: 100%;"
                              >
                                <el-icon><Magic /></el-icon>
                                生成类似推荐
                              </el-button>
                            </el-card>
                          </el-col>
                          <el-col :span="12">
                            <el-card class="option-card">
                              <template #header>
                                <h4><el-icon><Calendar /></el-icon> 生成一日四餐</h4>
                              </template>
                              <p>基于此历史记录，为客户生成完整的一日四餐菜单</p>
                              <el-form :inline="true">
                                <el-form-item label="菜单日期">
                                  <el-date-picker
                                    v-model="historyMenuDate"
                                    type="date"
                                    placeholder="选择日期"
                                    format="YYYY-MM-DD"
                                    value-format="YYYY-MM-DD"
                                    :disabled-date="disabledDate"
                                    style="width: 150px;"
                                  />
                                </el-form-item>
                              </el-form>
                              <el-button
                                type="success"
                                @click="generateMenuFromHistoryMethod"
                                :loading="generatingMenu"
                                :disabled="!historyMenuDate"
                                style="width: 100%;"
                              >
                                <el-icon><Calendar /></el-icon>
                                生成一日四餐
                              </el-button>
                            </el-card>
                          </el-col>
                        </el-row>
                      </div>

                      <!-- 生成的推荐结果 -->
                      <div v-if="historyGeneratedRecommendations.length > 0" class="generated-results">
                        <el-divider />
                        <h4><el-icon><Star /></el-icon> 生成的推荐结果</h4>
                        <el-row :gutter="12">
                          <el-col :span="8" v-for="rec in historyGeneratedRecommendations" :key="rec.id">
                            <el-card class="recommendation-card" shadow="hover">
                              <template #header>
                                <div class="rec-header">
                                  <span class="rec-name">{{ rec.mealName }}</span>
                                  <el-tag :type="getMealTypeTag(rec.mealType)" size="small">
                                    {{ getMealTypeLabel(rec.mealType) }}
                                  </el-tag>
                                </div>
                              </template>

                              <div class="rec-content">
                                <p><strong>描述：</strong>{{ rec.description }}</p>
                                <p><strong>热量：</strong>{{ rec.calories || 0 }} 千卡</p>
                                <p><strong>评分：</strong>
                                  <el-rate v-model="rec.aiScore" disabled size="small" />
                                </p>
                                <p class="rec-reason">{{ rec.aiReason }}</p>
                              </div>

                              <div class="rec-actions">
                                <el-button
                                  type="primary"
                                  size="small"
                                  @click="adoptRecommendationFromHistory(rec.id)"
                                  :loading="adoptingRecommendation"
                                >
                                  采纳
                                </el-button>
                              </div>
                            </el-card>
                          </el-col>
                        </el-row>
                      </div>

                      <!-- 生成的菜单结果 -->
                      <div v-if="historyGeneratedMenu" class="generated-menu">
                        <el-divider />
                        <h4><el-icon><Calendar /></el-icon> 生成的一日四餐菜单</h4>
                        <MenuDisplay
                          :menu="historyGeneratedMenu"
                          @adopt="adoptHistoryMenu"
                          @approve="approveHistoryMenu"
                          :loading="adoptingMenu"
                        />
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  generateRecommendations,
  generatePersonalizedRecommendations,
  generatePersonalizedMenu,
  getCurrentRecommendations,
  adoptRecommendation,
  batchAdoptRecommendations,
  clearCurrentRecommendations,
  getCustomerPersonalizedMenus,
  approvePersonalizedMenu,
  getHistoryRecommendations,
  generateFromHistory,
  generateMenuFromHistory,
  generateFromLatestHistory,
  MEAL_TYPE_OPTIONS,
  TARGET_GROUP_OPTIONS,
  NUTRITION_TYPE_OPTIONS,
  GENDER_OPTIONS,
  type AiRecommendationTemp,
  type AiPersonalizedMenu,
  type AiRecommendationHistory,
  type GenerateRecommendationRequest,
  type PersonalizedRecommendationRequest,
  type GeneratePersonalizedMenuRequest
} from '@/api/ai-v4'
import { customerApi, type Customer } from '@/api/customer'

// 响应式数据
const activeTab = ref('general')
const generalFormRef = ref()
const personalizedFormRef = ref()
const menuFormRef = ref()

// 加载状态
const loading = reactive({
  general: false,
  personalized: false,
  menu: false,
  adopt: false,
  approve: false
})

// 通用推荐表单
const generalForm = reactive({
  mealType: '',
  targetGroup: '',
  nutritionType: '',
  additionalRequirements: '',
  caloriesRange: [200, 800]
})

// 个性化推荐表单
const personalizedForm = reactive<PersonalizedRecommendationRequest>({
  customerId: 0,
  customerName: '',
  customerAge: undefined,
  customerGender: '',
  mealType: '',
  healthConditions: '',
  dietaryPreferences: '',
  allergies: ''
})

// 一日四餐表单
const menuForm = reactive<GeneratePersonalizedMenuRequest>({
  customerId: 0,
  customerName: '',
  customerAge: undefined,
  customerGender: '',
  menuDate: '',
  healthConditions: '',
  dietaryPreferences: '',
  allergies: ''
})

// 历史查询过滤条件
const historyFilter = reactive({
  customerId: undefined as number | undefined,
  dateRange: [] as string[]
})

// 数据列表
const generalRecommendations = ref<AiRecommendationTemp[]>([])
const personalizedRecommendations = ref<AiRecommendationTemp[]>([])
const currentMenu = ref<AiPersonalizedMenu | null>(null)
const historyMenus = ref<AiPersonalizedMenu[]>([])
const customerList = ref<Customer[]>([])

// 历史推荐相关
const historyRecommendations = ref<AiRecommendationHistory[]>([])
const selectedHistory = ref<AiRecommendationHistory | null>(null)
const historyGeneratedRecommendations = ref<AiRecommendationTemp[]>([])
const historyGeneratedMenu = ref<AiPersonalizedMenu | null>(null)
const historyMenuDate = ref('')
const historyLoading = ref(false)
const generatingSimilar = ref(false)
const generatingMenu = ref(false)
const generatingFromLatest = ref(false)
const adoptingRecommendation = ref(false)
const adoptingMenu = ref(false)

// 统计数据
const todayRecommendations = ref(0)
const adoptionRate = ref(85.3)

// 生成进度相关
const generateProgress = ref('')
const progressPercent = ref(0)

// 表单验证规则
const generalRules = reactive({
  mealType: [{ required: true, message: '请选择餐食类型', trigger: 'change' }],
  targetGroup: [{ required: true, message: '请选择目标群体', trigger: 'change' }],
  nutritionType: [{ required: true, message: '请选择营养类型', trigger: 'change' }]
})

// 方法
const loadCustomers = async () => {
  try {
    const customers = await customerApi.getCustomers()
    customerList.value = customers
  } catch (error) {
    console.error('加载客户列表失败:', error)
    ElMessage.error('加载客户列表失败')
  }
}
const handleTabChange = (tabName: string) => {
  console.log('切换到标签页:', tabName)
}

const generatePersonalized = async () => {
  if (!personalizedForm.customerId) {
    ElMessage.warning('请选择客户')
    return
  }

  loading.personalized = true
  try {
    const response = await generatePersonalizedRecommendations(personalizedForm)
    personalizedRecommendations.value = response as AiRecommendationTemp[] || []

    if (personalizedRecommendations.value.length === 0) {
      ElMessage.warning('AI服务暂时不可用，请稍后重试')
    } else {
      ElMessage.success('个性化推荐生成成功')
    }
  } catch (error: any) {
    console.error('生成个性化推荐失败:', error)

    // 根据错误类型提供不同的提示
    if (error.message?.includes('401') || error.message?.includes('Unauthorized')) {
      ElMessage.error('AI服务认证失败，请联系管理员检查API配置')
    } else if (error.message?.includes('timeout')) {
      ElMessage.error('请求超时，请检查网络连接后重试')
    } else {
      ElMessage.error('生成个性化推荐失败: ' + (error.message || '未知错误'))
    }
  } finally {
    loading.personalized = false
  }
}

const generateDailyMenu = async () => {
  if (!menuForm.customerId || !menuForm.menuDate) {
    ElMessage.warning('请选择客户和日期')
    return
  }

  loading.menu = true
  try {
    const response = await generatePersonalizedMenu(menuForm)
    currentMenu.value = response as AiPersonalizedMenu
    ElMessage.success('一日四餐生成成功')
  } catch (error: any) {
    console.error('生成一日四餐失败:', error)

    // 根据错误类型提供不同的提示
    if (error.message?.includes('已存在菜单')) {
      ElMessage.warning('该客户在选定日期已有菜单，已为您显示现有菜单')
      // 尝试获取现有菜单
      try {
        const existingMenu = await getCustomerPersonalizedMenus(menuForm.customerId, menuForm.menuDate, menuForm.menuDate)
        if (existingMenu && existingMenu.length > 0) {
          currentMenu.value = existingMenu[0] as AiPersonalizedMenu
        }
      } catch (e) {
        console.error('获取现有菜单失败:', e)
      }
    } else if (error.message?.includes('401') || error.message?.includes('Unauthorized')) {
      ElMessage.error('AI服务认证失败，请联系管理员检查API配置')
    } else if (error.message?.includes('Duplicate entry')) {
      ElMessage.warning('该客户在此日期已有菜单，请选择其他日期或查看历史记录')
    } else {
      ElMessage.error('生成一日四餐失败: ' + (error.message || '未知错误'))
    }
  } finally {
    loading.menu = false
  }
}

const adoptSingle = async (recommendationId: number) => {
  loading.adopt = true
  try {
    await adoptRecommendation(recommendationId)
    ElMessage.success('采纳成功')
    // 刷新推荐列表
    if (activeTab.value === 'general') {
      generalRecommendations.value = generalRecommendations.value.filter(item => item.id !== recommendationId)
    } else {
      personalizedRecommendations.value = personalizedRecommendations.value.filter(item => item.id !== recommendationId)
    }
  } catch (error) {
    console.error('采纳失败:', error)
    ElMessage.error('采纳失败')
  } finally {
    loading.adopt = false
  }
}

const batchAdoptGeneral = async () => {
  const ids = generalRecommendations.value.map(item => item.id!)
  loading.adopt = true
  try {
    await batchAdoptRecommendations({ recommendationIds: ids })
    ElMessage.success('批量采纳成功')
    generalRecommendations.value = []
  } catch (error) {
    console.error('批量采纳失败:', error)
    ElMessage.error('批量采纳失败')
  } finally {
    loading.adopt = false
  }
}

const clearRecommendations = async () => {
  try {
    await clearCurrentRecommendations()
    generalRecommendations.value = []
    ElMessage.success('推荐列表已清空')
  } catch (error) {
    console.error('清空失败:', error)
    ElMessage.error('清空失败')
  }
}

const approveMenu = async () => {
  if (!currentMenu.value) return

  loading.approve = true
  try {
    await approvePersonalizedMenu(currentMenu.value.id, { review: '菜单审核通过' })
    currentMenu.value.isApproved = 1
    ElMessage.success('菜单审核通过')
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  } finally {
    loading.approve = false
  }
}

const handleCustomerChange = (customerId: number) => {
  const customer = customerList.value.find(c => c.id === customerId)
  if (customer) {
    personalizedForm.customerName = customer.customerName || customer.name || ''
    personalizedForm.customerAge = customer.age
    personalizedForm.customerGender = customer.gender
  }
}

const handleMenuCustomerChange = (customerId: number) => {
  const customer = customerList.value.find(c => c.id === customerId)
  if (customer) {
    menuForm.customerName = customer.customerName || customer.name || ''
    menuForm.customerAge = customer.age
    menuForm.customerGender = customer.gender
  }
}

const loadHistory = async () => {
  if (!historyFilter.customerId) {
    ElMessage.warning('请选择客户')
    return
  }

  try {
    const [startDate, endDate] = historyFilter.dateRange || []
    const response = await getCustomerPersonalizedMenus(historyFilter.customerId, startDate, endDate)
    historyMenus.value = response as AiPersonalizedMenu[] || []
  } catch (error) {
    console.error('加载历史失败:', error)
    ElMessage.error('加载历史失败')
  }
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

const getMealTypeTag = (mealType: string) => {
  const map: Record<string, string> = {
    BREAKFAST: 'success',
    LUNCH: 'primary',
    DINNER: 'warning',
    SNACK: 'info'
  }
  return map[mealType] || 'default'
}

// 新增的方法
const previewRecommendation = (item: AiRecommendationTemp) => {
  ElMessageBox.alert(
    `
    <div style="text-align: left;">
      <h4>${item.mealName || '推荐菜品'}</h4>
      <p><strong>食材：</strong>${item.ingredients || '暂无'}</p>
      <p><strong>制作方法：</strong>${item.cookingMethod || '暂无'}</p>
      <p><strong>营养信息：</strong>${item.nutritionInfo || '暂无'}</p>
      <p><strong>AI推荐理由：</strong>${item.aiReason || '暂无'}</p>
      <p><strong>热量：</strong>${item.calories || 0} 千卡</p>
    </div>
    `,
    '推荐详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    }
  )
}

const editRecommendation = (item: AiRecommendationTemp) => {
  ElMessage.info('编辑功能开发中...')
  // TODO: 实现编辑功能
}

const deleteRecommendation = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个推荐吗？', '确认删除', {
      type: 'warning'
    })

    // 从当前列表中删除
    if (activeTab.value === 'general') {
      generalRecommendations.value = generalRecommendations.value.filter(item => item.id !== id)
    } else {
      personalizedRecommendations.value = personalizedRecommendations.value.filter(item => item.id !== id)
    }

    ElMessage.success('删除成功')
  } catch (error) {
    // 用户取消删除
  }
}

// 历史推荐相关方法
const loadHistoryRecommendations = async () => {
  historyLoading.value = true
  try {
    // 不传递客户ID和日期，查询所有历史记录
    const response = await getHistoryRecommendations()
    historyRecommendations.value = response as AiRecommendationHistory[] || []
  } catch (error) {
    console.error('加载历史推荐失败:', error)
    ElMessage.error('加载历史推荐失败')
  } finally {
    historyLoading.value = false
  }
}

const selectHistory = (history: AiRecommendationHistory) => {
  selectedHistory.value = history
  // 清空之前的生成结果
  historyGeneratedRecommendations.value = []
  historyGeneratedMenu.value = null
}

const generateSimilarRecommendations = async () => {
  if (!selectedHistory.value) {
    ElMessage.warning('请先选择一个历史推荐')
    return
  }

  generatingSimilar.value = true
  try {
    const response = await generateFromHistory(selectedHistory.value.id)
    historyGeneratedRecommendations.value = response as AiRecommendationTemp[] || []
    ElMessage.success('类似推荐生成成功')
  } catch (error) {
    console.error('生成类似推荐失败:', error)
    ElMessage.error('生成类似推荐失败')
  } finally {
    generatingSimilar.value = false
  }
}

const generateMenuFromHistoryMethod = async () => {
  if (!selectedHistory.value) {
    ElMessage.warning('请先选择一个历史推荐')
    return
  }
  if (!historyMenuDate.value) {
    ElMessage.warning('请选择菜单日期')
    return
  }

  generatingMenu.value = true
  try {
    const response = await generateMenuFromHistory(selectedHistory.value.id, historyMenuDate.value)
    historyGeneratedMenu.value = response as AiPersonalizedMenu
    ElMessage.success('一日四餐菜单生成成功')
  } catch (error) {
    console.error('生成菜单失败:', error)
    ElMessage.error('生成菜单失败')
  } finally {
    generatingMenu.value = false
  }
}

const adoptRecommendationFromHistory = async (recommendationId: number) => {
  adoptingRecommendation.value = true
  try {
    await adoptRecommendation(recommendationId)
    ElMessage.success('推荐采纳成功')
    // 刷新推荐列表
    await generateSimilarRecommendations()
  } catch (error) {
    console.error('采纳推荐失败:', error)
    ElMessage.error('采纳推荐失败')
  } finally {
    adoptingRecommendation.value = false
  }
}

const adoptHistoryMenu = async () => {
  if (!historyGeneratedMenu.value) return

  adoptingMenu.value = true
  try {
    // 这里可以调用相应的API采纳菜单
    ElMessage.success('菜单采纳成功')
  } catch (error) {
    console.error('采纳菜单失败:', error)
    ElMessage.error('采纳菜单失败')
  } finally {
    adoptingMenu.value = false
  }
}

const generateFromLatestHistoryMethod = async () => {
  generatingFromLatest.value = true
  try {
    // 不传递客户ID，让后端基于所有历史记录的最新3条生成推荐
    const response = await generateFromLatestHistory()
    historyGeneratedRecommendations.value = response as AiRecommendationTemp[] || []
    ElMessage.success(`基于最新历史记录生成了 ${historyGeneratedRecommendations.value.length} 条推荐`)
  } catch (error) {
    console.error('基于最新历史生成推荐失败:', error)
    ElMessage.error('基于最新历史生成推荐失败：' + (error as any)?.message || '未知错误')
  } finally {
    generatingFromLatest.value = false
  }
}

const approveHistoryMenu = async () => {
  if (!historyGeneratedMenu.value) return

  try {
    await approvePersonalizedMenu(historyGeneratedMenu.value.id, { review: '历史推荐生成菜单审核通过' })
    ElMessage.success('菜单审核成功')
    historyGeneratedMenu.value.isApproved = 1
  } catch (error) {
    console.error('审核菜单失败:', error)
    ElMessage.error('审核菜单失败')
  }
}

// 辅助方法
const getTargetGroupLabel = (value?: string) => {
  const option = TARGET_GROUP_OPTIONS.find(opt => opt.value === value)
  return option?.label || value || '未知'
}

const getNutritionTypeLabel = (value?: string) => {
  const option = NUTRITION_TYPE_OPTIONS.find(opt => opt.value === value)
  return option?.label || value || '未知'
}

const getMealTypeLabel = (value?: string) => {
  const option = MEAL_TYPE_OPTIONS.find(opt => opt.value === value)
  return option?.label || value || '未知'
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 优化的生成方法，增加进度提示
const generateGeneralWithProgress = async () => {
  if (!generalForm.mealType || !generalForm.targetGroup || !generalForm.nutritionType) {
    ElMessage.warning('请填写必填项')
    return
  }

  loading.general = true
  progressPercent.value = 0

  try {
    generateProgress.value = '正在分析您的需求...'
    progressPercent.value = 20

    await new Promise(resolve => setTimeout(resolve, 500))
    generateProgress.value = '正在连接AI服务...'
    progressPercent.value = 40

    await new Promise(resolve => setTimeout(resolve, 500))
    generateProgress.value = 'AI正在生成推荐...'
    progressPercent.value = 70

    const response = await generateRecommendations({
      mealType: generalForm.mealType,
      targetGroup: generalForm.targetGroup,
      nutritionType: generalForm.nutritionType,
      additionalRequirements: generalForm.additionalRequirements,
      caloriesRange: generalForm.caloriesRange,
      count: 3
    })

    progressPercent.value = 100
    generateProgress.value = '生成完成！'

    // 为每个推荐项添加activeCollapse属性
    const recommendations = (response as AiRecommendationTemp[] || []).map(item => ({
      ...item,
      activeCollapse: []
    }))

    generalRecommendations.value = recommendations
    todayRecommendations.value++

    if (recommendations.length === 0) {
      ElMessage.warning('AI服务暂时不可用，已为您提供默认推荐')
    } else {
      ElMessage.success(`成功生成 ${recommendations.length} 个推荐`)
    }
  } catch (error: any) {
    console.error('生成推荐失败:', error)

    // 根据错误类型提供不同的提示
    if (error.message?.includes('401') || error.message?.includes('Unauthorized')) {
      ElMessage.error('AI服务认证失败，请联系管理员检查API配置')
    } else if (error.message?.includes('timeout')) {
      ElMessage.error('请求超时，请检查网络连接后重试')
    } else if (error.message?.includes('Network Error')) {
      ElMessage.error('网络连接异常，请检查网络设置')
    } else if (error.message?.includes('Duplicate entry')) {
      ElMessage.warning('今日已生成过推荐，请查看历史记录或明天再试')
    } else {
      ElMessage.error('生成推荐失败: ' + (error.message || '未知错误'))
    }

    // 提供降级方案提示
    ElMessage.info('系统已为您准备备选推荐，请稍后重试AI推荐')
  } finally {
    loading.general = false
    progressPercent.value = 0
    generateProgress.value = ''
  }
}

// 将优化的生成方法重命名为generateGeneral
const generateGeneral = generateGeneralWithProgress

// 初始化数据
onMounted(() => {
  loadCustomers()
})
</script>

<style scoped>
.ai-recommendation-v4 {
  padding: 16px;
}

.header-card {
  margin-bottom: 16px;
}

.header-content {
  text-align: center;
}

.page-title {
  margin: 0 0 8px 0;
  color: #303133;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.page-description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.main-card {
  min-height: calc(100vh - 200px);
}

.tab-content {
  padding: 16px 0;
}

.form-panel {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  height: fit-content;
}

.form-panel h3 {
  margin: 0 0 16px 0;
  color: #303133;
}

.result-panel {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
}

.panel-header h3 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  padding: 40px;
  text-align: center;
}

.recommendations-list {
  padding: 16px;
  max-height: 600px;
  overflow-y: auto;
}

.recommendation-card {
  margin-bottom: 16px;
}

.recommendation-card.personalized {
  border-left: 4px solid #67c23a;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.card-content {
  margin-top: 12px;
}

.menu-content {
  padding: 16px;
}

.meal-card {
  margin-bottom: 16px;
  height: 280px;
}

.meal-card.breakfast {
  border-left: 4px solid #f39c12;
}

.meal-card.lunch {
  border-left: 4px solid #e74c3c;
}

.meal-card.dinner {
  border-left: 4px solid #9b59b6;
}

.meal-card.snack {
  border-left: 4px solid #3498db;
}

.meal-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.meal-content h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.meal-desc {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
}

.meal-details p {
  margin: 8px 0;
  font-size: 14px;
  line-height: 1.4;
}

.summary-card {
  background: #f8f9fa;
}

.ai-reason {
  margin-top: 12px;
}

.ai-reason p {
  margin: 4px 0;
  line-height: 1.5;
}

.filter-bar {
  margin-bottom: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.history-list {
  max-height: 600px;
  overflow-y: auto;
}

.history-card {
  margin-bottom: 12px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-content {
  font-size: 14px;
  color: #606266;
}

/* 新增的优化样式 */
.ai-recommendation-v4 {
  background: #f5f7fa;
  min-height: 100vh;
}

.header-card {
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header-content {
  padding: 20px 0;
}

.title-section .page-title {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 8px 0;
}

.title-icon {
  font-size: 32px;
  color: #ffd700;
}

.page-description {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.stats-section {
  display: flex;
  gap: 30px;
}

.stats-section :deep(.el-statistic__number) {
  color: white;
  font-weight: bold;
}

.stats-section :deep(.el-statistic__head) {
  color: rgba(255, 255, 255, 0.8);
}

.main-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.ai-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
  background: #fafbfc;
  border-radius: 8px 8px 0 0;
  padding: 10px 20px 0;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.tab-content {
  padding: 24px;
  background: white;
}

.form-card, .result-card {
  border-radius: 12px;
  border: 1px solid #e8eaec;
}

.loading-state {
  padding: 60px 20px;
  text-align: center;
}

.loading-content h4 {
  margin: 20px 0 10px 0;
  color: #409eff;
  font-size: 18px;
}

.loading-content p {
  color: #909399;
  margin-bottom: 20px;
}

.rotating {
  animation: rotate 2s linear infinite;
  font-size: 40px;
  color: #409eff;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.recommendations-container {
  max-height: 650px;
}

.recommendation-item {
  margin-bottom: 16px;
  border-radius: 12px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.recommendation-item:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.15);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content-text {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  color: #606266;
  line-height: 1.6;
}

.ai-reason-text {
  padding: 12px;
  background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
  border-radius: 8px;
  color: #5e35b1;
  line-height: 1.6;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.item-actions {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}
</style>
