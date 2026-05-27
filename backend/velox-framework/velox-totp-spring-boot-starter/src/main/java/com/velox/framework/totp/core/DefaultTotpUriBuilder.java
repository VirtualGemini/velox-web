package com.velox.framework.totp.core;

import com.velox.framework.totp.api.model.TotpSecret;
import com.velox.framework.totp.api.spi.TotpUriBuilder;
import com.velox.framework.totp.properties.VeloxTotpProperties;
import com.velox.framework.totp.support.uri.OtpAuthUris;

public class DefaultTotpUriBuilder implements TotpUriBuilder {

    private final VeloxTotpProperties properties;

    public DefaultTotpUriBuilder(VeloxTotpProperties properties) {
        this.properties = requireProperties(properties);
    }

    @Override
    public String buildOtpAuthUri(TotpSecret secret, String accountName) {
        TotpSecrets.requireValidForGeneration(secret);
        return OtpAuthUris.buildTotp(
                properties.getIssuer(),
                accountName,
                secret.base32(),
                secret.algorithm(),
                secret.digits(),
                secret.periodSeconds()
        );
    }

    private VeloxTotpProperties requireProperties(VeloxTotpProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        properties.validate();
        return properties;
    }
}
