package com.nursing.center.dto;

import com.nursing.center.common.enums.RoomType;
import lombok.Data;

/**
 * 房间查询DTO
 */
@Data
public class RoomQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long buildingId;
    private String roomNo;
    private String roomName;
    private Integer floorNo;
    private RoomType roomType;
    private Integer status;
}
