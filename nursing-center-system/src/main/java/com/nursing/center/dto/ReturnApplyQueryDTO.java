package com.nursing.center.dto;

import com.nursing.center.common.dto.BaseQueryParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 回院申请查询DTO
 * @author system
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReturnApplyQueryDTO extends BaseQueryParams {

    /**
     * 客户姓名
     */
    private String customerName;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请状态
     */
    private String applyStatus;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}
