package com.nursing.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.dto.*;
import com.nursing.center.service.CustomerCareService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/customer-care")
@RequiredArgsConstructor
@Validated
public class CustomerCareController {

    private final CustomerCareService customerCareService;

    /**
     * 分页查询客户护理设置
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<CustomerCarePageDTO>> getCustomerCarePage(CustomerCareQueryParams query) {
        IPage<CustomerCarePageDTO> page = customerCareService.getCustomerCarePage(query);
        return Result.success(page);
    }

    /**
     * 获取客户护理详情
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CustomerCareDetailDTO> getCustomerDetail(@PathVariable Long customerId) {
        CustomerCareDetailDTO detail = customerCareService.getCustomerCareDetail(customerId);
        return Result.success(detail);
    }

    /**
     * 设置客户护理级别
     */
    @PostMapping("/set-level")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> setCustomerCareLevel(@Valid @RequestBody CustomerCareLevelSetDTO dto) {
        customerCareService.setCustomerCareLevel(dto);
        return Result.success("护理级别设置成功");
    }

    /**
     * 移除客户护理级别
     */
    @DeleteMapping("/remove-level/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> removeCustomerCareLevel(@PathVariable Long customerId) {
        customerCareService.removeCustomerCareLevel(customerId);
        return Result.success("护理级别移除成功");
    }

    /**
     * 批量购买护理项目
     */
    @PostMapping("/purchase/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> purchaseCareItems(@PathVariable Long customerId,
                                            @RequestBody List<CustomerCareItemPurchaseDTO> careItems) {
        customerCareService.purchaseCareItems(customerId, careItems);
        return Result.success("护理项目购买成功");
    }    /**
     * 护理服务续费
     */
    @PostMapping("/renew/{customerCareId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> renewCareService(@PathVariable Long customerCareId,
                                           @RequestParam Integer additionalQuantity,
                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate newExpireDate) {
        customerCareService.renewCareService(customerCareId, additionalQuantity, newExpireDate);
        return Result.success("护理服务续费成功");
    }

    /**
     * 移除护理服务
     */
    @DeleteMapping("/{customerCareId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> removeCareService(@PathVariable Long customerCareId) {
        customerCareService.removeCareService(customerCareId);
        return Result.success("护理服务移除成功");
    }

    /**
     * 获取客户未拥有的护理项目
     */
    @GetMapping("/available/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CustomerCareDTO>> getAvailableItemsForCustomer(@PathVariable Long customerId,
                                                                      @RequestParam(required = false) String itemName) {
        List<CustomerCareDTO> items = customerCareService.getAvailableItemsForCustomer(customerId, itemName);
        return Result.success(items);
    }

    /**
     * 获取客户护理服务列表
     */
    @GetMapping("/services/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CustomerCareDTO>> getCustomerCareServices(@PathVariable Long customerId) {
        List<CustomerCareDTO> services = customerCareService.getCustomerCareServices(customerId);
        return Result.success(services);
    }
}
