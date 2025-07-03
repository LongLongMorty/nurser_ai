package com.nursing.center.dto;

import com.nursing.center.common.enums.RoomType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 房间DTO
 */
@Data
public class RoomDTO {
    private Long id;

    @NotNull(message = "楼栋ID不能为空")
    private Long buildingId;

    @NotBlank(message = "房间号不能为空")
    private String roomNo;

    @NotBlank(message = "房间名称不能为空")
    private String roomName;

    @NotNull(message = "楼层号不能为空")
    @Positive(message = "楼层号必须为正数")
    private Integer floorNo;

    @NotNull(message = "床位数不能为空")
    @Positive(message = "床位数必须为正数")
    private Integer bedCount;

    @NotNull(message = "房间类型不能为空")
    private RoomType roomType;

    private Integer status;

    // 关联信息
    private String buildingName;
    private String buildingNo;
    
    // 统计信息
    private Integer availableBeds;
    private Integer occupiedBeds;
}
