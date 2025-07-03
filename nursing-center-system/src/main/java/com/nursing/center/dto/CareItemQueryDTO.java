package com.nursing.center.dto;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.dto
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:31
 * @Description: TODO
 * @Version: 1.0
 */

import lombok.Data;

@Data
public class CareItemQueryDTO {
    private String status; // 改为String类型以支持前端传递"ACTIVE"等字符串
    private String itemName; // 模糊查询
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    
    /**
     * 获取状态的整数值
     * ACTIVE -> 1, INACTIVE -> 0
     */
    public Integer getStatusValue() {
        if (status == null) {
            return null;
        }
        switch (status.toUpperCase()) {
            case "ACTIVE":
                return 1;
            case "INACTIVE": 
                return 0;
            default:
                try {
                    return Integer.parseInt(status);
                } catch (NumberFormatException e) {
                    return null;
                }
        }
    }
}
