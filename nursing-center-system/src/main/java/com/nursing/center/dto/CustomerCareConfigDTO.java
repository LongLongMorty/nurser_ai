package com.nursing.center.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 客户护理配置DTO - 用于客户护理设置页面显示
 */
@Data
public class CustomerCareConfigDTO {
    private Long id;
    private String name;
    private String phone;
    private Long careLevelId;
    private String careLevelName;
    private Integer careItemCount;
    private LocalDate effectiveDate;
    private LocalDate createTime;
    
    // 用于设置护理级别时的额外信息
    private String idCard;
    private Integer age;
    private String gender;
    private String roomNo;
    private String bedNo;
}
