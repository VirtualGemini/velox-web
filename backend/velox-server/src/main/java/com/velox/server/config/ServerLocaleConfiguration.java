package com.velox.server.config;

import com.velox.framework.web.core.locale.VeloxLocaleResolver;
import com.velox.framework.web.spi.locale.RequestLocaleResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

@Configuration(proxyBeanMethods = false)
public class ServerLocaleConfiguration {

    @Bean(name = DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
    @ConditionalOnMissingBean(name = DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
    public LocaleResolver localeResolver(RequestLocaleResolver requestLocaleResolver) {
        return new VeloxLocaleResolver(requestLocaleResolver);
    }
}
