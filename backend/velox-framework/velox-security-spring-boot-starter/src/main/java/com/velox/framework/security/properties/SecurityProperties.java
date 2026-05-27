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
    private final Token token = new Token();

    public boolean isSwaggerPublicEnabled() {
        return swaggerPublicEnabled;
    }

    public void setSwaggerPublicEnabled(boolean swaggerPublicEnabled) {
        this.swaggerPublicEnabled = swaggerPublicEnabled;
    }

    public Password getPassword() {
        return password;
    }

    public Token getToken() {
        return token;
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

}
