package com.velox.module.system.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "openapi.system.auth.code_login_command.schema")
public class CodeLoginCommand {

    @Schema(description = "openapi.system.auth.code_login_command.type", example = "email")
    @NotBlank(message = "{validation.system.auth.code_login.type.not_blank}")
    private String type;

    @Schema(description = "openapi.system.auth.code_login_command.target", example = "user@example.com")
    @NotBlank(message = "{validation.system.auth.code_login.target.not_blank}")
    private String target;

    @Schema(description = "openapi.system.auth.code_login_command.code", example = "123456")
    @NotBlank(message = "{validation.system.auth.code_login.code.not_blank}")
    private String code;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
