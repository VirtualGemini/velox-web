package com.velox.module.system.user.security.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.email.api.builder.EmailBuilder;
import com.velox.email.api.message.SendResponse;
import com.velox.email.common.error.EmailErrorCode;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.framework.security.properties.SecurityProperties;
import com.velox.module.system.auth.store.VerificationCodeStore;
import com.velox.module.system.domain.model.User;
import com.velox.module.system.domain.model.UserSecurity;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.persistence.UserMapper;
import com.velox.module.system.persistence.UserSecurityMapper;
import com.velox.module.system.user.security.dto.EmailRebindCommand;
import com.velox.module.system.user.security.dto.EmailRebindSendCodeCommand;
import com.velox.module.system.user.security.dto.LoginMethodsUpdateCommand;
import com.velox.module.system.user.security.dto.MfaEmailUpdateCommand;
import com.velox.module.system.user.security.dto.SecurityStatusDTO;
import com.velox.module.system.user.security.service.AccountSecurityService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AccountSecurityServiceImpl implements AccountSecurityService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final UserMapper userMapper;
    private final UserSecurityMapper userSecurityMapper;
    private final SecuritySessionService securitySessionService;
    private final SecurityProperties securityProperties;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final VerificationCodeStore verificationCodeStore;
    private final ObjectProvider<EmailBuilder> emailBuilderProvider;

    public AccountSecurityServiceImpl(
            UserMapper userMapper,
            UserSecurityMapper userSecurityMapper,
            SecuritySessionService securitySessionService,
            SecurityProperties securityProperties,
            SystemEntityIdGenerator entityIdGenerator,
            VerificationCodeStore verificationCodeStore,
            ObjectProvider<EmailBuilder> emailBuilderProvider) {
        this.userMapper = userMapper;
        this.userSecurityMapper = userSecurityMapper;
        this.securitySessionService = securitySessionService;
        this.securityProperties = securityProperties;
        this.entityIdGenerator = entityIdGenerator;
        this.verificationCodeStore = verificationCodeStore;
        this.emailBuilderProvider = emailBuilderProvider;
    }

    @Override
    public SecurityStatusDTO getStatus() {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);
        UserSecurity security = getOrInitSecurity(user);

        SecurityProperties.Account account = securityProperties.getAccount();
        List<String> allowed = new ArrayList<>(account.getLoginMethods().getEnabled());

        List<String> stored = parseLoginMethods(security.getLoginMethods());
        if (stored.isEmpty()) {
            stored = new ArrayList<>(account.getLoginMethods().getDefaults());
        }
        List<String> effective = stored.stream()
                .filter(allowed::contains)
                .collect(Collectors.toList());

        SecurityStatusDTO dto = new SecurityStatusDTO();
        dto.setEmail(user.getEmail());
        dto.setEmailMasked(maskEmail(user.getEmail()));
        dto.setLoginMethods(stored);
        dto.setEffectiveLoginMethods(effective);
        dto.setAllowedLoginMethods(allowed);
        dto.setPasswordRequired(account.getLoginMethods().isPasswordRequired());

        SecurityStatusDTO.MfaStatus mfa = new SecurityStatusDTO.MfaStatus();
        mfa.setEmail(Integer.valueOf(1).equals(security.getMfaEmailEnabled()));
        mfa.setTotp(Integer.valueOf(1).equals(security.getMfaTotpEnabled()));
        dto.setMfa(mfa);

        dto.setEmailVerifiedAt(formatTime(security.getEmailVerifiedAt()));
        dto.setLastPasswordChangeAt(formatTime(security.getLastPasswordChangeAt()));
        return dto;
    }

    @Override
    public void sendEmailRebindCode(EmailRebindSendCodeCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);
        String newEmail = normalizeEmail(command.getNewEmail());
        if (newEmail == null || !EMAIL_PATTERN.matcher(newEmail).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }
        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            throw new ApiException(BusinessErrorCode.EMAIL_SAME_AS_CURRENT);
        }
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .eq(User::getEmail, newEmail)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw new ApiException(BusinessErrorCode.EMAIL_ALREADY_IN_USE);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        SecurityProperties.Account.Rebind.Email rebindConfig = securityProperties.getAccount().getRebind().getEmail();
        String code = RandomUtil.randomNumbers(6);
        if (!verificationCodeStore.trySaveRebindCode("email", newEmail, code,
                rebindConfig.getCodeTtlSeconds(), rebindConfig.getResendIntervalSeconds())) {
            throw new ApiException(BusinessErrorCode.REBIND_CODE_SEND_TOO_FREQUENT);
        }
        try {
            SendResponse response = emailBuilder.to(newEmail)
                    .subject("邮箱换绑验证码")
                    .text(buildRebindEmailContent(user.getUsername(), code))
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateRebindCode("email", newEmail);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            verificationCodeStore.invalidateRebindCode("email", newEmail);
            throw new ApiException(ex, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rebindEmail(EmailRebindCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);
        String newEmail = normalizeEmail(command.getNewEmail());
        if (newEmail == null || !EMAIL_PATTERN.matcher(newEmail).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }
        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            throw new ApiException(BusinessErrorCode.EMAIL_SAME_AS_CURRENT);
        }
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .eq(User::getEmail, newEmail)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw new ApiException(BusinessErrorCode.EMAIL_ALREADY_IN_USE);
        }

        VerificationCodeStore.VerificationResult result =
                verificationCodeStore.verifyRebindCode("email", newEmail, command.getCode());
        if (result == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.REBIND_CODE_EXPIRED);
        }
        if (result == VerificationCodeStore.VerificationResult.INVALID) {
            throw new ApiException(BusinessErrorCode.REBIND_CODE_ERROR);
        }

        user.setEmail(newEmail);
        user.setUpdateBy(userId);
        userMapper.updateById(user);

        UserSecurity security = getOrInitSecurity(user);
        security.setEmailVerifiedAt(LocalDateTime.now(ZoneOffset.UTC));
        security.setUpdateBy(userId);
        saveSecurity(security);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateLoginMethods(LoginMethodsUpdateCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);

        if (command.getMethods() == null || command.getMethods().isEmpty()) {
            throw new ApiException(BusinessErrorCode.LOGIN_METHOD_EMPTY);
        }
        Set<String> dedup = new LinkedHashSet<>();
        for (String method : command.getMethods()) {
            if (StringUtils.hasText(method)) {
                dedup.add(method.trim().toLowerCase());
            }
        }
        if (dedup.isEmpty()) {
            throw new ApiException(BusinessErrorCode.LOGIN_METHOD_EMPTY);
        }

        SecurityProperties.Account.LoginMethods config = securityProperties.getAccount().getLoginMethods();
        List<String> enabled = config.getEnabled();
        for (String method : dedup) {
            if (!enabled.contains(method)) {
                throw new ApiException(BusinessErrorCode.LOGIN_METHOD_NOT_ALLOWED);
            }
        }
        if (config.isPasswordRequired() && !dedup.contains("password")) {
            throw new ApiException(BusinessErrorCode.PASSWORD_METHOD_REQUIRED);
        }

        UserSecurity security = getOrInitSecurity(user);
        security.setLoginMethods(String.join(",", dedup));
        security.setUpdateBy(userId);
        saveSecurity(security);
        return true;
    }

    @Override
    public void sendMfaEmailCode() {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);
        String email = normalizeEmail(user.getEmail());
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND_TO_USER);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        SecurityProperties.Account.Mfa.Email mfaConfig = securityProperties.getAccount().getMfa().getEmail();
        String code = RandomUtil.randomNumbers(6);
        if (!verificationCodeStore.trySaveMfaCode(userId, code,
                mfaConfig.getTtlSeconds(), mfaConfig.getResendIntervalSeconds())) {
            throw new ApiException(BusinessErrorCode.MFA_CODE_SEND_TOO_FREQUENT);
        }
        try {
            SendResponse response = emailBuilder.to(email)
                    .subject("二段验证码")
                    .text(buildMfaCodeContent(user.getUsername(), code))
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateMfaCode(userId);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            verificationCodeStore.invalidateMfaCode(userId);
            throw new ApiException(ex, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMfaEmail(MfaEmailUpdateCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);
        Boolean enabled = command.getEnabled();
        if (enabled == null) {
            throw new ApiException(BusinessErrorCode.BUSINESS_ERROR);
        }

        UserSecurity security = getOrInitSecurity(user);
        if (Boolean.TRUE.equals(enabled)) {
            String email = normalizeEmail(user.getEmail());
            if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
                throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND_TO_USER);
            }
            if (!StringUtils.hasText(command.getCode())) {
                throw new ApiException(BusinessErrorCode.MFA_CODE_ERROR);
            }
            VerificationCodeStore.VerificationResult result =
                    verificationCodeStore.verifyMfaCode(userId, command.getCode());
            if (result == VerificationCodeStore.VerificationResult.EXPIRED) {
                throw new ApiException(BusinessErrorCode.MFA_CODE_EXPIRED);
            }
            if (result == VerificationCodeStore.VerificationResult.INVALID) {
                throw new ApiException(BusinessErrorCode.MFA_CODE_ERROR);
            }
            security.setMfaEmailEnabled(1);
        } else {
            security.setMfaEmailEnabled(0);
        }
        security.setUpdateBy(userId);
        saveSecurity(security);
        return true;
    }

    private User requireUser(String userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new ApiException(BusinessErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private UserSecurity getOrInitSecurity(User user) {
        UserSecurity security = userSecurityMapper.selectOne(new LambdaQueryWrapper<UserSecurity>()
                .eq(UserSecurity::getUserId, user.getId())
                .eq(UserSecurity::getDeleted, 0)
                .last("limit 1"));
        if (security != null) {
            return security;
        }
        SecurityProperties.Account.LoginMethods config = securityProperties.getAccount().getLoginMethods();
        UserSecurity created = new UserSecurity();
        created.setId(entityIdGenerator.nextId(UserSecurity.class));
        created.setUserId(user.getId());
        created.setLoginMethods(String.join(",", config.getDefaults()));
        created.setMfaEmailEnabled(0);
        created.setMfaTotpEnabled(0);
        created.setDeleted(0);
        return created;
    }

    private void saveSecurity(UserSecurity security) {
        security.setDeleted(0);
        if (userSecurityMapper.selectById(security.getId()) == null) {
            userSecurityMapper.insert(security);
            return;
        }
        userSecurityMapper.updateById(security);
    }

    private List<String> parseLoginMethods(String methods) {
        if (!StringUtils.hasText(methods)) {
            return new ArrayList<>();
        }
        return Arrays.stream(methods.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return email;
        }
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(ISO_FMT);
    }

    private EmailBuilder requireEmailBuilder() {
        EmailBuilder emailBuilder = emailBuilderProvider.getIfAvailable();
        if (emailBuilder == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
        }
        return emailBuilder;
    }

    private String buildRebindEmailContent(String username, String code) {
        return "您好，" + username + "：\n\n"
                + "您正在执行邮箱换绑操作。\n"
                + "本次换绑验证码为：" + code + "\n"
                + "验证码 10 分钟内有效，请勿泄露给他人。\n\n"
                + "如果这不是您的操作，请忽略本邮件并修改密码。";
    }

    private String buildMfaCodeContent(String username, String code) {
        return "您好，" + username + "：\n\n"
                + "您正在使用邮箱二段验证码。\n"
                + "本次验证码为：" + code + "\n"
                + "验证码 5 分钟内有效，请勿泄露给他人。\n\n"
                + "如果这不是您的操作，请尽快修改密码。";
    }
}
