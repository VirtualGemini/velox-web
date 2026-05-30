package com.velox.module.system.role.dto;

import java.util.List;

/**
 * 角色绑定的账号信息，用于删除角色前的风险提示。
 */
public class RoleBoundAccountsDTO {

    private String roleId;

    private String roleName;

    private List<String> accountNames;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getAccountNames() {
        return accountNames;
    }

    public void setAccountNames(List<String> accountNames) {
        this.accountNames = accountNames;
    }
}
