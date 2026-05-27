package com.velox.module.system.user.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.user.dto.AvatarUpdateCommand;
import com.velox.module.system.user.dto.LanguageUpdateCommand;
import com.velox.module.system.user.dto.UserInfoBasicDTO;
import com.velox.module.system.user.dto.UserInfoDTO;
import com.velox.module.system.user.dto.UserPasswordUpdateCommand;
import com.velox.module.system.user.dto.UserProfileUpdateCommand;
import com.velox.module.system.user.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "openapi.system.user.self.tag.name", description = "openapi.system.user.self.tag.description")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserInfoService userInfoService;

    public UserController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @Operation(summary = "openapi.system.user.self.get_info.summary")
    @GetMapping("/info")
    public Result<UserInfoBasicDTO> getUserInfo() {
        return Result.ok(userInfoService.getUserInfoBasicDTO());
    }

    @Operation(summary = "openapi.system.user.self.get_detail.summary")
    @GetMapping("/detail")
    @RequirePermission("system:user-center:profile-query")
    public Result<UserInfoDTO> getUserDetail() {
        return Result.ok(userInfoService.getUserInfoDTO());
    }

    @Operation(summary = "openapi.system.user.self.update_profile.summary")
    @PutMapping("/profile")
    @RequirePermission("system:user-center:profile-update")
    public Result<Boolean> updateProfile(@Valid @RequestBody UserProfileUpdateCommand command) {
        return Result.ok(userInfoService.updateCurrentUserProfile(command));
    }

    @Operation(summary = "openapi.system.user.self.update_password.summary")
    @PutMapping("/password")
    @RequirePermission("system:user-center:password-update")
    public Result<Boolean> updatePassword(@Valid @RequestBody UserPasswordUpdateCommand command) {
        return Result.ok(userInfoService.updateCurrentUserPassword(command));
    }

    @Operation(summary = "openapi.system.user.self.update_avatar.summary")
    @PutMapping("/avatar")
    @RequirePermission("system:user-center:avatar-update")
    public Result<UserInfoBasicDTO> updateAvatar(@RequestBody AvatarUpdateCommand command) {
        userInfoService.updateCurrentUserAvatar(command.getAvatarUrl());
        return Result.ok(userInfoService.getUserInfoBasicDTO());
    }

    @Operation(summary = "openapi.system.user.self.update_language.summary")
    @PutMapping("/language")
    public Result<Boolean> updateLanguage(@Valid @RequestBody LanguageUpdateCommand command) {
        return Result.ok(userInfoService.updateCurrentUserLanguage(command.getLanguage()));
    }
}
