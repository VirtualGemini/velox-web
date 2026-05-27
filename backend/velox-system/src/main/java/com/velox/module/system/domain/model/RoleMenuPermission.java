package com.velox.module.system.domain.model;

import com.velox.domain.model.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_role_menu_permission")
public class RoleMenuPermission extends BaseEntity {

    private String roleId;
    private String menuId;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = normalizeIdentifier(roleId);
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = normalizeIdentifier(menuId);
    }
}
