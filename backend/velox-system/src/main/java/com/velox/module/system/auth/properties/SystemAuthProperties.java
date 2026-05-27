package com.velox.module.system.auth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "velox.system.auth")
public class SystemAuthProperties {

    private final Login login = new Login();
    private final Captcha captcha = new Captcha();
    private final Verification verification = new Verification();
    private final Interceptor interceptor = new Interceptor();

    public Login getLogin() {
        return login;
    }

    public Captcha getCaptcha() {
        return captcha;
    }

    public Verification getVerification() {
        return verification;
    }

    public Interceptor getInterceptor() {
        return interceptor;
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

        private String secret = "change-this-verification-secret";
        private int resetCodeTtlSeconds = 600;
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

    public static class Interceptor {

        private List<String> authExcludeSuffixes = new ArrayList<>(List.of(
                "/login",
                "/login-code/send",
                "/login-code/login",
                "/captcha",
                "/register",
                "/forgot-password/code",
                "/forgot-password/reset",
                "/mfa/challenge/send-code",
                "/mfa/challenge/verify"
        ));

        private List<String> publicExcludePatterns = new ArrayList<>(List.of(
                "/file/*/get/**"
        ));

        private List<String> swaggerPublicExcludePatterns = new ArrayList<>(List.of(
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/doc.html",
                "/webjars/**"
        ));

        public List<String> getAuthExcludeSuffixes() {
            return authExcludeSuffixes;
        }

        public void setAuthExcludeSuffixes(List<String> authExcludeSuffixes) {
            this.authExcludeSuffixes = authExcludeSuffixes == null ? new ArrayList<>() : new ArrayList<>(authExcludeSuffixes);
        }

        public List<String> getPublicExcludePatterns() {
            return publicExcludePatterns;
        }

        public void setPublicExcludePatterns(List<String> publicExcludePatterns) {
            this.publicExcludePatterns = publicExcludePatterns == null ? new ArrayList<>() : new ArrayList<>(publicExcludePatterns);
        }

        public List<String> getSwaggerPublicExcludePatterns() {
            return swaggerPublicExcludePatterns;
        }

        public void setSwaggerPublicExcludePatterns(List<String> swaggerPublicExcludePatterns) {
            this.swaggerPublicExcludePatterns = swaggerPublicExcludePatterns == null
                    ? new ArrayList<>()
                    : new ArrayList<>(swaggerPublicExcludePatterns);
        }
    }
}
