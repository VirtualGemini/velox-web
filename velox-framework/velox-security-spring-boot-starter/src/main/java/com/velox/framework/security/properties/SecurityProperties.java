package com.velox.framework.security.properties;

import com.velox.framework.security.common.constant.SecurityConstants;
import com.velox.framework.security.common.prefix.SecurityPropertyPrefixes;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全相关配置
 */
@ConfigurationProperties(prefix = SecurityPropertyPrefixes.SECURITY)
public class SecurityProperties {

    private boolean swaggerPublicEnabled = false;

    private final Password password = new Password();

    private final Login login = new Login();

    private final Captcha captcha = new Captcha();
    private final Verification verification = new Verification();
    private final Token token = new Token();
    private final Account account = new Account();

    public boolean isSwaggerPublicEnabled() {
        return swaggerPublicEnabled;
    }

    public void setSwaggerPublicEnabled(boolean swaggerPublicEnabled) {
        this.swaggerPublicEnabled = swaggerPublicEnabled;
    }

    public Password getPassword() {
        return password;
    }

    public Login getLogin() {
        return login;
    }

    public Captcha getCaptcha() {
        return captcha;
    }

    public Verification getVerification() {
        return verification;
    }

    public Token getToken() {
        return token;
    }

    public Account getAccount() {
        return account;
    }

    public static class Password {

        /**
         * 可选：pbkdf2_sha512 / bcrypt / md5
         */
        private String algorithm = SecurityConstants.DEFAULT_PASSWORD_ALGORITHM;

        /**
         * 登录成功后是否将旧加密算法升级为当前算法
         */
        private boolean upgradeOnLogin = true;

        /**
         * bcrypt 计算强度，范围建议 10~14
         */
        private int bcryptStrength = 12;

        /**
         * PBKDF2 迭代次数
         */
        private int pbkdf2Iterations = 210000;

        /**
         * PBKDF2 输出长度（bit）
         */
        private int pbkdf2KeyLength = 256;

        /**
         * PBKDF2 盐值长度（byte）
         */
        private int pbkdf2SaltLength = 16;

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public boolean isUpgradeOnLogin() {
            return upgradeOnLogin;
        }

        public void setUpgradeOnLogin(boolean upgradeOnLogin) {
            this.upgradeOnLogin = upgradeOnLogin;
        }

        public int getBcryptStrength() {
            return bcryptStrength;
        }

        public void setBcryptStrength(int bcryptStrength) {
            this.bcryptStrength = bcryptStrength;
        }

        public int getPbkdf2Iterations() {
            return pbkdf2Iterations;
        }

        public void setPbkdf2Iterations(int pbkdf2Iterations) {
            this.pbkdf2Iterations = pbkdf2Iterations;
        }

        public int getPbkdf2KeyLength() {
            return pbkdf2KeyLength;
        }

        public void setPbkdf2KeyLength(int pbkdf2KeyLength) {
            this.pbkdf2KeyLength = pbkdf2KeyLength;
        }

        public int getPbkdf2SaltLength() {
            return pbkdf2SaltLength;
        }

        public void setPbkdf2SaltLength(int pbkdf2SaltLength) {
            this.pbkdf2SaltLength = pbkdf2SaltLength;
        }
    }

    public static class Login {
        private int maxFailCount = 5;
        private int lockMinutes = 30;
        private final Presence presence = new Presence();

        public int getMaxFailCount() {
            return maxFailCount;
        }

        public void setMaxFailCount(int maxFailCount) {
            this.maxFailCount = maxFailCount;
        }

        public int getLockMinutes() {
            return lockMinutes;
        }

        public void setLockMinutes(int lockMinutes) {
            this.lockMinutes = lockMinutes;
        }

        public Presence getPresence() {
            return presence;
        }

        public static class Presence {
            private boolean requestHeartbeatEnabled = true;
            private boolean loginSignalEnabled = true;
            private boolean logoutSignalEnabled = true;
            private int idleOfflineSeconds = 6000;
            private int logoutOfflineSeconds = 30;

            public boolean isRequestHeartbeatEnabled() {
                return requestHeartbeatEnabled;
            }

            public void setRequestHeartbeatEnabled(boolean requestHeartbeatEnabled) {
                this.requestHeartbeatEnabled = requestHeartbeatEnabled;
            }

            public boolean isLoginSignalEnabled() {
                return loginSignalEnabled;
            }

            public void setLoginSignalEnabled(boolean loginSignalEnabled) {
                this.loginSignalEnabled = loginSignalEnabled;
            }

            public boolean isLogoutSignalEnabled() {
                return logoutSignalEnabled;
            }

            public void setLogoutSignalEnabled(boolean logoutSignalEnabled) {
                this.logoutSignalEnabled = logoutSignalEnabled;
            }

            public int getIdleOfflineSeconds() {
                return idleOfflineSeconds;
            }

            public void setIdleOfflineSeconds(int idleOfflineSeconds) {
                this.idleOfflineSeconds = idleOfflineSeconds;
            }

            public int getLogoutOfflineSeconds() {
                return logoutOfflineSeconds;
            }

            public void setLogoutOfflineSeconds(int logoutOfflineSeconds) {
                this.logoutOfflineSeconds = logoutOfflineSeconds;
            }
        }
    }

    public static class Captcha {
        private int ttlSeconds = 120;
        private int maxCacheSize = 10000;

        public int getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(int ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }

        public int getMaxCacheSize() {
            return maxCacheSize;
        }

        public void setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
        }
    }

    public static class Verification {
        /**
         * 验证码摘要加密密钥，建议通过环境变量注入
         */
        private String secret = SecurityConstants.DEFAULT_VERIFICATION_SECRET;

        /**
         * 忘记密码验证码有效期（秒）
         */
        private int resetCodeTtlSeconds = 600;

        /**
         * 忘记密码验证码重复发送间隔（秒）
         */
        private int resetCodeResendIntervalSeconds = 60;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public int getResetCodeTtlSeconds() {
            return resetCodeTtlSeconds;
        }

        public void setResetCodeTtlSeconds(int resetCodeTtlSeconds) {
            this.resetCodeTtlSeconds = resetCodeTtlSeconds;
        }

        public int getResetCodeResendIntervalSeconds() {
            return resetCodeResendIntervalSeconds;
        }

        public void setResetCodeResendIntervalSeconds(int resetCodeResendIntervalSeconds) {
            this.resetCodeResendIntervalSeconds = resetCodeResendIntervalSeconds;
        }
    }

    public static class Token {
        /**
         * token provider 模式：stateful / jwt / custom
         */
        private String mode = SecurityConstants.DEFAULT_TOKEN_MODE;

        /**
         * provider 标识；供第三方扩展区分多实现
         */
        private String provider = SecurityConstants.DEFAULT_TOKEN_PROVIDER;

        /**
         * token 写入的请求头名
         */
        private String tokenName = SecurityConstants.DEFAULT_TOKEN_NAME;

        /**
         * token 有效期（秒）
         */
        private long timeout = 86400L;

        /**
         * 是否允许同账号并发登录
         */
        private boolean concurrent = true;

        /**
         * 是否共享同一 token；false 表示每次登录单独发放
         */
        private boolean share = false;

        /**
         * Sa-Token 原生 token-style，仅在 stateful provider 下生效
         */
        private String style = SecurityConstants.DEFAULT_TOKEN_STYLE;

        /**
         * 是否输出底层认证组件日志
         */
        private boolean logEnabled = false;

        /**
         * 是否从 Header 中读取 token
         */
        private boolean readHeader = true;

        /**
         * 是否从 Cookie 中读取 token
         */
        private boolean readCookie = false;

        /**
         * 是否将 token 回写到 Header
         */
        private boolean writeHeader = false;

        private final Jwt jwt = new Jwt();

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getTokenName() {
            return tokenName;
        }

        public void setTokenName(String tokenName) {
            this.tokenName = tokenName;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        public boolean isConcurrent() {
            return concurrent;
        }

        public void setConcurrent(boolean concurrent) {
            this.concurrent = concurrent;
        }

        public boolean isShare() {
            return share;
        }

        public void setShare(boolean share) {
            this.share = share;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public boolean isLogEnabled() {
            return logEnabled;
        }

        public void setLogEnabled(boolean logEnabled) {
            this.logEnabled = logEnabled;
        }

        public boolean isReadHeader() {
            return readHeader;
        }

        public void setReadHeader(boolean readHeader) {
            this.readHeader = readHeader;
        }

        public boolean isReadCookie() {
            return readCookie;
        }

        public void setReadCookie(boolean readCookie) {
            this.readCookie = readCookie;
        }

        public boolean isWriteHeader() {
            return writeHeader;
        }

        public void setWriteHeader(boolean writeHeader) {
            this.writeHeader = writeHeader;
        }

        public Jwt getJwt() {
            return jwt;
        }

        public static class Jwt {
            /**
             * jwt 运行形态：simple / mixin / stateless
             */
            private String strategy = SecurityConstants.DEFAULT_JWT_STRATEGY;

            /**
             * 签名密钥，建议通过环境变量注入
             */
            private String secret = SecurityConstants.DEFAULT_JWT_SECRET;

            /**
             * 是否在 jwt 模式下继续将 token 写入 header
             */
            private boolean writeHeader = true;

            public String getStrategy() {
                return strategy;
            }

            public void setStrategy(String strategy) {
                this.strategy = strategy;
            }

            public String getSecret() {
                return secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
            }

            public boolean isWriteHeader() {
                return writeHeader;
            }

            public void setWriteHeader(boolean writeHeader) {
                this.writeHeader = writeHeader;
            }
        }
    }

    public static class Account {

        private final LoginMethods loginMethods = new LoginMethods();
        private final Mfa mfa = new Mfa();
        private final Rebind rebind = new Rebind();

        public LoginMethods getLoginMethods() {
            return loginMethods;
        }

        public Mfa getMfa() {
            return mfa;
        }

        public Rebind getRebind() {
            return rebind;
        }

        public static class LoginMethods {
            private java.util.List<String> enabled = new java.util.ArrayList<>(
                    java.util.List.of("password", "email_code"));
            private java.util.List<String> defaults = new java.util.ArrayList<>(java.util.List.of("password"));
            private boolean passwordRequired = true;

            public java.util.List<String> getEnabled() {
                return enabled;
            }

            public void setEnabled(java.util.List<String> enabled) {
                this.enabled = enabled;
            }

            public java.util.List<String> getDefaults() {
                return defaults;
            }

            public void setDefaults(java.util.List<String> defaults) {
                this.defaults = defaults;
            }

            public boolean isPasswordRequired() {
                return passwordRequired;
            }

            public void setPasswordRequired(boolean passwordRequired) {
                this.passwordRequired = passwordRequired;
            }
        }

        public static class Mfa {
            private final Email email = new Email();
            private final Totp totp = new Totp();

            public Email getEmail() {
                return email;
            }

            public Totp getTotp() {
                return totp;
            }

            public static class Email {
                private boolean enabled = true;
                private int ttlSeconds = 300;
                private int resendIntervalSeconds = 60;
                private int challengeTtlSeconds = 300;

                public boolean isEnabled() {
                    return enabled;
                }

                public void setEnabled(boolean enabled) {
                    this.enabled = enabled;
                }

                public int getTtlSeconds() {
                    return ttlSeconds;
                }

                public void setTtlSeconds(int ttlSeconds) {
                    this.ttlSeconds = ttlSeconds;
                }

                public int getResendIntervalSeconds() {
                    return resendIntervalSeconds;
                }

                public void setResendIntervalSeconds(int resendIntervalSeconds) {
                    this.resendIntervalSeconds = resendIntervalSeconds;
                }

                public int getChallengeTtlSeconds() {
                    return challengeTtlSeconds;
                }

                public void setChallengeTtlSeconds(int challengeTtlSeconds) {
                    this.challengeTtlSeconds = challengeTtlSeconds;
                }
            }

            public static class Totp {
                private boolean enabled = false;

                public boolean isEnabled() {
                    return enabled;
                }

                public void setEnabled(boolean enabled) {
                    this.enabled = enabled;
                }
            }
        }

        public static class Rebind {
            private final Email email = new Email();

            public Email getEmail() {
                return email;
            }

            public static class Email {
                private int codeTtlSeconds = 600;
                private int resendIntervalSeconds = 60;

                public int getCodeTtlSeconds() {
                    return codeTtlSeconds;
                }

                public void setCodeTtlSeconds(int codeTtlSeconds) {
                    this.codeTtlSeconds = codeTtlSeconds;
                }

                public int getResendIntervalSeconds() {
                    return resendIntervalSeconds;
                }

                public void setResendIntervalSeconds(int resendIntervalSeconds) {
                    this.resendIntervalSeconds = resendIntervalSeconds;
                }
            }
        }
    }
}
