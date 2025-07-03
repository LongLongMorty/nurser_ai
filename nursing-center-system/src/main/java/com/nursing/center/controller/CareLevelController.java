package com.nursing.center.controller;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.controller
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:42
 * @Description: TODO
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.dto.CareLevelDTO;
import com.nursing.center.dto.CareLevelQueryDTO;
import com.nursing.center.dto.CareLevelItemConfig;
import com.nursing.center.dto.CareItemDTO;
import com.nursing.center.service.CareLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api/admin/care-level")
@RequiredArgsConstructor
@Validated
public class CareLevelController {

    private final CareLevelService careLevelService;

   
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<CareLevelDTO>> getCareLevelPage(CareLevelQueryDTO query) {
        IPage<CareLevelDTO> page = careLevelService.getCareLevelPage(query);
        return Result.success(page);
    }

    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CareLevelDTO> getCareLevelById(@PathVariable Long id) {
        CareLevelDTO careLevel = careLevelService.getCareLevelById(id);
        return Result.success(careLevel);
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> addCareLevel(@Valid @RequestBody CareLevelDTO careLevelDTO) {
        Long careLevelId = careLevelService.addCareLevel(careLevelDTO);
        return Result.success("护理级别添加成功", careLevelId);
    }

   
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateCareLevel(@Valid @RequestBody CareLevelDTO careLevelDTO) {
        careLevelService.updateCareLevel(careLevelDTO);
        return Result.success("护理级别修改成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteCareLevel(@PathVariable Long id) {
        careLevelService.deleteCareLevel(id);
        return Result.success("护理级别删除成功");
    }    
    @GetMapping("/{id}/items")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CareLevelItemConfig>> getItemConfigsByLevelId(@PathVariable Long id) {
        List<CareLevelItemConfig> itemConfigs = careLevelService.getItemConfigsByLevelId(id);
        return Result.success(itemConfigs);
    }

    
    @GetMapping("/{id}/available-items")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CareItemDTO>> getAvailableItemsForLevel(@PathVariable Long id) {
        List<CareItemDTO> items = careLevelService.getAvailableItemsForLevel(id);
        return Result.success(items);
    }

    
    @PostMapping("/{id}/configure-items")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> configureItems(@PathVariable Long id, @RequestBody List<Long> careItemIds) {
        careLevelService.configureItems(id, careItemIds);
        return Result.success("护理项目配置成功");
    }

   
    @PostMapping("/{id}/remove-items")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> removeItems(@PathVariable Long id, @RequestBody List<Long> careItemIds) {
        careLevelService.removeItems(id, careItemIds);
        return Result.success("护理项目移除成功");
    }

    
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CareLevelDTO>> getAllCareLevels() {
        List<CareLevelDTO> levels = careLevelService.getEnabledCareLevels();
        return Result.success(levels);
    }

    @GetMapping("/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CareLevelDTO>> getEnabledCareLevels() {
        List<CareLevelDTO> levels = careLevelService.getEnabledCareLevels();
        return Result.success(levels);
    }
}
