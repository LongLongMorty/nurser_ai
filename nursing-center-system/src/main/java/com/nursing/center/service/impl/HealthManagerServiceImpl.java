package com.nursing.center.service.impl;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.service.impl
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  14:13
 * @Description: TODO
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.common.enums.ServiceStatus;
import com.nursing.center.common.exception.BusinessException;
import com.nursing.center.common.utils.SecurityUtils;
import com.nursing.center.dto.*;
import com.nursing.center.entity.Customer;
import com.nursing.center.entity.CustomerCare;
import com.nursing.center.entity.HealthManagerCustomer;
import com.nursing.center.mapper.CustomerCareMapper;
import com.nursing.center.mapper.CustomerMapper;
import com.nursing.center.mapper.HealthManagerCustomerMapper;
import com.nursing.center.mapper.HealthManagerMapper;
import com.nursing.center.service.CareRecordService;
import com.nursing.center.service.HealthManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthManagerServiceImpl implements HealthManagerService {

    private final HealthManagerMapper healthManagerMapper;
    private final HealthManagerCustomerMapper healthManagerCustomerMapper;
    private final CustomerMapper customerMapper;
    private final CustomerCareMapper customerCareMapper;
    private final CareRecordService careRecordService;

    @Override
    public IPage<HealthManagerDTO> getHealthManagerPage(HealthManagerQueryDTO query) {
        Page<HealthManagerDTO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return healthManagerMapper.selectHealthManagerPage(page, query);
    }

    @Override
    public List<CustomerServiceDTO> getServiceCustomers(Long healthManagerId) {
        return healthManagerMapper.selectServiceCustomers(healthManagerId);
    }

    @Override
    public List<CustomerServiceDTO> getCustomersWithoutManager(String customerName) {
        return healthManagerMapper.selectCustomersWithoutManager(customerName);
    }    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setServiceCustomers(SetServiceCustomerDTO setServiceDTO) {
        log.info("为健康管家设置服务客户，健康管家ID: {}, 客户ID列表: {}",
                setServiceDTO.getHealthManagerId(), setServiceDTO.getCustomerIds());

        // 更新客户的健康管家ID
        for (Long customerId : setServiceDTO.getCustomerIds()) {
            // 使用UpdateWrapper来只更新健康管家ID，不影响其他字段
            UpdateWrapper<Customer> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", customerId)
                        .set("health_manager_id", setServiceDTO.getHealthManagerId())
                        .set("update_time", LocalDateTime.now());
            int result = customerMapper.update(null, updateWrapper);
            log.info("为客户 {} 设置健康管家关联，影响行数: {}", customerId, result);
        }

        log.info("成功为健康管家 {} 设置了 {} 个服务客户",
                setServiceDTO.getHealthManagerId(), setServiceDTO.getCustomerIds().size());
    }@Override
    @Transactional(rollbackFor = Exception.class)
    public void removeServiceCustomers(RemoveServiceCustomerDTO removeServiceDTO) {
        log.info("移除健康管家服务客户，健康管家ID: {}, 客户ID列表: {}",
                removeServiceDTO.getHealthManagerId(), removeServiceDTO.getCustomerIds());

        // 将客户的健康管家ID设置为null
        for (Long customerId : removeServiceDTO.getCustomerIds()) {
            // 使用UpdateWrapper来强制更新null值
            UpdateWrapper<Customer> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", customerId)
                        .set("health_manager_id", null)
                        .set("update_time", LocalDateTime.now());
            int result = customerMapper.update(null, updateWrapper);
            log.info("移除客户 {} 的健康管家关联，影响行数: {}", customerId, result);
        }

        log.info("成功从健康管家 {} 移除了 {} 个服务客户",
                removeServiceDTO.getHealthManagerId(), removeServiceDTO.getCustomerIds().size());
    }

    @Override
    public List<CustomerCareItemDTO> getCustomerCareItems(Long customerId) {
        List<CustomerCareItemDTO> items = healthManagerMapper.selectCustomerCareItems(customerId);

        // 设置护理状态
        LocalDate today = LocalDate.now();
        items.forEach(item -> {
            boolean canCare = true;
            String statusDesc = "正常";

            // 检查服务状态
            if (ServiceStatus.EXPIRED.equals(item.getServiceStatus())) {
                canCare = false;
                statusDesc = "已到期";
            } else if (ServiceStatus.USED_UP.equals(item.getServiceStatus())) {
                canCare = false;
                statusDesc = "已用完";
            } else if (ServiceStatus.ARREARS.equals(item.getServiceStatus())) {
                canCare = false;
                statusDesc = "欠费";
            } else if (item.getExpireDate().isBefore(today)) {
                canCare = false;
                statusDesc = "已到期";
            } else if (item.getRemainingQuantity() <= 0) {
                canCare = false;
                statusDesc = "已用完";
            }

            item.setCanCare(canCare);
            item.setStatusDesc(statusDesc);
        });

        return items;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long performDailyCare(DailyCareDTO dailyCareDTO) {
        // 设置健康管家为当前用户
        dailyCareDTO.setHealthManagerId(SecurityUtils.getCurrentUserId());

        // 验证健康管家是否为该客户的服务者
        validateServiceRelation(dailyCareDTO.getHealthManagerId(), dailyCareDTO.getCustomerId());

        // 验证护理项目是否可用
        validateCareItem(dailyCareDTO.getCustomerId(), dailyCareDTO.getCareItemId(), dailyCareDTO.getCareQuantity());

        // 创建护理记录
        CareRecordDTO careRecordDTO = new CareRecordDTO();
        careRecordDTO.setCustomerId(dailyCareDTO.getCustomerId());
        careRecordDTO.setCareItemId(dailyCareDTO.getCareItemId());
        careRecordDTO.setHealthManagerId(dailyCareDTO.getHealthManagerId());
        careRecordDTO.setCareTime(dailyCareDTO.getCareTime());
        careRecordDTO.setCareQuantity(dailyCareDTO.getCareQuantity());
        careRecordDTO.setCareContent(dailyCareDTO.getCareContent());
        careRecordDTO.setRemark(dailyCareDTO.getRemark());

        Long recordId = careRecordService.addCareRecord(careRecordDTO);

        log.info("健康管家日常护理完成，护理记录ID: {}, 健康管家ID: {}, 客户ID: {}, 护理项目ID: {}",
                recordId, dailyCareDTO.getHealthManagerId(), dailyCareDTO.getCustomerId(), dailyCareDTO.getCareItemId());

        return recordId;
    }

    @Override
    public List<CustomerServiceDTO> getMyServiceCustomers(String customerName) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("未登录");
        }

        List<CustomerServiceDTO> customers = healthManagerMapper.selectServiceCustomers(currentUserId);

        // 如果有客户名称条件，进行过滤
        if (StringUtils.hasText(customerName)) {
            customers = customers.stream()
                    .filter(customer -> customer.getCustomerName().contains(customerName))
                    .collect(Collectors.toList());
        }

        return customers;
    }

    /**
     * 验证健康管家是否存在
     */
    private void validateHealthManager(Long healthManagerId) {
        // 这里可以查询sys_user表验证健康管家是否存在且角色正确
        // 为简化，暂时省略具体实现
    }

    /**
     * 验证服务关系
     */
    private void validateServiceRelation(Long healthManagerId, Long customerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        if (!healthManagerId.equals(customer.getHealthManagerId())) {
            throw new BusinessException("您不是该客户的健康管家");
        }
    }

    /**
     * 验证护理项目是否可用
     */
    private void validateCareItem(Long customerId, Long careItemId, Integer careQuantity) {
        // 查询客户护理服务
        LambdaQueryWrapper<CustomerCare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerCare::getCustomerId, customerId)
                .eq(CustomerCare::getCareItemId, careItemId);

        CustomerCare customerCare = customerCareMapper.selectOne(wrapper);
        if (customerCare == null) {
            throw new BusinessException("客户未购买该护理项目");
        }

        // 检查服务是否到期
        if (customerCare.getExpireDate().isBefore(LocalDate.now())) {
            throw new BusinessException("护理服务已到期");
        }

        // 检查剩余数量
        if (customerCare.getRemainingQuantity() < careQuantity) {
            throw new BusinessException("护理服务剩余数量不足，剩余: " + customerCare.getRemainingQuantity());
        }

        // 检查服务状态
        if (!ServiceStatus.NORMAL.equals(customerCare.getServiceStatus())) {
            throw new BusinessException("护理服务状态异常: " + customerCare.getServiceStatus().getDescription());
        }
    }
}
