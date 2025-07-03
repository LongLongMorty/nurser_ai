package com.nursing.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nursing.center.entity.AiCallLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Map;

/**
 * AI调用日志表数据访问层
 */
@Mapper
public interface AiCallLogMapper extends BaseMapper<AiCallLog> {
    
    /**
     * 统计API调用情况
     */
    Map<String, Object> selectApiStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 统计每日调用量
     */
    Map<String, Object> selectDailyCallCount(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    
    /**
     * 统计成功率
     */
    Double selectSuccessRate(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
