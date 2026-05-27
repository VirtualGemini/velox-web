package com.velox.module.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;

@Schema(description = "openapi.system.role.menu_permission_update.schema")
public class RoleMenuPermissionUpdateCommand {

    @Schema(description = "openapi.system.role.menu_permission_update.menu_ids", example = "[\"01JRF6YQ8J0N7N7N7N7N7N7N7N\"]")
    private List<String> menuIds = Collections.emptyList();

    public List<String> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<String> menuIds) {
        this.menuIds = menuIds;
    }
}
