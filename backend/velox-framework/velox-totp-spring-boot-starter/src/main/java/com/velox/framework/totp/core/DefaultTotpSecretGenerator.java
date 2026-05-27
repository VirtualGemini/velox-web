package com.velox.framework.totp.core;

import com.velox.framework.totp.api.model.TotpSecret;
import com.velox.framework.totp.api.spi.TotpSecretGenerator;
import com.velox.framework.totp.properties.VeloxTotpProperties;
import com.velox.framework.totp.support.codec.Base32Codec;

import java.security.SecureRandom;

public class DefaultTotpSecretGenerator implements TotpSecretGenerator {

    private final VeloxTotpProperties properties;
    private final SecureRandom random;

    public DefaultTotpSecretGenerator(VeloxTotpProperties properties) {
        this(properties, new SecureRandom());
    }

    public DefaultTotpSecretGenerator(VeloxTotpProperties properties, SecureRandom random) {
        this.properties = requireProperties(properties);
        this.random = requireRandom(random);
    }

    @Override
    public TotpSecret generateSecret() {
        byte[] bytes = new byte[properties.getSecretSizeBytes()];
        random.nextBytes(bytes);
        return new TotpSecret(
                Base32Codec.encode(bytes),
                properties.getAlgorithm(),
                properties.getDigits(),
                properties.getPeriodSeconds()
        );
    }

    private VeloxTotpProperties requireProperties(VeloxTotpProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        properties.validate();
        return properties;
    }

    private SecureRandom requireRandom(SecureRandom random) {
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }
        return random;
    }
}
