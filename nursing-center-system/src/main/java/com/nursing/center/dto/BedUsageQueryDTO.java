package com.nursing.center.dto;

import com.nursing.center.common.enums.UsageStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 床位使用记录查询DTO
 */
@Data
public class BedUsageQueryDTO {
    
    /**
     * 分页参数
     */
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    
    /**
     * 客户姓名（模糊查询）
     */
    private String customerName;
    
    /**
     * 入住开始时间范围
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateFrom;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateTo;
    
    /**
     * 使用状态
     */
    private UsageStatus usageStatus;
    
    /**
     * 床位号（模糊查询）
     */
    private String bedNo;
    
    /**
     * 房间号（模糊查询）
     */
    private String roomNo;
    
    /**
     * 楼层号
     */
    private Integer floorNo;
}
