package com.velox.framework.totp.api.spi;

import com.velox.framework.totp.api.model.TotpSecret;

public interface TotpUriBuilder {

    String buildOtpAuthUri(TotpSecret secret, String accountName);
}
