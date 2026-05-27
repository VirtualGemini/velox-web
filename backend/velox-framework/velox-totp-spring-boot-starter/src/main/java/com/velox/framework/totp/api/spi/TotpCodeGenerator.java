package com.velox.framework.totp.api.spi;

import com.velox.framework.totp.api.model.TotpSecret;

public interface TotpCodeGenerator {

    String currentCode(TotpSecret secret);

    String codeAt(TotpSecret secret, long epochSecond);
}
