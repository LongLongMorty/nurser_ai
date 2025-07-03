

-- 1. AI推荐临时表（用于显示当前推荐，刷新时清空）
CREATE TABLE `ai_recommendation_temp` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
    `customer_name` varchar(100) DEFAULT NULL COMMENT '客户姓名',
    `customer_age` int DEFAULT NULL COMMENT '客户年龄',
    `customer_gender` varchar(10) DEFAULT NULL COMMENT '客户性别：MALE-男性，FEMALE-女性',
    `session_id` varchar(100) NOT NULL COMMENT '会话ID（用于一次推荐的分组）',
    `meal_name` varchar(200) NOT NULL COMMENT '食谱名称',
    `meal_type` varchar(50) NOT NULL COMMENT '餐食类型：BREAKFAST-早餐，LUNCH-午餐，DINNER-晚餐，SNACK-加餐',
    `description` text COMMENT '食谱描述',
    `main_food` varchar(200) DEFAULT NULL COMMENT '主食',
    `side_dishes` varchar(500) DEFAULT NULL COMMENT '副菜',
    `soup` varchar(200) DEFAULT NULL COMMENT '汤类',
    `fruits` varchar(200) DEFAULT NULL COMMENT '水果',
    `ingredients` text COMMENT '食材清单',
    `cooking_method` text COMMENT '制作方法',
    `calories` decimal(8,2) DEFAULT NULL COMMENT '热量（千卡）',
    `protein` decimal(8,2) DEFAULT NULL COMMENT '蛋白质（克）',
    `fat` decimal(8,2) DEFAULT NULL COMMENT '脂肪（克）',
    `carbohydrate` decimal(8,2) DEFAULT NULL COMMENT '碳水化合物（克）',
    `nutrition_info` text COMMENT '营养信息详情',
    `suitable_crowd` varchar(200) DEFAULT NULL COMMENT '适宜人群',
    `target_group` varchar(100) DEFAULT NULL COMMENT '目标群体：ELDERLY-老年人，DIABETIC-糖尿病，HYPERTENSION-高血压等',
    `dietary_restrictions` varchar(200) DEFAULT NULL COMMENT '饮食禁忌',
    `estimated_cost` decimal(8,2) DEFAULT NULL COMMENT '预估成本（元）',
    `ai_score` decimal(3,1) DEFAULT NULL COMMENT 'AI评分（1-10分）',
    `ai_reason` text COMMENT 'AI推荐理由',
    `ai_model` varchar(50) DEFAULT 'deepseek' COMMENT '使用的AI模型',
    `nutrition_type` varchar(50) DEFAULT NULL COMMENT '营养类型：HIGH_PROTEIN-高蛋白，LOW_SODIUM-低钠等',
    `additional_requirements` text COMMENT '附加要求',
    `is_from_history` tinyint(1) DEFAULT 0 COMMENT '是否来自历史推荐：0-否，1-是',
    `history_ref_id` bigint DEFAULT NULL COMMENT '历史记录引用ID',
    `user_id` bigint DEFAULT NULL COMMENT '生成推荐的用户ID',
    `user_age` int DEFAULT NULL COMMENT '用户年龄（个性化推荐）',
    `user_gender` varchar(10) DEFAULT NULL COMMENT '用户性别（个性化推荐）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_meal_type` (`meal_type`),
    KEY `idx_target_group` (`target_group`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI推荐临时表';

-- 2. AI推荐采纳历史表（记录所有被采纳的推荐）
CREATE TABLE `ai_recommendation_history` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `meal_config_id` bigint NOT NULL COMMENT '关联的meal_config表ID',
    `original_ai_content` text COMMENT '原始AI推荐内容（JSON格式）',
    `meal_name` varchar(200) NOT NULL COMMENT '食谱名称',
    `meal_type` varchar(50) NOT NULL COMMENT '餐食类型',
    `target_group` varchar(100) DEFAULT NULL COMMENT '目标群体',
    `nutrition_type` varchar(50) DEFAULT NULL COMMENT '营养类型',
    `ai_score` decimal(3,1) DEFAULT NULL COMMENT 'AI评分',
    `user_rating` decimal(3,1) DEFAULT NULL COMMENT '用户评分（1-10分）',
    `adoption_date` datetime NOT NULL COMMENT '采纳时间',
    `adopted_by_user_id` bigint NOT NULL COMMENT '采纳人用户ID',
    `adopted_by_user_name` varchar(100) DEFAULT NULL COMMENT '采纳人姓名',
    `effectiveness_score` decimal(3,1) DEFAULT NULL COMMENT '效果评分（后续反馈）',
    `is_recommended_again` tinyint(1) DEFAULT 1 COMMENT '是否可再次推荐：0-否，1-是',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_meal_config_id` (`meal_config_id`),
    KEY `idx_target_group` (`target_group`),
    KEY `idx_adoption_date` (`adoption_date`),
    KEY `idx_user_rating` (`user_rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI推荐采纳历史表';

-- 3. 客户个性化菜单表（为特定客户生成的一天四餐）
CREATE TABLE `ai_personalized_menu` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `customer_id` bigint NOT NULL COMMENT '客户ID',
    `customer_name` varchar(100) NOT NULL COMMENT '客户姓名',
    `customer_age` int DEFAULT NULL COMMENT '客户年龄',
    `customer_gender` varchar(10) DEFAULT NULL COMMENT '客户性别：MALE-男性，FEMALE-女性',
    `menu_date` date NOT NULL COMMENT '菜单日期',
    `session_id` varchar(100) NOT NULL COMMENT '生成会话ID',
    `health_conditions` text COMMENT '健康状况描述',
    `dietary_preferences` text COMMENT '饮食偏好',
    `allergies` text COMMENT '过敏信息',
    
    -- 早餐
    `breakfast_name` varchar(200) DEFAULT NULL COMMENT '早餐名称',
    `breakfast_description` text COMMENT '早餐描述',
    `breakfast_ingredients` text COMMENT '早餐食材',
    `breakfast_cooking_method` text COMMENT '早餐制作方法',
    `breakfast_calories` decimal(8,2) DEFAULT NULL COMMENT '早餐热量',
    `breakfast_nutrition` text COMMENT '早餐营养信息',
    
    -- 午餐
    `lunch_name` varchar(200) DEFAULT NULL COMMENT '午餐名称',
    `lunch_description` text COMMENT '午餐描述',
    `lunch_ingredients` text COMMENT '午餐食材',
    `lunch_cooking_method` text COMMENT '午餐制作方法',
    `lunch_calories` decimal(8,2) DEFAULT NULL COMMENT '午餐热量',
    `lunch_nutrition` text COMMENT '午餐营养信息',
    
    -- 晚餐
    `dinner_name` varchar(200) DEFAULT NULL COMMENT '晚餐名称',
    `dinner_description` text COMMENT '晚餐描述',
    `dinner_ingredients` text COMMENT '晚餐食材',
    `dinner_cooking_method` text COMMENT '晚餐制作方法',
    `dinner_calories` decimal(8,2) DEFAULT NULL COMMENT '晚餐热量',
    `dinner_nutrition` text COMMENT '晚餐营养信息',
    
    -- 加餐
    `snack_name` varchar(200) DEFAULT NULL COMMENT '加餐名称',
    `snack_description` text COMMENT '加餐描述',
    `snack_ingredients` text COMMENT '加餐食材',
    `snack_cooking_method` text COMMENT '加餐制作方法',
    `snack_calories` decimal(8,2) DEFAULT NULL COMMENT '加餐热量',
    `snack_nutrition` text COMMENT '加餐营养信息',
    
    -- 总计信息
    `total_calories` decimal(8,2) DEFAULT NULL COMMENT '一日总热量',
    `total_cost` decimal(8,2) DEFAULT NULL COMMENT '预估总成本',
    `ai_recommendation_reason` text COMMENT 'AI推荐理由',
    `ai_raw_response` text COMMENT 'AI原始响应内容',
    `nutritionist_review` text COMMENT '营养师审核意见',
    
    `is_approved` tinyint(1) DEFAULT 0 COMMENT '是否已审核：0-待审核，1-已审核',
    `approved_by` bigint DEFAULT NULL COMMENT '审核人ID',
    `approved_time` datetime DEFAULT NULL COMMENT '审核时间',
    
    `created_by_user_id` bigint NOT NULL COMMENT '创建人用户ID',
    `created_by_user_name` varchar(100) DEFAULT NULL COMMENT '创建人姓名',
    
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_customer_date` (`customer_id`, `menu_date`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_menu_date` (`menu_date`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户个性化菜单表';

-- 4. AI调用日志表（记录API调用情况）
CREATE TABLE `ai_call_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` varchar(100) NOT NULL COMMENT '会话ID',
    `api_type` varchar(50) NOT NULL COMMENT 'API类型：GENERAL-通用推荐，PERSONALIZED-个性化推荐',
    `request_params` text COMMENT '请求参数（JSON格式）',
    `ai_model` varchar(50) DEFAULT 'deepseek' COMMENT 'AI模型名称',
    `api_url` varchar(500) DEFAULT NULL COMMENT 'API调用地址',
    `request_time` datetime NOT NULL COMMENT '请求时间',
    `response_time` datetime DEFAULT NULL COMMENT '响应时间',
    `response_duration` int DEFAULT NULL COMMENT '响应耗时（毫秒）',
    `response_status` varchar(20) DEFAULT NULL COMMENT '响应状态：SUCCESS-成功，ERROR-失败',
    `response_content` text COMMENT '响应内容',
    `error_message` text COMMENT '错误信息',
    `token_usage` int DEFAULT NULL COMMENT 'Token使用量',
    `api_cost` decimal(10,4) DEFAULT NULL COMMENT 'API调用成本',
    `user_id` bigint DEFAULT NULL COMMENT '调用用户ID',
    `customer_id` bigint DEFAULT NULL COMMENT '关联客户ID（个性化推荐时）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_api_type` (`api_type`),
    KEY `idx_request_time` (`request_time`),
    KEY `idx_response_status` (`response_status`),
    KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI调用日志表';



-- 创建索引优化查询性能
-- 临时推荐表索引
CREATE INDEX `idx_temp_customer_age_gender` ON `ai_recommendation_temp` (`customer_age`, `customer_gender`);
CREATE INDEX `idx_temp_is_from_history` ON `ai_recommendation_temp` (`is_from_history`);

-- 个性化菜单表索引
CREATE INDEX `idx_menu_customer_age_gender` ON `ai_personalized_menu` (`customer_age`, `customer_gender`);
CREATE INDEX `idx_menu_approved` ON `ai_personalized_menu` (`is_approved`);

-- 历史表索引
CREATE INDEX `idx_history_effectiveness` ON `ai_recommendation_history` (`effectiveness_score`);
CREATE INDEX `idx_history_recommend_again` ON `ai_recommendation_history` (`is_recommended_again`);

-- 日志表索引
CREATE INDEX `idx_log_duration` ON `ai_call_log` (`response_duration`);
CREATE INDEX `idx_log_cost` ON `ai_call_log` (`api_cost`);

-- 完成
SELECT 'AI智能食谱推荐系统V4表结构创建完成！' as result;
