# AI智能食谱推荐系统

## 功能概述

AiRecommendationView.vue 是护理中心系统中的AI智能食谱推荐模块，集成了DeepSeek AI API，能够根据用户选择的条件实时生成营养均衡的膳食推荐。

## 主要功能

### 1. 智能推荐生成
- **实时表单响应**: 监听菜品类型、营养要求、适用人群的选择变化
- **DeepSeek AI集成**: 调用DeepSeek API生成专业的营养师级别推荐
- **多种兜底方案**: 当API不可用时，提供智能模拟推荐

### 2. 推荐管理
- **列表展示**: 显示所有AI推荐，包括菜品信息、营养数据、评分等
- **状态管理**: 支持采纳、拒绝、反馈等操作
- **批量处理**: 可批量采纳或拒绝多个推荐

### 3. SQL自动生成
- **meal_config格式**: 生成符合数据库表结构的SQL插入语句
- **实时显示**: 推荐生成后立即显示SQL语句
- **一键复制**: 支持复制SQL到剪贴板，便于数据库操作

### 4. 推荐记录
- **历史记录**: 保存最近10条生成的推荐
- **SQL查看**: 每条记录都包含对应的SQL插入语句
- **快速复制**: 单独复制每条记录的SQL

## 技术特性

### API集成
```typescript
// AI推荐API调用
const recommendations = await aiRecommendationApi.generateRecommendation({
  dishType: 'BREAKFAST',
  nutritionType: 'HIGH_PROTEIN',
  targetGroup: 'DIABETES'
})
```

### DeepSeek配置
```typescript
export const deepSeekConfig = {
  apiKey: import.meta.env.VITE_DEEPSEEK_API_KEY,
  baseURL: 'https://api.deepseek.com/v1',
  model: 'deepseek-chat'
}
```

### 数据结构
```typescript
interface AiRecommendationResponse {
  // 基础信息
  id: number
  dishName: string
  dishType: string
  
  // meal_config表字段
  mealName: string
  mealType: string
  description?: string
  mainFood?: string
  sideDishes?: string
  soup?: string
  fruits?: string
  
  // 营养信息
  calories: number
  protein: number
  fat: number
  carbohydrate: number
  nutritionInfo?: string
  
  // AI相关
  aiScore: number
  aiReason?: string
  sqlInsert?: string // 自动生成的SQL语句
}
```

## 使用流程

### 1. 选择条件
1. 选择菜品类型（早餐/午餐/晚餐/加餐）
2. 可选择营养要求（高蛋白/低盐/低糖/低脂/高纤维）
3. 可选择适用人群（糖尿病/高血压/心脏病/肾病/一般老人）

### 2. 生成推荐
1. 点击"生成推荐"按钮
2. 系统调用DeepSeek AI API
3. 生成专业营养师级别的推荐
4. 自动显示SQL插入语句

### 3. 管理推荐
1. 查看详细信息（营养分析、制作方法、AI推荐理由）
2. 采纳推荐（自动添加到膳食配置）
3. 拒绝推荐（标记为不适用）
4. 提供反馈（评分和建议）

### 4. SQL操作
1. 查看生成的SQL语句
2. 复制到剪贴板
3. 在数据库中执行，添加到meal_config表

## 环境配置

### 1. 环境变量
在 `.env` 文件中配置DeepSeek API密钥：
```
VITE_DEEPSEEK_API_KEY=your_deepseek_api_key
```

### 2. 后端API
确保以下API端点可用：
- `POST /api/ai/recommendation/generate` - 生成推荐
- `GET /api/ai/recommendation/list` - 获取推荐列表
- `POST /api/ai/recommendation/{id}/adopt` - 采纳推荐
- `POST /api/ai/recommendation/{id}/reject` - 拒绝推荐
- `POST /api/ai/recommendation/feedback` - 提交反馈

## 数据库表结构

推荐生成的SQL语句将数据插入到以下表：

### meal_config表
```sql
CREATE TABLE meal_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  meal_name VARCHAR(100) NOT NULL COMMENT '膳食名称',
  meal_type VARCHAR(20) NOT NULL COMMENT '膳食类型',
  description TEXT COMMENT '描述',
  main_food VARCHAR(200) COMMENT '主食',
  side_dishes VARCHAR(200) COMMENT '副菜',
  soup VARCHAR(100) COMMENT '汤品',
  fruits VARCHAR(100) COMMENT '水果',
  nutrition_info TEXT COMMENT '营养信息',
  suitable_crowd VARCHAR(100) COMMENT '适用人群',
  dietary_restrictions VARCHAR(200) COMMENT '饮食禁忌',
  cooking_method TEXT COMMENT '制作方法',
  estimated_cost DECIMAL(10,2) COMMENT '预估成本',
  status TINYINT DEFAULT 1 COMMENT '状态',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### ai_recommendation_log表（记录AI推荐）
```sql
CREATE TABLE ai_recommendation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  request_params JSON COMMENT '请求参数',
  ai_response JSON COMMENT 'AI响应',
  generated_sql TEXT COMMENT '生成的SQL',
  user_id BIGINT COMMENT '用户ID',
  status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 特色功能

### 1. 实时表单监听
系统会监听表单变化，当用户选择不同条件时，会实时提示可以生成对应的推荐。

### 2. 多层兜底机制
- 优先调用DeepSeek API
- API失败时使用智能模拟推荐
- 确保用户始终能获得推荐结果

### 3. 专业营养分析
生成的推荐包含：
- 详细营养成分分析
- 适用人群建议
- 专业制作方法
- AI推荐理由

### 4. 便捷的SQL生成
自动生成符合meal_config表结构的SQL语句，方便直接导入数据库。

## 未来扩展

1. **多AI模型支持**: 集成更多AI模型，提供多样化推荐
2. **个性化推荐**: 基于用户历史偏好优化推荐算法
3. **营养成分计算**: 自动计算详细的营养成分数据
4. **图片生成**: 集成AI图片生成，为推荐菜品生成图片
5. **语音交互**: 支持语音输入需求，语音播报推荐结果
