package com.velox.framework.security.api.token;

import com.velox.framework.security.properties.SecurityProperties;
import org.springframework.util.StringUtils;

/**
 * token provider 抽象。
 * starter 通过该接口选择底层 token 运行时实现。
 */
public interface SecurityTokenProvider {

    /**
     * provider 标识，用于配置选择。
     */
    String provider();

    /**
     * 模式标识，例如 stateful / jwt / custom。
     */
    String mode();

    /**
     * 是否支持当前运行环境。
     */
    default boolean isAvailable() {
        return true;
    }

    /**
     * 是否为 starter 内置实现。
     * 当自定义实现与内置实现同时命中时，优先采用自定义实现。
     */
    default boolean isBuiltIn() {
        return false;
    }

    /**
     * 当前 provider 是否支持指定配置。
     */
    default boolean supports(SecurityProperties.Token tokenProperties) {
        if (tokenProperties == null) {
            return false;
        }
        return matches(tokenProperties.getMode(), mode())
                && matches(tokenProperties.getProvider(), provider());
    }

    /**
     * 应用 token 运行时所需配置。
     */
    void customize(SecurityTokenRuntime runtime);

    private boolean matches(String configuredValue, String actualValue) {
        if (!StringUtils.hasText(configuredValue)) {
            return true;
        }
        if (!StringUtils.hasText(actualValue)) {
            return false;
        }
        return configuredValue.trim().equalsIgnoreCase(actualValue.trim());
    }
}
