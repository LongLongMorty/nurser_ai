package com.nursing.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nursing.center.entity.AiRecommendationHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI推荐历史 Mapper 接口
 */
@Mapper
public interface AiRecommendationHistoryMapper extends BaseMapper<AiRecommendationHistory> {

    /**
     * 根据客户ID查询推荐历史
     */
    List<AiRecommendationHistory> selectByCustomerId(@Param("customerId") Long customerId);

    /**
     * 根据客户ID和日期范围查询推荐历史
     */
    List<AiRecommendationHistory> selectByCustomerAndDateRange(
            @Param("customerId") Long customerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 根据推荐类型查询历史
     */
    List<AiRecommendationHistory> selectByRecommendationType(@Param("recommendationType") String recommendationType);

    /**
     * 查询已采纳的推荐历史
     */
    List<AiRecommendationHistory> selectAdoptedRecommendations(@Param("customerId") Long customerId);

    /**
     * 根据创建时间范围查询
     */
    List<AiRecommendationHistory> selectByCreateTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计客户的推荐次数
     */
    Long countByCustomerId(@Param("customerId") Long customerId);

    /**
     * 查询最近的推荐记录
     */
    List<AiRecommendationHistory> selectRecentRecommendations(
            @Param("customerId") Long customerId,
            @Param("limit") Integer limit
    );

    /**
     * 统计采纳次数
     */
    Long countAdoptions(@Param("mealType") String mealType, @Param("targetGroup") String targetGroup);

    /**
     * 查询高评分的历史记录
     */
    List<AiRecommendationHistory> selectHighRatedHistory(
            @Param("mealType") String mealType,
            @Param("targetGroup") String targetGroup,
            @Param("minRating") Double minRating,
            @Param("limit") Integer limit
    );

    /**
     * 根据条件查询历史记录
     */
    List<AiRecommendationHistory> selectByConditions(
            @Param("mealType") String mealType,
            @Param("targetGroup") String targetGroup,
            @Param("adoptionStatus") String adoptionStatus,
            @Param("limit") Integer limit
    );
}
