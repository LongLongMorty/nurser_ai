package com.nursing.center.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 客户护理级别设置DTO
 */
@Data
public class CustomerCareLevelSetDTO {
    
    @NotNull(message = "客户ID不能为空")
    private Long customerId;
    
    @NotNull(message = "护理级别ID不能为空")
    private Long careLevelId;
    
    @NotNull(message = "购买数量不能为空")
    private Integer purchaseQuantity;
    
    @NotNull(message = "到期日期不能为空")
    private String expireDate;
      /**
     * 护理项目列表
     */
    @Valid
    private List<CustomerCareItemPurchaseDTO> careItems;
}
