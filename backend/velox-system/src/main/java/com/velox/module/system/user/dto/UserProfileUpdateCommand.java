package com.velox.module.system.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UserProfileUpdateCommand {

    @NotBlank(message = "{validation.system.user.profile.real_name.not_blank}")
    @Size(max = 50, message = "{validation.system.user.profile.real_name.size}")
    private String realName;

    @NotBlank(message = "{validation.system.user.profile.nickname.not_blank}")
    @Size(max = 50, message = "{validation.system.user.profile.nickname.size}")
    private String nickname;

    @Email(message = "{validation.system.user.profile.email.invalid}")
    @Size(max = 100, message = "{validation.system.user.profile.email.size}")
    private String email;

    @NotBlank(message = "{validation.system.user.profile.phone.not_blank}")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "{validation.system.user.profile.phone.pattern}")
    private String phone;

    @Size(max = 255, message = "{validation.system.user.profile.address.size}")
    private String address;

    private Integer gender;

    @Size(max = 1000, message = "{validation.system.user.profile.introduction.size}")
    private String introduction;

    @Size(max = 255, message = "{validation.system.user.profile.signature.size}")
    private String signature;

    @Size(max = 50, message = "{validation.system.user.profile.position.size}")
    private String position;

    @Size(max = 100, message = "{validation.system.user.profile.company.size}")
    private String company;

    private List<@Size(max = 16, message = "{validation.system.user.profile.tags.item.size}") String> tags;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
