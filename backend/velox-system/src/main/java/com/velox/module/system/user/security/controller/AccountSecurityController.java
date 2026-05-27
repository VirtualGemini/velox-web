package com.velox.module.system.user.security.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.user.security.dto.EmailRebindCommand;
import com.velox.module.system.user.security.dto.EmailRebindProofDTO;
import com.velox.module.system.user.security.dto.EmailRebindProofVerifyCommand;
import com.velox.module.system.user.security.dto.EmailRebindSendCodeCommand;
import com.velox.module.system.user.security.dto.LoginMethodsUpdateCommand;
import com.velox.module.system.user.security.dto.MfaEmailUpdateCommand;
import com.velox.module.system.user.security.dto.MfaTotpDisableCommand;
import com.velox.module.system.user.security.dto.MfaTotpEnableCommand;
import com.velox.module.system.user.security.dto.MfaTotpProvisionDTO;
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

@Tag(name = "openapi.system.user.security.tag.name", description = "openapi.system.user.security.tag.description")
@RestController
@RequestMapping("/user/security")
public class AccountSecurityController {

    private final AccountSecurityService accountSecurityService;

    public AccountSecurityController(AccountSecurityService accountSecurityService) {
        this.accountSecurityService = accountSecurityService;
    }

    @Operation(summary = "openapi.system.user.security.get_status.summary")
    @GetMapping("/status")
    @RequirePermission("system:user-center:security-query")
    public Result<SecurityStatusDTO> getStatus() {
        return Result.ok(accountSecurityService.getStatus());
    }

    @Operation(summary = "openapi.system.user.security.send_email_rebind_proof_code.summary")
    @PostMapping("/email/rebind/proof/send-code")
    @RequirePermission("system:user-center:email-rebind")
    public Result<Void> sendEmailRebindProofCode() {
        accountSecurityService.sendEmailRebindProofCode();
        return Result.ok();
    }

    @Operation(summary = "openapi.system.user.security.verify_email_rebind_proof.summary")
    @PostMapping("/email/rebind/proof/verify")
    @RequirePermission("system:user-center:email-rebind")
    public Result<EmailRebindProofDTO> verifyEmailRebindProof(
            @Valid @RequestBody EmailRebindProofVerifyCommand command) {
        return Result.ok(accountSecurityService.verifyEmailRebindProof(command));
    }

    @Operation(summary = "openapi.system.user.security.send_email_rebind_code.summary")
    @PostMapping("/email/rebind/send-code")
    @RequirePermission("system:user-center:email-rebind")
    public Result<Void> sendEmailRebindCode(@Valid @RequestBody EmailRebindSendCodeCommand command) {
        accountSecurityService.sendEmailRebindCode(command);
        return Result.ok();
    }

    @Operation(summary = "openapi.system.user.security.rebind_email.summary")
    @PutMapping("/email/rebind")
    @RequirePermission("system:user-center:email-rebind")
    public Result<Boolean> rebindEmail(@Valid @RequestBody EmailRebindCommand command) {
        return Result.ok(accountSecurityService.rebindEmail(command));
    }

    @Operation(summary = "openapi.system.user.security.update_login_methods.summary")
    @PutMapping("/login-methods")
    @RequirePermission("system:user-center:security-update")
    public Result<Boolean> updateLoginMethods(@Valid @RequestBody LoginMethodsUpdateCommand command) {
        return Result.ok(accountSecurityService.updateLoginMethods(command));
    }

    @Operation(summary = "openapi.system.user.security.send_mfa_email_code.summary")
    @PostMapping("/mfa/email/send-code")
    @RequirePermission("system:user-center:mfa-update")
    public Result<Void> sendMfaEmailCode() {
        accountSecurityService.sendMfaEmailCode();
        return Result.ok();
    }

    @Operation(summary = "openapi.system.user.security.update_mfa_email.summary")
    @PutMapping("/mfa/email")
    @RequirePermission("system:user-center:mfa-update")
    public Result<Boolean> updateMfaEmail(@Valid @RequestBody MfaEmailUpdateCommand command) {
        return Result.ok(accountSecurityService.updateMfaEmail(command));
    }

    @Operation(summary = "openapi.system.user.security.provision_mfa_totp.summary")
    @PostMapping("/mfa/totp/provision")
    @RequirePermission("system:user-center:mfa-update")
    public Result<MfaTotpProvisionDTO> provisionMfaTotp() {
        return Result.ok(accountSecurityService.provisionMfaTotp());
    }

    @Operation(summary = "openapi.system.user.security.enable_mfa_totp.summary")
    @PutMapping("/mfa/totp/enable")
    @RequirePermission("system:user-center:mfa-update")
    public Result<Boolean> enableMfaTotp(@Valid @RequestBody MfaTotpEnableCommand command) {
        return Result.ok(accountSecurityService.enableMfaTotp(command));
    }

    @Operation(summary = "openapi.system.user.security.disable_mfa_totp.summary")
    @PutMapping("/mfa/totp/disable")
    @RequirePermission("system:user-center:mfa-update")
    public Result<Boolean> disableMfaTotp(@Valid @RequestBody MfaTotpDisableCommand command) {
        return Result.ok(accountSecurityService.disableMfaTotp(command));
    }
}
