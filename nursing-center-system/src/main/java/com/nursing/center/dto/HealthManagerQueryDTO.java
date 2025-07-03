package com.nursing.center.dto;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.dto
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  14:08
 * @Description: TODO
 * @Version: 1.0
 */
import lombok.Data;

@Data
public class HealthManagerQueryDTO {
    private String realName; // 模糊查询
    private String status; // 改为String类型以支持前端传递字符串
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    
    /**
     * 获取状态的整数值
     * @return 状态整数值，null表示不过滤
     */
    public Integer getStatusAsInteger() {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(status);
        } catch (NumberFormatException e) {
            // 如果不是数字，返回null表示不过滤
            return null;
        }
    }
}
