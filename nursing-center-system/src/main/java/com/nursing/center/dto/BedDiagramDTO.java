package com.nursing.center.dto;

import com.nursing.center.common.enums.BedStatus;
import com.nursing.center.common.enums.BedType;
import lombok.Data;

import java.util.List;

/**
 * 床位图表DTO - 用于显示床位布局图
 */
@Data
public class BedDiagramDTO {
    
    /**
     * 楼层号
     */
    private Integer floorNo;
    
    /**
     * 该楼层的所有房间
     */
    private List<RoomDiagramDTO> rooms;
    
    @Data
    public static class RoomDiagramDTO {
        /**
         * 房间ID
         */
        private Long roomId;
        
        /**
         * 房间号
         */
        private String roomNo;
        
        /**
         * 房间名称
         */
        private String roomName;
        
        /**
         * 房间类型
         */
        private String roomType;
        
        /**
         * 该房间的所有床位
         */
        private List<BedDiagramItemDTO> beds;
    }
    
    @Data
    public static class BedDiagramItemDTO {
        /**
         * 床位ID
         */
        private Long bedId;
        
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
         * 当前客户姓名（如果有）
         */
        private String customerName;
        
        /**
         * 当前客户ID（如果有）
         */
        private Long customerId;
        
        /**
         * 护理级别（如果有）
         */
        private String careLevel;
        
        /**
         * 健康管家姓名（如果有）
         */
        private String healthManagerName;
    }
}
