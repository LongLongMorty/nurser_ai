package com.nursing.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.common.exception.BusinessException;
import com.nursing.center.common.utils.PageResult;
import com.nursing.center.dto.PasswordResetDTO;
import com.nursing.center.dto.SysUserDTO;
import com.nursing.center.dto.SysUserQueryDTO;
import com.nursing.center.entity.SysUser;
import com.nursing.center.mapper.SysUserMapper;
import com.nursing.center.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {
    
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public PageResult<SysUser> getPage(SysUserQueryDTO queryDTO) {        // 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()), SysUser::getUsername, queryDTO.getUsername())
               .like(StringUtils.hasText(queryDTO.getRealName()), SysUser::getRealName, queryDTO.getRealName())
               .like(StringUtils.hasText(queryDTO.getPhone()), SysUser::getPhone, queryDTO.getPhone())
               .eq(queryDTO.getRole() != null, SysUser::getRole, queryDTO.getRole())
               .eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus())
               .ge(StringUtils.hasText(queryDTO.getStartDate()), SysUser::getCreateTime, queryDTO.getStartDate())
               .le(StringUtils.hasText(queryDTO.getEndDate()), SysUser::getCreateTime, queryDTO.getEndDate())
               .orderByDesc(SysUser::getCreateTime);
        
        // 执行分页查询
        Page<SysUser> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<SysUser> result = sysUserMapper.selectPage(page, wrapper);
        
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
    
    @Override
    public SysUser getById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
      @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser create(SysUserDTO userDTO) {
        // 检查用户名是否已存在
        if (checkUsernameExists(userDTO.getUsername(), null)) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建用户对象
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        
        // 设置默认密码：如果没有提供密码，使用手机号后6位
        String password = userDTO.getPassword();
        if (password == null || password.trim().isEmpty()) {
            if (userDTO.getPhone() != null && userDTO.getPhone().length() >= 6) {
                password = userDTO.getPhone().substring(userDTO.getPhone().length() - 6);
            } else {
                password = "123456"; // 默认密码
            }
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(password));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        // 保存用户
        int result = sysUserMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException("用户创建失败");
        }
        
        log.info("创建用户成功: {}, 密码设置为手机号后6位", user.getUsername());
        return user;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser update(SysUserDTO userDTO) {
        // 检查用户是否存在
        SysUser existingUser = getById(userDTO.getId());
        
        // 检查用户名是否已被其他用户使用
        if (checkUsernameExists(userDTO.getUsername(), userDTO.getId())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 更新用户信息
        BeanUtils.copyProperties(userDTO, existingUser, "id", "password", "createTime");
        
        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(userDTO.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        existingUser.setUpdateTime(LocalDateTime.now());
        
        // 执行更新
        int result = sysUserMapper.updateById(existingUser);
        if (result <= 0) {
            throw new BusinessException("用户更新失败");
        }
        
        log.info("更新用户成功: {}", existingUser.getUsername());
        return existingUser;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查用户是否存在
        SysUser user = getById(id);
        
        // 删除用户
        int result = sysUserMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException("用户删除失败");
        }
        
        log.info("删除用户成功: {}", user.getUsername());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的用户");
        }
        
        // 批量删除
        int result = sysUserMapper.deleteBatchIds(ids);
        if (result <= 0) {
            throw new BusinessException("批量删除失败");
        }
        
        log.info("批量删除用户成功，删除数量: {}", result);
    }
      @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, PasswordResetDTO resetDTO) {
        // 检查用户是否存在
        SysUser user = getById(id);
        
        String newPassword;
        if ("auto".equals(resetDTO.getPassword()) || resetDTO.getPassword() == null || resetDTO.getPassword().trim().isEmpty()) {
            // 自动生成密码：使用手机号后6位
            if (user.getPhone() != null && user.getPhone().length() >= 6) {
                newPassword = user.getPhone().substring(user.getPhone().length() - 6);
            } else {
                // 如果没有手机号，使用默认密码
                newPassword = "123456";
            }
        } else {
            newPassword = resetDTO.getPassword();
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        
        // 执行更新
        int result = sysUserMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException("密码重置失败");
        }
        
        log.info("重置用户密码成功: {}, 新密码为手机号后6位", user.getUsername());
    }
      @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id, Integer status) {
        // 检查用户是否存在
        SysUser user = getById(id);
        
        // 更新状态
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        
        // 执行更新
        int result = sysUserMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException("状态更新失败");
        }
        
        log.info("切换用户状态成功: {} - {}", user.getUsername(), status == 1 ? "启用" : "禁用");
    }
    
    @Override
    public boolean checkUsernameExists(String username, Long excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        
        // 如果是更新操作，排除当前用户ID
        if (excludeId != null) {
            wrapper.ne(SysUser::getId, excludeId);
        }
        
        return sysUserMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectOne(wrapper);
    }
}
