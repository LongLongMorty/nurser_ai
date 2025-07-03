package com.nursing.center.dto;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.dto
 * @Author: System
 * @CreateTime: 2025-01-21
 * @Description: 系统用户DTO
 * @Version: 1.0
 */

import com.nursing.center.common.enums.UserRole;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class SysUserDTO {
    
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20位之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;
    
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50位")
    private String realName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @NotNull(message = "用户角色不能为空")
    private UserRole role;
    
    private Integer status; // 0-禁用 1-启用
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    // 统计信息（只用于显示）
    private Integer totalCustomers; // 服务客户总数（健康管家角色）
    private Integer activeCustomers; // 在住客户数（健康管家角色）
}
