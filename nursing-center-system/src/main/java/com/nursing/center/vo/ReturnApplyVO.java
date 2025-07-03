package com.nursing.center.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 回院申请VO
 * @author system
 * @since 2024-01-01
 */
@Data
public class ReturnApplyVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 客户姓名
     */
    private String customerName;

    /**
     * 关联的外出申请ID
     */
    private Long outingApplyId;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 回院事由
     */
    private String returnReason;

    /**
     * 申请回院时间
     */
    private LocalDateTime requestedReturnDate;

    /**
     * 实际回院时间
     */
    private LocalDateTime actualReturnDate;

    /**
     * 申请状态
     */
    private String applyStatus;

    /**
     * 申请状态描述
     */
    private String applyStatusDesc;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 审批备注
     */
    private String approveRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 外出时间
     */
    private LocalDateTime outingDate;

    /**
     * 预计回院时间
     */
    private LocalDateTime expectedReturnDate;

    /**
     * 床位号
     */
    private String bedNo;

    /**
     * 房间号
     */
    private String roomNo;
}
