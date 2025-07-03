package com.nursing.center.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nursing.center.dto.ReturnApplyDTO;
import com.nursing.center.dto.ReturnApproveDTO;
import com.nursing.center.dto.ReturnApplyQueryDTO;
import com.nursing.center.vo.ReturnApplyVO;

/**
 * 回院申请服务接口
 * @author system
 * @since 2024-01-01
 */
public interface ReturnApplyService {

    /**
     * 提交回院申请
     * @param returnApplyDTO 回院申请信息
     * @return 申请ID
     */
    Long submitReturnApply(ReturnApplyDTO returnApplyDTO);

    /**
     * 分页查询回院申请列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<ReturnApplyVO> getReturnApplyPage(ReturnApplyQueryDTO queryDTO);

    /**
     * 根据ID查询回院申请详情
     * @param id 申请ID
     * @return 回院申请详情
     */
    ReturnApplyVO getReturnApplyById(Long id);

    /**
     * 审批回院申请
     * @param approveDTO 审批信息
     */
    void approveReturnApply(ReturnApproveDTO approveDTO);

    /**
     * 检查客户是否可以申请回院
     * @param customerId 客户ID
     * @return true-可以申请，false-不可以申请
     */
    boolean canApplyReturn(Long customerId);
}
