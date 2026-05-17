package com.velox.framework.security.common.message;

public final class SecurityCommonMessages {

    public static final String SECURITY_TOKEN_PROVIDER_NOT_FOUND =
            "No compatible SecurityTokenProvider found for configured mode/provider";

    public static final String SECURITY_TOKEN_PROVIDER_DUPLICATED =
            "Multiple SecurityTokenProvider implementations matched configured mode/provider";

    public static final String SECURITY_PERMISSION_PROVIDER_NOT_FOUND =
            "SecurityPermissionProvider is required and no compatible implementation was found";

    public static final String SECURITY_TOKEN_PROVIDER_ENGINE_REQUIRED =
            "Security token provider must supply engine";

    public static final String SECURITY_AUTHENTICATION_REQUIRED =
            "Login required";

    public static final String SECURITY_PERMISSION_REQUIRED =
            "Permission is required";

    public static final String SECURITY_PERMISSION_DENIED =
            "Permission denied";

    public static final String SECURITY_LOGIN_FAILED =
            "Login failed";

    public static final String SECURITY_LOGOUT_FAILED =
            "Logout failed";

    public static final String SECURITY_UNSUPPORTED_JWT_STRATEGY =
            "Unsupported jwt strategy";

    public static final String SECURITY_NOT_WEB_CONTEXT =
            "Current context is not a web request";

    public static String tokenProviderNotFound(String mode, String provider) {
        return SECURITY_TOKEN_PROVIDER_NOT_FOUND + ": mode=" + mode + ", provider=" + provider;
    }

    public static String tokenProviderDuplicated(String mode, String provider) {
        return SECURITY_TOKEN_PROVIDER_DUPLICATED + ": mode=" + mode + ", provider=" + provider;
    }

    public static String unsupportedJwtStrategy(String strategy) {
        return SECURITY_UNSUPPORTED_JWT_STRATEGY + ": " + strategy;
    }

    private SecurityCommonMessages() {
    }
}
