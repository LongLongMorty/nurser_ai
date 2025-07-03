package com.nursing.center.controller;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.controller
 * @Author: System
 * @CreateTime: 2025-01-21
 * @Description: 系统用户管理控制器
 * @Version: 1.0
 */

import com.nursing.center.common.result.Result;
import com.nursing.center.common.utils.PageResult;
import com.nursing.center.dto.SysUserDTO;
import com.nursing.center.dto.SysUserQueryDTO;
import com.nursing.center.dto.PasswordResetDTO;
import com.nursing.center.entity.SysUser;
import com.nursing.center.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
@Validated
public class SysUserController {

    private final SysUserService sysUserService;    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<SysUser>> getUserPage(SysUserQueryDTO query) {
        PageResult<SysUser> page = sysUserService.getPage(query);
        return Result.success(page);
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        return Result.success(user);
    }

    /**
     * 新增用户
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysUser> addUser(@Valid @RequestBody SysUserDTO userDTO) {
        SysUser user = sysUserService.create(userDTO);
        return Result.success("用户添加成功", user);
    }

    /**
     * 修改用户信息
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")    public Result<SysUser> updateUser(@Valid @RequestBody SysUserDTO userDTO) {
        SysUser user = sysUserService.update(userDTO);
        return Result.success("用户更新成功", user);
    }

    /**
     * 删除用户
     */    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteUser(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.success("用户删除成功");
    }

    /**
     * 批量删除用户
     */    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> batchDeleteUsers(@RequestBody List<Long> ids) {
        sysUserService.batchDelete(ids);
        return Result.success("用户批量删除成功");
    }

    /**
     * 重置用户密码
     */    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> resetPassword(@PathVariable Long id, @Valid @RequestBody PasswordResetDTO passwordResetDTO) {
        sysUserService.resetPassword(id, passwordResetDTO);
        return Result.success("密码重置成功");
    }

    /**
     * 启用/禁用用户
     */    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> toggleUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        sysUserService.toggleStatus(id, status);
        return Result.success(status == 1 ? "用户已启用" : "用户已禁用");
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> checkUsername(@RequestParam String username, @RequestParam(required = false) Long excludeId) {
        boolean exists = sysUserService.checkUsernameExists(username, excludeId);
        return Result.success(!exists);
    }
}
