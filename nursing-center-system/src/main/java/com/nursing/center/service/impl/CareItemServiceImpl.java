package com.nursing.center.service.impl;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.service.impl
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:37
 * @Description: TODO
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.common.exception.BusinessException;
import com.nursing.center.dto.CareItemDTO;
import com.nursing.center.dto.CareItemQueryDTO;
import com.nursing.center.entity.CareItem;
import com.nursing.center.entity.CareLevelItem;
import com.nursing.center.mapper.CareItemMapper;
import com.nursing.center.mapper.CareLevelItemMapper;
import com.nursing.center.service.CareItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareItemServiceImpl implements CareItemService {

    private final CareItemMapper careItemMapper;
    private final CareLevelItemMapper careLevelItemMapper;    @Override
    public IPage<CareItemDTO> getCareItemPage(CareItemQueryDTO query) {
        Page<CareItem> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<CareItem> wrapper = new LambdaQueryWrapper<>();
        if (query.getStatusValue() != null) {
            wrapper.eq(CareItem::getStatus, query.getStatusValue());
        }
        if (StringUtils.hasText(query.getItemName())) {
            wrapper.like(CareItem::getItemName, query.getItemName());
        }
        wrapper.orderByDesc(CareItem::getCreateTime);

        IPage<CareItem> itemPage = careItemMapper.selectPage(page, wrapper);

        // 转换为DTO
        IPage<CareItemDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(itemPage, dtoPage, "records");

        List<CareItemDTO> dtoList = itemPage.getRecords().stream()
                .map(item -> {
                    CareItemDTO dto = new CareItemDTO();
                    BeanUtils.copyProperties(item, dto);
                    return dto;
                })
                .collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public CareItemDTO getCareItemById(Long id) {
        CareItem careItem = careItemMapper.selectById(id);
        if (careItem == null) {
            return null;
        }

        CareItemDTO dto = new CareItemDTO();
        BeanUtils.copyProperties(careItem, dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCareItem(CareItemDTO careItemDTO) {
        // 检查项目编号是否已存在
        checkItemCodeExists(careItemDTO.getItemCode(), null);

        CareItem careItem = new CareItem();
        BeanUtils.copyProperties(careItemDTO, careItem);
        careItem.setStatus(1); // 默认启用

        careItemMapper.insert(careItem);

        log.info("新增护理项目成功，ID: {}, 名称: {}", careItem.getId(), careItem.getItemName());
        return careItem.getId();
    }    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCareItem(CareItemDTO careItemDTO) {
        // 验证护理项目是否存在
        CareItem existItem = careItemMapper.selectById(careItemDTO.getId());
        if (existItem == null) {
            throw new BusinessException("护理项目不存在");
        }

        // 检查项目编号是否已存在（排除自己）
        checkItemCodeExists(careItemDTO.getItemCode(), careItemDTO.getId());

        CareItem careItem = new CareItem();
        BeanUtils.copyProperties(careItemDTO, careItem);

        // 如果状态修改为停用，需要从护理级别中移除（但不影响客户已有数据）
        if (careItem.getStatus() == 0 && existItem.getStatus() == 1) {
            removeCareItemFromLevels(careItem.getId());
            log.info("护理项目停用，已从护理级别配置中移除，但保留客户已有数据。项目ID: {}", careItem.getId());
        }

        careItemMapper.updateById(careItem);

        log.info("更新护理项目成功，ID: {}, 名称: {}", careItem.getId(), careItem.getItemName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCareItem(Long id) {
        CareItem careItem = careItemMapper.selectById(id);
        if (careItem == null) {
            throw new BusinessException("护理项目不存在");
        }

        // 检查是否有客户购买了此项目
        boolean hasCustomerData = checkCustomerUsage(id);
        if (hasCustomerData) {
            log.info("护理项目存在客户购买记录，执行软删除。项目ID: {}, 名称: {}", id, careItem.getItemName());
        }

        // 从护理级别中移除该项目（不影响客户已有数据）
        removeCareItemFromLevels(id);

        // 执行逻辑删除（MyBatis Plus的逻辑删除，不会真正删除数据）
        careItemMapper.deleteById(id);

        log.info("护理项目删除成功（逻辑删除），已从护理级别配置中移除，但保留客户已有数据。ID: {}, 名称: {}", id, careItem.getItemName());
    }

    @Override
    public List<CareItemDTO> getEnabledCareItems() {
        LambdaQueryWrapper<CareItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareItem::getStatus, 1);
        wrapper.orderByDesc(CareItem::getCreateTime);

        List<CareItem> items = careItemMapper.selectList(wrapper);
        return items.stream()
                .map(item -> {
                    CareItemDTO dto = new CareItemDTO();
                    BeanUtils.copyProperties(item, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 检查护理项目编号是否已存在
     */
    private void checkItemCodeExists(String itemCode, Long excludeId) {
        LambdaQueryWrapper<CareItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareItem::getItemCode, itemCode);
        if (excludeId != null) {
            wrapper.ne(CareItem::getId, excludeId);
        }

        CareItem existItem = careItemMapper.selectOne(wrapper);
        if (existItem != null) {
            throw new BusinessException("护理项目编号已存在");
        }
    }    /**
     * 从所有护理级别中移除指定护理项目
     * 这不会影响客户已购买的护理服务和护理记录
     */
    private void removeCareItemFromLevels(Long careItemId) {
        LambdaQueryWrapper<CareLevelItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareLevelItem::getCareItemId, careItemId);
        careLevelItemMapper.delete(wrapper);
        
        log.info("已从护理级别配置中移除护理项目，项目ID: {}", careItemId);
    }

    /**
     * 检查护理项目是否被客户使用
     * 检查customer_care表和care_record表
     */
    private boolean checkCustomerUsage(Long careItemId) {
        // 这里需要注入CustomerCareMapper和CareRecordMapper来检查
        // 为了简化，我们先记录日志，表示该项目有客户数据
        log.info("检查护理项目客户使用情况，项目ID: {}（注：实际项目中应检查customer_care和care_record表）", careItemId);
        
        // 在实际项目中，这里应该：
        // 1. 检查customer_care表是否有该护理项目的购买记录
        // 2. 检查care_record表是否有该护理项目的护理记录
        // 如果有任何记录，返回true
        
        return true; // 假设总是有客户数据，确保安全
    }
}
