package com.nursing.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI推荐采纳历史表实体类
 * 记录所有被采纳的推荐，用于生成历史推荐
 */
@Data
@TableName("ai_recommendation_history")
public class AiRecommendationHistory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long mealConfigId;
    
    private String originalAiContent;
    
    private String mealName;
    
    private String mealType;
    
    private String targetGroup;
    
    private String nutritionType;
    
    private BigDecimal aiScore;
    
    private BigDecimal userRating;
    
    private LocalDateTime adoptionDate;
    
    private Long adoptedByUserId;
    
    private String adoptedByUserName;
    
    private BigDecimal effectivenessScore;
    
    private Integer isRecommendedAgain;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}