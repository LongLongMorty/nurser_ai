package com.nursing.center.dto;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.dto
 * @Author: System
 * @CreateTime: 2025-01-21
 * @Description: 密码重置DTO
 * @Version: 1.0
 */

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class PasswordResetDTO {
    
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(auto|.{6,20})$", message = "密码长度必须在6-20位之间，或使用'auto'自动生成")
    private String password;
}
