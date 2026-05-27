package com.velox.module.system.auth.controller;

import com.velox.common.result.Result;
import com.velox.module.system.auth.dto.CaptchaDTO;
import com.velox.module.system.auth.dto.CodeLoginCommand;
import com.velox.module.system.auth.dto.ForgotPasswordCodeCommand;
import com.velox.module.system.auth.dto.LoginCodeSendCommand;
import com.velox.module.system.auth.dto.LoginCommand;
import com.velox.module.system.auth.dto.MfaChallengeSendCodeCommand;
import com.velox.module.system.auth.dto.MfaChallengeVerifyCommand;
import com.velox.module.system.auth.dto.RegisterCommand;
import com.velox.module.system.auth.dto.ResetPasswordCommand;
import com.velox.module.system.auth.dto.TokenDTO;
import com.velox.module.system.auth.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "openapi.system.auth.login.tag.name", description = "openapi.system.auth.login.tag.description")
@RestController
@RequestMapping("/auth")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @Operation(summary = "openapi.system.auth.login.get_captcha.summary")
    @GetMapping("/captcha")
    public Result<CaptchaDTO> getCaptcha() {
        return Result.ok(loginService.generateCaptcha());
    }

    @Operation(summary = "openapi.system.auth.login.login.summary")
    @PostMapping("/login")
    public Result<TokenDTO> login(@Valid @RequestBody LoginCommand command) {
        return Result.ok(loginService.login(command));
    }

    @Operation(summary = "openapi.system.auth.login.send_login_code.summary")
    @PostMapping("/login-code/send")
    public Result<Void> sendLoginCode(@Valid @RequestBody LoginCodeSendCommand command) {
        loginService.sendLoginCode(command);
        return Result.ok();
    }

    @Operation(summary = "openapi.system.auth.login.login_by_code.summary")
    @PostMapping("/login-code/login")
    public Result<TokenDTO> loginByCode(@Valid @RequestBody CodeLoginCommand command) {
        return Result.ok(loginService.loginByCode(command));
    }

    @Operation(summary = "openapi.system.auth.login.register.summary")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterCommand command) {
        loginService.register(command);
        return Result.ok();
    }

    @Operation(summary = "openapi.system.auth.login.send_reset_password_code.summary")
    @PostMapping("/forgot-password/code")
    public Result<Void> sendResetPasswordCode(@Valid @RequestBody ForgotPasswordCodeCommand command) {
        loginService.sendResetPasswordCode(command);
        return Result.ok();
    }

    @Operation(summary = "openapi.system.auth.login.reset_password.summary")
    @PostMapping("/forgot-password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordCommand command) {
        loginService.resetPassword(command);
        return Result.ok();
    }

    @Operation(summary = "openapi.system.auth.login.logout.summary")
    @PostMapping("/logout")
    public Result<Void> logout() {
        loginService.logout();
        return Result.ok();
    }

    @Operation(summary = "openapi.system.auth.login.send_mfa_challenge_code.summary")
    @PostMapping("/mfa/challenge/send-code")
    public Result<Void> sendMfaChallengeCode(@Valid @RequestBody MfaChallengeSendCodeCommand command) {
        loginService.sendMfaChallengeCode(command);
        return Result.ok();
    }

    @Operation(summary = "openapi.system.auth.login.verify_mfa_challenge.summary")
    @PostMapping("/mfa/challenge/verify")
    public Result<TokenDTO> verifyMfaChallenge(@Valid @RequestBody MfaChallengeVerifyCommand command) {
        return Result.ok(loginService.verifyMfaChallenge(command));
    }
}
