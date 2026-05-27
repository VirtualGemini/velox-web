package com.velox.framework.security.api.session;

import com.velox.framework.security.common.message.SecurityCommonMessages;
import com.velox.framework.security.exception.SecurityAuthenticationException;

public interface SecuritySessionService {

    String login(String loginId);

    default String login(String loginId, String sessionKey) {
        return login(loginId);
    }

    void logout();

    boolean isAuthenticated();

    String currentLoginIdOrNull();

    default String requireCurrentLoginId() {
        String loginId = currentLoginIdOrNull();
        if (loginId == null || loginId.isBlank()) {
            throw new SecurityAuthenticationException(SecurityCommonMessages.SECURITY_AUTHENTICATION_REQUIRED);
        }
        return loginId;
    }

    String currentTokenOrNull();
}
