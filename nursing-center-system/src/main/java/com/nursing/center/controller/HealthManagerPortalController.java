package com.nursing.center.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.common.result.Result;
import com.nursing.center.common.exception.BusinessException;
import com.nursing.center.common.enums.ApplyStatus;
import com.nursing.center.dto.*;
import com.nursing.center.vo.ReturnApplyVO;
import com.nursing.center.service.CustomerService;
import com.nursing.center.service.CustomerCareService;
import com.nursing.center.service.CareRecordService;
import com.nursing.center.service.ReturnApplyService;
import com.nursing.center.service.OutingApplyService;
import com.nursing.center.service.CheckoutApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import com.nursing.center.common.utils.SecurityUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康管家端专用控制器
 */
@RestController
@RequestMapping("/api/health-manager-portal")
@RequiredArgsConstructor
@Validated
@Slf4j
public class HealthManagerPortalController {

    private final CustomerService customerService;
    private final CustomerCareService customerCareService;
    private final CareRecordService careRecordService;
    private final ReturnApplyService returnApplyService;
    private final OutingApplyService outingApplyService;
    private final CheckoutApplyService checkoutApplyService;

    /**
     * 获取健康管家服务的客户列表
     */
    @GetMapping("/customers")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<IPage<CustomerDTO>> getMyCustomers(CustomerQueryDTO query) {
        // 获取当前登录的健康管家ID
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 查询服务客户列表", healthManagerId);
        
        // 设置健康管家ID过滤条件
        query.setHealthManagerId(healthManagerId);
          IPage<CustomerDTO> page = customerService.getCustomerPage(query);
        return Result.success(page);
    }

    /**
     * 获取客户详情
     */
    @GetMapping("/customers/{customerId}")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<CustomerDTO> getCustomerDetail(@PathVariable Long customerId) {
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 查询客户详情: {}", healthManagerId, customerId);
        
        // 验证该客户是否由当前健康管家服务
        if (!isMyCustomer(customerId, healthManagerId)) {
            return Result.error("无权访问该客户信息");
        }
        
        CustomerDTO customer = customerService.getCustomerById(customerId);
        return Result.success(customer);
    }

    /**
     * 获取客户的护理项目配置
     */
    @GetMapping("/customers/{customerId}/care-items")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<List<CustomerCareDTO>> getCustomerCareItems(@PathVariable Long customerId) {
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 查询客户 {} 的护理项目", healthManagerId, customerId);
        
        // 验证该客户是否由当前健康管家服务
        if (!isMyCustomer(customerId, healthManagerId)) {
            return Result.error("无权访问该客户的护理项目");
        }
        
        List<CustomerCareDTO> careItems = customerCareService.getCustomerCareItems(customerId);
        return Result.success(careItems);
    }

    /**
     * 执行护理并生成护理记录
     */
    @PostMapping("/care-records")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<String> addCareRecord(@Valid @RequestBody CareRecordDTO careRecordDTO) {
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 添加护理记录", healthManagerId);
        
        // 验证该客户是否由当前健康管家服务
        if (!isMyCustomer(careRecordDTO.getCustomerId(), healthManagerId)) {
            return Result.error("无权为该客户添加护理记录");
        }        // 设置健康管家ID
        careRecordDTO.setHealthManagerId(healthManagerId);
        
        careRecordService.addCareRecord(careRecordDTO);
        return Result.success("护理记录添加成功");
    }

    /**
     * 获取护理记录列表
     */
    @GetMapping("/care-records")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<IPage<CareRecordDTO>> getCareRecords(CareRecordQueryDTO query) {
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 查询护理记录", healthManagerId);
        
        // 设置健康管家ID过滤条件
        query.setHealthManagerId(healthManagerId);
          IPage<CareRecordDTO> page = careRecordService.getCareRecordPage(query);
        return Result.success(page);
    }

    /**
     * 获取护理记录详情
     */
    @GetMapping("/care-records/{recordId}")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<CareRecordDTO> getCareRecordDetail(@PathVariable Long recordId) {
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 查询护理记录详情: {}", healthManagerId, recordId);
        
        CareRecordDTO careRecord = careRecordService.getCareRecordById(recordId);
        if (careRecord == null) {
            return Result.error("护理记录不存在");
        }
        
        // 验证该护理记录是否属于当前健康管家
        if (!healthManagerId.equals(careRecord.getHealthManagerId())) {
            return Result.error("无权访问该护理记录");
        }
          return Result.success(careRecord);
    }

    /**
     * 隐藏护理记录
     */
    @PutMapping("/care-records/{recordId}/hide")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<String> hideCareRecord(@PathVariable Long recordId) {
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 隐藏护理记录 {}", healthManagerId, recordId);
        
        careRecordService.hideCareRecord(recordId, healthManagerId);        return Result.success("护理记录已隐藏");
    }

    /**
     * 获取外出客户列表（床位状态为OUT的客户）
     */
    @GetMapping("/customers/outing")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<IPage<OutingCustomerDTO>> getOutingCustomers(CustomerQueryDTO query) {
        // 获取当前登录的健康管家ID
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 查询外出客户列表", healthManagerId);
        
        // 设置健康管家ID过滤条件
        query.setHealthManagerId(healthManagerId);
        
        IPage<OutingCustomerDTO> page = customerService.getOutingCustomerPage(query);
        return Result.success(page);
    }

    /**
     * 健康管家提交回院申请
     */
    @PostMapping("/return-apply")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<Long> submitReturnApply(@Valid @RequestBody ReturnApplyDTO returnApplyDTO) {
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 提交回院申请，客户ID: {}", healthManagerId, returnApplyDTO.getCustomerId());
        
        // 验证该客户是否由当前健康管家服务
        if (!isMyCustomer(returnApplyDTO.getCustomerId(), healthManagerId)) {
            throw new BusinessException("无权为该客户提交回院申请");
        }
        
        // 检查客户是否允许申请回院
        if (!canApplyReturn(returnApplyDTO.getCustomerId(), returnApplyDTO.getOutingApplyId())) {
            throw new BusinessException("该客户当前状态不允许申请回院");
        }        // 设置申请人为当前健康管家
        returnApplyDTO.setApplicantId(healthManagerId);
        
        Long applyId = returnApplyService.submitReturnApply(returnApplyDTO);        return Result.success("回院申请提交成功", applyId);
    }

    /**
     * 获取回院申请列表
     */
    @GetMapping("/return-apply")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<IPage<ReturnApplyVO>> getReturnApplyList(ReturnApplyQueryDTO query) {
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 查询回院申请列表", healthManagerId);
        
        // 设置申请人ID过滤条件
        query.setApplicantId(healthManagerId);
        
        IPage<ReturnApplyVO> page = returnApplyService.getReturnApplyPage(query);
        return Result.success(page);
    }

    /**
     * 获取退住申请列表
     */
    @GetMapping("/checkout-apply")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<IPage<CheckoutApplyDTO>> getCheckoutApplyList(CheckoutApplyQueryDTO query) {
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 查询退住申请列表", healthManagerId);
        
        // 设置申请人ID过滤条件
        query.setApplicantId(healthManagerId);
        
        IPage<CheckoutApplyDTO> page = checkoutApplyService.getCheckoutApplyPage(query);
        return Result.success(page);
    }

    /**
     * 获取外出申请列表
     */
    @GetMapping("/outing-apply")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<IPage<OutingApplyDTO>> getOutingApplyList(OutingApplyQueryDTO query) {
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 查询外出申请列表", healthManagerId);
        
        // 设置申请人ID过滤条件
        query.setApplicantId(healthManagerId);
        
        IPage<OutingApplyDTO> page = outingApplyService.getOutingApplyPage(query);
        return Result.success(page);
    }

    /**
     * 健康管家提交外出申请
     */
    @PostMapping("/outing-apply")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<Long> submitOutingApply(@Valid @RequestBody OutingApplyDTO outingApplyDTO) {
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 提交外出申请，客户ID: {}", healthManagerId, outingApplyDTO.getCustomerId());
        
        // 验证该客户是否由当前健康管家服务
        if (!isMyCustomer(outingApplyDTO.getCustomerId(), healthManagerId)) {
            throw new BusinessException("无权为该客户提交外出申请");
        }
        
        // 设置申请人为当前健康管家
        outingApplyDTO.setApplicantId(healthManagerId);
        outingApplyDTO.setCreateTime(LocalDateTime.now());
        
        Long applyId = outingApplyService.submitOutingApply(outingApplyDTO);
        return Result.success("外出申请提交成功", applyId);
    }

    /**
     * 健康管家提交退住申请
     */
    @PostMapping("/checkout-apply")
    @PreAuthorize("hasRole('HEALTH_MANAGER')")
    public Result<Long> submitCheckoutApply(@Valid @RequestBody CheckoutApplyDTO checkoutApplyDTO) {
        Long healthManagerId = getCurrentHealthManagerId();
        log.info("健康管家 {} 提交退住申请，客户ID: {}", healthManagerId, checkoutApplyDTO.getCustomerId());
        
        // 验证该客户是否由当前健康管家服务
        if (!isMyCustomer(checkoutApplyDTO.getCustomerId(), healthManagerId)) {
            throw new BusinessException("无权为该客户提交退住申请");
        }        // 设置申请人为当前健康管家
        checkoutApplyDTO.setApplicantId(healthManagerId);
        checkoutApplyDTO.setCreateTime(LocalDateTime.now());
        
        // 调用退住申请服务
        Long applyId = checkoutApplyService.submitCheckoutApply(checkoutApplyDTO);
        return Result.success("退住申请提交成功", applyId);
    }

    /**
     * 检查客户是否可以申请回院
     * 逻辑：只要客户床位状态为 OUT，且存在一条 apply_status = 'APPROVED' 且 actual_return_date IS NULL 的外出申请
     */
    private boolean canApplyReturn(Long customerId, Long outingApplyId) {
        try {
            // 1. 检查客户床位状态是否为 OUT
            CustomerDTO customer = customerService.getCustomerById(customerId);
            if (customer == null) {
                log.warn("客户不存在: {}", customerId);
                return false;
            }
            
            // 获取客户床位信息
            if (customer.getBedId() == null) {
                log.warn("客户 {} 未分配床位", customerId);
                return false;
            }
            
            // 通过 customerService 检查床位状态
            String bedStatus = customerService.getBedStatus(customer.getBedId());
            if (!"OUT".equals(bedStatus)) {
                log.warn("客户 {} 床位状态不是 OUT，当前状态: {}", customerId, bedStatus);
                return false;
            }
            
            // 2. 检查外出申请是否存在且状态正确
            if (outingApplyId == null) {
                log.warn("外出申请ID不能为空");
                return false;
            }
            
            // 检查外出申请状态
            OutingApplyDTO outingApply = outingApplyService.getOutingApplyById(outingApplyId);
            if (outingApply == null) {
                log.warn("外出申请不存在: {}", outingApplyId);
                return false;
            }
            
            if (!customerId.equals(outingApply.getCustomerId())) {
                log.warn("外出申请 {} 不属于客户 {}", outingApplyId, customerId);
                return false;            }
            
            if (!ApplyStatus.APPROVED.equals(outingApply.getApplyStatus())) {
                log.warn("外出申请 {} 状态不是 APPROVED，当前状态: {}", outingApplyId, outingApply.getApplyStatus());
                return false;
            }
            
            if (outingApply.getActualReturnDate() != null) {
                log.warn("外出申请 {} 已经回院，回院时间: {}", outingApplyId, outingApply.getActualReturnDate());
                return false;
            }
            
            log.info("客户 {} 符合回院申请条件", customerId);
            return true;
            
        } catch (Exception e) {
            log.error("检查客户回院申请条件时发生异常", e);
            return false;
        }
    }

    /**
     * 获取当前登录的健康管家ID
     */
    private Long getCurrentHealthManagerId() {
        // 使用SecurityUtils获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 对于健康管家角色，用户ID就是健康管家ID
        // 这里假设sys_user表中的健康管家记录，其ID就是健康管家ID
        return userId;    }

    /**
     * 验证客户是否由当前健康管家服务
     */
    private boolean isMyCustomer(Long customerId, Long healthManagerId) {
        return customerService.isCustomerServedByHealthManager(customerId, healthManagerId);
    }
}
