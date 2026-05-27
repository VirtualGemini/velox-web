package com.velox.framework.security.api.authorization;

import java.util.List;

public interface SecurityPermissionProvider {

    List<String> getPermissions(String loginId);

    List<String> getRoles(String loginId);
}
