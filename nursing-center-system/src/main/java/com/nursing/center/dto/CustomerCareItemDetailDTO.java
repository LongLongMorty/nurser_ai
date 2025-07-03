package com.nursing.center.dto;

import lombok.Data;

/**
 * 客户护理项目详情DTO
 */
@Data
public class CustomerCareItemDetailDTO {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 护理项目ID
     */
    private Long careItemId;
    
    /**
     * 护理项目名称
     */
    private String careItemName;
    
    /**
     * 单价
     */
    private Double unitPrice;
    
    /**
     * 购买日期
     */
    private String purchaseDate;
    
    /**
     * 购买数量
     */
    private Integer purchaseQuantity;
    
    /**
     * 已使用数量
     */
    private Integer usedQuantity;
    
    /**
     * 剩余数量
     */
    private Integer remainingQuantity;
    
    /**
     * 到期日期
     */
    private String expireDate;
    
    /**
     * 服务状态
     */
    private String serviceStatus;
}
