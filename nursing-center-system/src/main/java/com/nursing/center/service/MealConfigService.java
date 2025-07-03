package com.nursing.center.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.dto.MealConfigDTO;
import com.nursing.center.dto.MealConfigQueryDTO;

import java.util.List;

/**
 * 膳食配置服务接口
 */
public interface MealConfigService {

    /**
     * 分页查询膳食配置
     */
    IPage<MealConfigDTO> getMealConfigPage(MealConfigQueryDTO query);

    /**
     * 根据ID查询膳食配置详情
     */
    MealConfigDTO getMealConfigById(Long id);

    /**
     * 新增膳食配置
     */
    Long addMealConfig(MealConfigDTO configDTO);

    /**
     * 修改膳食配置
     */
    void updateMealConfig(MealConfigDTO configDTO);

    /**
     * 删除膳食配置（逻辑删除）
     */
    void deleteMealConfig(Long id);

    /**
     * 根据餐次类型查询膳食配置
     */
    List<MealConfigDTO> getMealConfigsByType(String mealType);    /**
     * 获取所有启用的膳食配置
     */
    List<MealConfigDTO> getActiveMealConfigs();

    /**
     * 批量更新膳食配置状态
     */
    void batchUpdateStatus(List<Long> ids, Integer status);
}
