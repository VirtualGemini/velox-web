package com.velox.module.system.auth.config;

import com.velox.framework.security.api.authorization.SecurityPermissionProvider;
import com.velox.module.system.permission.service.PermissionService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VeloxSecurityPermissionProvider implements SecurityPermissionProvider {

    private final PermissionService permissionService;

    public VeloxSecurityPermissionProvider(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public List<String> getPermissions(String loginId) {
        String userId = normalizeLoginId(loginId);
        if (userId == null) {
            return List.of();
        }
        return permissionService.getUserPermissionMarks(userId);
    }

    @Override
    public List<String> getRoles(String loginId) {
        String userId = normalizeLoginId(loginId);
        if (userId == null) {
            return List.of();
        }
        return permissionService.getUserRoleCodes(userId);
    }

    private String normalizeLoginId(String loginId) {
        if (loginId == null) {
            return null;
        }
        String normalized = loginId.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
