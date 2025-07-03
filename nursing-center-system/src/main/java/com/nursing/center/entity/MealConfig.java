package com.nursing.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 膳食配置实体类
 * @author system
 * @since 2025-06-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meal_config")
public class MealConfig extends BaseEntity {

    /**
     * 膳食名称
     */
    @TableField("meal_name")
    private String mealName;

    /**
     * 膳食类型：BREAKFAST-早餐，LUNCH-午餐，DINNER-晚餐，SNACK-加餐
     */
    @TableField("meal_type")
    private String mealType;

    /**
     * 膳食描述
     */
    @TableField("description")
    private String description;

    /**
     * 主食内容（JSON格式）
     */
    @TableField("main_food")
    private String mainFood;

    /**
     * 菜品内容（JSON格式）
     */
    @TableField("side_dishes")
    private String sideDishes;

    /**
     * 汤品
     */
    @TableField("soup")
    private String soup;

    /**
     * 水果
     */
    @TableField("fruits")
    private String fruits;

    /**
     * 营养信息（JSON格式）
     */
    @TableField("nutrition_info")
    private String nutritionInfo;

    /**
     * 适宜人群（JSON格式）
     */
    @TableField("suitable_crowd")
    private String suitableCrowd;

    /**
     * 饮食禁忌（JSON格式）
     */
    @TableField("dietary_restrictions")
    private String dietaryRestrictions;

    /**
     * 烹饪方法说明
     */
    @TableField("cooking_method")
    private String cookingMethod;

    /**
     * 预估成本（元）
     */
    @TableField("estimated_cost")
    private BigDecimal estimatedCost;

    /**
     * 创建者ID
     */    @TableField("creator_id")
    private Long creatorId;

    /**
     * 创建者姓名
     */
    @TableField("creator_name")
    private String creatorName;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;
}
