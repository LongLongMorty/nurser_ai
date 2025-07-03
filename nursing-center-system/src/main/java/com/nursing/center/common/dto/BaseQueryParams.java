package com.nursing.center.common.dto;

import lombok.Data;

/**
 * 基础查询参数
 * @author system
 * @since 2024-01-01
 */
@Data
public class BaseQueryParams {
    
    /**
     * 页码，从1开始
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
    
    /**
     * 关键字搜索
     */
    private String keyword;
    
    /**
     * 兼容前端传递的current参数
     */
    private Integer current;
    
    /**
     * 获取实际的页码
     */
    public Integer getPage() {
        return current != null ? current : page;
    }
    
    /**
     * 获取实际的页面大小
     */
    public Integer getSize() {
        return size != null ? size : 10;
    }
}
