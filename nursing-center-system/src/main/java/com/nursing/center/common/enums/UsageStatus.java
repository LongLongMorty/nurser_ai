package com.nursing.center.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 床位使用状态枚举
 */
public enum UsageStatus {
    USING("USING", "正在使用"),
    HISTORY("HISTORY", "使用历史");

    @EnumValue
    @JsonValue
    private final String code;
    private final String description;

    UsageStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
