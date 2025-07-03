package com.nursing.center.dto;

import com.nursing.center.common.dto.BaseQueryParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 膳食配置查询DTO
 * @author system
 * @since 2025-06-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MealConfigQueryDTO extends BaseQueryParams {

    /**
     * 膳食名称
     */
    private String mealName;

    /**
     * 膳食类型
     */
    private String mealType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 适宜人群
     */
    private String suitableCrowd;

    /**
     * 获取页码（兼容MyBatis-Plus分页）
     */
    public Integer getPageNum() {
        return getPage();
    }    /**
     * 获取页大小（兼容MyBatis-Plus分页）
     */
    public Integer getPageSize() {
        return getSize();
    }
}
