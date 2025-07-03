package com.nursing.center.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 客户护理设置DTO - 用于客户护理设置页面的数据展示
 */
@Data
public class CustomerCareSettingDTO {
    private Long id;
    private String name;
    private String phone;
    private String idCard;
    private Integer age;
    private String gender;
    
    // 护理级别信息
    private Long careLevelId;
    private String careLevelName;
    
    // 护理项目统计
    private Integer careItemCount;
    
    // 服务时间信息
    private LocalDate effectiveDate;
    private LocalDate createTime;
    
    // 床位信息
    private String buildingName;
    private String roomNo;
    private String bedNo;
    
    // 健康管理师信息
    private String healthManagerName;
}
