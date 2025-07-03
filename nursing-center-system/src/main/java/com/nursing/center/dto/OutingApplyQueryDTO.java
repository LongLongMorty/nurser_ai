package com.nursing.center.dto;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.dto
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:55
 * @Description: TODO
 * @Version: 1.0
 */
import com.nursing.center.common.enums.ApplyStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate; // 新增导入
import java.time.LocalDateTime;

@Data
public class OutingApplyQueryDTO {
    private String customerName; // 模糊查询
    private ApplyStatus applyStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate; // 修改类型
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate; // 修改类型
    private Long applicantId; // 查询某个申请人的申请
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
