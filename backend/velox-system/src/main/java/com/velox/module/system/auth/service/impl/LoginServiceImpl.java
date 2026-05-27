package com.velox.module.system.auth.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.email.api.builder.EmailBuilder;
import com.velox.email.api.message.SendResponse;
import com.velox.email.common.error.EmailErrorCode;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.auth.dto.CaptchaDTO;
import com.velox.module.system.auth.dto.CodeLoginCommand;
import com.velox.module.system.auth.dto.ForgotPasswordCodeCommand;
import com.velox.module.system.auth.dto.LoginCodeSendCommand;
import com.velox.module.system.auth.dto.LoginCommand;
import com.velox.module.system.auth.dto.MfaChallengeSendCodeCommand;
import com.velox.module.system.auth.dto.MfaChallengeVerifyCommand;
import com.velox.module.system.auth.dto.RegisterCommand;
import com.velox.module.system.auth.dto.ResetPasswordCommand;
import com.velox.module.system.auth.dto.TokenDTO;
import com.velox.module.system.auth.properties.SystemAccountSecurityProperties;
import com.velox.module.system.auth.properties.SystemAuthProperties;
import com.velox.module.system.auth.service.LoginService;
import com.velox.module.system.auth.service.PasswordCipherService;
import com.velox.module.system.auth.status.ActiveUserStatusService;
import com.velox.module.system.auth.store.VerificationCodeStore;
import com.velox.module.system.domain.model.Profile;
import com.velox.module.system.domain.model.Role;
import com.velox.module.system.domain.model.User;
import com.velox.module.system.domain.model.UserRole;
import com.velox.module.system.domain.model.UserSecurity;
import com.velox.module.system.domain.model.UserSession;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.persistence.ProfileMapper;
import com.velox.module.system.persistence.RoleMapper;
import com.velox.module.system.persistence.UserMapper;
import com.velox.module.system.persistence.UserRoleMapper;
import com.velox.module.system.persistence.UserSecurityMapper;
import com.velox.framework.totp.api.model.TotpVerifyResult;
import com.velox.framework.totp.api.service.TotpService;
import com.wf.captcha.SpecCaptcha;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoginServiceImpl implements LoginService {

    private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^1[3-9]\\d{9}$");
    private static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserMapper userMapper;
    private final ProfileMapper profileMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserSecurityMapper userSecurityMapper;
    private final PasswordCipherService passwordCipherService;
    private final SystemAuthProperties authProperties;
    private final SystemAccountSecurityProperties accountSecurityProperties;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final ObjectProvider<EmailBuilder> emailBuilderProvider;
    private final VerificationCodeStore verificationCodeStore;
    private final ActiveUserStatusService activeUserStatusService;
    private final SecuritySessionService securitySessionService;
    private final TotpService totpService;

    public LoginServiceImpl(UserMapper userMapper,
                            ProfileMapper profileMapper,
                            RoleMapper roleMapper,
                            UserRoleMapper userRoleMapper,
                            UserSecurityMapper userSecurityMapper,
                            PasswordCipherService passwordCipherService,
                            SystemAuthProperties authProperties,
                            SystemAccountSecurityProperties accountSecurityProperties,
                            SystemEntityIdGenerator entityIdGenerator,
                            ObjectProvider<EmailBuilder> emailBuilderProvider,
                            VerificationCodeStore verificationCodeStore,
                            ActiveUserStatusService activeUserStatusService,
                            SecuritySessionService securitySessionService,
                            TotpService totpService) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.userSecurityMapper = userSecurityMapper;
        this.passwordCipherService = passwordCipherService;
        this.authProperties = authProperties;
        this.accountSecurityProperties = accountSecurityProperties;
        this.entityIdGenerator = entityIdGenerator;
        this.emailBuilderProvider = emailBuilderProvider;
        this.verificationCodeStore = verificationCodeStore;
        this.activeUserStatusService = activeUserStatusService;
        this.securitySessionService = securitySessionService;
        this.totpService = totpService;
    }

    @Override
    public CaptchaDTO generateCaptcha() {
        CaptchaDTO dto = new CaptchaDTO();
        dto.setIsCaptchaOn(true);

        SpecCaptcha specCaptcha = new SpecCaptcha(120, 40, 4);
        String key = IdUtil.simpleUUID();
        verificationCodeStore.saveCaptcha(key, specCaptcha.text());

        dto.setCaptchaCodeKey(key);
        dto.setCaptchaCodeImg(specCaptcha.toBase64());

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenDTO login(LoginCommand command) {
        validateCaptchaIfPresent(command.getCaptchaCode(), command.getCaptchaCodeKey());

        String username = command.getUsername();
        String password = command.getPassword();

        if (username == null || username.isBlank()) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        if (password == null || password.isBlank()) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        User user = findUserByAccount(username);

        if (user == null) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        checkLoginLock(user);

        if (!passwordCipherService.matches(password, user.getPassword())) {
            increaseLoginFailCount(user);
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        if (Integer.valueOf(4).equals(user.getStatus())) {
            throw new ApiException(BusinessErrorCode.ACCOUNT_DISABLED);
        }

        UserSecurity security = ensureUserSecurity(user);
        ensureLoginMethodAllowed(security, "password");

        resetLoginFailCount(user);
        upgradePasswordIfNeeded(user, password);

        String mfaType = resolveMfaType(security, "password");
        if (mfaType != null) {
            return issueMfaChallenge(user, mfaType);
        }

        return performLogin(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterCommand command) {
        if (!command.getPassword().equals(command.getConfirmPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_MISMATCH);
        }

        User existUser = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .eq(User::getUsername, command.getUsername())
        );

        if (existUser != null) {
            throw new ApiException(BusinessErrorCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setId(entityIdGenerator.nextId(User.class));
        user.setUsername(command.getUsername());
        user.setPassword(passwordCipherService.encode(command.getPassword()));
        user.setStatus(1);
        user.setLoginFailCount(0);
        user.setDeleted(0);

        userMapper.insert(user);

        Profile profile = new Profile();
        profile.setId(entityIdGenerator.nextId(Profile.class));
        profile.setUserId(user.getId());
        profile.setNickname(command.getUsername());
        profile.setAvatar(buildDefaultAvatar(command.getUsername()));
        profile.setGender(0);
        profile.setDeleted(0);
        profileMapper.insert(profile);

        Role defaultRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .eq(Role::getRoleCode, "R_USER")
                .last("limit 1"));
        if (defaultRole != null && defaultRole.getId() != null) {
            UserRole userRole = new UserRole();
            userRole.setId(entityIdGenerator.nextId(UserRole.class));
            userRole.setUserId(user.getId());
            userRole.setRoleId(defaultRole.getId());
            userRole.setDeleted(0);
            userRoleMapper.insert(userRole);
        }

        UserSecurity security = new UserSecurity();
        security.setId(entityIdGenerator.nextId(UserSecurity.class));
        security.setUserId(user.getId());
        security.setLoginMethods(String.join(",",
                accountSecurityProperties.getLoginMethods().getDefaults()));
        security.setMfaEmailEnabled(0);
        security.setMfaTotpEnabled(0);
        security.setDeleted(0);
        userSecurityMapper.insert(security);
    }

    @Override
    public void sendResetPasswordCode(ForgotPasswordCodeCommand command) {
        String email = normalizeEmail(command.getEmail());
        if (email == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        User user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        String code = RandomUtil.randomNumbers(6);
        if (!verificationCodeStore.trySaveResetCode(email, code)) {
            throw new ApiException(BusinessErrorCode.RESET_CODE_SEND_TOO_FREQUENT);
        }
        try {
            SendResponse response = emailBuilder.to(email)
                    .subject("密码重置验证码")
                    .text(buildResetPasswordMailContent(user.getUsername(), code))
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateResetCode(email);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            verificationCodeStore.invalidateResetCode(email);
            throw new ApiException(exception, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public void resetPassword(ResetPasswordCommand command) {
        String email = normalizeEmail(command.getEmail());
        if (email == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }
        if (!command.getNewPassword().equals(command.getConfirmPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_MISMATCH);
        }

        User user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        VerificationCodeStore.VerificationResult verificationResult =
                verificationCodeStore.verifyResetCode(email, command.getCode());
        if (verificationResult == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.RESET_CODE_EXPIRED);
        }
        if (verificationResult == VerificationCodeStore.VerificationResult.INVALID) {
            throw new ApiException(BusinessErrorCode.RESET_CODE_ERROR);
        }

        if (passwordCipherService.matches(command.getNewPassword(), user.getPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_SAME_AS_OLD);
        }

        user.setPassword(passwordCipherService.encode(command.getNewPassword().trim()));
        user.setLoginFailCount(0);
        user.setLoginFailTime(null);
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout() {
        String userId = securitySessionService.currentLoginIdOrNull();
        String tokenValue = securitySessionService.currentTokenOrNull();
        securitySessionService.logout();
        activeUserStatusService.recordLogout(userId, tokenValue);
    }

    @Override
    public void sendLoginCode(LoginCodeSendCommand command) {
        String type = command.getType() == null ? "" : command.getType().trim().toLowerCase();
        if (!"email".equals(type)) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        String email = normalizeEmail(command.getTarget());
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        User user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        String code = RandomUtil.randomNumbers(6);
        if (!verificationCodeStore.trySaveLoginCode(email, code)) {
            throw new ApiException(BusinessErrorCode.LOGIN_CODE_SEND_TOO_FREQUENT);
        }
        try {
            SendResponse response = emailBuilder.to(email)
                    .subject("登录验证码")
                    .text(buildLoginCodeMailContent(user.getUsername(), code))
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateLoginCode(email);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            verificationCodeStore.invalidateLoginCode(email);
            throw new ApiException(exception, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenDTO loginByCode(CodeLoginCommand command) {
        String type = command.getType() == null ? "" : command.getType().trim().toLowerCase();
        if (!"email".equals(type)) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        String email = normalizeEmail(command.getTarget());
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        User user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        checkLoginLock(user);

        VerificationCodeStore.VerificationResult verificationResult =
                verificationCodeStore.verifyLoginCode(email, command.getCode());
        if (verificationResult == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.LOGIN_CODE_EXPIRED);
        }
        if (verificationResult == VerificationCodeStore.VerificationResult.INVALID) {
            increaseLoginFailCount(user);
            throw new ApiException(BusinessErrorCode.LOGIN_CODE_ERROR);
        }

        if (Integer.valueOf(4).equals(user.getStatus())) {
            throw new ApiException(BusinessErrorCode.ACCOUNT_DISABLED);
        }

        UserSecurity security = ensureUserSecurity(user);
        ensureLoginMethodAllowed(security, "email_code");

        resetLoginFailCount(user);

        // 邮箱验证码登录天然完成了邮箱因素校验，因此跳过邮箱二次验证；
        // 但 TOTP 是独立因素，仍需要继续走虚拟 MFA 设备验证挑战。
        String mfaType = resolveMfaType(security, "email_code");
        if (mfaType != null) {
            return issueMfaChallenge(user, mfaType);
        }

        return performLogin(user);
    }

    @Override
    public void sendMfaChallengeCode(MfaChallengeSendCodeCommand command) {
        String userId = verificationCodeStore.peekMfaChallenge(command.getChallenge());
        if (!StringUtils.hasText(userId)) {
            throw new ApiException(BusinessErrorCode.MFA_CHALLENGE_INVALID);
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new ApiException(BusinessErrorCode.USER_NOT_FOUND);
        }
        UserSecurity security = ensureUserSecurity(user);
        // 仅当前挑战属于"邮箱二次验证"时才能下发；TOTP 由认证器生成，无需也不允许触发邮件。
        if (Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
            throw new ApiException(BusinessErrorCode.MFA_CHALLENGE_INVALID);
        }
        if (!Integer.valueOf(1).equals(security.getMfaEmailEnabled())) {
            throw new ApiException(BusinessErrorCode.MFA_NOT_ENABLED);
        }
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
                    .subject("登录二次验证码")
                    .text(buildMfaCodeContent(user.getUsername(), code))
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateMfaCode(userId);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            verificationCodeStore.invalidateMfaCode(userId);
            throw new ApiException(exception, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenDTO verifyMfaChallenge(MfaChallengeVerifyCommand command) {
        String userId = verificationCodeStore.peekMfaChallenge(command.getChallenge());
        if (!StringUtils.hasText(userId)) {
            throw new ApiException(BusinessErrorCode.MFA_CHALLENGE_EXPIRED);
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new ApiException(BusinessErrorCode.USER_NOT_FOUND);
        }

        UserSecurity security = ensureUserSecurity(user);
        if (Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
            if (!StringUtils.hasText(security.getMfaTotpSecret())) {
                throw new ApiException(BusinessErrorCode.MFA_TOTP_NOT_PROVISIONED);
            }
            TotpVerifyResult totpResult = totpService.verify(security.getMfaTotpSecret(), command.getCode());
            if (!totpResult.matched()) {
                throw new ApiException(BusinessErrorCode.MFA_TOTP_CODE_ERROR);
            }
        } else if (Integer.valueOf(1).equals(security.getMfaEmailEnabled())) {
            VerificationCodeStore.VerificationResult result =
                    verificationCodeStore.verifyMfaCode(userId, command.getCode());
            if (result == VerificationCodeStore.VerificationResult.EXPIRED) {
                throw new ApiException(BusinessErrorCode.MFA_CODE_EXPIRED);
            }
            if (result == VerificationCodeStore.VerificationResult.INVALID) {
                throw new ApiException(BusinessErrorCode.MFA_CODE_ERROR);
            }
        } else {
            // 挑战已颁发但用户中途关闭了所有虚拟 MFA 设备验证方式 —— 让挑战失效以保持一致性。
            throw new ApiException(BusinessErrorCode.MFA_NOT_ENABLED);
        }

        verificationCodeStore.consumeMfaChallenge(command.getChallenge());

        return performLogin(user);
    }

    private TokenDTO performLogin(User user) {
        String sessionId = entityIdGenerator.nextId(UserSession.class);
        String token = securitySessionService.login(user.getId(), sessionId);
        try {
            activeUserStatusService.recordLogin(user.getId(), sessionId, token);
        } catch (RuntimeException exception) {
            try {
                securitySessionService.logout();
            } catch (RuntimeException ignored) {
                // 会话表写入失败时优先回滚当前 token，避免发出不可追踪的登录态。
            }
            throw exception;
        }
        return new TokenDTO(token, null);
    }

    /**
     * 解析当前登录方式下应该走的虚拟 MFA 设备验证类型：
     * - 优先 TOTP（独立因素，对所有登录方式生效）
     * - 其次邮箱二次验证，但仅对密码登录生效（邮箱验证码登录本身已校验邮箱）
     */
    private String resolveMfaType(UserSecurity security, String loginMethod) {
        if (security == null) {
            return null;
        }
        if (Integer.valueOf(1).equals(security.getMfaTotpEnabled()) && totpService.isEnabled()) {
            return "totp";
        }
        SystemAccountSecurityProperties.Mfa mfaConfig = accountSecurityProperties.getMfa();
        if ("password".equals(loginMethod)
                && mfaConfig.getEmail().isEnabled()
                && Integer.valueOf(1).equals(security.getMfaEmailEnabled())) {
            return "email";
        }
        return null;
    }

    private TokenDTO issueMfaChallenge(User user, String mfaType) {
        String challenge = IdUtil.simpleUUID();
        SystemAccountSecurityProperties.Mfa.Email mfaConfig = accountSecurityProperties.getMfa().getEmail();
        verificationCodeStore.saveMfaChallenge(challenge, user.getId(), mfaConfig.getChallengeTtlSeconds());
        TokenDTO dto = new TokenDTO();
        dto.setMfaChallenge(challenge);
        dto.setMfaType(mfaType);
        if ("email".equals(mfaType)) {
            dto.setMfaEmailMasked(maskEmail(user.getEmail()));
        } else if ("totp".equals(mfaType)) {
            dto.setMfaTotpDigits(6);
        }
        return dto;
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

    private UserSecurity ensureUserSecurity(User user) {
        UserSecurity security = userSecurityMapper.selectOne(new LambdaQueryWrapper<UserSecurity>()
                .eq(UserSecurity::getUserId, user.getId())
                .eq(UserSecurity::getDeleted, 0)
                .last("limit 1"));
        if (security != null) {
            return security;
        }
        UserSecurity created = new UserSecurity();
        created.setId(entityIdGenerator.nextId(UserSecurity.class));
        created.setUserId(user.getId());
        created.setLoginMethods(String.join(",",
                accountSecurityProperties.getLoginMethods().getDefaults()));
        created.setMfaEmailEnabled(0);
        created.setMfaTotpEnabled(0);
        created.setDeleted(0);
        userSecurityMapper.insert(created);
        return created;
    }

    private void ensureLoginMethodAllowed(UserSecurity security, String method) {
        List<String> enabled = accountSecurityProperties.getLoginMethods().getEnabled();
        if (enabled == null || !enabled.contains(method)) {
            throw new ApiException(BusinessErrorCode.LOGIN_METHOD_DISABLED);
        }
        List<String> stored = parseLoginMethods(security.getLoginMethods());
        if (stored.isEmpty()) {
            stored = accountSecurityProperties.getLoginMethods().getDefaults();
        }
        if (!stored.contains(method)) {
            throw new ApiException(BusinessErrorCode.LOGIN_METHOD_DISABLED);
        }
    }

    private List<String> parseLoginMethods(String methods) {
        if (!StringUtils.hasText(methods)) {
            return List.of();
        }
        return Arrays.stream(methods.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private void validateCaptchaIfPresent(String captchaCode, String key) {
        boolean captchaCodeBlank = captchaCode == null || captchaCode.isBlank();
        boolean keyBlank = key == null || key.isBlank();

        if (captchaCodeBlank && keyBlank) {
            return;
        }

        if (captchaCodeBlank || keyBlank) {
            throw new ApiException(BusinessErrorCode.CAPTCHA_ERROR);
        }

        VerificationCodeStore.VerificationResult captchaResult = verificationCodeStore.consumeCaptcha(key, captchaCode);
        if (captchaResult == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.CAPTCHA_EXPIRED);
        }
        if (captchaResult == VerificationCodeStore.VerificationResult.INVALID) {
            throw new ApiException(BusinessErrorCode.CAPTCHA_ERROR);
        }
    }

    private void checkLoginLock(User user) {
        if (user.getLoginFailTime() == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (user.getLoginFailTime().isAfter(now)) {
            throw new ApiException(BusinessErrorCode.ACCOUNT_LOCKED);
        }
        user.setLoginFailCount(0);
        user.setLoginFailTime(null);
        userMapper.updateById(user);
    }

    private void increaseLoginFailCount(User user) {
        int failCount = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount();
        user.setLoginFailCount(failCount + 1);

        if (failCount + 1 >= authProperties.getLogin().getMaxFailCount()) {
            user.setLoginFailTime(LocalDateTime.now(ZoneOffset.UTC)
                    .plusMinutes(authProperties.getLogin().getLockMinutes()));
        }

        userMapper.updateById(user);
    }

    private void resetLoginFailCount(User user) {
        if (user.getLoginFailCount() != null && user.getLoginFailCount() > 0) {
            user.setLoginFailCount(0);
            user.setLoginFailTime(null);
            userMapper.updateById(user);
        }
    }

    private void upgradePasswordIfNeeded(User user, String rawPassword) {
        if (!passwordCipherService.needsUpgrade(user.getPassword())) {
            return;
        }
        user.setPassword(passwordCipherService.encode(rawPassword));
        userMapper.updateById(user);
    }

    private String buildDefaultAvatar(String username) {
        String seed = username == null || username.isBlank() ? "user" : username.trim();
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + seed;
    }

    private User findUserByEmail(String email) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getDeleted, 0)
                        .eq(User::getEmail, email)
                        .last("limit 1")
        );
    }

    /**
     * 登录支持账号、手机号、邮箱三选一匹配。
     */
    private User findUserByAccount(String account) {
        String trimmed = account.trim();

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getDeleted, 0)
                        .eq(User::getUsername, trimmed)
                        .last("limit 1")
        );
        if (user != null) {
            return user;
        }

        if (PHONE_PATTERN.matcher(trimmed).matches()) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getDeleted, 0)
                            .eq(User::getPhone, trimmed)
                            .last("limit 1")
            );
            if (user != null) {
                return user;
            }
        }

        String lower = trimmed.toLowerCase();
        if (EMAIL_PATTERN.matcher(lower).matches()) {
            user = findUserByEmail(lower);
        }
        return user;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String buildResetPasswordMailContent(String username, String code) {
        return "您好，" + username + "：\n\n"
                + "您正在执行忘记密码操作。\n"
                + "本次密码重置验证码为：" + code + "\n"
                + "验证码 10 分钟内有效，请勿泄露给他人。\n\n"
                + "如果这不是您的操作，请忽略本邮件。";
    }

    private String buildLoginCodeMailContent(String username, String code) {
        return "您好，" + username + "：\n\n"
                + "您正在通过邮箱验证码登录。\n"
                + "本次登录验证码为：" + code + "\n"
                + "验证码 10 分钟内有效，请勿泄露给他人。\n\n"
                + "如果这不是您的操作，请尽快修改密码。";
    }

    private String buildMfaCodeContent(String username, String code) {
        return "您好，" + username + "：\n\n"
                + "您正在通过邮箱二次验证完成登录。\n"
                + "本次验证码为：" + code + "\n"
                + "验证码 5 分钟内有效，请勿泄露给他人。\n\n"
                + "如果这不是您的操作，请尽快修改密码。";
    }

    private EmailBuilder requireEmailBuilder() {
        EmailBuilder emailBuilder = emailBuilderProvider.getIfAvailable();
        if (emailBuilder == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
        }
        return emailBuilder;
    }
}
