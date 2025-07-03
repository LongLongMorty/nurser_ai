package com.nursing.center.common.enums;

/**
 * 审批状态枚举
 * @author system
 * @since 2025-06-22
 */
public enum ApprovalStatus {
    DRAFT("草稿"),
    PENDING("待审批"),
    APPROVED("已审批"),
    REJECTED("已拒绝");

    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
