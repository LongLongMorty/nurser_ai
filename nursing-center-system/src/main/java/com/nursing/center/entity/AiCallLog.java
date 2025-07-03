package com.nursing.center.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI调用日志表实体类
 * 记录API调用情况
 */
@Data
@TableName("ai_call_log")
public class AiCallLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String sessionId;
    
    private String apiType;
    
    private String requestParams;
    
    private String aiModel;
    
    private String apiUrl;
    
    private LocalDateTime requestTime;
    
    private LocalDateTime responseTime;
    
    private Integer responseDuration;
    
    private String responseStatus;
    
    private String responseContent;
    
    private String errorMessage;
    
    private Integer tokenUsage;
    
    private BigDecimal apiCost;
    
    private Long userId;
    
    private Long customerId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
