package com.nursing.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI推荐临时表实体类
 * 用于显示当前推荐，刷新时清空
 */
@Data
@TableName("ai_recommendation_temp")
public class AiRecommendationTemp {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long customerId;
    
    private String customerName;
    
    private Integer customerAge;
    
    private String customerGender;
    
    private String sessionId;
    
    private String mealName;
    
    private String mealType;
    
    private String description;
    
    private String mainFood;
    
    private String sideDishes;
    
    private String soup;
    
    private String fruits;
    
    private String ingredients;
    
    private String cookingMethod;
    
    private BigDecimal calories;
    
    private BigDecimal protein;
    
    private BigDecimal fat;
    
    private BigDecimal carbohydrate;
    
    private String nutritionInfo;
    
    private String suitableCrowd;
    
    private String targetGroup;
    
    private String dietaryRestrictions;
    
    private BigDecimal estimatedCost;
    
    private BigDecimal aiScore;
    
    private String aiReason;
    
    private String aiModel;
    
    private String nutritionType;
    
    private String additionalRequirements;
    
    private Integer isFromHistory;
    
    private Long historyRefId;
    
    private Long userId;
    
    private Integer userAge;
    
    private String userGender;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}