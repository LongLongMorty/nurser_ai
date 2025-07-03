package com.nursing.center.dto;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.dto
 * @Author: System
 * @CreateTime: 2025-01-21
 * @Description: 系统用户查询DTO
 * @Version: 1.0
 */

import com.nursing.center.common.enums.UserRole;
import lombok.Data;

@Data
public class SysUserQueryDTO {
    
    private Integer page = 0;
    private Integer size = 10;
    
    private String username;
    private String realName;
    private UserRole role;
    private Integer status;
    private String phone;
    
    // 时间范围查询
    private String startDate;
    private String endDate;
}
