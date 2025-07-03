package com.nursing.center.service;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.service
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:12
 * @Description: TODO
 * @Version: 1.0
 */
import com.nursing.center.dto.*;

import java.util.List;

public interface BedService {

    /**
     * 获取床位统计信息
     */
    BedStatisticsDTO getBedStatistics();

    /**
     * 根据楼层查询床位信息
     */
    List<BedInfoDTO> getBedsByFloor(Integer floorNo);

    /**
     * 查询房间内的空闲床位
     */
    List<BedInfoDTO> getAvailableBedsByRoom(Long roomId);

    /**
     * 获取床位详细信息
     */
    BedDetailDTO getBedDetail(Long bedId);    /**
     * 获取床位图表数据（按楼层显示所有床位状态）
     */
    List<BedInfoDTO> getBedDiagram();
    
    /**
     * 根据楼层查询房间及床位信息（用于床位示意图）
     */
    List<BedDiagramDTO> getBedDiagramByFloor(Integer floorNo);

    /**
     * 获取床位使用历史记录
     */
    List<BedInfoDTO> getBedUsageHistory(Long bedId, Long customerId, Integer page, Integer size);

    /**
     * 床位交换
     */
    void swapBeds(BedSwapDTO bedSwapDTO);    /**
     * 获取可交换的床位列表
     */
    List<BedInfoDTO> getSwappableBeds(Long customerId);    /**
     * 获取所有楼层的床位示意图数据（按楼层分组）
     */
    List<BedDiagramDTO> getBedDiagramsGrouped();
    
    /**
     * 根据床位ID获取床位实体
     */
    com.nursing.center.entity.Bed getBedById(Long bedId);
    
    /**
     * 更新床位状态
     */
    void updateBedStatus(Long bedId, com.nursing.center.common.enums.BedStatus bedStatus);
    
    /**
     * 更新客户的床位信息
     */
    void updateCustomerBedInfo(Long customerId, Long newBedId);
}
