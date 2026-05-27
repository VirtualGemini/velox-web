package com.velox.module.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "openapi.system.auth.reset_password_command.schema")
public class ResetPasswordCommand {

    @Schema(description = "openapi.system.auth.reset_password_command.email", example = "user@example.com")
    @NotBlank(message = "{validation.system.auth.reset_password.email.not_blank}")
    @Email(message = "{validation.system.auth.reset_password.email.invalid}")
    private String email;

    @Schema(description = "openapi.system.auth.reset_password_command.code", example = "123456")
    @NotBlank(message = "{validation.system.auth.reset_password.code.not_blank}")
    private String code;

    @Schema(description = "openapi.system.auth.reset_password_command.new_password", example = "123456")
    @NotBlank(message = "{validation.system.auth.reset_password.new_password.not_blank}")
    @Size(min = 6, max = 20, message = "{validation.system.auth.reset_password.new_password.size}")
    private String newPassword;

    @Schema(description = "openapi.system.auth.reset_password_command.confirm_password", example = "123456")
    @NotBlank(message = "{validation.system.auth.reset_password.confirm_password.not_blank}")
    private String confirmPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
