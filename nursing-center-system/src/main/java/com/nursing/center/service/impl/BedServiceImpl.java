package com.nursing.center.service.impl;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.service.impl
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:12
 * @Description: TODO
 * @Version: 1.0
 */
import com.nursing.center.common.enums.BedStatus;
import com.nursing.center.common.enums.BedType;
import com.nursing.center.dto.*;
import com.nursing.center.entity.Bed;
import com.nursing.center.mapper.BedMapper;
import com.nursing.center.service.BedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BedServiceImpl implements BedService {

    private final BedMapper bedMapper;

    @Override
    public BedStatisticsDTO getBedStatistics() {
        return bedMapper.selectBedStatistics();
    }

    @Override
    public List<BedInfoDTO> getBedsByFloor(Integer floorNo) {
        return bedMapper.selectBedsByFloor(floorNo);
    }

    @Override
    public List<BedInfoDTO> getAvailableBedsByRoom(Long roomId) {
        return bedMapper.selectAvailableBedsByRoom(roomId);
    }

    @Override
    public BedDetailDTO getBedDetail(Long bedId) {
        return bedMapper.selectBedDetail(bedId);
    }

    @Override
    public List<BedInfoDTO> getBedDiagram() {
        return bedMapper.selectBedDiagram();
    }
    
    @Override
    public List<BedDiagramDTO> getBedDiagramByFloor(Integer floorNo) {
        List<Map<String, Object>> rawData = bedMapper.selectBedDiagramByFloor(floorNo);
        return buildBedDiagramFromRawData(rawData);
    }

    @Override
    public List<BedDiagramDTO> getBedDiagramsGrouped() {
        List<BedDiagramDTO> allFloors = new ArrayList<>();
        
        // 获取1-5层的数据
        for (int floor = 1; floor <= 5; floor++) {
            List<BedDiagramDTO> floorData = getBedDiagramByFloor(floor);
            allFloors.addAll(floorData);
        }
        
        return allFloors;
    }

    @Override
    public List<BedInfoDTO> getBedUsageHistory(Long bedId, Long customerId, Integer page, Integer size) {
        Integer offset = (page - 1) * size;
        return bedMapper.selectBedUsageHistory(bedId, customerId, offset, size);
    }

    @Override
    @Transactional
    public void swapBeds(BedSwapDTO bedSwapDTO) {
        // 床位交换逻辑
        Bed sourceBed = bedMapper.selectById(bedSwapDTO.getSourceBedId());
        Bed targetBed = bedMapper.selectById(bedSwapDTO.getTargetBedId());
        
        if (sourceBed == null || targetBed == null) {
            throw new RuntimeException("床位不存在");
        }
        
        log.info("床位交换：从床位{}到床位{}", bedSwapDTO.getSourceBedId(), bedSwapDTO.getTargetBedId());
    }    @Override
    public List<BedInfoDTO> getSwappableBeds(Long customerId) {
        return bedMapper.selectSwappableBeds(customerId);
    }
    
    @Override
    public com.nursing.center.entity.Bed getBedById(Long bedId) {
        return bedMapper.selectById(bedId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBedStatus(Long bedId, com.nursing.center.common.enums.BedStatus bedStatus) {
        com.nursing.center.entity.Bed bed = new com.nursing.center.entity.Bed();
        bed.setId(bedId);
        bed.setBedStatus(bedStatus);
        bedMapper.updateById(bed);
        log.info("已更新床位{}状态为: {}", bedId, bedStatus);
    }
      @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomerBedInfo(Long customerId, Long newBedId) {
        // 获取新床位的详细信息
        BedDetailDTO bedDetail = bedMapper.selectBedDetail(newBedId);
        if (bedDetail == null) {
            throw new RuntimeException("获取床位详情失败");
        }
        
        // 更新客户的床位信息
        bedMapper.updateCustomerBedInfo(customerId, newBedId, 
            bedDetail.getRoom().getId(), 
            bedDetail.getBuilding().getId());
        
        log.info("已更新客户{}的床位信息，新床位ID: {}", customerId, newBedId);
    }

    /**
     * 从原始数据构建床位图表DTO
     */
    private List<BedDiagramDTO> buildBedDiagramFromRawData(List<Map<String, Object>> rawData) {
        Map<Integer, Map<Long, BedDiagramDTO.RoomDiagramDTO>> floorRoomMap = new HashMap<>();
        
        for (Map<String, Object> row : rawData) {
            try {
                Integer floorNo = (Integer) row.get("floorNo");
                Long roomId = ((Number) row.get("roomId")).longValue();
                
                // 确保楼层存在
                floorRoomMap.computeIfAbsent(floorNo, k -> new HashMap<>());
                
                // 确保房间存在
                if (!floorRoomMap.get(floorNo).containsKey(roomId)) {
                    BedDiagramDTO.RoomDiagramDTO room = new BedDiagramDTO.RoomDiagramDTO();
                    room.setRoomId(roomId);
                    room.setRoomNo((String) row.get("roomNo"));
                    room.setRoomName((String) row.get("roomName"));
                    room.setRoomType((String) row.get("roomType"));
                    room.setBeds(new ArrayList<>());
                    floorRoomMap.get(floorNo).put(roomId, room);
                }
                
                // 添加床位信息（如果存在）
                Object bedIdObj = row.get("bedId");
                if (bedIdObj != null) {
                    BedDiagramDTO.BedDiagramItemDTO bed = new BedDiagramDTO.BedDiagramItemDTO();
                    bed.setBedId(((Number) bedIdObj).longValue());
                    bed.setBedNo((String) row.get("bedNo"));
                    
                    // 安全地转换枚举
                    String bedTypeStr = (String) row.get("bedType");
                    if (bedTypeStr != null) {
                        try {
                            bed.setBedType(BedType.valueOf(bedTypeStr));
                        } catch (IllegalArgumentException e) {
                            log.warn("未知的床位类型: {}", bedTypeStr);
                            bed.setBedType(BedType.STANDARD); // 默认值
                        }
                    }
                    
                    String bedStatusStr = (String) row.get("bedStatus");
                    if (bedStatusStr != null) {
                        try {
                            bed.setBedStatus(BedStatus.valueOf(bedStatusStr));
                        } catch (IllegalArgumentException e) {
                            log.warn("未知的床位状态: {}", bedStatusStr);
                            bed.setBedStatus(BedStatus.AVAILABLE); // 默认值
                        }
                    }
                    
                    bed.setCustomerName((String) row.get("customerName"));
                    Object customerIdObj = row.get("customerId");
                    if (customerIdObj != null) {
                        bed.setCustomerId(((Number) customerIdObj).longValue());
                    }
                    bed.setCareLevel((String) row.get("careLevel"));
                    bed.setHealthManagerName((String) row.get("healthManagerName"));
                    
                    floorRoomMap.get(floorNo).get(roomId).getBeds().add(bed);
                }
            } catch (Exception e) {
                log.error("处理床位数据时出错: {}", e.getMessage(), e);
            }
        }
        
        // 转换为结果列表
        List<BedDiagramDTO> result = new ArrayList<>();
        for (Map.Entry<Integer, Map<Long, BedDiagramDTO.RoomDiagramDTO>> floorEntry : floorRoomMap.entrySet()) {
            BedDiagramDTO floorDTO = new BedDiagramDTO();
            floorDTO.setFloorNo(floorEntry.getKey());
            floorDTO.setRooms(new ArrayList<>(floorEntry.getValue().values()));
            result.add(floorDTO);
        }
        
        return result;
    }
}
