package com.velox.framework.totp.api.spi;

import com.velox.framework.totp.api.model.TotpSecret;
import com.velox.framework.totp.api.model.TotpVerifyResult;

public interface TotpVerifier {

    TotpVerifyResult verify(TotpSecret secret, String code);
}
