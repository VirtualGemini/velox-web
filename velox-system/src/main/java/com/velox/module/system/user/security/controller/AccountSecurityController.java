package com.velox.module.system.user.security.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.user.security.dto.EmailRebindCommand;
import com.velox.module.system.user.security.dto.EmailRebindSendCodeCommand;
import com.velox.module.system.user.security.dto.LoginMethodsUpdateCommand;
import com.velox.module.system.user.security.dto.MfaEmailUpdateCommand;
import com.velox.module.system.user.security.dto.SecurityStatusDTO;
import com.velox.module.system.user.security.service.AccountSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "账号安全", description = "账号安全设置相关接口")
@RestController
@RequestMapping("/user/security")
public class AccountSecurityController {

    private final AccountSecurityService accountSecurityService;

    public AccountSecurityController(AccountSecurityService accountSecurityService) {
        this.accountSecurityService = accountSecurityService;
    }

    @Operation(summary = "查询账号安全状态")
    @GetMapping("/status")
    @RequirePermission("system:user-center:security-query")
    public Result<SecurityStatusDTO> getStatus() {
        return Result.ok(accountSecurityService.getStatus());
    }

    @Operation(summary = "发送邮箱换绑验证码")
    @PostMapping("/email/rebind/send-code")
    @RequirePermission("system:user-center:email-rebind")
    public Result<Void> sendEmailRebindCode(@Valid @RequestBody EmailRebindSendCodeCommand command) {
        accountSecurityService.sendEmailRebindCode(command);
        return Result.ok();
    }

    @Operation(summary = "提交邮箱换绑")
    @PutMapping("/email/rebind")
    @RequirePermission("system:user-center:email-rebind")
    public Result<Boolean> rebindEmail(@Valid @RequestBody EmailRebindCommand command) {
        return Result.ok(accountSecurityService.rebindEmail(command));
    }

    @Operation(summary = "更新登录方式")
    @PutMapping("/login-methods")
    @RequirePermission("system:user-center:security-update")
    public Result<Boolean> updateLoginMethods(@Valid @RequestBody LoginMethodsUpdateCommand command) {
        return Result.ok(accountSecurityService.updateLoginMethods(command));
    }

    @Operation(summary = "发送邮箱 MFA 验证码")
    @PostMapping("/mfa/email/send-code")
    @RequirePermission("system:user-center:mfa-update")
    public Result<Void> sendMfaEmailCode() {
        accountSecurityService.sendMfaEmailCode();
        return Result.ok();
    }

    @Operation(summary = "开启或关闭邮箱二段验证")
    @PutMapping("/mfa/email")
    @RequirePermission("system:user-center:mfa-update")
    public Result<Boolean> updateMfaEmail(@Valid @RequestBody MfaEmailUpdateCommand command) {
        return Result.ok(accountSecurityService.updateMfaEmail(command));
    }
}
