package com.velox.module.system.user.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.user.dto.UserListItemDTO;
import com.velox.module.system.user.dto.UserQuery;
import com.velox.module.system.user.dto.UserSaveCommand;
import com.velox.module.system.user.service.UserManageService;
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

import com.velox.common.result.PageResult;

@Tag(name = "openapi.system.user.manage.tag.name", description = "openapi.system.user.manage.tag.description")
@RestController
@RequestMapping("/user")
public class UserManageController {

    private final UserManageService userManageService;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public UserManageController(
            UserManageService userManageService,
            SystemFrontendIdCodecSupport frontendIdCodecSupport
    ) {
        this.userManageService = userManageService;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Operation(summary = "openapi.system.user.manage.list.summary")
    @RequirePermission("system:user:query")
    @GetMapping("/list")
    public Result<PageResult<UserListItemDTO>> list(UserQuery query) {
        return Result.ok(userManageService.list(query));
    }

    @Operation(summary = "openapi.system.user.manage.create.summary")
    @RequirePermission("system:user:create")
    @PostMapping
    public Result<String> create(@Valid @RequestBody UserSaveCommand command) {
        return Result.ok(frontendIdCodecSupport.encodeIdentifier(userManageService.create(command)));
    }

    @Operation(summary = "openapi.system.user.manage.update.summary")
    @RequirePermission("system:user:update")
    @PutMapping("/{userId}")
    public Result<Boolean> update(@PathVariable("userId") String userId, @Valid @RequestBody UserSaveCommand command) {
        return Result.ok(userManageService.update(userId, command));
    }

    @Operation(summary = "openapi.system.user.manage.delete.summary")
    @RequirePermission("system:user:delete")
    @DeleteMapping("/{userId}")
    public Result<Boolean> delete(@PathVariable("userId") String userId) {
        return Result.ok(userManageService.delete(userId));
    }
}
