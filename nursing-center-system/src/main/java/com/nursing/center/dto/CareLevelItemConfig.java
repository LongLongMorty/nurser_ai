package com.nursing.center.dto;

import lombok.Data;

/**
 * 护理级别项目配置DTO
 */
@Data
public class CareLevelItemConfig {
    private Long id;
    private Long careLevelId;
    private Long careItemId;
    private CareItemDTO careItem;
    private String createTime;
}
