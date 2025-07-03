package com.nursing.center.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.common.exception.BusinessException;
import com.nursing.center.dto.MealConfigDTO;
import com.nursing.center.dto.MealConfigQueryDTO;
import com.nursing.center.entity.MealConfig;
import com.nursing.center.mapper.MealConfigMapper;
import com.nursing.center.service.MealConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 膳食配置服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MealConfigServiceImpl implements MealConfigService {

    private final MealConfigMapper mealConfigMapper;

    @Override
    public IPage<MealConfigDTO> getMealConfigPage(MealConfigQueryDTO query) {
        Page<MealConfigDTO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return mealConfigMapper.selectMealConfigPage(page, query);
    }

    @Override
    public MealConfigDTO getMealConfigById(Long id) {
        MealConfig mealConfig = mealConfigMapper.selectById(id);
        if (mealConfig == null) {
            throw new BusinessException("膳食配置不存在");
        }
        
        MealConfigDTO dto = new MealConfigDTO();
        BeanUtils.copyProperties(mealConfig, dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addMealConfig(MealConfigDTO configDTO) {
        // 验证膳食名称是否重复
        if (mealConfigMapper.existsByName(configDTO.getMealName())) {
            throw new BusinessException("膳食名称已存在");
        }

        MealConfig mealConfig = new MealConfig();
        BeanUtils.copyProperties(configDTO, mealConfig);
        mealConfig.setCreateTime(LocalDateTime.now());
        mealConfig.setUpdateTime(LocalDateTime.now());
        mealConfig.setDeleted(0);
        
        mealConfigMapper.insert(mealConfig);
        log.info("新增膳食配置成功，ID: {}, 名称: {}", mealConfig.getId(), mealConfig.getMealName());
        return mealConfig.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMealConfig(MealConfigDTO configDTO) {
        MealConfig existingConfig = mealConfigMapper.selectById(configDTO.getId());
        if (existingConfig == null) {
            throw new BusinessException("膳食配置不存在");
        }

        // 验证膳食名称是否重复（排除自己）
        if (!existingConfig.getMealName().equals(configDTO.getMealName()) && 
            mealConfigMapper.existsByName(configDTO.getMealName())) {
            throw new BusinessException("膳食名称已存在");
        }

        MealConfig mealConfig = new MealConfig();
        BeanUtils.copyProperties(configDTO, mealConfig);
        mealConfig.setUpdateTime(LocalDateTime.now());
        
        mealConfigMapper.updateById(mealConfig);
        log.info("修改膳食配置成功，ID: {}, 名称: {}", mealConfig.getId(), mealConfig.getMealName());
    }    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMealConfig(Long id) {
        MealConfig mealConfig = mealConfigMapper.selectById(id);
        if (mealConfig == null) {
            throw new BusinessException("膳食配置不存在");
        }
        
        // 检查是否被膳食日历引用（暂时注释掉，因为该方法未实现）
        // if (mealConfigMapper.isReferencedByCalendar(id)) {
        //     throw new BusinessException("该膳食配置已被膳食日历引用，不能删除");
        // }
        
        mealConfigMapper.deleteById(id);
        log.info("删除膳食配置成功，ID: {}", id);
    }

    @Override
    public List<MealConfigDTO> getMealConfigsByType(String mealType) {
        return mealConfigMapper.selectByMealType(mealType);
    }

    @Override
    public List<MealConfigDTO> getActiveMealConfigs() {
        return mealConfigMapper.selectActiveMealConfigs();
    }    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("更新的ID列表不能为空");
        }
        
        mealConfigMapper.batchUpdateStatus(ids, status);
        log.info("批量更新膳食配置状态成功，数量: {}, 状态: {}", ids.size(), status);
    }
}
