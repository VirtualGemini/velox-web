package com.velox.module.system.auth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "velox.system.account-security")
public class SystemAccountSecurityProperties {

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

        private List<String> enabled = new ArrayList<>(List.of("password", "email_code"));
        private List<String> defaults = new ArrayList<>(List.of("password"));
        private boolean passwordRequired = true;

        public List<String> getEnabled() {
            return enabled;
        }

        public void setEnabled(List<String> enabled) {
            this.enabled = enabled;
        }

        public List<String> getDefaults() {
            return defaults;
        }

        public void setDefaults(List<String> defaults) {
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
