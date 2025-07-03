package com.nursing.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户个性化菜单实体类
 * 为特定客户生成的一天四餐
 */
@Data
@TableName("ai_personalized_menu")
public class AiPersonalizedMenu {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long customerId;
    
    private String customerName;
    
    private Integer customerAge;
    
    private String customerGender;
    
    private LocalDate menuDate;
    
    private String sessionId;
    
    private String healthConditions;
    
    private String dietaryPreferences;
    
    private String allergies;
    
    // 早餐
    private String breakfastName;
    private String breakfastDescription;
    private String breakfastIngredients;
    private String breakfastCookingMethod;
    private BigDecimal breakfastCalories;
    private String breakfastNutrition;
    
    // 午餐
    private String lunchName;
    private String lunchDescription;
    private String lunchIngredients;
    private String lunchCookingMethod;
    private BigDecimal lunchCalories;
    private String lunchNutrition;
    
    // 晚餐
    private String dinnerName;
    private String dinnerDescription;
    private String dinnerIngredients;
    private String dinnerCookingMethod;
    private BigDecimal dinnerCalories;
    private String dinnerNutrition;
    
    // 加餐
    private String snackName;
    private String snackDescription;
    private String snackIngredients;
    private String snackCookingMethod;
    private BigDecimal snackCalories;
    private String snackNutrition;
    
    // 总计信息
    private BigDecimal totalCalories;
    private BigDecimal totalCost;
    private String aiRecommendationReason;
    private String aiRawResponse;
    private String nutritionistReview;
    
    private Integer isApproved;
    private Long approvedBy;
    private LocalDateTime approvedTime;
    
    private Long createdByUserId;
    private String createdByUserName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
