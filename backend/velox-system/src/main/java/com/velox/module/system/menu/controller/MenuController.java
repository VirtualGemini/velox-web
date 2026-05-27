package com.velox.module.system.menu.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequireAuthenticated;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.menu.dto.MenuSaveCommand;
import com.velox.module.system.menu.dto.MenuRouteDTO;
import com.velox.module.system.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "openapi.system.menu.tag.name", description = "openapi.system.menu.tag.description")
@RestController
@RequestMapping("/v3/system/menus")
public class MenuController {

    private final MenuService menuService;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public MenuController(
            MenuService menuService,
            SystemFrontendIdCodecSupport frontendIdCodecSupport
    ) {
        this.menuService = menuService;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Operation(summary = "openapi.system.menu.get_simple_menus.summary")
    @RequireAuthenticated
    @GetMapping("/simple")
    public Result<List<MenuRouteDTO>> getSimpleMenus() {
        return Result.ok(menuService.getSimpleMenus());
    }

    @Operation(summary = "openapi.system.menu.get_grantable_menus.summary",
            description = "openapi.system.menu.get_grantable_menus.description")
    @RequirePermission("system:menu:query")
    @GetMapping
    public Result<List<MenuRouteDTO>> getGrantableMenus() {
        return Result.ok(menuService.getSimpleMenus());
    }

    @Operation(summary = "openapi.system.menu.create.summary")
    @RequirePermission("system:menu:create")
    @PostMapping
    public Result<String> create(@Valid @RequestBody MenuSaveCommand command) {
        return Result.ok(frontendIdCodecSupport.encodeIdentifier(menuService.create(command)));
    }

    @Operation(summary = "openapi.system.menu.update.summary")
    @RequirePermission("system:menu:update")
    @PutMapping("/{menuId}")
    public Result<Boolean> update(@PathVariable("menuId") String menuId, @Valid @RequestBody MenuSaveCommand command) {
        return Result.ok(menuService.update(menuId, command));
    }

    @Operation(summary = "openapi.system.menu.delete.summary")
    @RequirePermission("system:menu:delete")
    @DeleteMapping("/{menuId}")
    public Result<Boolean> delete(@PathVariable("menuId") String menuId) {
        return Result.ok(menuService.delete(menuId));
    }

}
