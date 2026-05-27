package com.velox.module.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "openapi.system.auth.forgot_password_code_command.schema")
public class ForgotPasswordCodeCommand {

    @Schema(description = "openapi.system.auth.forgot_password_code_command.email", example = "user@example.com")
    @NotBlank(message = "{validation.system.auth.forgot_password.email.not_blank}")
    @Email(message = "{validation.system.auth.forgot_password.email.invalid}")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
