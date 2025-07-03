package com.nursing.center.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nursing.center.common.enums.UsageStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 床位使用详情实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bed_usage")
public class BedUsage extends BaseEntity {
    
    /**
     * 客户ID
     */
    private Long customerId;
    
    /**
     * 床位ID
     */
    private Long bedId;
    
    /**
     * 房间ID
     */
    private Long roomId;
    
    /**
     * 楼栋ID
     */
    private Long buildingId;
    
    /**
     * 入住开始时间
     */
    private LocalDate startDate;
    
    /**
     * 入住结束时间
     */
    private LocalDate endDate;
    
    /**
     * 使用状态
     */
    private UsageStatus usageStatus;
}
