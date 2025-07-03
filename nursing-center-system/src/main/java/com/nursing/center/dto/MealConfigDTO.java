package com.nursing.center.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 膳食配置DTO
 * @author system
 * @since 2025-06-22
 */
@Data
public class MealConfigDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 膳食名称
     */
    @NotBlank(message = "膳食名称不能为空")
    private String mealName;

    /**
     * 膳食类型：BREAKFAST-早餐，LUNCH-午餐，DINNER-晚餐，SNACK-加餐
     */
    @NotBlank(message = "膳食类型不能为空")
    private String mealType;

    /**
     * 膳食描述
     */
    private String description;

    /**
     * 主食内容（JSON格式）
     */
    private String mainFood;

    /**
     * 菜品内容（JSON格式）
     */
    private String sideDishes;

    /**
     * 汤品
     */
    private String soup;

    /**
     * 水果
     */
    private String fruits;

    /**
     * 营养信息（JSON格式：热量、蛋白质、脂肪、碳水化合物等）
     */
    private String nutritionInfo;

    /**
     * 适宜人群（JSON格式）
     */
    private String suitableCrowd;

    /**
     * 饮食禁忌（JSON格式）
     */
    private String dietaryRestrictions;

    /**
     * 烹饪方法说明
     */
    private String cookingMethod;

    /**
     * 预估成本（元）
     */
    private BigDecimal estimatedCost;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建者姓名
     */
    private String creatorName;

    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer deleted;
}
