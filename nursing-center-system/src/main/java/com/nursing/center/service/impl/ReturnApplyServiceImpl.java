package com.nursing.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.common.enums.ApplyStatus;
import com.nursing.center.common.enums.BedStatus;
import com.nursing.center.common.exception.BusinessException;
import com.nursing.center.common.utils.SecurityUtils;
import com.nursing.center.dto.ReturnApplyDTO;
import com.nursing.center.dto.ReturnApproveDTO;
import com.nursing.center.dto.ReturnApplyQueryDTO;
import com.nursing.center.entity.*;
import com.nursing.center.mapper.*;
import com.nursing.center.service.ReturnApplyService;
import com.nursing.center.vo.ReturnApplyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 回院申请服务实现类
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnApplyServiceImpl implements ReturnApplyService {

    private final ReturnApplyMapper returnApplyMapper;
    private final OutingApplyMapper outingApplyMapper;
    private final CustomerMapper customerMapper;
    private final BedMapper bedMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitReturnApply(ReturnApplyDTO returnApplyDTO) {
        // 1. 验证客户是否可以申请回院
        if (!canApplyReturn(returnApplyDTO.getCustomerId())) {
            throw new BusinessException("该客户当前状态不允许申请回院");
        }

        // 2. 验证外出申请是否存在且状态正确
        OutingApply outingApply = outingApplyMapper.selectById(returnApplyDTO.getOutingApplyId());
        if (outingApply == null) {
            throw new BusinessException("外出申请不存在");
        }        if (!ApplyStatus.APPROVED.equals(outingApply.getApplyStatus())) {
            throw new BusinessException("外出申请未审批通过，无法申请回院");
        }
        if (outingApply.getActualReturnDate() != null) {
            throw new BusinessException("该外出申请已有回院记录");
        }

        // 3. 检查是否已有未处理的回院申请
        LambdaQueryWrapper<ReturnApply> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(ReturnApply::getCustomerId, returnApplyDTO.getCustomerId())
                      .eq(ReturnApply::getOutingApplyId, returnApplyDTO.getOutingApplyId())
                      .in(ReturnApply::getApplyStatus, ApplyStatus.SUBMITTED.name());
        
        ReturnApply existingApply = returnApplyMapper.selectOne(existingWrapper);
        if (existingApply != null) {
            throw new BusinessException("该外出申请已有待审批的回院申请");
        }

        // 4. 创建回院申请
        ReturnApply returnApply = new ReturnApply();
        BeanUtils.copyProperties(returnApplyDTO, returnApply);
        returnApply.setApplyStatus(ApplyStatus.SUBMITTED.name());
        returnApply.setCreateTime(LocalDateTime.now());
        returnApply.setUpdateTime(LocalDateTime.now());

        returnApplyMapper.insert(returnApply);

        log.info("回院申请提交成功，申请ID: {}, 客户ID: {}, 申请人: {}", 
                returnApply.getId(), returnApplyDTO.getCustomerId(), returnApplyDTO.getApplicantId());

        return returnApply.getId();
    }

    @Override
    public IPage<ReturnApplyVO> getReturnApplyPage(ReturnApplyQueryDTO queryDTO) {
        Page<ReturnApplyVO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        return returnApplyMapper.selectReturnApplyPage(page, queryDTO);
    }

    @Override
    public ReturnApplyVO getReturnApplyById(Long id) {
        return returnApplyMapper.selectReturnApplyById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveReturnApply(ReturnApproveDTO approveDTO) {
        // 1. 获取回院申请信息
        ReturnApply returnApply = returnApplyMapper.selectById(approveDTO.getApplyId());
        if (returnApply == null) {
            throw new BusinessException("回院申请不存在");
        }

        if (!ApplyStatus.SUBMITTED.name().equals(returnApply.getApplyStatus())) {
            throw new BusinessException("只能审批待审批状态的申请");
        }

        // 2. 更新审批信息
        ReturnApply updateApply = new ReturnApply();
        updateApply.setId(approveDTO.getApplyId());
        updateApply.setApplyStatus(approveDTO.getApplyStatus().name());
        updateApply.setApproverId(SecurityUtils.getCurrentUserId());
        updateApply.setApproveTime(LocalDateTime.now());
        updateApply.setApproveRemark(approveDTO.getApproveRemark());
        updateApply.setUpdateTime(LocalDateTime.now());

        returnApplyMapper.updateById(updateApply);

        // 3. 如果审批通过，需要处理回院逻辑
        if (ApplyStatus.APPROVED.equals(approveDTO.getApplyStatus())) {
            handleApprovedReturn(returnApply);
        }

        log.info("回院申请审批完成，申请ID: {}, 审批结果: {}, 审批人: {}",
                approveDTO.getApplyId(), approveDTO.getApplyStatus(), SecurityUtils.getCurrentUserId());
    }

    @Override
    public boolean canApplyReturn(Long customerId) {
        // 1. 检查客户是否存在
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            return false;
        }

        // 2. 检查客户床位状态是否为OUT
        if (customer.getBedId() == null) {
            return false;
        }        Bed bed = bedMapper.selectById(customer.getBedId());
        if (bed == null || !BedStatus.OUT.equals(bed.getBedStatus())) {
            return false;
        }

        // 3. 检查是否有已审批通过但未回院的外出申请
        LambdaQueryWrapper<OutingApply> outingWrapper = new LambdaQueryWrapper<>();
        outingWrapper.eq(OutingApply::getCustomerId, customerId)
                    .eq(OutingApply::getApplyStatus, ApplyStatus.APPROVED.name())
                    .isNull(OutingApply::getActualReturnDate);
        
        OutingApply approvedOuting = outingApplyMapper.selectOne(outingWrapper);
        return approvedOuting != null;
    }

    /**
     * 处理审批通过的回院申请
     */
    private void handleApprovedReturn(ReturnApply returnApply) {
        // 1. 更新外出申请的实际回院时间
        OutingApply outingApply = new OutingApply();
        outingApply.setId(returnApply.getOutingApplyId());
        outingApply.setActualReturnDate(returnApply.getRequestedReturnDate());
        outingApply.setUpdateTime(LocalDateTime.now());
        outingApplyMapper.updateById(outingApply);        // 2. 更新床位状态为OCCUPIED
        Customer customer = customerMapper.selectById(returnApply.getCustomerId());
        if (customer != null && customer.getBedId() != null) {
            Bed bed = new Bed();
            bed.setId(customer.getBedId());
            bed.setBedStatus(BedStatus.OCCUPIED);
            bed.setUpdateTime(LocalDateTime.now());
            bedMapper.updateById(bed);
        }

        // 3. 更新回院申请的实际回院时间
        ReturnApply updateReturn = new ReturnApply();
        updateReturn.setId(returnApply.getId());
        updateReturn.setActualReturnDate(returnApply.getRequestedReturnDate());
        updateReturn.setUpdateTime(LocalDateTime.now());
        returnApplyMapper.updateById(updateReturn);

        log.info("回院申请处理完成，客户ID: {}, 床位状态已更新为OCCUPIED", returnApply.getCustomerId());
    }
}
