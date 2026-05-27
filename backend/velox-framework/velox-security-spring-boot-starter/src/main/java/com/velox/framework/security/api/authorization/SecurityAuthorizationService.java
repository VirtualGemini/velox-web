package com.velox.framework.security.api.authorization;

public interface SecurityAuthorizationService {

    void checkAuthenticated();

    void checkPermission(String permission);
}
