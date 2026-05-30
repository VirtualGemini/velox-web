package com.velox.module.system.role.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.role.dto.RoleBoundAccountsDTO;
import com.velox.module.system.role.dto.RoleListItemDTO;
import com.velox.module.system.role.dto.RoleMenuPermissionUpdateCommand;
import com.velox.module.system.role.dto.RoleQuery;
import com.velox.module.system.role.dto.RoleSaveCommand;
import com.velox.module.system.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.velox.common.result.PageResult;

@Tag(name = "openapi.system.role.tag.name", description = "openapi.system.role.tag.description")
@RestController
@RequestMapping("/role")
public class RoleController {

    private final RoleService roleService;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public RoleController(
            RoleService roleService,
            SystemFrontendIdCodecSupport frontendIdCodecSupport
    ) {
        this.roleService = roleService;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Operation(summary = "openapi.system.role.list.summary")
    @RequirePermission("system:role:query")
    @GetMapping("/list")
    public Result<PageResult<RoleListItemDTO>> list(RoleQuery query) {
        return Result.ok(roleService.list(query));
    }

    @Operation(summary = "openapi.system.role.create.summary")
    @RequirePermission("system:role:create")
    @PostMapping
    public Result<String> create(@Valid @RequestBody RoleSaveCommand command) {
        return Result.ok(frontendIdCodecSupport.encodeIdentifier(roleService.create(command)));
    }

    @Operation(summary = "openapi.system.role.update.summary")
    @RequirePermission("system:role:update")
    @PutMapping("/{roleId}")
    public Result<Boolean> update(@PathVariable("roleId") String roleId, @Valid @RequestBody RoleSaveCommand command) {
        return Result.ok(roleService.update(roleId, command));
    }

    @Operation(summary = "openapi.system.role.delete.summary")
    @RequirePermission("system:role:delete")
    @DeleteMapping("/{roleId}")
    public Result<Boolean> delete(@PathVariable("roleId") String roleId) {
        return Result.ok(roleService.delete(roleId));
    }

    @Operation(summary = "openapi.system.role.delete_batch.summary")
    @RequirePermission("system:role:delete")
    @DeleteMapping("/delete-batch")
    public Result<Boolean> deleteBatch(@RequestParam("ids") List<String> ids) {
        return Result.ok(roleService.deleteBatch(ids));
    }

    @Operation(summary = "openapi.system.role.bound_accounts.summary")
    @RequirePermission("system:role:query")
    @GetMapping("/bound-accounts")
    public Result<List<RoleBoundAccountsDTO>> boundAccounts(@RequestParam("ids") List<String> ids) {
        return Result.ok(roleService.getBoundAccounts(ids));
    }

    @Operation(summary = "openapi.system.role.get_menu_permissions.summary")
    @RequirePermission("system:role:query")
    @GetMapping("/{roleId}/menu-permissions")
    public Result<List<String>> getRoleMenuPermissions(@PathVariable("roleId") String roleId) {
        return Result.ok(frontendIdCodecSupport.encodeIdentifiers(roleService.getRoleMenuPermissions(roleId)));
    }

    @Operation(summary = "openapi.system.role.update_menu_permissions.summary")
    @RequirePermission("system:role:permission")
    @PutMapping("/{roleId}/menu-permissions")
    public Result<Boolean> updateRoleMenuPermissions(
            @PathVariable("roleId") String roleId,
            @Valid @RequestBody(required = false) RoleMenuPermissionUpdateCommand command
    ) {
        return Result.ok(roleService.updateRoleMenuPermissions(roleId, command));
    }
}
