package com.nursing.center.controller;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.controller
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:45
 * @Description: TODO
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.dto.CareRecordDTO;
import com.nursing.center.dto.CareRecordQueryDTO;
import com.nursing.center.service.CareRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/api/admin/care-record")
@RequiredArgsConstructor
@Validated
public class CareRecordController {

    private final CareRecordService careRecordService;    // 分页查询护理记录
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<CareRecordDTO>> getCareRecordPage(CareRecordQueryDTO query) {
        IPage<CareRecordDTO> page = careRecordService.getCareRecordPage(query);
        return Result.success(page);
    }

    // 根据客户ID查询护理记录
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<CareRecordDTO>> getCustomerCareRecords(@PathVariable Long customerId,
                                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<CareRecordDTO> page = careRecordService.getCustomerCareRecords(customerId, pageNum, pageSize);
        return Result.success(page);
    }

    // 根据ID查询护理记录详情 - 放在{id}路由之前避免冲突
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CareRecordDTO> getCareRecordById(@PathVariable Long id) {
        CareRecordDTO record = careRecordService.getCareRecordById(id);
        return Result.success(record);
    }
    
    // 添加护理记录
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> addCareRecord(@Valid @RequestBody CareRecordDTO careRecordDTO) {
        Long recordId = careRecordService.addCareRecord(careRecordDTO);
        return Result.success("护理记录添加成功", recordId);
    }

    // 删除护理记录（逻辑删除）
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteCareRecord(@PathVariable Long id) {
        careRecordService.deleteCareRecord(id);
        return Result.success("护理记录删除成功");
    }
}
