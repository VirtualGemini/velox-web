package com.velox.module.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "openapi.system.auth.register_command.schema")
public class RegisterCommand {

    @Schema(description = "openapi.system.auth.register_command.username", example = "admin")
    @NotBlank(message = "{validation.system.auth.register.username.not_blank}")
    @Size(min = 3, max = 20, message = "{validation.system.auth.register.username.size}")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "{validation.system.auth.register.username.pattern}")
    private String username;

    @Schema(description = "openapi.system.auth.register_command.password", example = "123456")
    @NotBlank(message = "{validation.system.auth.register.password.not_blank}")
    @Size(min = 6, max = 20, message = "{validation.system.auth.register.password.size}")
    private String password;

    @Schema(description = "openapi.system.auth.register_command.confirm_password", example = "123456")
    @NotBlank(message = "{validation.system.auth.register.confirm_password.not_blank}")
    private String confirmPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
