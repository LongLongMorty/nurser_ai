package com.nursing.center.service.impl;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.service.impl
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  15:30
 * @Description: Room service implementation
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.common.exception.BusinessException;
import com.nursing.center.dto.RoomDTO;
import com.nursing.center.dto.RoomQueryDTO;
import com.nursing.center.entity.Building;
import com.nursing.center.entity.Room;
import com.nursing.center.mapper.BuildingMapper;
import com.nursing.center.mapper.RoomMapper;
import com.nursing.center.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;

    @Override
    public IPage<RoomDTO> getRoomPage(RoomQueryDTO query) {
        Page<RoomDTO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return roomMapper.selectRoomPage(page, query);
    }

    @Override
    public RoomDTO getRoomById(Long id) {
        return roomMapper.selectRoomById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addRoom(RoomDTO roomDTO) {
        // 验证数据
        validateRoomData(roomDTO);

        // 检查楼栋是否存在
        validateBuilding(roomDTO.getBuildingId());        // 检查房间号是否已存在
        checkRoomNumberExists(roomDTO.getBuildingId(), roomDTO.getRoomNo(), null);

        // 转换DTO为Entity
        Room room = new Room();
        BeanUtils.copyProperties(roomDTO, room);

        // 设置默认值
        if (room.getStatus() == null) {
            room.setStatus(1); // 默认启用
        }

        // 保存房间信息
        roomMapper.insert(room);

        log.info("新增房间成功，房间ID: {}, 房间号: {}", room.getId(), room.getRoomNo());
        return room.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(RoomDTO roomDTO) {
        // 验证房间是否存在
        Room existRoom = roomMapper.selectById(roomDTO.getId());
        if (existRoom == null) {
            throw new BusinessException("房间不存在");
        }

        // 验证数据
        validateRoomData(roomDTO);

        // 检查楼栋是否存在
        validateBuilding(roomDTO.getBuildingId());        // 检查房间号是否已存在（排除当前房间）
        checkRoomNumberExists(roomDTO.getBuildingId(), roomDTO.getRoomNo(), roomDTO.getId());

        // 转换DTO为Entity
        Room room = new Room();
        BeanUtils.copyProperties(roomDTO, room);

        // 更新房间信息
        roomMapper.updateById(room);

        log.info("更新房间成功，房间ID: {}, 房间号: {}", room.getId(), room.getRoomNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Long id) {
        Room room = roomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException("房间不存在");
        }        // 检查房间下是否有床位  
        RoomDTO roomWithStats = roomMapper.selectRoomById(id);
        if (roomWithStats != null && roomWithStats.getOccupiedBeds() != null && roomWithStats.getOccupiedBeds() > 0) {
            throw new BusinessException("房间下还有入住客户，无法删除");
        }

        roomMapper.deleteById(id);

        log.info("删除房间成功，房间ID: {}", id);
    }

    @Override
    public List<RoomDTO> getRoomsByBuildingId(Long buildingId) {
        return roomMapper.selectRoomsByBuildingId(buildingId);
    }

    @Override
    public List<RoomDTO> getEnabledRooms() {
        return roomMapper.selectEnabledRooms();
    }

    @Override
    public List<RoomDTO> getRoomsGroupedByFloor(Long buildingId) {
        return roomMapper.selectRoomsGroupedByFloor(buildingId);
    }    /**
     * 验证房间数据
     */
    private void validateRoomData(RoomDTO roomDTO) {
        if (!StringUtils.hasText(roomDTO.getRoomNo())) {
            throw new BusinessException("房间号不能为空");
        }

        if (roomDTO.getBuildingId() == null) {
            throw new BusinessException("楼栋ID不能为空");
        }

        if (roomDTO.getFloorNo() == null || roomDTO.getFloorNo() <= 0) {
            throw new BusinessException("楼层必须大于0");
        }

        if (roomDTO.getBedCount() == null || roomDTO.getBedCount() <= 0) {
            throw new BusinessException("最大床位数必须大于0");
        }

        if (roomDTO.getBedCount() > 20) {
            throw new BusinessException("单个房间床位数不能超过20");
        }
    }

    /**
     * 验证楼栋是否存在
     */
    private void validateBuilding(Long buildingId) {
        Building building = buildingMapper.selectById(buildingId);
        if (building == null) {
            throw new BusinessException("楼栋不存在");
        }
        if (building.getStatus() != 1) {
            throw new BusinessException("楼栋已禁用，无法添加房间");
        }
    }    /**
     * 检查房间号是否已存在
     */
    private void checkRoomNumberExists(Long buildingId, String roomNo, Long excludeId) {
        // 这里需要在RoomMapper中添加检查方法，暂时省略实现
        // 实际项目中应该添加相应的查询方法
    }
}
