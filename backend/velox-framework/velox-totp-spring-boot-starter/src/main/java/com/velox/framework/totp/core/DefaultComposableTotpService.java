package com.velox.framework.totp.core;

import com.velox.framework.totp.api.model.TotpProvisioning;
import com.velox.framework.totp.api.model.TotpSecret;
import com.velox.framework.totp.api.model.TotpVerifyResult;
import com.velox.framework.totp.api.service.TotpService;
import com.velox.framework.totp.api.spi.TotpCodeGenerator;
import com.velox.framework.totp.api.spi.TotpSecretGenerator;
import com.velox.framework.totp.api.spi.TotpUriBuilder;
import com.velox.framework.totp.api.spi.TotpVerifier;
import com.velox.framework.totp.properties.VeloxTotpProperties;

public class DefaultComposableTotpService implements TotpService {

    private final VeloxTotpProperties properties;
    private final TotpSecretGenerator secretGenerator;
    private final TotpCodeGenerator codeGenerator;
    private final TotpUriBuilder uriBuilder;
    private final TotpVerifier verifier;

    public DefaultComposableTotpService(VeloxTotpProperties properties,
                                        TotpSecretGenerator secretGenerator,
                                        TotpCodeGenerator codeGenerator,
                                        TotpUriBuilder uriBuilder,
                                        TotpVerifier verifier) {
        this.properties = requireProperties(properties);
        this.secretGenerator = requireSecretGenerator(secretGenerator);
        this.codeGenerator = requireCodeGenerator(codeGenerator);
        this.uriBuilder = requireUriBuilder(uriBuilder);
        this.verifier = requireVerifier(verifier);
    }

    @Override
    public boolean isEnabled() {
        return properties.isEnabled();
    }

    @Override
    public TotpSecret generateSecret() {
        return secretGenerator.generateSecret();
    }

    @Override
    public String buildOtpAuthUri(TotpSecret secret, String accountName) {
        return uriBuilder.buildOtpAuthUri(secret, accountName);
    }

    @Override
    public TotpProvisioning provision(String accountName) {
        TotpSecret secret = generateSecret();
        return new TotpProvisioning(secret, buildOtpAuthUri(secret, accountName), properties.getIssuer(), accountName);
    }

    @Override
    public String currentCode(TotpSecret secret) {
        return codeGenerator.currentCode(secret);
    }

    @Override
    public TotpVerifyResult verify(TotpSecret secret, String code) {
        return verifier.verify(secret, code);
    }

    @Override
    public TotpVerifyResult verify(String base32Secret, String code) {
        return verifier.verify(new TotpSecret(
                base32Secret,
                properties.getAlgorithm(),
                properties.getDigits(),
                properties.getPeriodSeconds()
        ), code);
    }

    private VeloxTotpProperties requireProperties(VeloxTotpProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        properties.validate();
        return properties;
    }

    private TotpSecretGenerator requireSecretGenerator(TotpSecretGenerator secretGenerator) {
        if (secretGenerator == null) {
            throw new IllegalArgumentException("secretGenerator must not be null");
        }
        return secretGenerator;
    }

    private TotpCodeGenerator requireCodeGenerator(TotpCodeGenerator codeGenerator) {
        if (codeGenerator == null) {
            throw new IllegalArgumentException("codeGenerator must not be null");
        }
        return codeGenerator;
    }

    private TotpUriBuilder requireUriBuilder(TotpUriBuilder uriBuilder) {
        if (uriBuilder == null) {
            throw new IllegalArgumentException("uriBuilder must not be null");
        }
        return uriBuilder;
    }

    private TotpVerifier requireVerifier(TotpVerifier verifier) {
        if (verifier == null) {
            throw new IllegalArgumentException("verifier must not be null");
        }
        return verifier;
    }
}
