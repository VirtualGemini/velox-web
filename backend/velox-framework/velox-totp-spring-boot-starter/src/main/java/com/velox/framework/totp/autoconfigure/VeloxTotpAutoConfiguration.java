package com.velox.framework.totp.autoconfigure;

import com.velox.framework.totp.api.service.TotpService;
import com.velox.framework.totp.api.spi.TotpCodeGenerator;
import com.velox.framework.totp.api.spi.TotpSecretGenerator;
import com.velox.framework.totp.api.spi.TotpUriBuilder;
import com.velox.framework.totp.api.spi.TotpVerifier;
import com.velox.framework.totp.common.prefix.TotpPropertyPrefixes;
import com.velox.framework.totp.core.DefaultComposableTotpService;
import com.velox.framework.totp.core.DefaultTotpCodeGenerator;
import com.velox.framework.totp.core.DefaultTotpSecretGenerator;
import com.velox.framework.totp.core.DefaultTotpUriBuilder;
import com.velox.framework.totp.core.DefaultTotpVerifier;
import com.velox.framework.totp.noop.DisabledTotpService;
import com.velox.framework.totp.properties.VeloxTotpProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@EnableConfigurationProperties(VeloxTotpProperties.class)
public class VeloxTotpAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = TotpPropertyPrefixes.TOTP, name = TotpPropertyPrefixes.ENABLED, havingValue = TotpPropertyPrefixes.FALSE, matchIfMissing = true)
    @ConditionalOnMissingBean(TotpService.class)
    public TotpService disabledTotpService() {
        return new DisabledTotpService();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = TotpPropertyPrefixes.TOTP, name = TotpPropertyPrefixes.ENABLED, havingValue = TotpPropertyPrefixes.TRUE)
    @ConditionalOnMissingBean(TotpService.class)
    static class EnabledTotpConfiguration {

        @Bean
        @ConditionalOnMissingBean(TotpSecretGenerator.class)
        public TotpSecretGenerator totpSecretGenerator(VeloxTotpProperties properties) {
            return new DefaultTotpSecretGenerator(properties);
        }

        @Bean
        @ConditionalOnMissingBean(TotpCodeGenerator.class)
        public TotpCodeGenerator totpCodeGenerator() {
            return new DefaultTotpCodeGenerator();
        }

        @Bean
        @ConditionalOnMissingBean(TotpUriBuilder.class)
        public TotpUriBuilder totpUriBuilder(VeloxTotpProperties properties) {
            return new DefaultTotpUriBuilder(properties);
        }

        @Bean
        @ConditionalOnMissingBean(TotpVerifier.class)
        public TotpVerifier totpVerifier(VeloxTotpProperties properties, TotpCodeGenerator codeGenerator) {
            return new DefaultTotpVerifier(properties, codeGenerator);
        }

        @Bean
        public TotpService totpService(VeloxTotpProperties properties,
                                       TotpSecretGenerator secretGenerator,
                                       TotpCodeGenerator codeGenerator,
                                       TotpUriBuilder uriBuilder,
                                       TotpVerifier verifier) {
            return new DefaultComposableTotpService(properties, secretGenerator, codeGenerator, uriBuilder, verifier);
        }
    }
}
