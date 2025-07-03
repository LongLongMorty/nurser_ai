package com.nursing.center.dto;

import lombok.Data;

/**
 * 客户护理配置查询DTO
 */
@Data
public class CustomerCareConfigQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    
    // 客户姓名(模糊查询)
    private String customerName;
    
    // 护理级别ID
    private Long careLevelId;
    
    // 状态过滤
    private String status;
}
