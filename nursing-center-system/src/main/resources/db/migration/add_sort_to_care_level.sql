-- 为护理级别表添加排序字段
ALTER TABLE `care_level` ADD COLUMN `sort` int DEFAULT '0' COMMENT '排序值，数值越小排序越靠前';
