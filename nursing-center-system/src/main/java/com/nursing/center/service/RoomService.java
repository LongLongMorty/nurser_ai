package com.nursing.center.service;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.service
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  15:30
 * @Description: Room service interface
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.dto.RoomDTO;
import com.nursing.center.dto.RoomQueryDTO;

import java.util.List;

public interface RoomService {

    /**
     * 分页查询房间信息
     */
    IPage<RoomDTO> getRoomPage(RoomQueryDTO query);

    /**
     * 根据ID查询房间详情
     */
    RoomDTO getRoomById(Long id);

    /**
     * 新增房间
     */
    Long addRoom(RoomDTO roomDTO);

    /**
     * 更新房间信息
     */
    void updateRoom(RoomDTO roomDTO);

    /**
     * 删除房间
     */
    void deleteRoom(Long id);

    /**
     * 根据楼栋ID获取房间列表
     */
    List<RoomDTO> getRoomsByBuildingId(Long buildingId);

    /**
     * 获取所有启用的房间
     */
    List<RoomDTO> getEnabledRooms();

    /**
     * 根据楼栋ID获取按楼层分组的房间列表
     */
    List<RoomDTO> getRoomsGroupedByFloor(Long buildingId);
}
