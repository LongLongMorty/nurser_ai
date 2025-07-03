package com.nursing.center.service.impl;

import com.nursing.center.service.DashboardService;
import com.nursing.center.mapper.CustomerMapper;
import com.nursing.center.mapper.BedMapper;
import com.nursing.center.mapper.RoomMapper;
import com.nursing.center.mapper.SysUserMapper;
import com.nursing.center.entity.Room;
import com.nursing.center.entity.Bed;
import com.nursing.center.entity.SysUser;
import com.nursing.center.common.enums.BedStatus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 仪表板数据服务实现
 */
@Service
public class DashboardServiceImpl implements DashboardService {    @Autowired
    private CustomerMapper customerMapper;
    
    @Autowired
    private BedMapper bedMapper;
    
    @Autowired
    private RoomMapper roomMapper;
    
    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
          // 客户统计 - 从数据库获取真实数据
        Long activeCustomers = customerMapper.selectCount(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.nursing.center.entity.Customer>lambdaQuery()
                .eq(com.nursing.center.entity.Customer::getStatus, 1) // 在住客户
        );
        
        // 床位统计 - 从数据库获取真实数据
        Long totalBeds = bedMapper.selectCount(null); // 总床位数
        Long occupiedBeds = bedMapper.selectCount(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.nursing.center.entity.Bed>lambdaQuery()
                .eq(com.nursing.center.entity.Bed::getBedStatus, "OCCUPIED") // 已占用床位
        );
        Long availableBeds = totalBeds - occupiedBeds; // 可用床位
        
        // 外出客户数
        Long outingCustomers = bedMapper.selectCount(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.nursing.center.entity.Bed>lambdaQuery()
                .eq(com.nursing.center.entity.Bed::getBedStatus, "OUT") // 外出状态床位
        );
        
        // 在线健康管家数量 - 从数据库获取真实数据
        Long onlineManagers = sysUserMapper.selectCount(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getRole, "HEALTH_MANAGER") // 健康管家角色
                .eq(SysUser::getStatus, 1) // 启用状态
                .eq(SysUser::getDeleted, 0) // 未删除
        );
        
        // 今日入住/退住数据 - 实际项目中应该根据日期查询，这里使用模拟数据
        stats.put("totalCustomers", activeCustomers);
        stats.put("totalBeds", totalBeds);
        stats.put("occupiedBeds", occupiedBeds);
        stats.put("availableBeds", availableBeds);
        stats.put("outingCustomers", outingCustomers);
        stats.put("onlineManagers", onlineManagers); // 在线健康管家数量
        stats.put("pendingApplies", 3); // 待处理申请 - 需要根据实际申请表查询
        stats.put("todayCheckIns", 2); // 今日入住 - 需要根据今日日期查询
        stats.put("todayCheckOuts", 1); // 今日退住 - 需要根据今日日期查询
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getCustomerAgeDistribution() {
        List<Map<String, Object>> distribution = new ArrayList<>();
          // 从数据库查询年龄分布        
        // 65-70岁
        Long count65_70 = customerMapper.selectCount(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.nursing.center.entity.Customer>lambdaQuery()
                .ge(com.nursing.center.entity.Customer::getAge, 65)
                .lt(com.nursing.center.entity.Customer::getAge, 70)
                .eq(com.nursing.center.entity.Customer::getStatus, 1)
        );
        
        // 70-75岁
        Long count70_75 = customerMapper.selectCount(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.nursing.center.entity.Customer>lambdaQuery()
                .ge(com.nursing.center.entity.Customer::getAge, 70)
                .lt(com.nursing.center.entity.Customer::getAge, 75)
                .eq(com.nursing.center.entity.Customer::getStatus, 1)
        );
        
        // 75-80岁
        Long count75_80 = customerMapper.selectCount(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.nursing.center.entity.Customer>lambdaQuery()
                .ge(com.nursing.center.entity.Customer::getAge, 75)
                .lt(com.nursing.center.entity.Customer::getAge, 80)
                .eq(com.nursing.center.entity.Customer::getStatus, 1)
        );
        
        // 80-85岁
        Long count80_85 = customerMapper.selectCount(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.nursing.center.entity.Customer>lambdaQuery()
                .ge(com.nursing.center.entity.Customer::getAge, 80)
                .lt(com.nursing.center.entity.Customer::getAge, 85)
                .eq(com.nursing.center.entity.Customer::getStatus, 1)
        );
        
        // 85-90岁
        Long count85_90 = customerMapper.selectCount(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.nursing.center.entity.Customer>lambdaQuery()
                .ge(com.nursing.center.entity.Customer::getAge, 85)
                .lt(com.nursing.center.entity.Customer::getAge, 90)
                .eq(com.nursing.center.entity.Customer::getStatus, 1)
        );
        
        // 90岁以上
        Long count90_plus = customerMapper.selectCount(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.nursing.center.entity.Customer>lambdaQuery()
                .ge(com.nursing.center.entity.Customer::getAge, 90)
                .eq(com.nursing.center.entity.Customer::getStatus, 1)
        );
          
        // 始终返回所有6个年龄段，包括0人的年龄段
        distribution.add(createAgeGroup("65-70岁", count65_70.intValue(), "A"));
        distribution.add(createAgeGroup("70-75岁", count70_75.intValue(), "B"));
        distribution.add(createAgeGroup("75-80岁", count75_80.intValue(), "C"));
        distribution.add(createAgeGroup("80-85岁", count80_85.intValue(), "D"));
        distribution.add(createAgeGroup("85-90岁", count85_90.intValue(), "E"));
        distribution.add(createAgeGroup("90岁以上", count90_plus.intValue(), "F"));
        
        return distribution;
    }

    @Override
    public List<Map<String, Object>> getCareLevelDistribution() {
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        // 这里应该根据护理级别表查询，暂时使用模拟数据
        // 实际应该根据 customer 表的 care_level_id 字段统计
        distribution.add(createCareLevel("特级护理", 8, "#FF6B6B"));
        distribution.add(createCareLevel("一级护理", 25, "#4ECDC4"));
        distribution.add(createCareLevel("二级护理", 35, "#45B7D1"));
        distribution.add(createCareLevel("三级护理", 20, "#96ceb4"));
        
        return distribution;
    }

    @Override
    public List<Map<String, Object>> getBedOccupancyTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        // 这里应该查询床位使用历史记录，暂时生成模拟数据
        Calendar cal = Calendar.getInstance();
        for (int i = days - 1; i >= 0; i--) {
            cal.add(Calendar.DATE, -1);
            Map<String, Object> data = new HashMap<>();
            data.put("date", String.format("%04d-%02d-%02d",
                    cal.get(Calendar.YEAR), 
                    cal.get(Calendar.MONTH) + 1, 
                    cal.get(Calendar.DATE)));
            data.put("occupancy", 75 + (int)(Math.random() * 20)); // 75-95%占用率
            trend.add(data);
        }
        
        return trend;
    }

    @Override
    public List<Map<String, Object>> getRecentActivities(int limit) {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // 这里应该查询最近的操作日志，暂时使用模拟数据
        activities.add(createActivity("入住", "新客户完成入住手续", "2024-06-17 10:30:00"));
        activities.add(createActivity("退住", "客户提交退住申请", "2024-06-17 09:15:00"));
        activities.add(createActivity("护理", "完成护理服务记录", "2024-06-17 08:45:00"));
        activities.add(createActivity("膳食", "今日午餐安排完成", "2024-06-17 11:00:00"));
        activities.add(createActivity("体检", "月度健康检查完成", "2024-06-17 07:30:00"));
        
        // 限制返回数量
        if (activities.size() > limit) {
            activities = activities.subList(0, limit);
        }
        
        return activities;
    }    @Override
    public List<Map<String, Object>> getFloorOccupancyStats() {
        List<Map<String, Object>> floorStats = new ArrayList<>();
        
        // 查询数据库中实际有哪些楼层
        List<Integer> floors = roomMapper.selectList(
            new LambdaQueryWrapper<Room>()
                .select(Room::getFloorNo)
                .eq(Room::getStatus, 1) // 只查询启用的房间
                .groupBy(Room::getFloorNo)
        ).stream().map(Room::getFloorNo).distinct().sorted().toList();
        
        for (Integer floor : floors) {
            Map<String, Object> floorStat = new HashMap<>();
            
            // 查询该楼层总床位数
            List<Room> floorRooms = roomMapper.selectList(
                new LambdaQueryWrapper<Room>()
                    .eq(Room::getFloorNo, floor)
                    .eq(Room::getStatus, 1)
            );
            
            int totalBeds = 0;
            int occupiedBeds = 0;
            
            for (Room room : floorRooms) {
                // 查询该房间的床位
                List<Bed> beds = bedMapper.selectList(
                    new LambdaQueryWrapper<Bed>()
                        .eq(Bed::getRoomId, room.getId())
                        .eq(Bed::getStatus, 1)
                );
                
                totalBeds += beds.size();
                
                // 统计已占用床位
                long occupied = beds.stream()
                    .filter(bed -> BedStatus.OCCUPIED.equals(bed.getBedStatus()))
                    .count();
                occupiedBeds += (int) occupied;
            }
            
            int availableBeds = totalBeds - occupiedBeds;
            double occupancyRate = totalBeds > 0 ? (double) occupiedBeds / totalBeds * 100 : 0;
            
            floorStat.put("floorNo", floor);
            floorStat.put("floorName", floor + "楼");
            floorStat.put("totalBeds", totalBeds);
            floorStat.put("occupiedBeds", occupiedBeds);
            floorStat.put("availableBeds", availableBeds);
            floorStat.put("occupancyRate", Math.round(occupancyRate));
            
            floorStats.add(floorStat);
        }
        
        return floorStats;
    }

    private Map<String, Object> createAgeGroup(String ageGroup, Integer count, String code) {
        Map<String, Object> group = new HashMap<>();
        group.put("ageGroup", ageGroup);
        group.put("count", count);
        group.put("code", code);
        return group;
    }

    private Map<String, Object> createCareLevel(String levelName, Integer count, String color) {
        Map<String, Object> level = new HashMap<>();
        level.put("levelName", levelName);
        level.put("count", count);
        level.put("color", color);
        return level;
    }

    private Map<String, Object> createActivity(String type, String content, String time) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("type", type);
        activity.put("content", content);
        activity.put("time", time);
        return activity;
    }

    @Override
    public List<Map<String, Object>> getCustomerDetails() {
        // 获取在住客户的详细信息
        List<Map<String, Object>> customers = customerMapper.getCustomerDetails();
        return customers != null ? customers : new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getOnlineManagerDetails() {
        // 获取在线管家的详细信息
        List<Map<String, Object>> managers = sysUserMapper.getOnlineManagerDetails();
        return managers != null ? managers : new ArrayList<>();
    }
}
