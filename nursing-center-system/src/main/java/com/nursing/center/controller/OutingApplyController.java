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
import com.nursing.center.dto.OutingApplyDTO;
import com.nursing.center.dto.OutingApplyQueryDTO;
import com.nursing.center.dto.OutingApproveDTO;
import com.nursing.center.dto.OutingReturnDTO;
import com.nursing.center.service.OutingApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/outing-apply")
@RequiredArgsConstructor
@Validated
public class OutingApplyController {

    private final OutingApplyService outingApplyService;

    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<OutingApplyDTO>> getOutingApplyPage(OutingApplyQueryDTO query) {
        IPage<OutingApplyDTO> page = outingApplyService.getOutingApplyPage(query);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<OutingApplyDTO> getOutingApplyById(@PathVariable Long id) {
        OutingApplyDTO apply = outingApplyService.getOutingApplyById(id);
        return Result.success(apply);
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> approveOutingApply(@Valid @RequestBody OutingApproveDTO approveDTO) {
        outingApplyService.approveOutingApply(approveDTO);
        return Result.success("外出申请审批完成");
    }

    @PostMapping("/return")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> registerReturn(@Valid @RequestBody OutingReturnDTO returnDTO) {
        outingApplyService.registerReturn(returnDTO);
        return Result.success("回院登记完成");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteOutingApply(@PathVariable Long id) {
        outingApplyService.deleteOutingApply(id);
        return Result.success("外出申请删除成功");
    }
}
