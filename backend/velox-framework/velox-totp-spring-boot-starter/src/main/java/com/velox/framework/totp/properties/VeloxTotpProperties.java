package com.velox.framework.totp.properties;

import com.velox.framework.totp.common.message.TotpCommonMessages;
import com.velox.framework.totp.common.prefix.TotpPropertyPrefixes;
import com.velox.framework.totp.exception.TotpConfigException;
import com.velox.framework.totp.support.type.TotpAlgorithm;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = TotpPropertyPrefixes.TOTP)
public class VeloxTotpProperties {

    private static final String DEFAULT_ISSUER = "Velox";
    private static final int DEFAULT_DIGITS = 6;
    private static final int DEFAULT_PERIOD_SECONDS = 30;
    private static final int DEFAULT_SECRET_SIZE_BYTES = 20;
    private static final int DEFAULT_VERIFY_WINDOW_STEPS = 1;
    private static final int MIN_DIGITS = 6;
    private static final int MAX_DIGITS = 10;
    private static final int MIN_PERIOD_SECONDS = 1;
    private static final int MIN_SECRET_SIZE_BYTES = 16;
    private static final int MIN_VERIFY_WINDOW_STEPS = 0;

    /**
     * 默认关闭。仅当业务模块明确启用 MFA-TOTP 时再打开。
     */
    private boolean enabled = false;

    /**
     * 二维码 / otpauth URI 中显示的发行方名称，建议为产品名或租户名。
     */
    private String issuer = DEFAULT_ISSUER;

    /**
     * 口令位数，所有目标客户端均兼容 6/7/8 位；常见值为 6。
     */
    @Min(MIN_DIGITS)
    @Max(MAX_DIGITS)
    private int digits = DEFAULT_DIGITS;

    /**
     * 时间步长，秒。RFC 6238 推荐 30 秒，所有目标客户端默认都按 30 秒解析。
     */
    @Min(MIN_PERIOD_SECONDS)
    private int periodSeconds = DEFAULT_PERIOD_SECONDS;

    /**
     * HMAC 算法。默认 SHA1 以兼容 Google / Microsoft / 腾讯等仅支持 SHA1 的客户端。
     */
    @NotNull
    private TotpAlgorithm algorithm = TotpAlgorithm.SHA1;

    /**
     * 生成新 secret 时的字节长度。RFC 4226 推荐 SHA1 ≥ 160 bit（20 字节），此处给 20 字节。
     */
    @Min(MIN_SECRET_SIZE_BYTES)
    private int secretSizeBytes = DEFAULT_SECRET_SIZE_BYTES;

    /**
     * 校验时允许的相邻时间步数：1 表示当前窗口 ± 1 步（±30 秒），有效补偿设备时钟漂移。
     */
    @Min(MIN_VERIFY_WINDOW_STEPS)
    private int verifyWindowSteps = DEFAULT_VERIFY_WINDOW_STEPS;

    @AssertTrue(message = TotpCommonMessages.ISSUER_MUST_NOT_BE_BLANK)
    public boolean isIssuerValidWhenEnabled() {
        return !enabled || (issuer != null && !issuer.isBlank());
    }

    public void validate() {
        if (digits < MIN_DIGITS || digits > MAX_DIGITS) {
            throw new TotpConfigException(TotpCommonMessages.DIGITS_OUT_OF_RANGE);
        }
        if (periodSeconds < MIN_PERIOD_SECONDS) {
            throw new TotpConfigException(TotpCommonMessages.PERIOD_OUT_OF_RANGE);
        }
        if (secretSizeBytes < MIN_SECRET_SIZE_BYTES) {
            throw new TotpConfigException(TotpCommonMessages.SECRET_SIZE_OUT_OF_RANGE);
        }
        if (verifyWindowSteps < MIN_VERIFY_WINDOW_STEPS) {
            throw new TotpConfigException(TotpCommonMessages.VERIFY_WINDOW_OUT_OF_RANGE);
        }
        if (algorithm == null) {
            throw new TotpConfigException(TotpCommonMessages.ALGORITHM_MUST_NOT_BE_NULL);
        }
        if (enabled && (issuer == null || issuer.isBlank())) {
            throw new TotpConfigException(TotpCommonMessages.ISSUER_MUST_NOT_BE_BLANK);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public int getDigits() {
        return digits;
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public int getPeriodSeconds() {
        return periodSeconds;
    }

    public void setPeriodSeconds(int periodSeconds) {
        this.periodSeconds = periodSeconds;
    }

    public TotpAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(TotpAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public int getSecretSizeBytes() {
        return secretSizeBytes;
    }

    public void setSecretSizeBytes(int secretSizeBytes) {
        this.secretSizeBytes = secretSizeBytes;
    }

    public int getVerifyWindowSteps() {
        return verifyWindowSteps;
    }

    public void setVerifyWindowSteps(int verifyWindowSteps) {
        this.verifyWindowSteps = verifyWindowSteps;
    }
}
