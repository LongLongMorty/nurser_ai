package com.nursing.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nursing.center.entity.AiRecommendationTemp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI推荐临时表数据访问层
 */
@Mapper
public interface AiRecommendationTempMapper extends BaseMapper<AiRecommendationTemp> {
    
    /**
     * 根据会话ID删除推荐
     */
    int deleteBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据会话ID查询推荐列表
     */
    List<AiRecommendationTemp> selectBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 根据客户信息查询个性化推荐
     */
    List<AiRecommendationTemp> selectByCustomerInfo(
            @Param("customerId") Long customerId,
            @Param("customerAge") Integer customerAge,
            @Param("customerGender") String customerGender,
            @Param("mealType") String mealType
    );
    
    /**
     * 批量插入推荐
     */
    int batchInsert(@Param("list") List<AiRecommendationTemp> list);
}
