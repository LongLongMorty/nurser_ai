package com.nursing.center.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 床位交换DTO
 */
@Data
public class BedSwapDTO {
    
    /**
     * 源客户ID
     */
    private Long sourceCustomerId;
    
    /**
     * 源床位ID
     */
    private Long sourceBedId;
    
    /**
     * 目标客户ID（可选，用于两人交换）
     */
    private Long targetCustomerId;
    
    /**
     * 目标床位ID
     */
    private Long targetBedId;
    
    /**
     * 交换原因
     */
    private String swapReason;    /**
     * 交换日期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime swapDate;
    
    /**
     * 备注
     */
    private String remark;
}
