package com.nursing.center.dto;

import lombok.Data;

/**
 * 客户护理设置查询参数DTO
 */
@Data
public class CustomerCareQueryParams {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 客户姓名（模糊查询）
     */
    private String customerName;
    
    /**
     * 护理级别ID
     */
    private Long careLevelId;
    
    /**
     * 状态
     */
    private String status;
}
