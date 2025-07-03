package com.nursing.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 回院申请实体类
 * @author system
 * @since 2024-01-01
 */
@Data
@TableName("return_apply")
public class ReturnApply {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 客户ID
     */
    @TableField("customer_id")
    private Long customerId;

    /**
     * 关联的外出申请ID
     */
    @TableField("outing_apply_id")
    private Long outingApplyId;

    /**
     * 申请人ID（健康管家）
     */
    @TableField("applicant_id")
    private Long applicantId;

    /**
     * 回院事由
     */
    @TableField("return_reason")
    private String returnReason;

    /**
     * 申请回院时间
     */
    @TableField("requested_return_date")
    private LocalDateTime requestedReturnDate;

    /**
     * 实际回院时间
     */
    @TableField("actual_return_date")
    private LocalDateTime actualReturnDate;

    /**
     * 申请状态：SUBMITTED-已提交，APPROVED-通过，REJECTED-不通过
     */
    @TableField("apply_status")
    private String applyStatus;

    /**
     * 审批人ID
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 审批时间
     */
    @TableField("approve_time")
    private LocalDateTime approveTime;

    /**
     * 审批备注
     */
    @TableField("approve_remark")
    private String approveRemark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
