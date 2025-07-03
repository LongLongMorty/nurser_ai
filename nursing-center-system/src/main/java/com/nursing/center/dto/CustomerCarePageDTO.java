package com.nursing.center.dto;

import lombok.Data;

/**
 * 客户护理设置分页查询结果DTO
 */
@Data
public class CustomerCarePageDTO {
    
    /**
     * 客户ID
     */
    private Long id;
      /**
     * 客户姓名
     */
    private String customerName;
    
    /**
     * 客户电话
     */
    private String phone;
    
    /**
     * 护理级别ID
     */
    private Long careLevelId;
    
    /**
     * 护理级别名称
     */
    private String careLevelName;
    
    /**
     * 护理项目数量
     */
    private Integer careItemCount;
    
    /**
     * 生效日期
     */
    private String effectiveDate;
    
    /**
     * 创建时间
     */
    private String createTime;
}
