package com.velox.framework.security.support.session;

import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.framework.security.common.message.SecurityCommonMessages;
import com.velox.framework.security.exception.SecurityAuthenticationException;
import org.springframework.util.StringUtils;

public class SaTokenSecuritySessionService implements SecuritySessionService {

    @Override
    public String login(String loginId) {
        return login(loginId, null);
    }

    @Override
    public String login(String loginId, String sessionKey) {
        try {
            SaLoginModel loginModel = SaLoginModel.create();
            if (StringUtils.hasText(sessionKey)) {
                loginModel.setDevice(sessionKey.trim());
            }
            StpUtil.login(loginId, loginModel);
            return StpUtil.getTokenValue();
        } catch (SaTokenException exception) {
            throw new SecurityAuthenticationException(SecurityCommonMessages.SECURITY_LOGIN_FAILED, exception);
        }
    }

    @Override
    public void logout() {
        try {
            String tokenValue = StpUtil.getTokenValue();
            if (!StringUtils.hasText(tokenValue)) {
                return;
            }
            StpUtil.logoutByTokenValue(tokenValue);
        } catch (SaTokenException exception) {
            throw new SecurityAuthenticationException(SecurityCommonMessages.SECURITY_LOGOUT_FAILED, exception);
        }
    }

    @Override
    public boolean isAuthenticated() {
        return StpUtil.isLogin();
    }

    @Override
    public String currentLoginIdOrNull() {
        return StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : null;
    }

    @Override
    public String currentTokenOrNull() {
        return StpUtil.getTokenValue();
    }
}
