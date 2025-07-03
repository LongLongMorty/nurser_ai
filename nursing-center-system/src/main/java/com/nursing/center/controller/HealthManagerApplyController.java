package com.nursing.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.common.utils.SecurityUtils;
import com.nursing.center.dto.*;
import com.nursing.center.service.CheckoutApplyService;
import com.nursing.center.service.OutingApplyService;
import com.nursing.center.service.ReturnApplyService;
import com.nursing.center.vo.ReturnApplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 健康管家-申请管理控制器
 * 包含退住申请、外出申请、回院申请等功能
 * @author system
 * @since 2025-01-16
 */
@RestController
@RequestMapping("/api/health-manager/apply")
@RequiredArgsConstructor
@Validated
public class HealthManagerApplyController {

    private final CheckoutApplyService checkoutApplyService;
    private final OutingApplyService outingApplyService;
    private final ReturnApplyService returnApplyService;

    // ==================== 退住申请 ====================

    /**
     * 提交退住申请
     */
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<Long> submitCheckoutApply(@Valid @RequestBody CheckoutApplyDTO checkoutApplyDTO) {
        // 设置申请人为当前健康管家
        checkoutApplyDTO.setApplicantId(SecurityUtils.getCurrentUserId());
        Long applyId = checkoutApplyService.submitCheckoutApply(checkoutApplyDTO);
        return Result.success("退住申请提交成功", applyId);
    }

    /**
     * 查询我的退住申请
     */
    @GetMapping("/checkout/my")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<IPage<CheckoutApplyDTO>> getMyCheckoutApplies(CheckoutApplyQueryDTO query) {
        // 设置申请人为当前健康管家
        query.setApplicantId(SecurityUtils.getCurrentUserId());
        IPage<CheckoutApplyDTO> page = checkoutApplyService.getCheckoutApplyPage(query);
        return Result.success(page);
    }

    // ==================== 外出申请 ====================

    /**
     * 提交外出申请
     */
    @PostMapping("/outing")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<Long> submitOutingApply(@Valid @RequestBody OutingApplyDTO outingApplyDTO) {
        // 设置申请人为当前健康管家
        outingApplyDTO.setApplicantId(SecurityUtils.getCurrentUserId());
        Long applyId = outingApplyService.submitOutingApply(outingApplyDTO);
        return Result.success("外出申请提交成功", applyId);
    }

    /**
     * 查询我的外出申请
     */
    @GetMapping("/outing/my")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<IPage<OutingApplyDTO>> getMyOutingApplies(OutingApplyQueryDTO query) {
        // 只查询当前健康管家提交的申请
        query.setApplicantId(SecurityUtils.getCurrentUserId());
        IPage<OutingApplyDTO> page = outingApplyService.getOutingApplyPage(query);
        return Result.success(page);
    }

    /**
     * 登记回院（外出申请相关）
     */
    @PostMapping("/outing/return")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<String> registerReturn(@Valid @RequestBody OutingReturnDTO returnDTO) {
        outingApplyService.registerReturn(returnDTO);
        return Result.success("回院登记完成");
    }

    // ==================== 回院申请 ====================

    /**
     * 提交回院申请
     */
    @PostMapping("/return")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<Long> submitReturnApply(@Valid @RequestBody ReturnApplyDTO returnApplyDTO) {
        // 设置申请人为当前健康管家
        returnApplyDTO.setApplicantId(SecurityUtils.getCurrentUserId());
        Long applyId = returnApplyService.submitReturnApply(returnApplyDTO);
        return Result.success("回院申请提交成功", applyId);
    }

    /**
     * 查询我的回院申请
     */
    @GetMapping("/return/my")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<IPage<ReturnApplyVO>> getMyReturnApplies(ReturnApplyQueryDTO query) {
        // 只查询当前健康管家提交的申请
        query.setApplicantId(SecurityUtils.getCurrentUserId());
        IPage<ReturnApplyVO> page = returnApplyService.getReturnApplyPage(query);
        return Result.success(page);
    }

    /**
     * 检查客户是否可以申请回院
     */
    @GetMapping("/return/can-apply/{customerId}")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<Boolean> canApplyReturn(@PathVariable Long customerId) {
        boolean canApply = returnApplyService.canApplyReturn(customerId);
        return Result.success("查询成功", canApply);
    }
}
