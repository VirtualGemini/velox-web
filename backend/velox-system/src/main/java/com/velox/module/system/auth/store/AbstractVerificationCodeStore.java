package com.velox.module.system.auth.store;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.velox.module.system.auth.properties.SystemAuthProperties;

import java.nio.charset.StandardCharsets;

abstract class AbstractVerificationCodeStore implements VerificationCodeStore {

    protected static final String CAPTCHA_PREFIX = "auth:captcha:";
    protected static final String RESET_PREFIX = "auth:reset:";
    protected static final String RESET_SENT_PREFIX = "auth:reset:sent:";
    protected static final String LOGIN_CODE_PREFIX = "auth:login:";
    protected static final String LOGIN_CODE_SENT_PREFIX = "auth:login:sent:";
    protected static final String REBIND_PREFIX = "auth:rebind:";
    protected static final String REBIND_SENT_PREFIX = "auth:rebind:sent:";
    protected static final String MFA_CODE_PREFIX = "auth:mfa:email:";
    protected static final String MFA_CODE_SENT_PREFIX = "auth:mfa:email:sent:";
    protected static final String MFA_CHALLENGE_PREFIX = "auth:mfa:challenge:";
    protected static final String PROOF_TICKET_PREFIX = "auth:proof:";

    protected final SystemAuthProperties authProperties;

    protected AbstractVerificationCodeStore(SystemAuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    protected String digest(String code) {
        String secret = authProperties.getVerification().getSecret();
        HMac hMac = new HMac(HmacAlgorithm.HmacSHA256, secret.getBytes(StandardCharsets.UTF_8));
        return hMac.digestHex(code == null ? "" : code.trim().toLowerCase());
    }

    protected String rebindKey(String scope, String target) {
        return REBIND_PREFIX + scope + ":" + target;
    }

    protected String rebindSentKey(String scope, String target) {
        return REBIND_SENT_PREFIX + scope + ":" + target;
    }

    protected String proofTicketKey(String scene, String proofTicket) {
        return PROOF_TICKET_PREFIX + scene + ":" + proofTicket;
    }
}
