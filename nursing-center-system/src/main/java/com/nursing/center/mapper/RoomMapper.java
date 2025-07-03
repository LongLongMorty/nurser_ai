package com.nursing.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.dto.RoomDTO;
import com.nursing.center.dto.RoomQueryDTO;
import com.nursing.center.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoomMapper extends BaseMapper<Room> {

    /**
     * 分页查询房间信息
     */
    IPage<RoomDTO> selectRoomPage(Page<RoomDTO> page, @Param("query") RoomQueryDTO query);

    /**
     * 根据ID查询房间详情
     */
    RoomDTO selectRoomById(@Param("id") Long id);

    /**
     * 根据楼栋ID获取房间列表
     */
    List<RoomDTO> selectRoomsByBuildingId(@Param("buildingId") Long buildingId);

    /**
     * 获取所有启用的房间
     */
    List<RoomDTO> selectEnabledRooms();

    /**
     * 根据楼栋ID获取按楼层分组的房间列表
     */
    List<RoomDTO> selectRoomsGroupedByFloor(@Param("buildingId") Long buildingId);
}
