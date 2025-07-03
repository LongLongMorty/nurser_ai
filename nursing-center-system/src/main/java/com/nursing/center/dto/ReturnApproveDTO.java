package com.nursing.center.dto;

import com.nursing.center.common.enums.ApplyStatus;
import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 回院申请审批DTO
 * @author system
 * @since 2024-01-01
 */
@Data
public class ReturnApproveDTO {

    /**
     * 申请ID
     */
    @NotNull(message = "申请ID不能为空")
    private Long applyId;

    /**
     * 审批状态
     */
    @NotNull(message = "审批状态不能为空")
    private ApplyStatus applyStatus;

    /**
     * 审批备注
     */
    private String approveRemark;
}
