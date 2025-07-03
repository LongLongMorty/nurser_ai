package com.nursing.center.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.dto.BedInfoDTO;
import com.nursing.center.dto.BedUsageDTO;
import com.nursing.center.dto.BedUsageQueryDTO;
import com.nursing.center.dto.RoomDTO;
import com.nursing.center.mapper.BedUsageMapper;
import com.nursing.center.service.BedUsageService;
import com.nursing.center.service.RoomService;
import com.nursing.center.service.BedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 床位使用记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BedUsageServiceImpl implements BedUsageService {
    
    private final BedUsageMapper bedUsageMapper;
    private final RoomService roomService;
    private final BedService bedService;
    
    @Override
    public IPage<BedUsageDTO> getBedUsagePage(BedUsageQueryDTO query) {
        Page<BedUsageDTO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return bedUsageMapper.selectBedUsagePage(page, query);
    }
    
    @Override
    public BedUsageDTO getBedUsageById(Long id) {
        return bedUsageMapper.selectBedUsageById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEndDate(Long id, LocalDate endDate) {
        bedUsageMapper.updateEndDate(id, endDate);
        log.info("床位使用记录结束时间已更新，ID: {}, 结束时间: {}", id, endDate);
    }    @Override
    @Transactional(rollbackFor = Exception.class)
    public void swapBed(com.nursing.center.dto.BedSwapDTO bedSwapDTO) {
        log.info("开始执行床位调换，客户ID: {}, 原床位ID: {}, 目标床位ID: {}", 
                 bedSwapDTO.getSourceCustomerId(), bedSwapDTO.getSourceBedId(), bedSwapDTO.getTargetBedId());
        
        try {            // 验证调换日期（只能当天调换当天办理）
            LocalDate today = LocalDate.now();
            if (bedSwapDTO.getSwapDate() == null) {
                bedSwapDTO.setSwapDate(java.time.LocalDateTime.now());
            }
            LocalDate swapDateOnly = bedSwapDTO.getSwapDate().toLocalDate();
            if (!swapDateOnly.equals(today)) {
                throw new RuntimeException("床位调换只能当天办理");
            }
            
            // 1. 验证目标床位是否空闲 - 直接查询床位状态
            com.nursing.center.entity.Bed targetBed = bedService.getBedById(bedSwapDTO.getTargetBedId());
            if (targetBed == null) {
                throw new RuntimeException("目标床位不存在");
            }
            if (!com.nursing.center.common.enums.BedStatus.AVAILABLE.equals(targetBed.getBedStatus())) {
                throw new RuntimeException("目标床位不是空闲状态，只能调换到空闲床位");
            }
            
            // 2. 获取目标床位的房间和楼栋信息
            BedUsageMapper.BedLocationInfo bedLocationInfo = bedUsageMapper.getBedLocationInfo(bedSwapDTO.getTargetBedId());
            if (bedLocationInfo == null) {
                throw new RuntimeException("获取目标床位信息失败");
            }
            
            // 3. 结束当前床位使用记录（床位使用结束时间为当前日期）
            bedUsageMapper.endCurrentUsageByCustomerId(bedSwapDTO.getSourceCustomerId(), today);
            log.info("已结束客户{}的当前床位使用记录", bedSwapDTO.getSourceCustomerId());
            
            // 4. 修改旧床位状态为空闲
            bedService.updateBedStatus(bedSwapDTO.getSourceBedId(), com.nursing.center.common.enums.BedStatus.AVAILABLE);
            log.info("已将旧床位{}状态更新为空闲", bedSwapDTO.getSourceBedId());
            
            // 5. 修改新床位状态为有人
            bedService.updateBedStatus(bedSwapDTO.getTargetBedId(), com.nursing.center.common.enums.BedStatus.OCCUPIED);
            log.info("已将新床位{}状态更新为有人", bedSwapDTO.getTargetBedId());
            
            // 6. 添加新床位使用记录（入住时间为当前日期）
            com.nursing.center.entity.BedUsage newUsage = new com.nursing.center.entity.BedUsage();
            newUsage.setCustomerId(bedSwapDTO.getSourceCustomerId());
            newUsage.setBedId(bedSwapDTO.getTargetBedId());
            newUsage.setRoomId(bedLocationInfo.getRoomId());
            newUsage.setBuildingId(bedLocationInfo.getBuildingId());
            newUsage.setStartDate(today);
            newUsage.setUsageStatus(com.nursing.center.common.enums.UsageStatus.USING);
            
            bedUsageMapper.insertBedUsage(newUsage);
            log.info("已创建新的床位使用记录，客户ID: {}, 床位ID: {}", 
                     bedSwapDTO.getSourceCustomerId(), bedSwapDTO.getTargetBedId());
            
            // 7. 更新客户信息（房间号、床位号、楼号）
            bedService.updateCustomerBedInfo(bedSwapDTO.getSourceCustomerId(), bedSwapDTO.getTargetBedId());
            log.info("已更新客户{}的床位信息", bedSwapDTO.getSourceCustomerId());
            
            log.info("床位调换完成，原因: {}", bedSwapDTO.getSwapReason());
        } catch (Exception e) {
            log.error("床位调换失败", e);
            throw new RuntimeException("床位调换失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<RoomDTO> getRoomsForSwap() {
        // 获取所有启用的房间
        return roomService.getEnabledRooms();
    }
    
    @Override
    public List<BedInfoDTO> getAvailableBedsForSwap(Long roomId) {
        // 获取房间内的空闲床位
        return bedService.getAvailableBedsByRoom(roomId);
    }
    
    @Override
    public List<RoomDTO> getRoomsByFloorForSwap(Integer floorNo) {
        // 获取指定楼层的房间列表
        // 这里需要用到RoomService的方法，但目前只有通过楼栋ID获取的方法
        // 我们需要从所有房间中筛选指定楼层的房间
        List<RoomDTO> allRooms = roomService.getEnabledRooms();
        return allRooms.stream()
                .filter(room -> room.getFloorNo().equals(floorNo))
                .collect(java.util.stream.Collectors.toList());
    }
}
