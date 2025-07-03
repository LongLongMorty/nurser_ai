package com.nursing.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nursing.center.entity.ReturnApply;
import com.nursing.center.dto.ReturnApplyQueryDTO;
import com.nursing.center.vo.ReturnApplyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 回院申请Mapper接口
 * @author system
 * @since 2024-01-01
 */
@Mapper
public interface ReturnApplyMapper extends BaseMapper<ReturnApply> {

    /**
     * 分页查询回院申请列表
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 回院申请VO分页数据
     */
    IPage<ReturnApplyVO> selectReturnApplyPage(Page<ReturnApplyVO> page, @Param("query") ReturnApplyQueryDTO queryDTO);

    /**
     * 根据ID查询回院申请详情
     * @param id 申请ID
     * @return 回院申请VO
     */
    ReturnApplyVO selectReturnApplyById(@Param("id") Long id);
}
