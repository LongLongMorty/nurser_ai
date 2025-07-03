package com.nursing.center.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 膳食日历DTO（周菜单）
 * @author system
 * @since 2025-06-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MealCalendarDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 周菜单名称，如：第1周菜单
     */
    private String weekName;

    /**
     * 星期几：1-周一，2-周二...7-周日
     */
    private Integer dayOfWeek;

    /**
     * 早餐配置ID
     */
    private Long breakfastConfigId;

    /**
     * 早餐名称
     */
    private String breakfastName;

    /**
     * 午餐配置ID
     */
    private Long lunchConfigId;

    /**
     * 午餐名称
     */
    private String lunchName;

    /**
     * 晚餐配置ID
     */
    private Long dinnerConfigId;

    /**
     * 晚餐名称
     */
    private String dinnerName;

    /**
     * 加餐配置ID
     */
    private Long snackConfigId;

    /**
     * 加餐名称
     */
    private String snackName;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer isActive;

    /**
     * 特殊说明
     */
    private String specialNotes;

    /**
     * 当日总预估成本
     */
    private BigDecimal totalEstimatedCost;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建者姓名
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
