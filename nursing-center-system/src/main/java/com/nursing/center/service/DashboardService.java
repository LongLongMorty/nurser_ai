package com.nursing.center.service;

import java.util.List;
import java.util.Map;

/**
 * 仪表板数据服务接口
 */
public interface DashboardService {
    
    /**
     * 获取统计数据
     */
    Map<String, Object> getStats();
    
    /**
     * 获取客户年龄分布
     */
    List<Map<String, Object>> getCustomerAgeDistribution();
    
    /**
     * 获取护理级别分布
     */
    List<Map<String, Object>> getCareLevelDistribution();
    
    /**
     * 获取床位占用趋势
     */
    List<Map<String, Object>> getBedOccupancyTrend(int days);
    
    /**
     * 获取最近活动
     */
    List<Map<String, Object>> getRecentActivities(int limit);
    
    /**
     * 获取各楼层入住统计
     */
    List<Map<String, Object>> getFloorOccupancyStats();
    
    /**
     * 获取在住客户详细信息
     */
    List<Map<String, Object>> getCustomerDetails();
    
    /**
     * 获取在线管家详细信息
     */
    List<Map<String, Object>> getOnlineManagerDetails();
}
