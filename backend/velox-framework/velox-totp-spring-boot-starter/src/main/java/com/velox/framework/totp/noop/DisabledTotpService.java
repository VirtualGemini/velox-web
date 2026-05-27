package com.velox.framework.totp.noop;

import com.velox.framework.totp.api.model.TotpProvisioning;
import com.velox.framework.totp.api.model.TotpSecret;
import com.velox.framework.totp.api.model.TotpVerifyResult;
import com.velox.framework.totp.api.service.TotpService;
import com.velox.framework.totp.common.error.TotpErrorCode;
import com.velox.framework.totp.common.message.TotpCommonMessages;
import com.velox.framework.totp.exception.VeloxTotpException;

/**
 * 占位实现，仅在 {@code velox.totp.enabled=false} 时注册，使依赖该能力的业务模块可以注入但优雅降级。
 */
public class DisabledTotpService implements TotpService {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public TotpSecret generateSecret() {
        throw new VeloxTotpException(TotpCommonMessages.TOTP_CAPABILITY_DISABLED);
    }

    @Override
    public String buildOtpAuthUri(TotpSecret secret, String accountName) {
        throw new VeloxTotpException(TotpCommonMessages.TOTP_CAPABILITY_DISABLED);
    }

    @Override
    public TotpProvisioning provision(String accountName) {
        throw new VeloxTotpException(TotpCommonMessages.TOTP_CAPABILITY_DISABLED);
    }

    @Override
    public String currentCode(TotpSecret secret) {
        throw new VeloxTotpException(TotpCommonMessages.TOTP_CAPABILITY_DISABLED);
    }

    @Override
    public TotpVerifyResult verify(TotpSecret secret, String code) {
        return TotpVerifyResult.failure(TotpErrorCode.DISABLED, TotpCommonMessages.TOTP_CAPABILITY_DISABLED);
    }

    @Override
    public TotpVerifyResult verify(String base32Secret, String code) {
        return TotpVerifyResult.failure(TotpErrorCode.DISABLED, TotpCommonMessages.TOTP_CAPABILITY_DISABLED);
    }
}
