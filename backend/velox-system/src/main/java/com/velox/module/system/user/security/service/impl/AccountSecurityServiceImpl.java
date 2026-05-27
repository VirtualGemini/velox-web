package com.velox.module.system.user.security.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.email.api.builder.EmailBuilder;
import com.velox.email.api.message.SendResponse;
import com.velox.email.common.error.EmailErrorCode;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.auth.properties.SystemAccountSecurityProperties;
import com.velox.module.system.auth.service.PasswordCipherService;
import com.velox.module.system.auth.store.VerificationCodeStore;
import com.velox.module.system.domain.model.User;
import com.velox.module.system.domain.model.UserSecurity;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.persistence.UserMapper;
import com.velox.module.system.persistence.UserSecurityMapper;
import com.velox.module.system.user.security.dto.EmailRebindCommand;
import com.velox.module.system.user.security.dto.EmailRebindProofDTO;
import com.velox.module.system.user.security.dto.EmailRebindProofVerifyCommand;
import com.velox.module.system.user.security.dto.EmailRebindSendCodeCommand;
import com.velox.module.system.user.security.dto.LoginMethodsUpdateCommand;
import com.velox.module.system.user.security.dto.MfaEmailUpdateCommand;
import com.velox.module.system.user.security.dto.MfaTotpDisableCommand;
import com.velox.module.system.user.security.dto.MfaTotpEnableCommand;
import com.velox.module.system.user.security.dto.MfaTotpProvisionDTO;
import com.velox.module.system.user.security.dto.SecurityStatusDTO;
import com.velox.module.system.user.security.service.AccountSecurityService;
import com.velox.framework.totp.api.model.TotpProvisioning;
import com.velox.framework.totp.api.model.TotpVerifyResult;
import com.velox.framework.totp.api.service.TotpService;
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
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AccountSecurityServiceImpl implements AccountSecurityService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String REBIND_SCENE = "email_rebind";
    private static final String REBIND_PROOF_SCOPE = "email-proof";
    private static final String REBIND_PROOF_TYPE_CURRENT_EMAIL_CODE = "current_email_code";
    private static final String REBIND_PROOF_TYPE_TOTP = "totp";
    private static final String REBIND_PROOF_TYPE_PASSWORD = "password";

    private final UserMapper userMapper;
    private final UserSecurityMapper userSecurityMapper;
    private final SecuritySessionService securitySessionService;
    private final SystemAccountSecurityProperties accountSecurityProperties;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final VerificationCodeStore verificationCodeStore;
    private final ObjectProvider<EmailBuilder> emailBuilderProvider;
    private final PasswordCipherService passwordCipherService;
    private final TotpService totpService;

    public AccountSecurityServiceImpl(
            UserMapper userMapper,
            UserSecurityMapper userSecurityMapper,
            SecuritySessionService securitySessionService,
            SystemAccountSecurityProperties accountSecurityProperties,
            SystemEntityIdGenerator entityIdGenerator,
            VerificationCodeStore verificationCodeStore,
            ObjectProvider<EmailBuilder> emailBuilderProvider,
            PasswordCipherService passwordCipherService,
            TotpService totpService) {
        this.userMapper = userMapper;
        this.userSecurityMapper = userSecurityMapper;
        this.securitySessionService = securitySessionService;
        this.accountSecurityProperties = accountSecurityProperties;
        this.entityIdGenerator = entityIdGenerator;
        this.verificationCodeStore = verificationCodeStore;
        this.emailBuilderProvider = emailBuilderProvider;
        this.passwordCipherService = passwordCipherService;
        this.totpService = totpService;
    }

    @Override
    public SecurityStatusDTO getStatus() {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);
        UserSecurity security = getOrInitSecurity(user);

        List<String> allowed = new ArrayList<>(accountSecurityProperties.getLoginMethods().getEnabled());

        List<String> stored = parseLoginMethods(security.getLoginMethods());
        if (stored.isEmpty()) {
            stored = new ArrayList<>(accountSecurityProperties.getLoginMethods().getDefaults());
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
        dto.setPasswordRequired(accountSecurityProperties.getLoginMethods().isPasswordRequired());

        SecurityStatusDTO.MfaStatus mfa = new SecurityStatusDTO.MfaStatus();
        mfa.setEmail(Integer.valueOf(1).equals(security.getMfaEmailEnabled()));
        mfa.setTotp(Integer.valueOf(1).equals(security.getMfaTotpEnabled()));
        dto.setMfa(mfa);

        dto.setEmailVerifiedAt(formatTime(security.getEmailVerifiedAt()));
        dto.setLastPasswordChangeAt(formatTime(security.getLastPasswordChangeAt()));
        return dto;
    }

    @Override
    public void sendEmailRebindProofCode() {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);
        String currentEmail = normalizeEmail(user.getEmail());
        if (currentEmail == null || !EMAIL_PATTERN.matcher(currentEmail).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND_TO_USER);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        SystemAccountSecurityProperties.Rebind.Email rebindConfig = accountSecurityProperties.getRebind().getEmail();
        String code = RandomUtil.randomNumbers(6);
        if (!verificationCodeStore.trySaveRebindCode(
                REBIND_PROOF_SCOPE,
                currentEmail,
                code,
                rebindConfig.getCodeTtlSeconds(),
                rebindConfig.getResendIntervalSeconds())) {
            throw new ApiException(BusinessErrorCode.REBIND_CODE_SEND_TOO_FREQUENT);
        }
        try {
            SendResponse response = emailBuilder.to(currentEmail)
                    .subject("邮箱换绑身份验证")
                    .text(buildCurrentEmailProofContent(user.getUsername(), code))
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateRebindCode(REBIND_PROOF_SCOPE, currentEmail);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            verificationCodeStore.invalidateRebindCode(REBIND_PROOF_SCOPE, currentEmail);
            throw new ApiException(ex, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public EmailRebindProofDTO verifyEmailRebindProof(EmailRebindProofVerifyCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);
        UserSecurity security = getOrInitSecurity(user);
        String expectedProofType = resolveRebindProofType(user, security);
        String actualProofType = normalizeProofType(command.getProofType());
        if (!expectedProofType.equals(actualProofType)) {
            throw new ApiException(BusinessErrorCode.REBIND_PROOF_TYPE_MISMATCH);
        }

        switch (expectedProofType) {
            case REBIND_PROOF_TYPE_CURRENT_EMAIL_CODE -> verifyCurrentEmailProof(user, command.getCurrentEmailCode());
            case REBIND_PROOF_TYPE_TOTP -> verifyTotpProof(security, command.getTotpCode());
            case REBIND_PROOF_TYPE_PASSWORD -> verifyPasswordProof(user, command.getCurrentPassword());
            default -> throw new ApiException(BusinessErrorCode.REBIND_PROOF_TYPE_MISMATCH);
        }

        int proofTtlSeconds = accountSecurityProperties.getMfa().getEmail().getChallengeTtlSeconds();
        String proofTicket = UUID.randomUUID().toString().replace("-", "");
        verificationCodeStore.saveProofTicket(REBIND_SCENE, proofTicket, userId, proofTtlSeconds);

        EmailRebindProofDTO dto = new EmailRebindProofDTO();
        dto.setProofTicket(proofTicket);
        dto.setExpiresInSeconds(proofTtlSeconds);
        return dto;
    }

    @Override
    public void sendEmailRebindCode(EmailRebindSendCodeCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);
        requireRebindProof(userId, command.getProofTicket());
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
        SystemAccountSecurityProperties.Rebind.Email rebindConfig = accountSecurityProperties.getRebind().getEmail();
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
        requireRebindProof(userId, command.getProofTicket());
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
                verificationCodeStore.verifyRebindCode("email", newEmail, command.getNewEmailCode());
        if (result == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.REBIND_CODE_EXPIRED);
        }
        if (result == VerificationCodeStore.VerificationResult.INVALID) {
            throw new ApiException(BusinessErrorCode.REBIND_CODE_ERROR);
        }
        consumeRebindProof(userId, command.getProofTicket());

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

        SystemAccountSecurityProperties.LoginMethods config = accountSecurityProperties.getLoginMethods();
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
        SystemAccountSecurityProperties.Mfa.Email mfaConfig = accountSecurityProperties.getMfa().getEmail();
        String code = RandomUtil.randomNumbers(6);
        if (!verificationCodeStore.trySaveMfaCode(userId, code,
                mfaConfig.getTtlSeconds(), mfaConfig.getResendIntervalSeconds())) {
            throw new ApiException(BusinessErrorCode.MFA_CODE_SEND_TOO_FREQUENT);
        }
        try {
            SendResponse response = emailBuilder.to(email)
                    .subject("二次验证码")
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
            if (Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
                throw new ApiException(BusinessErrorCode.MFA_ALREADY_ENABLED);
            }
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

    @Override
    public MfaTotpProvisionDTO provisionMfaTotp() {
        if (!totpService.isEnabled()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_SERVICE_DISABLED);
        }
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);
        UserSecurity security = getOrInitSecurity(user);
        if (Integer.valueOf(1).equals(security.getMfaEmailEnabled())) {
            throw new ApiException(BusinessErrorCode.MFA_ALREADY_ENABLED);
        }

        String accountName = StringUtils.hasText(user.getEmail()) ? user.getEmail() : user.getUsername();
        TotpProvisioning provisioning = totpService.provision(accountName);
        MfaTotpProvisionDTO dto = new MfaTotpProvisionDTO();
        dto.setSecret(provisioning.secret().base32());
        dto.setOtpAuthUri(provisioning.otpAuthUri());
        dto.setIssuer(provisioning.issuer());
        dto.setAccountName(provisioning.accountName());
        dto.setDigits(provisioning.secret().digits());
        dto.setPeriodSeconds(provisioning.secret().periodSeconds());
        dto.setAlgorithm(provisioning.secret().algorithm().name());
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean enableMfaTotp(MfaTotpEnableCommand command) {
        if (!totpService.isEnabled()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_SERVICE_DISABLED);
        }
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);
        UserSecurity security = getOrInitSecurity(user);
        if (Integer.valueOf(1).equals(security.getMfaEmailEnabled())) {
            throw new ApiException(BusinessErrorCode.MFA_ALREADY_ENABLED);
        }

        TotpVerifyResult result = totpService.verify(command.getSecret(), command.getCode());
        if (!result.matched()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_CODE_ERROR);
        }

        security.setMfaTotpEnabled(1);
        security.setMfaTotpSecret(command.getSecret());
        security.setUpdateBy(userId);
        saveSecurity(security);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disableMfaTotp(MfaTotpDisableCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        User user = requireUser(userId);
        UserSecurity security = getOrInitSecurity(user);
        if (!Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_NOT_ENABLED);
        }
        if (!StringUtils.hasText(security.getMfaTotpSecret())) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_NOT_PROVISIONED);
        }
        if (!totpService.isEnabled()) {
            // 关闭路径即使能力被运维下线，也允许凭口令解绑 —— 但需要服务可用才能校验。
            throw new ApiException(BusinessErrorCode.MFA_TOTP_SERVICE_DISABLED);
        }

        TotpVerifyResult result = totpService.verify(security.getMfaTotpSecret(), command.getCode());
        if (!result.matched()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_CODE_ERROR);
        }

        security.setMfaTotpEnabled(0);
        security.setMfaTotpSecret(null);
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
        SystemAccountSecurityProperties.LoginMethods config = accountSecurityProperties.getLoginMethods();
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

    private String normalizeProofType(String proofType) {
        if (!StringUtils.hasText(proofType)) {
            return "";
        }
        return proofType.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveRebindProofType(User user, UserSecurity security) {
        if (StringUtils.hasText(normalizeEmail(user.getEmail()))) {
            return REBIND_PROOF_TYPE_CURRENT_EMAIL_CODE;
        }
        if (Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
            return REBIND_PROOF_TYPE_TOTP;
        }
        return REBIND_PROOF_TYPE_PASSWORD;
    }

    private void verifyCurrentEmailProof(User user, String currentEmailCode) {
        String currentEmail = normalizeEmail(user.getEmail());
        if (currentEmail == null || !EMAIL_PATTERN.matcher(currentEmail).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND_TO_USER);
        }
        VerificationCodeStore.VerificationResult result =
                verificationCodeStore.verifyRebindCode(REBIND_PROOF_SCOPE, currentEmail, currentEmailCode);
        if (result == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.REBIND_CURRENT_EMAIL_CODE_EXPIRED);
        }
        if (result == VerificationCodeStore.VerificationResult.INVALID) {
            throw new ApiException(BusinessErrorCode.REBIND_CURRENT_EMAIL_CODE_ERROR);
        }
    }

    private void verifyTotpProof(UserSecurity security, String totpCode) {
        if (!Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_NOT_ENABLED);
        }
        if (!StringUtils.hasText(security.getMfaTotpSecret())) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_NOT_PROVISIONED);
        }
        if (!totpService.isEnabled()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_SERVICE_DISABLED);
        }
        TotpVerifyResult result = totpService.verify(security.getMfaTotpSecret(), totpCode);
        if (!result.matched()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_CODE_ERROR);
        }
    }

    private void verifyPasswordProof(User user, String currentPassword) {
        if (!passwordCipherService.matches(currentPassword, user.getPassword())) {
            throw new ApiException(BusinessErrorCode.REBIND_PASSWORD_ERROR);
        }
    }

    private void requireRebindProof(String userId, String proofTicket) {
        if (!StringUtils.hasText(proofTicket)) {
            throw new ApiException(BusinessErrorCode.REBIND_PROOF_REQUIRED);
        }
        String storedUserId = verificationCodeStore.peekProofTicket(REBIND_SCENE, proofTicket.trim());
        if (!userId.equals(storedUserId)) {
            throw new ApiException(BusinessErrorCode.REBIND_PROOF_INVALID);
        }
    }

    private void consumeRebindProof(String userId, String proofTicket) {
        if (!StringUtils.hasText(proofTicket)) {
            throw new ApiException(BusinessErrorCode.REBIND_PROOF_REQUIRED);
        }
        String storedUserId = verificationCodeStore.consumeProofTicket(REBIND_SCENE, proofTicket.trim());
        if (!userId.equals(storedUserId)) {
            throw new ApiException(BusinessErrorCode.REBIND_PROOF_INVALID);
        }
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

    private String buildCurrentEmailProofContent(String username, String code) {
        return "您好，" + username + "：\n\n"
                + "您正在验证当前身份，以继续执行邮箱换绑。\n"
                + "本次验证码为：" + code + "\n"
                + "验证码 10 分钟内有效，请勿泄露给他人。\n\n"
                + "如果这不是您的操作，请忽略本邮件并尽快修改密码。";
    }

    private String buildMfaCodeContent(String username, String code) {
        return "您好，" + username + "：\n\n"
                + "您正在使用邮箱二次验证码。\n"
                + "本次验证码为：" + code + "\n"
                + "验证码 5 分钟内有效，请勿泄露给他人。\n\n"
                + "如果这不是您的操作，请尽快修改密码。";
    }
}
