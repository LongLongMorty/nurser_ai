package com.nursing.center.dto;

import com.nursing.center.common.enums.UsageStatus;
import lombok.Data;

import java.time.LocalDate;

/**
 * 床位使用记录DTO
 */
@Data
public class BedUsageDTO {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 入住开始时间
     */
    private LocalDate startDate;
    
    /**
     * 入住结束时间
     */
    private LocalDate endDate;
    
    /**
     * 使用天数
     */
    private Integer duration;
    
    /**
     * 使用状态
     */
    private UsageStatus usageStatus;
    
    // 客户信息
    private Long customerId;
    private String customerName;
    private Integer age;
    private String gender;
    private String idCard;
    
    // 床位位置信息
    private Long bedId;
    private String bedNo;
    private String roomNo;
    private String buildingNo;
    private Integer floorNo;
    private String fullLocation; // 如：606号楼-2楼-201室-A床
}
