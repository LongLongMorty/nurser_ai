-- 回院申请表
DROP TABLE IF EXISTS `return_apply`;
CREATE TABLE `return_apply` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `customer_id` bigint NOT NULL COMMENT '客户ID',
                                `outing_apply_id` bigint NOT NULL COMMENT '关联的外出申请ID',
                                `applicant_id` bigint NOT NULL COMMENT '申请人ID（健康管家）',
                                `return_reason` text NOT NULL COMMENT '回院事由',
                                `requested_return_date` datetime NOT NULL COMMENT '申请回院时间',
                                `actual_return_date` datetime DEFAULT NULL COMMENT '实际回院时间',
                                `apply_status` varchar(20) DEFAULT 'SUBMITTED' COMMENT '申请状态：SUBMITTED-已提交，APPROVED-通过，REJECTED-不通过',
                                `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
                                `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
                                `approve_remark` text COMMENT '审批备注',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
                                PRIMARY KEY (`id`),
                                KEY `idx_customer_id` (`customer_id`),
                                KEY `idx_outing_apply_id` (`outing_apply_id`),
                                KEY `idx_applicant_id` (`applicant_id`),
                                KEY `idx_apply_status` (`apply_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回院申请表';
