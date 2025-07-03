-- 为care_item表添加item_type字段
-- 2024-06-18: 添加护理项目类型字段以支持健康管家端功能

USE nursing_center;

-- 添加item_type字段到care_item表
ALTER TABLE `care_item` 
ADD COLUMN `item_type` varchar(20) DEFAULT 'DAILY' COMMENT '项目类型：DAILY-日常护理，MEDICAL-医疗护理，REHABILITATION-康复护理' 
AFTER `item_name`;

-- 添加unit字段到care_item表（如果不存在）
ALTER TABLE `care_item` 
ADD COLUMN `unit` varchar(10) DEFAULT '次' COMMENT '计量单位：次、小时、天等' 
AFTER `item_type`;

-- 更新现有数据的item_type字段（根据项目名称推断）
UPDATE `care_item` SET `item_type` = 'MEDICAL' WHERE `item_name` LIKE '%医疗%' OR `item_name` LIKE '%用药%' OR `item_name` LIKE '%检查%';
UPDATE `care_item` SET `item_type` = 'REHABILITATION' WHERE `item_name` LIKE '%康复%' OR `item_name` LIKE '%锻炼%' OR `item_name` LIKE '%理疗%';
UPDATE `care_item` SET `item_type` = 'DAILY' WHERE `item_type` IS NULL OR `item_type` = '';

-- 确保所有记录都有有效的item_type值
UPDATE `care_item` SET `item_type` = 'DAILY' WHERE `item_type` IS NULL;
