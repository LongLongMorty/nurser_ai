package com.nursing.center.controller;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.controller
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  14:00
 * @Description: TODO
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.dto.CheckoutApplyDTO;
import com.nursing.center.dto.CheckoutApplyQueryDTO;
import com.nursing.center.dto.CheckoutApproveDTO;
import com.nursing.center.service.CheckoutApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/checkout-apply")
@RequiredArgsConstructor
@Validated
public class CheckoutApplyController {

    private final CheckoutApplyService checkoutApplyService;

 
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<CheckoutApplyDTO>> getCheckoutApplyPage(CheckoutApplyQueryDTO query) {
        IPage<CheckoutApplyDTO> page = checkoutApplyService.getCheckoutApplyPage(query);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CheckoutApplyDTO> getCheckoutApplyById(@PathVariable Long id) {
        CheckoutApplyDTO apply = checkoutApplyService.getCheckoutApplyById(id);
        return Result.success(apply);
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> approveCheckoutApply(@Valid @RequestBody CheckoutApproveDTO approveDTO) {
        checkoutApplyService.approveCheckoutApply(approveDTO);
        return Result.success("退住申请审批完成");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteCheckoutApply(@PathVariable Long id) {
        checkoutApplyService.deleteCheckoutApply(id);
        return Result.success("退住申请删除成功");
    }
}
