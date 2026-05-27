package com.velox.framework.security.common.constant;

public final class SecurityConstants {

    public static final String EMPTY = "";

    public static final String DEFAULT_PASSWORD_ALGORITHM = "bcrypt";
    public static final String DEFAULT_VERIFICATION_SECRET = "change-this-verification-secret";
    public static final String DEFAULT_TOKEN_MODE = "stateful";
    public static final String DEFAULT_TOKEN_PROVIDER = EMPTY;
    public static final String DEFAULT_TOKEN_NAME = "Authorization";
    public static final String DEFAULT_TOKEN_STYLE = "uuid";
    public static final String DEFAULT_LOGIN_TYPE = "login";
    public static final String DEFAULT_JWT_STRATEGY = "mixin";
    public static final String DEFAULT_JWT_SECRET = "change-this-jwt-secret";

    public static final String TOKEN_MODE_STATEFUL = "stateful";
    public static final String TOKEN_MODE_JWT = "jwt";

    public static final String TOKEN_PROVIDER_SATOKEN = "satoken";
    public static final String TOKEN_PROVIDER_SATOKEN_JWT = "satoken-jwt";

    public static final String JWT_STRATEGY_MIXIN = "mixin";
    public static final String JWT_STRATEGY_SIMPLE = "simple";
    public static final String JWT_STRATEGY_STATELESS = "stateless";

    public static final String SA_TOKEN_STATEFUL_PROVIDER_BEAN_NAME = "veloxSaTokenStatefulTokenProvider";
    public static final String SA_TOKEN_JWT_PROVIDER_BEAN_NAME = "veloxSaTokenJwtTokenProvider";

    private SecurityConstants() {
    }
}
