package com.nursing.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 膳食日历实体类（周菜单）
 * @author system
 * @since 2025-06-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("meal_calendar")
public class MealCalendar implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 周菜单名称，如：第1周菜单
     */
    @TableField("week_name")
    private String weekName;

    /**
     * 星期几：1-周一，2-周二...7-周日
     */
    @TableField("day_of_week")
    private Integer dayOfWeek;

    /**
     * 早餐配置ID
     */
    @TableField("breakfast_config_id")
    private Long breakfastConfigId;

    /**
     * 午餐配置ID
     */
    @TableField("lunch_config_id")
    private Long lunchConfigId;    /**
     * 晚餐配置ID
     */
    @TableField("dinner_config_id")
    private Long dinnerConfigId;

    /**
     * 加餐配置ID
     */
    @TableField("snack_config_id")
    private Long snackConfigId;

    /**
     * 是否启用：0-禁用，1-启用
     */
    @TableField("is_active")
    private Integer isActive;

    /**
     * 特殊说明
     */
    @TableField("special_notes")
    private String specialNotes;

    /**
     * 当日总预估成本
     */
    @TableField("total_estimated_cost")
    private BigDecimal totalEstimatedCost;    /**
     * 创建者ID
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 创建者姓名
     */
    @TableField("creator_name")
    private String creatorName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
