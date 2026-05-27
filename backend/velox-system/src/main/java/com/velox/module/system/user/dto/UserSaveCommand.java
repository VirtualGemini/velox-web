package com.velox.module.system.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class UserSaveCommand {

    @NotBlank(message = "{validation.system.user.save.username.not_blank}")
    @Size(min = 2, max = 50, message = "{validation.system.user.save.username.size}")
    private String username;

    @Size(min = 6, max = 32, message = "{validation.system.user.save.password.size}")
    private String password;

    @NotBlank(message = "{validation.system.user.save.nickname.not_blank}")
    @Size(max = 50, message = "{validation.system.user.save.nickname.size}")
    private String nickname;

    @Email(message = "{validation.system.user.save.email.invalid}")
    @Size(max = 100, message = "{validation.system.user.save.email.size}")
    private String email;

    @NotBlank(message = "{validation.system.user.save.phone.not_blank}")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "{validation.system.user.save.phone.pattern}")
    private String phone;

    private Integer gender;

    @NotEmpty(message = "{validation.system.user.save.role_codes.not_empty}")
    private List<String> roleCodes;

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }
}
