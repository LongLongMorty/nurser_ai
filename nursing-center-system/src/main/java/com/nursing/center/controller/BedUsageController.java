package com.nursing.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.dto.BedInfoDTO;
import com.nursing.center.dto.BedSwapDTO;
import com.nursing.center.dto.BedUsageDTO;
import com.nursing.center.dto.BedUsageQueryDTO;
import com.nursing.center.service.BedUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 床位使用记录控制器
 */
@RestController
@RequestMapping("/api/admin/bed-usage")
@RequiredArgsConstructor
public class BedUsageController {
    
    private final BedUsageService bedUsageService;
    
    /**
     * 分页查询床位使用记录
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<BedUsageDTO>> getBedUsagePage(BedUsageQueryDTO query) {
        IPage<BedUsageDTO> page = bedUsageService.getBedUsagePage(query);
        return Result.success(page);
    }
    
    /**
     * 根据ID查询床位使用记录详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BedUsageDTO> getBedUsageById(@PathVariable Long id) {
        BedUsageDTO bedUsage = bedUsageService.getBedUsageById(id);
        return Result.success(bedUsage);
    }
      /**
     * 更新床位使用记录的结束时间
     */
    @PutMapping("/{id}/end-date")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateEndDate(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        bedUsageService.updateEndDate(id, endDate);
        return Result.success("结束时间更新成功");
    }
    
    /**
     * 床位调换
     */
    @PostMapping("/swap")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> swapBed(@RequestBody BedSwapDTO bedSwapDTO) {
        bedUsageService.swapBed(bedSwapDTO);
        return Result.success("床位调换成功");
    }
    
    /**
     * 获取房间列表（用于床位调换）
     */
    @GetMapping("/swap/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<java.util.List<com.nursing.center.dto.RoomDTO>> getRoomsForSwap() {
        java.util.List<com.nursing.center.dto.RoomDTO> rooms = bedUsageService.getRoomsForSwap();
        return Result.success(rooms);
    }
    
    /**
     * 根据房间ID获取空闲床位列表（用于床位调换）
     */
    @GetMapping("/swap/available-beds/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<java.util.List<BedInfoDTO>> getAvailableBedsForSwap(@PathVariable Long roomId) {
        java.util.List<BedInfoDTO> beds = bedUsageService.getAvailableBedsForSwap(roomId);
        return Result.success(beds);
    }
    
    /**
     * 按楼层获取房间列表（用于床位调换）
     */
    @GetMapping("/swap/rooms/floor/{floorNo}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<java.util.List<com.nursing.center.dto.RoomDTO>> getRoomsByFloorForSwap(@PathVariable Integer floorNo) {
        java.util.List<com.nursing.center.dto.RoomDTO> rooms = bedUsageService.getRoomsByFloorForSwap(floorNo);
        return Result.success(rooms);
    }
}
