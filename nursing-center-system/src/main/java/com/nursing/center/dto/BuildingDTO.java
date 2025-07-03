package com.nursing.center.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 楼栋DTO
 */
@Data
public class BuildingDTO {
    private Long id;

    @NotBlank(message = "楼栋编号不能为空")
    private String buildingNo;

    @NotBlank(message = "楼栋名称不能为空")
    private String buildingName;

    @NotNull(message = "楼层数不能为空")
    @Positive(message = "楼层数必须为正数")
    private Integer floorCount;

    private Integer status;

    // 统计信息
    private Integer totalRooms;
    private Integer totalBeds;
    private Integer occupiedBeds;
}
