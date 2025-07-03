package com.nursing.center.controller;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.controller
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  14:13
 * @Description: TODO
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.dto.*;
import com.nursing.center.service.HealthManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api/admin/health-manager")
@RequiredArgsConstructor
@Validated
@Slf4j
public class HealthManagerController {

    private final HealthManagerService healthManagerService;


    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<HealthManagerDTO>> getHealthManagerPage(HealthManagerQueryDTO query) {
        IPage<HealthManagerDTO> page = healthManagerService.getHealthManagerPage(query);
        return Result.success(page);
    }

    
    @GetMapping("/{id}/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CustomerServiceDTO>> getServiceCustomers(@PathVariable Long id) {
        List<CustomerServiceDTO> customers = healthManagerService.getServiceCustomers(id);
        return Result.success(customers);
    }

    
    @GetMapping("/customers/without-manager")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CustomerServiceDTO>> getCustomersWithoutManager(@RequestParam(required = false) String customerName) {
        List<CustomerServiceDTO> customers = healthManagerService.getCustomersWithoutManager(customerName);
        return Result.success(customers);
    }

   
    @PostMapping("/set-customers")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> setServiceCustomers(@Valid @RequestBody SetServiceCustomerDTO setServiceDTO) {
        healthManagerService.setServiceCustomers(setServiceDTO);
        return Result.success("服务客户设置成功");
    }

      @PostMapping("/remove-customers")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> removeServiceCustomers(@Valid @RequestBody RemoveServiceCustomerDTO removeServiceDTO) {
        log.info("收到移除服务客户请求 - 健康管家ID: {}, 客户ID列表: {}", 
                removeServiceDTO.getHealthManagerId(), removeServiceDTO.getCustomerIds());
        healthManagerService.removeServiceCustomers(removeServiceDTO);
        return Result.success("服务客户移除成功");
    }
}
