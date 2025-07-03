package com.nursing.center.service;

import com.nursing.center.common.utils.PageResult;
import com.nursing.center.dto.PasswordResetDTO;
import com.nursing.center.dto.SysUserDTO;
import com.nursing.center.dto.SysUserQueryDTO;
import com.nursing.center.entity.SysUser;

import java.util.List;

/**
 * 系统用户服务接口
 */
public interface SysUserService {
    
    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<SysUser> getPage(SysUserQueryDTO queryDTO);
    
    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    SysUser getById(Long id);
    
    /**
     * 新增用户
     *
     * @param userDTO 用户信息
     * @return 新增的用户
     */
    SysUser create(SysUserDTO userDTO);
    
    /**
     * 更新用户信息
     *
     * @param userDTO 用户信息
     * @return 更新后的用户
     */
    SysUser update(SysUserDTO userDTO);
    
    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void delete(Long id);
    
    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     */
    void batchDelete(List<Long> ids);
    
    /**
     * 重置用户密码
     *
     * @param id          用户ID
     * @param resetDTO    密码重置信息
     */
    void resetPassword(Long id, PasswordResetDTO resetDTO);
      /**
     * 切换用户状态
     *
     * @param id     用户ID
     * @param status 新状态
     */
    void toggleStatus(Long id, Integer status);
    
    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param excludeId 排除的用户ID（用于更新时检查）
     * @return 是否存在
     */
    boolean checkUsernameExists(String username, Long excludeId);
    
    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);
}
