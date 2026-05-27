package com.velox.module.system.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LanguageUpdateCommand {

    @NotBlank(message = "{validation.system.user.language.language.not_blank}")
    @Pattern(regexp = "^(zh|en)$", message = "{validation.system.user.language.language.pattern}")
    private String language;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
