package com.nursing.center.dto;

import lombok.Data;

/**
 * 客户护理设置查询参数DTO
 */
@Data
public class CustomerCareSettingQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    
    // 查询条件
    private String customerName;  // 客户姓名（模糊查询）
    private Long careLevelId;     // 护理级别ID
    private Integer status;       // 状态
}
