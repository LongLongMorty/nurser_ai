package com.nursing.center.dto;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 客户护理设置分页查询参数
 */
@Data
public class CustomerCarePageQueryDTO {
    
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;
    
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 10;
    
    /**
     * 客户姓名（模糊查询）
     */
    private String customerName;
    
    /**
     * 护理级别ID（null表示查询未设置护理级别的客户）
     */
    private Long careLevelId;
}
