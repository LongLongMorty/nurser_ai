package com.nursing.center.controller;

import com.nursing.center.common.result.Result;
import com.nursing.center.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 仪表板控制器
 * 提供数据可视化相关的API接口
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = dashboardService.getStats();
        return Result.success(stats);
    }

    /**
     * 获取床位占用趋势
     */
    @GetMapping("/bed-occupancy-trend")
    public Result<List<Map<String, Object>>> getBedOccupancyTrend(
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> trend = dashboardService.getBedOccupancyTrend(days);
        return Result.success(trend);
    }

    /**
     * 获取客户年龄分布
     */
    @GetMapping("/customer-age-distribution")
    public Result<List<Map<String, Object>>> getCustomerAgeDistribution() {
        List<Map<String, Object>> distribution = dashboardService.getCustomerAgeDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取护理级别分布
     */
    @GetMapping("/care-level-distribution")
    public Result<List<Map<String, Object>>> getCareLevelDistribution() {
        List<Map<String, Object>> distribution = dashboardService.getCareLevelDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取最近活动
     */
    @GetMapping("/recent-activities")
    public Result<List<Map<String, Object>>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> activities = dashboardService.getRecentActivities(limit);
        return Result.success(activities);
    }

    /**
     * 获取楼层入住统计
     */
    @GetMapping("/floor-occupancy-stats")
    public Result<List<Map<String, Object>>> getFloorOccupancyStats() {
        List<Map<String, Object>> floorStats = dashboardService.getFloorOccupancyStats();
        return Result.success(floorStats);
    }

    /**
     * 获取在住客户详细信息
     */
    @GetMapping("/customer-details")
    public Result<List<Map<String, Object>>> getCustomerDetails() {
        List<Map<String, Object>> customerDetails = dashboardService.getCustomerDetails();
        return Result.success(customerDetails);
    }

    /**
     * 获取在线管家详细信息
     */
    @GetMapping("/online-manager-details")
    public Result<List<Map<String, Object>>> getOnlineManagerDetails() {
        List<Map<String, Object>> managerDetails = dashboardService.getOnlineManagerDetails();
        return Result.success(managerDetails);
    }
}
