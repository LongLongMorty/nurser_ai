package com.nursing.center.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 膳食日历查询DTO（周菜单）
 * @author system
 * @since 2025-06-24
 */
@Data
public class MealCalendarQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 周菜单名称，如：第1周菜单
     */
    private String weekName;

    /**
     * 星期几：1-周一，2-周二...7-周日
     */
    private Integer dayOfWeek;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer isActive;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建者姓名
     */
    private String creatorName;
}
