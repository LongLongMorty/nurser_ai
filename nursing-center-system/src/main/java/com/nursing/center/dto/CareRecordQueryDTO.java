package com.nursing.center.dto;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.dto
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:33
 * @Description: TODO
 * @Version: 1.0
 */
import lombok.Data;

@Data
public class CareRecordQueryDTO {
    private Long customerId;
    private Long careItemId;
    private Long healthManagerId;
    private String customerName; // 模糊查询
    private String startTime;
    private String endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
