package com.nursing.center.dto;

/**
 * @BelongsProject: nursing-center-system
 * @BelongsPackage: com.nursing.center.dto
 * @Author: LongLongMorty
 * @CreateTime: 2025-05-29  13:08
 * @Description: TODO
 * @Version: 1.0
 */
import com.nursing.center.common.enums.CustomerType;
import lombok.Data;

@Data
public class CustomerQueryDTO {
    private String customerName; // 模糊查询
    private CustomerType customerType;
    private Integer status;
    private Long healthManagerId; // 健康管家ID过滤
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    
    // 支持前端的 current 和 size 参数
    private Integer current;
    private Integer size;
    
    // 获取实际的页码
    public Integer getPageNum() {
        return current != null ? current : pageNum;
    }
    
    // 获取实际的页面大小
    public Integer getPageSize() {
        return size != null ? size : pageSize;
    }
}
