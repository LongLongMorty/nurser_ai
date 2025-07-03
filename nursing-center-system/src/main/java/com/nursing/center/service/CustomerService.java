package com.nursing.center.service;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.service
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:11
 * @Description: TODO
 * @Version: 1.0
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.dto.CustomerDTO;
import com.nursing.center.dto.CustomerQueryDTO;
import com.nursing.center.dto.OutingCustomerDTO;
import com.nursing.center.controller.CustomerController.CheckInRequestDTO;

public interface CustomerService {

    /**
     * 分页查询客户信息
     */
    IPage<CustomerDTO> getCustomerPage(CustomerQueryDTO query);

    /**
     * 根据ID查询客户信息
     */
    CustomerDTO getCustomerById(Long id);

    /**
     * 添加客户
     */
    Long addCustomer(CustomerDTO customerDTO);

    /**
     * 修改客户信息
     */
    void updateCustomer(CustomerDTO customerDTO);

    /**
     * 删除客户（逻辑删除）
     */
    void deleteCustomer(Long id);

    /**
     * 客户入住登记
     */
    void checkInCustomer(Long customerId, CheckInRequestDTO checkInRequest);    /**
     * 验证客户是否由指定健康管家服务
     */
    boolean isCustomerServedByHealthManager(Long customerId, Long healthManagerId);

    /**
     * 分页查询外出客户（床位状态为OUT的客户）
     */
    IPage<OutingCustomerDTO> getOutingCustomerPage(CustomerQueryDTO query);

    /**
     * 获取床位状态
     */
    String getBedStatus(Long bedId);
}
