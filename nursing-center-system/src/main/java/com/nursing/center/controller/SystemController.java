package com.nursing.center.controller;

import com.nursing.center.common.result.Result;
import com.nursing.center.common.enums.UserRole;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 系统管理控制器
 * 提供角色管理、护理级别管理、护理项目管理等功能
 */
@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemController {

    /**
     * 获取所有可用角色
     */
    @GetMapping("/roles")
    public Result<List<Map<String, Object>>> getRoles() {
        List<Map<String, Object>> roles = new ArrayList<>();
        
        for (UserRole role : UserRole.values()) {
            Map<String, Object> roleInfo = new HashMap<>();
            roleInfo.put("id", role.ordinal() + 1);
            roleInfo.put("code", role.name());
            roleInfo.put("name", role.getDescription());
            roleInfo.put("description", role.getDescription());
            roleInfo.put("status", "ACTIVE");
            roleInfo.put("createTime", "2024-01-01 00:00:00");
            roles.add(roleInfo);
        }
        
        return Result.success(roles);
    }

    /**
     * 获取护理级别列表
     */
    @GetMapping("/care-levels")
    public Result<List<Map<String, Object>>> getCareLevels() {
        List<Map<String, Object>> careLevels = new ArrayList<>();
        
        // 模拟护理级别数据
        careLevels.add(createCareLevel(1L, "特级护理", "24小时专人护理", 500.0, "ACTIVE"));
        careLevels.add(createCareLevel(2L, "一级护理", "每4小时巡视一次", 300.0, "ACTIVE"));
        careLevels.add(createCareLevel(3L, "二级护理", "每8小时巡视一次", 200.0, "ACTIVE"));
        careLevels.add(createCareLevel(4L, "三级护理", "每日巡视2次", 100.0, "ACTIVE"));
        
        return Result.success(careLevels);
    }

    /**
     * 创建护理级别
     */
    @PostMapping("/care-levels")
    public Result<String> createCareLevel(@RequestBody Map<String, Object> careLevel) {
        // 实际应该调用服务层保存到数据库
        return Result.success("护理级别创建成功");
    }

    /**
     * 更新护理级别
     */
    @PutMapping("/care-levels/{id}")
    public Result<String> updateCareLevel(@PathVariable Long id, @RequestBody Map<String, Object> careLevel) {
        // 实际应该调用服务层更新数据库
        return Result.success("护理级别更新成功");
    }

    /**
     * 删除护理级别
     */
    @DeleteMapping("/care-levels/{id}")
    public Result<String> deleteCareLevel(@PathVariable Long id) {
        // 实际应该调用服务层删除数据库记录
        return Result.success("护理级别删除成功");
    }

    /**
     * 获取护理项目列表
     */
    @GetMapping("/care-items")
    public Result<List<Map<String, Object>>> getCareItems() {
        List<Map<String, Object>> careItems = new ArrayList<>();
        
        // 模拟护理项目数据
        careItems.add(createCareItem(1L, "日常生活照料", "协助客户进行日常生活", 50.0, "ACTIVE"));
        careItems.add(createCareItem(2L, "医疗护理", "提供专业医疗护理服务", 100.0, "ACTIVE"));
        careItems.add(createCareItem(3L, "康复训练", "协助客户进行康复训练", 80.0, "ACTIVE"));
        careItems.add(createCareItem(4L, "心理疏导", "提供心理健康咨询", 60.0, "ACTIVE"));
        careItems.add(createCareItem(5L, "营养配餐", "制定个性化营养方案", 30.0, "ACTIVE"));
        
        return Result.success(careItems);
    }

    /**
     * 创建护理项目
     */
    @PostMapping("/care-items")
    public Result<String> createCareItem(@RequestBody Map<String, Object> careItem) {
        // 实际应该调用服务层保存到数据库
        return Result.success("护理项目创建成功");
    }

    /**
     * 更新护理项目
     */
    @PutMapping("/care-items/{id}")
    public Result<String> updateCareItem(@PathVariable Long id, @RequestBody Map<String, Object> careItem) {
        // 实际应该调用服务层更新数据库
        return Result.success("护理项目更新成功");
    }

    /**
     * 删除护理项目
     */
    @DeleteMapping("/care-items/{id}")
    public Result<String> deleteCareItem(@PathVariable Long id) {
        // 实际应该调用服务层删除数据库记录
        return Result.success("护理项目删除成功");
    }

    /**
     * 获取系统统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getSystemStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 模拟统计数据
        statistics.put("totalUsers", 25);
        statistics.put("activeUsers", 23);
        statistics.put("totalRoles", UserRole.values().length);
        statistics.put("careLevels", 4);
        statistics.put("careItems", 15);
        statistics.put("systemHealth", "GOOD");
        
        return Result.success(statistics);
    }

    /**
     * 创建护理级别信息
     */
    private Map<String, Object> createCareLevel(Long id, String name, String description, Double price, String status) {
        Map<String, Object> careLevel = new HashMap<>();
        careLevel.put("id", id);
        careLevel.put("name", name);
        careLevel.put("description", description);
        careLevel.put("price", price);
        careLevel.put("status", status);
        careLevel.put("createTime", "2024-01-01 00:00:00");
        careLevel.put("updateTime", "2024-01-01 00:00:00");
        return careLevel;
    }

    /**
     * 创建护理项目信息
     */
    private Map<String, Object> createCareItem(Long id, String name, String description, Double price, String status) {
        Map<String, Object> careItem = new HashMap<>();
        careItem.put("id", id);
        careItem.put("name", name);
        careItem.put("description", description);
        careItem.put("price", price);
        careItem.put("status", status);
        careItem.put("createTime", "2024-01-01 00:00:00");
        careItem.put("updateTime", "2024-01-01 00:00:00");
        return careItem;
    }
}
