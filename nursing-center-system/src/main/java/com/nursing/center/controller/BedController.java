package com.nursing.center.controller;

import com.nursing.center.common.result.Result;
import com.nursing.center.dto.*;
import com.nursing.center.service.BedService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/bed")
@RequiredArgsConstructor
public class BedController {

    private final BedService bedService;


    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BedStatisticsDTO> getBedStatistics() {
        BedStatisticsDTO statistics = bedService.getBedStatistics();
        return Result.success(statistics);
    }

   
    @GetMapping("/floor/{floorNo}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BedInfoDTO>> getBedsByFloor(@PathVariable Integer floorNo) {
        List<BedInfoDTO> beds = bedService.getBedsByFloor(floorNo);
        return Result.success(beds);
    }    @GetMapping("/available/room/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BedInfoDTO>> getAvailableBedsByRoom(@PathVariable Long roomId) {
        List<BedInfoDTO> beds = bedService.getAvailableBedsByRoom(roomId);
        return Result.success(beds);
    }

    /**
     * 获取床位详细信息
     */
    @GetMapping("/detail/{bedId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BedDetailDTO> getBedDetail(@PathVariable Long bedId) {
        BedDetailDTO bedDetail = bedService.getBedDetail(bedId);
        return Result.success(bedDetail);
    }    /**
     * 获取床位图表数据（按楼层显示所有床位状态）
     */
    @GetMapping("/diagram")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BedDiagramDTO>> getBedDiagram() {
        // 获取所有楼层的床位图表数据
        List<BedDiagramDTO> allFloors = new ArrayList<>();
        
        // 假设有5层楼
        for (int floor = 1; floor <= 5; floor++) {
            List<BedDiagramDTO> floorData = bedService.getBedDiagramByFloor(floor);
            allFloors.addAll(floorData);
        }
        
        return Result.success(allFloors);
    }
    
    /**
     * 根据楼层查询房间及床位信息（用于床位示意图）
     */
    @GetMapping("/diagram/floor/{floorNo}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BedDiagramDTO>> getBedDiagramByFloor(@PathVariable Integer floorNo) {
        List<BedDiagramDTO> diagram = bedService.getBedDiagramByFloor(floorNo);
        return Result.success(diagram);
    }

    /**
     * 获取床位使用详情列表
     */
    @GetMapping("/usage/history")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BedInfoDTO>> getBedUsageHistory(
            @RequestParam(required = false) Long bedId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        List<BedInfoDTO> history = bedService.getBedUsageHistory(bedId, customerId, page, size);
        return Result.success(history);
    }

    /**
     * 床位交换
     */
    @PostMapping("/swap")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> swapBeds(@RequestBody BedSwapDTO bedSwapDTO) {
        bedService.swapBeds(bedSwapDTO);
        return Result.success("床位交换成功");
    }    /**
     * 获取可交换的床位列表
     */
    @GetMapping("/swappable/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BedInfoDTO>> getSwappableBeds(@PathVariable Long customerId) {
        List<BedInfoDTO> beds = bedService.getSwappableBeds(customerId);
        return Result.success(beds);
    }
    
    /**
     * 获取所有楼层的床位示意图数据（按楼层分组）
     */
    @GetMapping("/diagrams/grouped")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BedDiagramDTO>> getBedDiagramsGrouped() {
        List<BedDiagramDTO> diagrams = bedService.getBedDiagramsGrouped();
        return Result.success(diagrams);
    }
}