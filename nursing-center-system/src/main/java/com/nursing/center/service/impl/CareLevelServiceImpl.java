package com.nursing.center.service.impl;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.service.impl
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:35
 * @Description: TODO
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.common.exception.BusinessException;
import com.nursing.center.dto.CareLevelDTO;
import com.nursing.center.dto.CareLevelQueryDTO;
import com.nursing.center.dto.CareLevelItemConfig;
import com.nursing.center.dto.CareItemDTO;
import com.nursing.center.entity.CareLevel;
import com.nursing.center.entity.CareLevelItem;
import com.nursing.center.mapper.CareLevelItemMapper;
import com.nursing.center.mapper.CareLevelMapper;
import com.nursing.center.service.CareLevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareLevelServiceImpl implements CareLevelService {

    private final CareLevelMapper careLevelMapper;
    private final CareLevelItemMapper careLevelItemMapper;    @Override
    public IPage<CareLevelDTO> getCareLevelPage(CareLevelQueryDTO query) {
        Page<CareLevelDTO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<CareLevelDTO> result = careLevelMapper.selectCareLevelPage(page, query);
        
        // 转换状态字段：1 -> ACTIVE, 0 -> INACTIVE
        result.getRecords().forEach(dto -> {
            if (dto.getStatus() != null) {
                dto.setStatus("1".equals(dto.getStatus()) || "ACTIVE".equals(dto.getStatus()) ? "ACTIVE" : "INACTIVE");
            }
        });
        
        return result;
    }    @Override
    public CareLevelDTO getCareLevelById(Long id) {
        CareLevelDTO dto = careLevelMapper.selectCareLevelWithItems(id);
        if (dto != null && dto.getStatus() != null) {
            // 转换状态字段：1 -> ACTIVE, 0 -> INACTIVE
            dto.setStatus("1".equals(dto.getStatus()) || "ACTIVE".equals(dto.getStatus()) ? "ACTIVE" : "INACTIVE");
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCareLevel(CareLevelDTO careLevelDTO) {
        // 检查编码是否已存在
        checkLevelCodeExists(careLevelDTO.getLevelCode(), null);        CareLevel careLevel = new CareLevel();
        BeanUtils.copyProperties(careLevelDTO, careLevel);
        
        // 转换状态字段：ACTIVE -> 1, INACTIVE -> 0，默认为ACTIVE
        if (careLevelDTO.getStatus() != null) {
            careLevel.setStatus("ACTIVE".equals(careLevelDTO.getStatus()) ? 1 : 0);
        } else {
            careLevel.setStatus(1); // 默认启用
        }

        careLevelMapper.insert(careLevel);

        log.info("新增护理级别成功，ID: {}, 名称: {}", careLevel.getId(), careLevel.getLevelName());
        return careLevel.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCareLevel(CareLevelDTO careLevelDTO) {
        // 验证护理级别是否存在
        CareLevel existLevel = careLevelMapper.selectById(careLevelDTO.getId());
        if (existLevel == null) {
            throw new BusinessException("护理级别不存在");
        }

        // 检查编码是否已存在（排除自己）
        checkLevelCodeExists(careLevelDTO.getLevelCode(), careLevelDTO.getId());        CareLevel careLevel = new CareLevel();
        BeanUtils.copyProperties(careLevelDTO, careLevel);
        
        // 转换状态字段：ACTIVE -> 1, INACTIVE -> 0
        if (careLevelDTO.getStatus() != null) {
            careLevel.setStatus("ACTIVE".equals(careLevelDTO.getStatus()) ? 1 : 0);
        }

        careLevelMapper.updateById(careLevel);

        log.info("更新护理级别成功，ID: {}, 名称: {}", careLevel.getId(), careLevel.getLevelName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCareLevel(Long id) {
        CareLevel careLevel = careLevelMapper.selectById(id);
        if (careLevel == null) {
            throw new BusinessException("护理级别不存在");
        }

        // 逻辑删除护理级别
        careLevelMapper.deleteById(id);

        // 删除护理级别项目关联
        LambdaQueryWrapper<CareLevelItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareLevelItem::getCareLevelId, id);
        careLevelItemMapper.delete(wrapper);

        log.info("删除护理级别成功，ID: {}, 名称: {}", id, careLevel.getLevelName());
    }

    @Override
    public List<CareItemDTO> getItemsByLevelId(Long careLevelId) {
        return careLevelItemMapper.selectItemsByLevelId(careLevelId);
    }

    @Override
    public List<CareItemDTO> getAvailableItemsForLevel(Long careLevelId) {
        return careLevelItemMapper.selectAvailableItemsNotInLevel(careLevelId);
    }    @Override
    @Transactional(rollbackFor = Exception.class)
    public void configureItems(Long careLevelId, List<Long> careItemIds) {
        // 验证护理级别是否存在
        CareLevel careLevel = careLevelMapper.selectById(careLevelId);
        if (careLevel == null) {
            throw new BusinessException("护理级别不存在");
        }

        // 查询已存在的关联记录，避免重复插入
        LambdaQueryWrapper<CareLevelItem> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(CareLevelItem::getCareLevelId, careLevelId)
                   .in(CareLevelItem::getCareItemId, careItemIds);
        List<CareLevelItem> existingItems = careLevelItemMapper.selectList(existWrapper);
        Set<Long> existingItemIds = existingItems.stream()
                .map(CareLevelItem::getCareItemId)
                .collect(Collectors.toSet());

        // 过滤出需要新增的项目ID（去除已存在的）
        List<Long> newCareItemIds = careItemIds.stream()
                .filter(itemId -> !existingItemIds.contains(itemId))
                .collect(Collectors.toList());

        if (newCareItemIds.isEmpty()) {
            log.info("护理级别配置项目：所有项目已存在，无需添加。级别ID: {}", careLevelId);
            return;
        }

        // 批量添加护理项目关联
        List<CareLevelItem> careLevelItems = newCareItemIds.stream()
                .map(itemId -> {
                    CareLevelItem item = new CareLevelItem();
                    item.setCareLevelId(careLevelId);
                    item.setCareItemId(itemId);
                    return item;
                })
                .collect(Collectors.toList());

        careLevelItems.forEach(careLevelItemMapper::insert);

        log.info("护理级别配置项目成功，级别ID: {}, 新增项目数量: {}, 跳过已存在项目数量: {}", 
                careLevelId, newCareItemIds.size(), existingItemIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeItems(Long careLevelId, List<Long> careItemIds) {
        careLevelItemMapper.deleteByCareItemIds(careLevelId, careItemIds);
        log.info("护理级别移除项目成功，级别ID: {}, 项目数量: {}", careLevelId, careItemIds.size());
    }    @Override
    public List<CareLevelDTO> getEnabledCareLevels() {
        List<CareLevelDTO> levels = careLevelMapper.selectEnabledCareLevels();
        
        // 转换状态字段：1 -> ACTIVE, 0 -> INACTIVE
        levels.forEach(dto -> {
            if (dto.getStatus() != null) {
                dto.setStatus("1".equals(dto.getStatus()) || "ACTIVE".equals(dto.getStatus()) ? "ACTIVE" : "INACTIVE");
            }
        });
        
        return levels;
    }

    @Override
    public List<CareLevelItemConfig> getItemConfigsByLevelId(Long careLevelId) {
        return careLevelItemMapper.selectItemConfigsByLevelId(careLevelId);
    }

    /**
     * 检查护理级别编码是否已存在
     */
    private void checkLevelCodeExists(String levelCode, Long excludeId) {
        LambdaQueryWrapper<CareLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareLevel::getLevelCode, levelCode);
        if (excludeId != null) {
            wrapper.ne(CareLevel::getId, excludeId);
        }

        CareLevel existLevel = careLevelMapper.selectOne(wrapper);
        if (existLevel != null) {
            throw new BusinessException("护理级别编码已存在");
        }
    }
}
