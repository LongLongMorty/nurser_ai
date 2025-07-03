package com.nursing.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.dto.MealConfigDTO;
import com.nursing.center.dto.MealConfigQueryDTO;
import com.nursing.center.service.MealConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 膳食配置控制器
 */
@RestController
@RequestMapping("/api/admin/meal-config")
@RequiredArgsConstructor
@Validated
@Slf4j
public class MealConfigController {

    private final MealConfigService mealConfigService;

    /**
     * 分页查询膳食配置
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<MealConfigDTO>> getMealConfigPage(MealConfigQueryDTO query) {
        IPage<MealConfigDTO> page = mealConfigService.getMealConfigPage(query);
        return Result.success(page);
    }

    /**
     * 根据ID查询膳食配置详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<MealConfigDTO> getMealConfigById(@PathVariable Long id) {
        MealConfigDTO config = mealConfigService.getMealConfigById(id);
        return Result.success(config);
    }

    /**
     * 新增膳食配置
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> addMealConfig(@Valid @RequestBody MealConfigDTO configDTO) {
        Long id = mealConfigService.addMealConfig(configDTO);
        return Result.success(id);
    }

    /**
     * 修改膳食配置
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateMealConfig(@Valid @RequestBody MealConfigDTO configDTO) {
        mealConfigService.updateMealConfig(configDTO);
        return Result.success("修改成功");
    }

    /**
     * 删除膳食配置
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteMealConfig(@PathVariable Long id) {
        mealConfigService.deleteMealConfig(id);
        return Result.success("删除成功");
    }

    /**
     * 根据餐次类型查询膳食配置
     */
    @GetMapping("/by-type")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<MealConfigDTO>> getMealConfigsByType(@RequestParam String mealType) {
        List<MealConfigDTO> configs = mealConfigService.getMealConfigsByType(mealType);
        return Result.success(configs);
    }

    /**
     * 获取所有启用的膳食配置
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<MealConfigDTO>> getActiveMealConfigs() {
        List<MealConfigDTO> configs = mealConfigService.getActiveMealConfigs();
        return Result.success(configs);
    }

    /**
     * 批量更新膳食配置状态
     */
    @PostMapping("/batch-status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> batchUpdateStatus(@RequestBody BatchUpdateStatusRequest request) {
        mealConfigService.batchUpdateStatus(request.getIds(), request.getStatus());
        return Result.success("批量更新成功");
    }    /**
     * 批量状态更新请求DTO
     */
    public static class BatchUpdateStatusRequest {
        private List<Long> ids;
        private Integer status;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
