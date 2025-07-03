package com.nursing.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.dto.BedUsageDTO;
import com.nursing.center.dto.BedUsageQueryDTO;
import com.nursing.center.entity.BedUsage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 床位使用记录Mapper
 */
@Mapper
public interface BedUsageMapper extends BaseMapper<BedUsage> {
    
    /**
     * 逻辑删除客户的当前床位使用记录
     */
    void logicalDeleteCurrentUsageByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * 根据客户ID和床位ID查询当前使用记录
     */
    BedUsage selectCurrentUsageByCustomerAndBed(@Param("customerId") Long customerId, @Param("bedId") Long bedId);
    
    /**
     * 结束客户的当前床位使用记录
     */
    void endCurrentUsageByCustomerId(@Param("customerId") Long customerId, @Param("endDate") LocalDate endDate);
      /**
     * 创建新的床位使用记录
     */
    void insertBedUsage(BedUsage bedUsage);
    
    /**
     * 分页查询床位使用记录
     */
    IPage<BedUsageDTO> selectBedUsagePage(Page<BedUsageDTO> page, @Param("query") BedUsageQueryDTO query);
    
    /**
     * 根据ID查询床位使用记录详情
     */
    BedUsageDTO selectBedUsageById(@Param("id") Long id);
      /**
     * 更新床位使用记录的结束时间
     */
    void updateEndDate(@Param("id") Long id, @Param("endDate") LocalDate endDate);
    
    /**
     * 根据床位ID获取床位的房间和楼栋信息
     */
    @org.apache.ibatis.annotations.Select("SELECT b.id, b.room_id as roomId, r.building_id as buildingId FROM bed b " +
            "INNER JOIN room r ON b.room_id = r.id WHERE b.id = #{bedId}")
    @org.apache.ibatis.annotations.Results({
        @org.apache.ibatis.annotations.Result(property = "id", column = "id"),
        @org.apache.ibatis.annotations.Result(property = "roomId", column = "roomId"),
        @org.apache.ibatis.annotations.Result(property = "buildingId", column = "buildingId")
    })
    BedLocationInfo getBedLocationInfo(@Param("bedId") Long bedId);
    
    /**
     * 内部类：床位位置信息
     */
    class BedLocationInfo {
        private Long id;
        private Long roomId;
        private Long buildingId;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }
        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }
    }
}
