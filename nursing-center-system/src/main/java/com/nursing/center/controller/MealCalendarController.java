package com.nursing.center.controller;

import com.nursing.center.common.result.Result;
import com.nursing.center.dto.MealCalendarDTO;
import com.nursing.center.service.MealCalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 膳食日历控制器（固定七天菜单）
 * @author system
 * @since 2025-06-24
 */
@Slf4j
@RestController
@RequestMapping("/api/meal-calendar")
@RequiredArgsConstructor
public class MealCalendarController {

    private final MealCalendarService mealCalendarService;

    /**
     * 获取七天菜单（固定循环）
     */
    @GetMapping("/week-menu")
    public Result<List<MealCalendarDTO>> getWeekMenu() {
        List<MealCalendarDTO> weekMenu = mealCalendarService.getWeekMenu();
        return Result.success(weekMenu);
    }

    /**
     * 更新单天菜单（四餐）
     */
    @PutMapping("/day")
    public Result<Boolean> updateDayMenu(@RequestBody MealCalendarDTO calendarDTO) {
        boolean success = mealCalendarService.updateMealCalendar(calendarDTO);
        return Result.success(success);
    }
}
