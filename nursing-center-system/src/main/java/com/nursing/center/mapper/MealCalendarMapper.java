package com.nursing.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nursing.center.dto.MealCalendarDTO;
import com.nursing.center.entity.MealCalendar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 膳食日历Mapper接口（简化版 - 仅查询固定七天菜单）
 * @author system
 * @since 2025-06-24
 */
@Mapper
public interface MealCalendarMapper extends BaseMapper<MealCalendar> {

    /**
     * 根据周菜单名称查询完整的一周菜单
     */
    List<MealCalendarDTO> selectWeekMenu(@Param("weekName") String weekName);
}
