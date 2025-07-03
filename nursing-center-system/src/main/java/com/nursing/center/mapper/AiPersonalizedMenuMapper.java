package com.nursing.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nursing.center.entity.AiPersonalizedMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 客户个性化菜单表数据访问层
 */
@Mapper
public interface AiPersonalizedMenuMapper extends BaseMapper<AiPersonalizedMenu> {
    
    /**
     * 根据客户ID和日期查询菜单
     */
    AiPersonalizedMenu selectByCustomerAndDate(
            @Param("customerId") Long customerId,
            @Param("menuDate") LocalDate menuDate
    );
    
    /**
     * 查询客户菜单历史
     */
    List<AiPersonalizedMenu> selectCustomerMenuHistory(
            @Param("customerId") Long customerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 查询待审核的菜单
     */
    List<AiPersonalizedMenu> selectPendingApproval();
    
    /**
     * 根据年龄性别查询相似菜单
     */
    List<AiPersonalizedMenu> selectSimilarMenus(
            @Param("customerAge") Integer customerAge,
            @Param("customerGender") String customerGender,
            @Param("limit") Integer limit
    );
}
