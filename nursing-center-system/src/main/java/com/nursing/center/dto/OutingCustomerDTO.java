package com.nursing.center.dto;

import com.nursing.center.common.enums.CustomerType;
import lombok.Data;

import java.time.LocalDate;

/**
 * 外出客户信息DTO
 * 用于显示床位状态为OUT的客户列表
 */
@Data
public class OutingCustomerDTO {
    
    /**
     * 客户ID
     */
    private Long customerId;
    
    /**
     * 客户姓名
     */
    private String customerName;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 身份证号
     */
    private String idCard;
    
    /**
     * 客户类型
     */
    private CustomerType customerType;
    
    /**
     * 床位信息（楼栋-房间-床位）
     */
    private String bedInfo;
    
    /**
     * 入住日期
     */
    private LocalDate checkInDate;
    
    /**
     * 合同到期日期
     */
    private LocalDate contractExpireDate;
    
    /**
     * 健康管家ID
     */
    private Long healthManagerId;
    
    /**
     * 健康管家姓名
     */
    private String healthManagerName;
    
    /**
     * 外出申请ID（最近的一次已审批通过的外出申请）
     */
    private Long outingApplyId;
    
    /**
     * 外出时间
     */
    private String outingTime;
    
    /**
     * 预计回院时间
     */
    private String expectedReturnTime;
    
    /**
     * 外出事由
     */
    private String outingReason;
}
