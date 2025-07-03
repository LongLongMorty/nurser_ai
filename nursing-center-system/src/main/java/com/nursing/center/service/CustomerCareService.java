package com.nursing.center.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.dto.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 客户护理服务接口
 */
public interface CustomerCareService {

    /**
     * 分页查询客户护理设置
     */
    IPage<CustomerCarePageDTO> getCustomerCarePage(CustomerCareQueryParams query);

    /**
     * 获取客户护理详情
     */
    CustomerCareDetailDTO getCustomerCareDetail(Long customerId);

    /**
     * 查询客户的护理服务列表
     */
    List<CustomerCareDTO> getCustomerCareServices(Long customerId);

    /**
     * 为客户设置护理级别
     */
    void setCustomerCareLevel(CustomerCareLevelSetDTO customerCareLevelSetDTO);

    /**
     * 移除客户护理级别
     */
    void removeCustomerCareLevel(Long customerId);

    /**
     * 为客户购买护理项目
     */
    void purchaseCareItems(Long customerId, List<CustomerCareItemPurchaseDTO> careItems);

    /**
     * 客户护理服务续费
     */
    void renewCareService(Long customerCareId, Integer additionalQuantity, LocalDate newExpireDate);

    /**
     * 移除客户护理服务
     */
    void removeCareService(Long customerCareId);

    /**
     * 查询客户未拥有的护理项目
     */
    List<CustomerCareDTO> getAvailableItemsForCustomer(Long customerId, String itemName);

    /**
     * 获取客户的护理项目配置（用于健康管家端）
     */
    List<CustomerCareDTO> getCustomerCareItems(Long customerId);
}
