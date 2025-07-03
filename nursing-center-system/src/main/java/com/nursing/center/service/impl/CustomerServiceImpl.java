package com.nursing.center.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.common.enums.CustomerType;
import com.nursing.center.common.exception.BusinessException;
import com.nursing.center.controller.CustomerController.CheckInRequestDTO;
import com.nursing.center.dto.CustomerDTO;
import com.nursing.center.dto.CustomerQueryDTO;
import com.nursing.center.dto.OutingCustomerDTO;
import com.nursing.center.entity.Customer;
import com.nursing.center.entity.Bed;
import com.nursing.center.mapper.CustomerMapper;
import com.nursing.center.mapper.BedMapper;
import com.nursing.center.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 客户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;
    private final BedMapper bedMapper;


    @Override
    public IPage<CustomerDTO> getCustomerPage(CustomerQueryDTO query) {
        Page<CustomerDTO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return customerMapper.selectCustomerPage(page, query);
    }

    @Override
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        
        CustomerDTO customerDTO = new CustomerDTO();
        BeanUtils.copyProperties(customer, customerDTO);
        return customerDTO;
    }    @Override
    public Long addCustomer(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDTO, customer);
        customer.setCustomerType(CustomerType.SELF_CARE); // 默认自理
        customer.setStatus(1); // 正常状态
        
        customerMapper.insert(customer);
        log.info("添加客户成功，ID: {}", customer.getId());
        return customer.getId();
    }    @Override
    public void updateCustomer(CustomerDTO customerDTO) {
        Customer existingCustomer = customerMapper.selectById(customerDTO.getId());
        if (existingCustomer == null) {
            throw new BusinessException("客户不存在");
        }
        
        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDTO, customer);
        customerMapper.updateById(customer);
        log.info("更新客户成功，ID: {}", customer.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCustomer(Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        
        // 逻辑删除
        customerMapper.deleteById(id);
        log.info("删除客户成功，ID: {}", id);
    }    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkInCustomer(Long customerId, CheckInRequestDTO checkInRequest) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        
        // 更新入住信息
        Customer updateCustomer = new Customer();
        updateCustomer.setId(customerId);
        updateCustomer.setBuildingId(checkInRequest.getBuildingId());
        updateCustomer.setRoomId(checkInRequest.getRoomId());
        updateCustomer.setBedId(checkInRequest.getBedId());
        updateCustomer.setCheckInDate(LocalDate.parse(checkInRequest.getCheckInDate()));
        updateCustomer.setContractExpireDate(LocalDate.parse(checkInRequest.getContractExpireDate()));
          customerMapper.updateById(updateCustomer);
        log.info("客户入住成功，客户ID: {}, 床位ID: {}", customerId, checkInRequest.getBedId());    }
    @Override
    public boolean isCustomerServedByHealthManager(Long customerId, Long healthManagerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            return false;
        }
        return healthManagerId.equals(customer.getHealthManagerId());
    }

    @Override
    public IPage<OutingCustomerDTO> getOutingCustomerPage(CustomerQueryDTO query) {
        Page<OutingCustomerDTO> page = new Page<>(query.getPageNum(), query.getPageSize());
        return customerMapper.selectOutingCustomerPage(page, query);
    }    @Override
    public String getBedStatus(Long bedId) {
        if (bedId == null) {
            return null;
        }
        
        Bed bed = bedMapper.selectById(bedId);
        if (bed == null) {
            return null;
        }
        
        // 返回床位状态的字符串值
        return bed.getBedStatus().name();
    }
}
