package com.velox.framework.totp.api.spi;

import com.velox.framework.totp.api.model.TotpSecret;

public interface TotpSecretGenerator {

    TotpSecret generateSecret();
}
