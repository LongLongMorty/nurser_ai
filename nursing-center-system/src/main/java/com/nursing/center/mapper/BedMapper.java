package com.nursing.center.mapper;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.mapper
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:10
 * @Description: TODO
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nursing.center.dto.*;
import com.nursing.center.entity.Bed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface BedMapper extends BaseMapper<Bed> {

    /**
     * 查询床位统计信息
     */
    BedStatisticsDTO selectBedStatistics();

    /**
     * 根据楼层查询床位信息
     */
    List<BedInfoDTO> selectBedsByFloor(@Param("floorNo") Integer floorNo);

    /**
     * 查询房间内的空闲床位
     */
    List<BedInfoDTO> selectAvailableBedsByRoom(@Param("roomId") Long roomId);

    /**
     * 根据床位ID查询床位详细信息
     */
    BedDetailDTO selectBedDetail(@Param("bedId") Long bedId);    /**
     * 获取床位图表数据（按楼层显示所有床位状态）
     */
    List<BedInfoDTO> selectBedDiagram();
      /**
     * 根据楼层查询房间及床位信息（用于床位示意图）
     */
    List<Map<String, Object>> selectBedDiagramByFloor(@Param("floorNo") Integer floorNo);

    /**
     * 获取床位使用历史记录
     */
    List<BedInfoDTO> selectBedUsageHistory(@Param("bedId") Long bedId, @Param("customerId") Long customerId, @Param("offset") Integer offset, @Param("size") Integer size);    /**
     * 获取可交换的床位列表
     */
    List<BedInfoDTO> selectSwappableBeds(@Param("customerId") Long customerId);
      /**
     * 更新客户的床位信息
     */
    void updateCustomerBedInfo(@Param("customerId") Long customerId, 
                              @Param("bedId") Long bedId,
                              @Param("roomId") Long roomId,
                              @Param("buildingId") Long buildingId);
}
