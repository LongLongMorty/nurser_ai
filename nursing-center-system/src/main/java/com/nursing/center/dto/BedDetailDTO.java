package com.nursing.center.dto;

import com.nursing.center.common.enums.BedStatus;
import com.nursing.center.common.enums.BedType;
import lombok.Data;

import java.time.LocalDate;

/**
 * 床位详细信息DTO
 */
@Data
public class BedDetailDTO {
    
    /**
     * 床位ID
     */
    private Long id;
    
    /**
     * 床位号
     */
    private String bedNo;
    
    /**
     * 床位类型
     */
    private BedType bedType;
    
    /**
     * 床位状态
     */
    private BedStatus bedStatus;
    
    /**
     * 房间信息
     */
    private RoomInfoDTO room;
    
    /**
     * 楼栋信息
     */
    private BuildingInfoDTO building;
    
    /**
     * 当前入住客户信息
     */
    private CurrentCustomerDTO currentCustomer;
    
    /**
     * 床位使用历史
     */
    private java.util.List<BedUsageHistoryDTO> usageHistory;
    
    @Data
    public static class RoomInfoDTO {
        private Long id;
        private String roomNo;
        private String roomName;
        private Integer floorNo;
    }
    
    @Data
    public static class BuildingInfoDTO {
        private Long id;
        private String buildingNo;
        private String buildingName;
    }
    
    @Data
    public static class CurrentCustomerDTO {
        private Long id;
        private String customerName;
        private Integer age;
        private String gender;
        private LocalDate checkInDate;
        private String healthManagerName;
    }
    
    @Data
    public static class BedUsageHistoryDTO {
        private Long customerId;
        private String customerName;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer duration; // 使用天数
    }
}
