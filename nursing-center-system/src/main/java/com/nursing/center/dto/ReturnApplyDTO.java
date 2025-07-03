package com.nursing.center.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 回院申请DTO
 * @author system
 * @since 2024-01-01
 */
@Data
public class ReturnApplyDTO {

    /**
     * 客户ID
     */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    /**
     * 关联的外出申请ID
     */
    @NotNull(message = "外出申请ID不能为空")
    private Long outingApplyId;

    /**
     * 申请人ID（健康管家）
     */
    private Long applicantId;

    /**
     * 回院事由
     */
    @NotBlank(message = "回院事由不能为空")
    private String returnReason;

    /**
     * 申请回院时间
     */
    @NotNull(message = "申请回院时间不能为空")
    private LocalDateTime requestedReturnDate;

    /**
     * 备注
     */
    private String remarks;
}
