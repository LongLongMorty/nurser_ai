package com.nursing.center.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.dto.BedUsageDTO;
import com.nursing.center.dto.BedUsageQueryDTO;

import java.time.LocalDate;

/**
 * 床位使用记录服务接口
 */
public interface BedUsageService {
    
    /**
     * 分页查询床位使用记录
     */
    IPage<BedUsageDTO> getBedUsagePage(BedUsageQueryDTO query);
    
    /**
     * 根据ID查询床位使用记录详情
     */
    BedUsageDTO getBedUsageById(Long id);
    
    /**
     * 更新床位使用记录的结束时间
     */
    void updateEndDate(Long id, LocalDate endDate);
      /**
     * 床位调换
     */
    void swapBed(com.nursing.center.dto.BedSwapDTO bedSwapDTO);
    
    /**
     * 获取房间列表（用于床位调换）
     */
    java.util.List<com.nursing.center.dto.RoomDTO> getRoomsForSwap();
    
    /**
     * 根据房间ID获取空闲床位列表（用于床位调换）
     */
    java.util.List<com.nursing.center.dto.BedInfoDTO> getAvailableBedsForSwap(Long roomId);
    
    /**
     * 按楼层获取房间列表（用于床位调换）
     */
    java.util.List<com.nursing.center.dto.RoomDTO> getRoomsByFloorForSwap(Integer floorNo);
}
