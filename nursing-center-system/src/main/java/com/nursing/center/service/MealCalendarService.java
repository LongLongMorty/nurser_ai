package com.nursing.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nursing.center.dto.MealCalendarDTO;
import com.nursing.center.entity.MealCalendar;

import java.util.List;

/**
 * 膳食日历服务接口（简化版 - 仅显示固定七天菜单并支持编辑）
 * @author system
 * @since 2025-06-24
 */
public interface MealCalendarService extends IService<MealCalendar> {

    /**
     * 获取七天菜单（固定循环）
     * @return 七天菜单列表
     */
    List<MealCalendarDTO> getWeekMenu();

    /**
     * 更新单天菜单（四餐）
     * @param calendarDTO 膳食日历信息
     * @return 是否更新成功
     */
    boolean updateMealCalendar(MealCalendarDTO calendarDTO);
}
