package com.nursing.center.controller;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.controller
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  15:30
 * @Description: Building management controller
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.dto.BuildingDTO;
import com.nursing.center.dto.BuildingQueryDTO;
import com.nursing.center.service.BuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/building")
@RequiredArgsConstructor
@Validated
public class BuildingController {

    private final BuildingService buildingService;

    /**
     * 分页查询楼栋信息
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<BuildingDTO>> getBuildingPage(BuildingQueryDTO query) {
        IPage<BuildingDTO> page = buildingService.getBuildingPage(query);
        return Result.success(page);
    }

    /**
     * 根据ID查询楼栋详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BuildingDTO> getBuildingById(@PathVariable Long id) {
        BuildingDTO building = buildingService.getBuildingById(id);
        return Result.success(building);
    }

    /**
     * 新增楼栋
     */    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> addBuilding(@RequestBody @Valid BuildingDTO buildingDTO) {
        Long id = buildingService.addBuilding(buildingDTO);
        return Result.success("楼栋添加成功", id);
    }

    /**
     * 更新楼栋信息
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateBuilding(@RequestBody @Valid BuildingDTO buildingDTO) {
        buildingService.updateBuilding(buildingDTO);
        return Result.success();
    }

    /**
     * 删除楼栋
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return Result.success();
    }

    /**
     * 获取所有启用的楼栋列表
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BuildingDTO>> getEnabledBuildings() {
        List<BuildingDTO> buildings = buildingService.getEnabledBuildings();
        return Result.success(buildings);
    }
}
