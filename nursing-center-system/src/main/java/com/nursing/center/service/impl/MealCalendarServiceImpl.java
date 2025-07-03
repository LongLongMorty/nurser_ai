package com.nursing.center.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nursing.center.dto.MealCalendarDTO;
import com.nursing.center.entity.MealCalendar;
import com.nursing.center.mapper.MealCalendarMapper;
import com.nursing.center.service.MealCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MealCalendarServiceImpl extends ServiceImpl<MealCalendarMapper, MealCalendar> implements MealCalendarService {

    private final MealCalendarMapper mealCalendarMapper;

    /**
     * 获取七天菜单（固定循环）
     */
    @Override
    public List<MealCalendarDTO> getWeekMenu() {
        // 查询固定的七天菜单，使用固定的week_name='固定菜单'
        return mealCalendarMapper.selectWeekMenu("固定菜单");
    }

    /**
     * 编辑单天菜单（四餐）
     */
    @Override
    @Transactional
    public boolean updateMealCalendar(MealCalendarDTO calendarDTO) {
        MealCalendar entity = new MealCalendar();
        BeanUtils.copyProperties(calendarDTO, entity);
        return this.updateById(entity);
    }
}
