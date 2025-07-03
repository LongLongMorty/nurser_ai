-- 东软颐养中心管理系统数据库脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS nursing_center DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE nursing_center;

-- 1. 系统用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `username` varchar(50) NOT NULL COMMENT '用户名',
                            `password` varchar(255) NOT NULL COMMENT '密码',
                            `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
                            `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                            `role` varchar(20) NOT NULL COMMENT '角色：ADMIN-管理员, HEALTH_MANAGER-健康管家',
                            `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 2. 楼栋表
DROP TABLE IF EXISTS `building`;
CREATE TABLE `building` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `building_no` varchar(10) NOT NULL COMMENT '楼栋编号',
                            `building_name` varchar(50) NOT NULL COMMENT '楼栋名称',
                            `floor_count` int DEFAULT '0' COMMENT '楼层数',
                            `status` tinyint DEFAULT '1' COMMENT '状态：0-停用，1-启用',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_building_no` (`building_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='楼栋表';

-- 3. 房间表
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                        `building_id` bigint NOT NULL COMMENT '楼栋ID',
                        `room_no` varchar(10) NOT NULL COMMENT '房间号',
                        `room_name` varchar(50) DEFAULT NULL COMMENT '房间名称',
                        `floor_no` int NOT NULL COMMENT '楼层号',
                        `bed_count` int DEFAULT '0' COMMENT '床位数量',
                        `room_type` varchar(20) DEFAULT 'STANDARD' COMMENT '房间类型：STANDARD-标准间，VIP-贵宾间',
                        `status` tinyint DEFAULT '1' COMMENT '状态：0-停用，1-启用',
                        `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                        PRIMARY KEY (`id`),
                        KEY `idx_building_id` (`building_id`),
                        UNIQUE KEY `uk_building_room` (`building_id`, `room_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间表';

-- 4. 床位表
DROP TABLE IF EXISTS `bed`;
CREATE TABLE `bed` (
                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                       `room_id` bigint NOT NULL COMMENT '房间ID',
                       `bed_no` varchar(10) NOT NULL COMMENT '床位号',
                       `bed_type` varchar(20) DEFAULT 'STANDARD' COMMENT '床位类型：STANDARD-标准床，CARE-护理床',
                       `bed_status` varchar(20) DEFAULT 'AVAILABLE' COMMENT '床位状态：AVAILABLE-空闲，OCCUPIED-有人，OUT-外出',
                       `status` tinyint DEFAULT '1' COMMENT '状态：0-停用，1-启用',
                       `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                       `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                       PRIMARY KEY (`id`),
                       KEY `idx_room_id` (`room_id`),
                       UNIQUE KEY `uk_room_bed` (`room_id`, `bed_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='床位表';

-- 5. 客户表
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `customer_name` varchar(50) NOT NULL COMMENT '客户姓名',
                            `age` int DEFAULT NULL COMMENT '年龄',
                            `gender` varchar(10) NOT NULL COMMENT '性别：MALE-男，FEMALE-女',
                            `id_card` varchar(18) NOT NULL COMMENT '身份证号',
                            `birth_date` date DEFAULT NULL COMMENT '出生日期',
                            `blood_type` varchar(10) DEFAULT NULL COMMENT '血型：A、B、AB、O',
                            `guardian_name` varchar(50) DEFAULT NULL COMMENT '家属姓名',
                            `guardian_phone` varchar(20) DEFAULT NULL COMMENT '家属联系电话',
                            `building_id` bigint DEFAULT NULL COMMENT '楼栋ID',
                            `room_id` bigint DEFAULT NULL COMMENT '房间ID',
                            `bed_id` bigint DEFAULT NULL COMMENT '床位ID',
                            `check_in_date` date DEFAULT NULL COMMENT '入住时间',
                            `contract_expire_date` date DEFAULT NULL COMMENT '合同到期时间',
                            `care_level_id` bigint DEFAULT NULL COMMENT '护理级别ID',
                            `health_manager_id` bigint DEFAULT NULL COMMENT '健康管家ID',
                            `customer_type` varchar(20) DEFAULT 'SELF_CARE' COMMENT '客户类型：SELF_CARE-自理老人，CARE-护理老人',
                            `status` tinyint DEFAULT '1' COMMENT '状态：0-已退住，1-在住',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_id_card` (`id_card`),
                            KEY `idx_health_manager_id` (`health_manager_id`),
                            KEY `idx_care_level_id` (`care_level_id`),
                            KEY `idx_bed_id` (`bed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- 6. 床位使用详情表
DROP TABLE IF EXISTS `bed_usage`;
CREATE TABLE `bed_usage` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `customer_id` bigint NOT NULL COMMENT '客户ID',
                             `bed_id` bigint NOT NULL COMMENT '床位ID',
                             `room_id` bigint NOT NULL COMMENT '房间ID',
                             `building_id` bigint NOT NULL COMMENT '楼栋ID',
                             `start_date` date NOT NULL COMMENT '入住开始时间',
                             `end_date` date DEFAULT NULL COMMENT '入住结束时间',
                             `usage_status` varchar(20) DEFAULT 'USING' COMMENT '使用状态：USING-正在使用，HISTORY-使用历史',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                             PRIMARY KEY (`id`),
                             KEY `idx_customer_id` (`customer_id`),
                             KEY `idx_bed_id` (`bed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='床位使用详情表';

-- 7. 护理级别表
DROP TABLE IF EXISTS `care_level`;
CREATE TABLE `care_level` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `level_name` varchar(50) NOT NULL COMMENT '护理级别名称',
                              `level_code` varchar(20) NOT NULL COMMENT '护理级别编码',
                              `description` text COMMENT '描述',
                              `status` tinyint DEFAULT '1' COMMENT '状态：0-停用，1-启用',
                              `sort` int DEFAULT '0' COMMENT '排序值，数值越小排序越靠前',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_level_code` (`level_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='护理级别表';

-- 8. 护理项目表
DROP TABLE IF EXISTS `care_item`;
CREATE TABLE `care_item` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `item_code` varchar(20) NOT NULL COMMENT '项目编号',
                             `item_name` varchar(100) NOT NULL COMMENT '项目名称',
                             `price` decimal(10,2) DEFAULT '0.00' COMMENT '价格',
                             `execute_cycle` int DEFAULT '1' COMMENT '执行周期（天）',
                             `execute_times` int DEFAULT '1' COMMENT '执行次数',
                             `description` text COMMENT '描述',
                             `status` tinyint DEFAULT '1' COMMENT '状态：0-停用，1-启用',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_item_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='护理项目表';

-- 9. 护理级别项目关联表
DROP TABLE IF EXISTS `care_level_item`;
CREATE TABLE `care_level_item` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `care_level_id` bigint NOT NULL COMMENT '护理级别ID',
                                   `care_item_id` bigint NOT NULL COMMENT '护理项目ID',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_level_item` (`care_level_id`, `care_item_id`),
                                   KEY `idx_care_level_id` (`care_level_id`),
                                   KEY `idx_care_item_id` (`care_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='护理级别项目关联表';

-- 10. 客户护理服务表
DROP TABLE IF EXISTS `customer_care`;
CREATE TABLE `customer_care` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `customer_id` bigint NOT NULL COMMENT '客户ID',
                                 `care_item_id` bigint NOT NULL COMMENT '护理项目ID',
                                 `purchase_date` date NOT NULL COMMENT '购买服务日期',
                                 `purchase_quantity` int DEFAULT '1' COMMENT '购买数量',
                                 `used_quantity` int DEFAULT '0' COMMENT '已使用数量',
                                 `remaining_quantity` int DEFAULT '0' COMMENT '剩余数量',
                                 `expire_date` date NOT NULL COMMENT '服务到期日期',
                                 `service_status` varchar(20) DEFAULT 'NORMAL' COMMENT '服务状态：NORMAL-正常，EXPIRED-到期，ARREARS-欠费，USED_UP-用完',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_customer_id` (`customer_id`),
                                 KEY `idx_care_item_id` (`care_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户护理服务表';

-- 11. 护理记录表
DROP TABLE IF EXISTS `care_record`;
CREATE TABLE `care_record` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `customer_id` bigint NOT NULL COMMENT '客户ID',
                               `care_item_id` bigint NOT NULL COMMENT '护理项目ID',
                               `health_manager_id` bigint NOT NULL COMMENT '健康管家ID',
                               `care_time` datetime NOT NULL COMMENT '护理时间',
                               `care_quantity` int DEFAULT '1' COMMENT '护理数量',
                               `care_content` text COMMENT '护理内容',
                               `remark` text COMMENT '备注',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                               PRIMARY KEY (`id`),
                               KEY `idx_customer_id` (`customer_id`),
                               KEY `idx_care_item_id` (`care_item_id`),
                               KEY `idx_health_manager_id` (`health_manager_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='护理记录表';

-- 12. 退住申请表
DROP TABLE IF EXISTS `checkout_apply`;
CREATE TABLE `checkout_apply` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `customer_id` bigint NOT NULL COMMENT '客户ID',
                                  `applicant_id` bigint NOT NULL COMMENT '申请人ID（健康管家）',
                                  `checkout_type` varchar(20) NOT NULL COMMENT '退住类型：NORMAL-正常退住，DEATH-死亡退住，RESERVE-保留床位',
                                  `checkout_reason` text COMMENT '退住原因',
                                  `checkout_date` date NOT NULL COMMENT '退住时间',
                                  `apply_status` varchar(20) DEFAULT 'SUBMITTED' COMMENT '申请状态：SUBMITTED-已提交，APPROVED-通过，REJECTED-不通过',
                                  `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
                                  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
                                  `approve_remark` text COMMENT '审批备注',
                                  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_customer_id` (`customer_id`),
                                  KEY `idx_applicant_id` (`applicant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退住申请表';

-- 13. 外出申请表
DROP TABLE IF EXISTS `outing_apply`;
CREATE TABLE `outing_apply` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `customer_id` bigint NOT NULL COMMENT '客户ID',
                                `applicant_id` bigint NOT NULL COMMENT '申请人ID（健康管家）',
                                `outing_reason` varchar(50) NOT NULL COMMENT '外出事由：MEDICAL-就医，VISIT_FAMILY-探亲，SHOPPING-购物，WALK-散步，OTHER-其他',
                                `outing_date` datetime NOT NULL COMMENT '外出时间',
                                `expected_return_date` datetime NOT NULL COMMENT '预计回院时间',
                                `actual_return_date` datetime DEFAULT NULL COMMENT '实际回院时间',


                                `apply_status` varchar(20) DEFAULT 'SUBMITTED' COMMENT '申请状态：SUBMITTED-待审批，APPROVED-已通过，REJECTED-已拒绝，COMPLETED-已完成',
                                `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
                                `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
                                `approve_remark` text COMMENT '审批备注',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                PRIMARY KEY (`id`),
                                KEY `idx_customer_id` (`customer_id`),
                                KEY `idx_applicant_id` (`applicant_id`),
                                KEY `idx_apply_status` (`apply_status`),
                                KEY `idx_outing_date` (`outing_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外出申请表';



-- 16. 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` bigint NOT NULL COMMENT '操作用户ID',
                                 `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
                                 `operation_desc` text COMMENT '操作描述',
                                 `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
                                 `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
                                 `request_params` text COMMENT '请求参数',
                                 `response_result` text COMMENT '响应结果',
                                 `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
                                 `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
                                 `execute_time` bigint DEFAULT NULL COMMENT '执行时间（毫秒）',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_user_id` (`user_id`),
                                 KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============膳食管理模块=============

-- 1. 膳食配置表（膳食配方）
DROP TABLE IF EXISTS `meal_config`;
CREATE TABLE `meal_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `meal_name` varchar(100) NOT NULL COMMENT '膳食名称',
  `meal_type` varchar(20) NOT NULL COMMENT '膳食类型：BREAKFAST-早餐，LUNCH-午餐，DINNER-晚餐，SNACK-加餐',
  `description` text COMMENT '膳食描述',
  `main_food` text COMMENT '主食内容（JSON格式）',
  `side_dishes` text COMMENT '菜品内容（JSON格式）',
  `soup` varchar(200) COMMENT '汤品',
  `fruits` varchar(200) COMMENT '水果',
  `nutrition_info` text COMMENT '营养信息（JSON格式：热量、蛋白质、脂肪、碳水化合物等）',
  `suitable_crowd` text COMMENT '适宜人群（JSON格式）',
  `dietary_restrictions` text COMMENT '饮食禁忌（JSON格式）',
  `cooking_method` text COMMENT '烹饪方法说明',
  `estimated_cost` decimal(10,2) COMMENT '预估成本（元）',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `creator_name` varchar(50) COMMENT '创建者姓名',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_meal_type` (`meal_type`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='膳食配置表';

-- 2. 膳食日历表
DROP TABLE IF EXISTS `meal_calendar`;
CREATE TABLE `meal_calendar` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `meal_date` date NOT NULL COMMENT '膳食日期',
  `day_of_week` tinyint NOT NULL COMMENT '星期几：1-周一，2-周二...7-周日',
  `breakfast_config_id` bigint COMMENT '早餐配置ID',
  `lunch_config_id` bigint COMMENT '午餐配置ID',
  `dinner_config_id` bigint COMMENT '晚餐配置ID',
  `morning_snack_config_id` bigint COMMENT '上午加餐配置ID',
  `afternoon_snack_config_id` bigint COMMENT '下午加餐配置ID',
  `special_notes` text COMMENT '特殊说明',
  `total_estimated_cost` decimal(10,2) COMMENT '当日总预估成本',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `creator_name` varchar(50) COMMENT '创建者姓名',
  `approval_status` varchar(20) DEFAULT 'DRAFT' COMMENT '审批状态：DRAFT-草稿，PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝',
  `approver_id` bigint COMMENT '审批人ID',
  `approver_name` varchar(50) COMMENT '审批人姓名',
  `approve_time` datetime COMMENT '审批时间',
  `approve_remark` text COMMENT '审批备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meal_date` (`meal_date`),
  KEY `idx_approval_status` (`approval_status`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_approver_id` (`approver_id`),
  CONSTRAINT `fk_breakfast_config` FOREIGN KEY (`breakfast_config_id`) REFERENCES `meal_config` (`id`),
  CONSTRAINT `fk_lunch_config` FOREIGN KEY (`lunch_config_id`) REFERENCES `meal_config` (`id`),
  CONSTRAINT `fk_dinner_config` FOREIGN KEY (`dinner_config_id`) REFERENCES `meal_config` (`id`),
  CONSTRAINT `fk_morning_snack_config` FOREIGN KEY (`morning_snack_config_id`) REFERENCES `meal_config` (`id`),
  CONSTRAINT `fk_afternoon_snack_config` FOREIGN KEY (`afternoon_snack_config_id`) REFERENCES `meal_config` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='膳食日历表';

-- 3. AI食谱推荐记录表
DROP TABLE IF EXISTS `ai_recipe_recommendation`;
CREATE TABLE `ai_recipe_recommendation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(100) NOT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) COMMENT '用户姓名',
  `user_question` text NOT NULL COMMENT '用户提问内容',
  `ai_response` text NOT NULL COMMENT 'AI回复内容',
  `question_type` varchar(50) COMMENT '问题类型：SYMPTOMS-症状咨询，NUTRITION-营养建议，RECIPE-食谱推荐，DIETARY_RESTRICTIONS-饮食禁忌等',
  `related_symptoms` varchar(500) COMMENT '相关症状标签',
  `recommended_foods` text COMMENT '推荐食物（JSON格式）',
  `avoid_foods` text COMMENT '避免食物（JSON格式）',
  `is_adopted` tinyint DEFAULT '0' COMMENT '是否被采纳：0-未采纳，1-已采纳到膳食配置',
  `adopted_meal_config_id` bigint COMMENT '采纳到的膳食配置ID',
  `response_time` bigint COMMENT 'AI响应时间（毫秒）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_question_type` (`question_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI食谱推荐记录表';

-- 4. 膳食评价反馈表（可选）
DROP TABLE IF EXISTS `meal_feedback`;
CREATE TABLE `meal_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `meal_calendar_id` bigint NOT NULL COMMENT '膳食日历ID',
  `meal_date` date NOT NULL COMMENT '膳食日期',
  `meal_type` varchar(20) NOT NULL COMMENT '膳食类型',
  `customer_id` bigint COMMENT '客户ID（如果是客户反馈）',
  `customer_name` varchar(50) COMMENT '客户姓名',
  `staff_id` bigint COMMENT '员工ID（如果是员工反馈）',
  `staff_name` varchar(50) COMMENT '员工姓名',
  `taste_rating` tinyint COMMENT '口味评分：1-5分',
  `nutrition_rating` tinyint COMMENT '营养评分：1-5分',
  `portion_rating` tinyint COMMENT '分量评分：1-5分',
  `overall_rating` tinyint COMMENT '综合评分：1-5分',
  `feedback_content` text COMMENT '反馈内容',
  `suggestions` text COMMENT '改进建议',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_meal_calendar_id` (`meal_calendar_id`),
  KEY `idx_meal_date` (`meal_date`),
  KEY `idx_customer_id` (`customer_id`),
  CONSTRAINT `fk_meal_feedback_calendar` FOREIGN KEY (`meal_calendar_id`) REFERENCES `meal_calendar` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='膳食评价反馈表';