package com.nursing.center.dto;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.dto
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:53
 * @Description: TODO
 * @Version: 1.0
 */
import com.nursing.center.common.enums.ApplyStatus;
import com.nursing.center.common.enums.CheckoutType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CheckoutApplyDTO {
    private Long id;    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    private Long applicantId; // 申请人ID（健康管家）
    private String applicantName; // 申请人姓名（用于显示）

    @NotNull(message = "退住类型不能为空")
    private CheckoutType checkoutType;

    @NotBlank(message = "退住原因不能为空")
    private String checkoutReason;

    @NotNull(message = "退住时间不能为空")
    private LocalDate checkoutDate;

    private ApplyStatus applyStatus;
    private Long approverId;
    private LocalDateTime approveTime;
    private String approveRemark;
    private LocalDateTime createTime; // 添加 createTime 字段
    private LocalDateTime updateTime; // 添加 updateTime 字段

    // 关联信息
    private String customerName;
    private String approverName;
    private String bedInfo; // 床位信息（楼栋-房间-床位）
}
