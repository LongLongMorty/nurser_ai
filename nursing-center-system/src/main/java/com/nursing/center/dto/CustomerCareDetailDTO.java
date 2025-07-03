package com.nursing.center.dto;

import lombok.Data;

import java.util.List;

/**
 * 客户护理详情DTO
 */
@Data
public class CustomerCareDetailDTO {
    
    /**
     * 客户ID
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
     * 护理级别ID
     */
    private Long careLevelId;
    
    /**
     * 护理级别名称
     */
    private String careLevelName;
    
    /**
     * 护理项目列表
     */
    private List<CustomerCareItemDetailDTO> careItems;
    
    /**
     * 生效日期
     */
    private String effectiveDate;
    
    /**
     * 创建时间
     */
    private String createTime;
}
