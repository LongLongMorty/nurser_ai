package com.nursing.center.dto;

import lombok.Data;

/**
 * 楼栋查询DTO
 */
@Data
public class BuildingQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String buildingNo;
    private String buildingName;
    private Integer status;
}
