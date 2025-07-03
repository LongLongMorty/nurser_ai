package com.nursing.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.dto.ReturnApproveDTO;
import com.nursing.center.dto.ReturnApplyQueryDTO;
import com.nursing.center.service.ReturnApplyService;
import com.nursing.center.vo.ReturnApplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 回院申请管理控制器
 * @author system
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/admin/return-apply")
@RequiredArgsConstructor
@Validated
public class ReturnApplyController {

    private final ReturnApplyService returnApplyService;

    /**
     * 分页查询回院申请列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<ReturnApplyVO>> getReturnApplyPage(@Valid ReturnApplyQueryDTO queryDTO) {
        IPage<ReturnApplyVO> result = returnApplyService.getReturnApplyPage(queryDTO);
        return Result.success("查询成功", result);
    }

    /**
     * 根据ID查询回院申请详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ReturnApplyVO> getReturnApplyById(@PathVariable Long id) {
        ReturnApplyVO result = returnApplyService.getReturnApplyById(id);
        return Result.success("查询成功", result);
    }    /**
     * 审批回院申请
     */
    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approveReturnApply(@Valid @RequestBody ReturnApproveDTO approveDTO) {
        returnApplyService.approveReturnApply(approveDTO);
        return Result.success();
    }

    /**
     * 检查客户是否可以申请回院
     */
    @GetMapping("/can-apply/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HEALTH_MANAGER')")
    public Result<Boolean> canApplyReturn(@PathVariable Long customerId) {
        boolean canApply = returnApplyService.canApplyReturn(customerId);
        return Result.success("查询成功", canApply);
    }
}
