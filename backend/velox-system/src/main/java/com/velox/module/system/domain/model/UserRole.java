package com.velox.module.system.domain.model;

import com.velox.domain.model.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_user_role")
public class UserRole extends BaseEntity {

    private String userId;

    private String roleId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = normalizeIdentifier(userId);
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = normalizeIdentifier(roleId);
    }
}
