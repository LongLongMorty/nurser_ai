package com.nursing.center.service.impl;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.service.impl
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  15:30
 * @Description: Building service implementation
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.common.exception.BusinessException;
import com.nursing.center.dto.BuildingDTO;
import com.nursing.center.dto.BuildingQueryDTO;
import com.nursing.center.entity.Building;
import com.nursing.center.mapper.BuildingMapper;
import com.nursing.center.service.BuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildingServiceImpl implements BuildingService {

    private final BuildingMapper buildingMapper;

    @Override
    public IPage<BuildingDTO> getBuildingPage(BuildingQueryDTO query) {
        Page<BuildingDTO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return buildingMapper.selectBuildingPage(page, query);
    }

    @Override
    public BuildingDTO getBuildingById(Long id) {
        return buildingMapper.selectBuildingById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addBuilding(BuildingDTO buildingDTO) {
        // 验证数据
        validateBuildingData(buildingDTO);

        // 检查楼栋名称是否已存在
        checkBuildingNameExists(buildingDTO.getBuildingName(), null);

        // 转换DTO为Entity
        Building building = new Building();
        BeanUtils.copyProperties(buildingDTO, building);

        // 设置默认值
        if (building.getStatus() == null) {
            building.setStatus(1); // 默认启用
        }

        // 保存楼栋信息
        buildingMapper.insert(building);

        log.info("新增楼栋成功，楼栋ID: {}, 名称: {}", building.getId(), building.getBuildingName());
        return building.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBuilding(BuildingDTO buildingDTO) {
        // 验证楼栋是否存在
        Building existBuilding = buildingMapper.selectById(buildingDTO.getId());
        if (existBuilding == null) {
            throw new BusinessException("楼栋不存在");
        }

        // 验证数据
        validateBuildingData(buildingDTO);

        // 检查楼栋名称是否已存在（排除当前楼栋）
        checkBuildingNameExists(buildingDTO.getBuildingName(), buildingDTO.getId());

        // 转换DTO为Entity
        Building building = new Building();
        BeanUtils.copyProperties(buildingDTO, building);

        // 更新楼栋信息
        buildingMapper.updateById(building);

        log.info("更新楼栋成功，楼栋ID: {}, 名称: {}", building.getId(), building.getBuildingName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBuilding(Long id) {
        Building building = buildingMapper.selectById(id);
        if (building == null) {
            throw new BusinessException("楼栋不存在");
        }

        // 检查楼栋下是否有房间
        BuildingDTO buildingWithStats = buildingMapper.selectBuildingById(id);
        if (buildingWithStats != null && buildingWithStats.getTotalRooms() > 0) {
            throw new BusinessException("楼栋下还有房间，无法删除");
        }

        buildingMapper.deleteById(id);

        log.info("删除楼栋成功，楼栋ID: {}", id);
    }

    @Override
    public List<BuildingDTO> getEnabledBuildings() {
        return buildingMapper.selectEnabledBuildings();
    }    /**
     * 验证楼栋数据
     */
    private void validateBuildingData(BuildingDTO buildingDTO) {
        if (!StringUtils.hasText(buildingDTO.getBuildingName())) {
            throw new BusinessException("楼栋名称不能为空");
        }

        if (buildingDTO.getFloorCount() == null || buildingDTO.getFloorCount() <= 0) {
            throw new BusinessException("楼层数必须大于0");
        }

        if (buildingDTO.getFloorCount() > 50) {
            throw new BusinessException("楼层数不能超过50层");
        }
    }

    /**
     * 检查楼栋名称是否已存在
     */
    private void checkBuildingNameExists(String buildingName, Long excludeId) {
        // 这里需要在BuildingMapper中添加检查方法，暂时省略实现
        // 实际项目中应该添加相应的查询方法
    }
}
