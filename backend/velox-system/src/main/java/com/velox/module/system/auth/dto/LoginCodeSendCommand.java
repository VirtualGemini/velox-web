package com.velox.module.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "openapi.system.auth.login_code_send_command.schema")
public class LoginCodeSendCommand {

    @Schema(description = "openapi.system.auth.login_code_send_command.type", example = "email")
    @NotBlank(message = "{validation.system.auth.login_code_send.type.not_blank}")
    private String type;

    @Schema(description = "openapi.system.auth.login_code_send_command.target", example = "user@example.com")
    @NotBlank(message = "{validation.system.auth.login_code_send.target.not_blank}")
    private String target;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
