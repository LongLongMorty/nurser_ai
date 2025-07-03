package com.nursing.center.service;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.service
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  15:30
 * @Description: Building service interface
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.dto.BuildingDTO;
import com.nursing.center.dto.BuildingQueryDTO;

import java.util.List;

public interface BuildingService {

    /**
     * 分页查询楼栋信息
     */
    IPage<BuildingDTO> getBuildingPage(BuildingQueryDTO query);

    /**
     * 根据ID查询楼栋详情
     */
    BuildingDTO getBuildingById(Long id);

    /**
     * 新增楼栋
     */
    Long addBuilding(BuildingDTO buildingDTO);

    /**
     * 更新楼栋信息
     */
    void updateBuilding(BuildingDTO buildingDTO);

    /**
     * 删除楼栋
     */
    void deleteBuilding(Long id);

    /**
     * 获取所有启用的楼栋
     */
    List<BuildingDTO> getEnabledBuildings();
}
