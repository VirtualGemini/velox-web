package com.velox.module.system.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RoleSaveCommand {

    @NotBlank(message = "{validation.system.role.save.role_name.not_blank}")
    @Size(min = 2, max = 50, message = "{validation.system.role.save.role_name.size}")
    private String roleName;

    @NotBlank(message = "{validation.system.role.save.role_code.not_blank}")
    @Size(min = 2, max = 50, message = "{validation.system.role.save.role_code.size}")
    private String roleCode;

    @NotBlank(message = "{validation.system.role.save.description.not_blank}")
    @Size(max = 255, message = "{validation.system.role.save.description.size}")
    private String description;

    @NotNull(message = "{validation.system.role.save.enabled.not_null}")
    private Boolean enabled;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
