package com.velox.framework.security.support.authorization;

import cn.dev33.satoken.stp.StpInterface;
import com.velox.framework.security.api.authorization.SecurityPermissionProvider;

import java.util.List;

public class SaTokenPermissionProviderAdapter implements SecurityPermissionProvider {

    private final StpInterface stpInterface;
    private final String loginType;

    public SaTokenPermissionProviderAdapter(StpInterface stpInterface, String loginType) {
        this.stpInterface = stpInterface;
        this.loginType = loginType;
    }

    @Override
    public List<String> getPermissions(String loginId) {
        return stpInterface.getPermissionList(loginId, loginType);
    }

    @Override
    public List<String> getRoles(String loginId) {
        return stpInterface.getRoleList(loginId, loginType);
    }
}
