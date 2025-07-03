package com.nursing.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.dto.MealConfigDTO;
import com.nursing.center.dto.MealConfigQueryDTO;
import com.nursing.center.entity.MealConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import java.util.List;

/**
 * 膳食配置Mapper接口
 * @author system
 * @since 2025-06-22
 */
@Mapper
public interface MealConfigMapper extends BaseMapper<MealConfig> {

    /**
     * 分页查询膳食配置
     */
    IPage<MealConfigDTO> selectMealConfigPage(Page<MealConfigDTO> page, @Param("query") MealConfigQueryDTO query);

    /**
     * 根据膳食名称检查是否存在
     */
    boolean existsByName(@Param("mealName") String mealName);

    /**
     * 检查膳食配置是否被膳食日历引用
     */
    boolean isReferencedByCalendar(@Param("configId") Long configId);

    /**
     * 根据餐次类型查询膳食配置
     */
    List<MealConfigDTO> selectByMealType(@Param("mealType") String mealType);

    /**
     * 查询所有启用的膳食配置
     */
    List<MealConfigDTO> selectActiveMealConfigs();

    /**
     * 批量更新膳食配置状态
     */
    void batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);
}
