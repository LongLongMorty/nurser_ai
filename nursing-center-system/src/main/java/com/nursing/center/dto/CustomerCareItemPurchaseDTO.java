package com.nursing.center.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 客户护理项目购买DTO
 */
@Data
public class CustomerCareItemPurchaseDTO {
    
    @NotNull(message = "护理项目ID不能为空")
    private Long careItemId;
    
    @NotNull(message = "购买数量不能为空")
    private Integer quantity = 1;
    
    @NotNull(message = "到期日期不能为空")
    private String expireDate;
}
