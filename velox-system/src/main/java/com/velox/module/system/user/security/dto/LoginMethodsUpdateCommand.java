package com.velox.module.system.user.security.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class LoginMethodsUpdateCommand {

    @NotNull
    @NotEmpty
    private List<@Size(max = 32) String> methods;

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }
}
