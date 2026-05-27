package com.velox.framework.web.autoconfigure;

import com.velox.framework.web.support.servlet.RequestLogInterceptor;
import com.velox.framework.web.common.path.WebPathConstants;
import com.velox.framework.web.common.servlet.WebFilterNames;
import com.velox.framework.web.core.logging.DefaultRequestLogHandler;
import com.velox.framework.web.core.mvc.DefaultVeloxWebMvcConfigurer;
import com.velox.framework.web.core.path.DefaultApiPathPrefixResolver;
import com.velox.framework.web.core.locale.DefaultRequestLocaleResolver;
import com.velox.framework.web.core.servlet.DefaultRequestLocaleFilterRegistrationCustomizer;
import com.velox.framework.web.core.servlet.DefaultRequestTimeZoneFilterRegistrationCustomizer;
import com.velox.framework.web.core.servlet.DefaultTraceIdFilterRegistrationCustomizer;
import com.velox.framework.web.core.timezone.DefaultRequestTimeZoneResolver;
import com.velox.framework.web.core.tracing.DefaultTraceIdResolver;
import com.velox.framework.web.properties.VeloxProperties;
import com.velox.framework.web.properties.VeloxWebProperties;
import com.velox.framework.web.spi.locale.RequestLocaleResolver;
import com.velox.framework.web.spi.logging.RequestLogHandler;
import com.velox.framework.web.spi.mvc.VeloxWebMvcConfigurer;
import com.velox.framework.web.spi.path.ApiPathPrefixResolver;
import com.velox.framework.web.spi.servlet.RequestLocaleFilterRegistrationCustomizer;
import com.velox.framework.web.spi.servlet.RequestTimeZoneFilterRegistrationCustomizer;
import com.velox.framework.web.spi.timezone.RequestTimeZoneResolver;
import com.velox.framework.web.spi.servlet.TraceIdFilterRegistrationCustomizer;
import com.velox.framework.web.spi.tracing.TraceIdResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

@AutoConfiguration(before = WebMvcAutoConfiguration.class)
@EnableConfigurationProperties(VeloxProperties.class)
public class VeloxWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApiPathPrefixResolver apiPathPrefixResolver() {
        return new DefaultApiPathPrefixResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestTimeZoneResolver requestTimeZoneResolver() {
        return new DefaultRequestTimeZoneResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestLocaleResolver requestLocaleResolver() {
        return new DefaultRequestLocaleResolver();
    }

    @Bean(name = "localeResolver")
    @ConditionalOnMissingBean(name = DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME)
    public LocaleResolver localeResolver(RequestLocaleResolver requestLocaleResolver) {
        return new com.velox.framework.web.core.locale.VeloxLocaleResolver(requestLocaleResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceIdResolver traceIdResolver() {
        return new DefaultTraceIdResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestLogHandler requestLogHandler() {
        return new DefaultRequestLogHandler();
    }

    @Bean
    @ConditionalOnMissingBean(RequestLogInterceptor.class)
    public RequestLogInterceptor veloxRequestLogInterceptor(RequestLogHandler requestLogHandler) {
        return new RequestLogInterceptor(requestLogHandler);
    }

    @Bean
    @ConditionalOnMissingBean(VeloxWebMvcConfigurer.class)
    public VeloxWebMvcConfigurer veloxWebMvcConfigurer(VeloxWebProperties properties,
                                                       ApiPathPrefixResolver apiPathPrefixResolver,
                                                       RequestLogInterceptor requestLogInterceptor) {
        return new DefaultVeloxWebMvcConfigurer(properties, apiPathPrefixResolver, requestLogInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean(com.velox.framework.web.TraceIdFilter.class)
    public com.velox.framework.web.TraceIdFilter veloxTraceIdFilter(TraceIdResolver traceIdResolver) {
        return new com.velox.framework.web.TraceIdFilter(traceIdResolver);
    }

    @Bean
    @ConditionalOnMissingBean(TraceIdFilterRegistrationCustomizer.class)
    public TraceIdFilterRegistrationCustomizer traceIdFilterRegistrationCustomizer() {
        return new DefaultTraceIdFilterRegistrationCustomizer();
    }

    @Bean
    @ConditionalOnMissingBean(name = "traceIdFilterRegistration")
    public FilterRegistrationBean<jakarta.servlet.Filter> traceIdFilterRegistration(
            com.velox.framework.web.TraceIdFilter traceIdFilter,
            TraceIdFilterRegistrationCustomizer registrationCustomizer) {
        return registrationCustomizer.register(traceIdFilter);
    }

    @Bean
    @ConditionalOnMissingBean(com.velox.framework.web.RequestTimeZoneFilter.class)
    public com.velox.framework.web.RequestTimeZoneFilter veloxRequestTimeZoneFilter(
            RequestTimeZoneResolver requestTimeZoneResolver) {
        return new com.velox.framework.web.RequestTimeZoneFilter(requestTimeZoneResolver);
    }

    @Bean
    @ConditionalOnMissingBean(RequestTimeZoneFilterRegistrationCustomizer.class)
    public RequestTimeZoneFilterRegistrationCustomizer requestTimeZoneFilterRegistrationCustomizer() {
        return new DefaultRequestTimeZoneFilterRegistrationCustomizer();
    }

    @Bean
    @ConditionalOnMissingBean(name = "requestTimeZoneFilterRegistration")
    public FilterRegistrationBean<jakarta.servlet.Filter> requestTimeZoneFilterRegistration(
            com.velox.framework.web.RequestTimeZoneFilter requestTimeZoneFilter,
            RequestTimeZoneFilterRegistrationCustomizer registrationCustomizer) {
        return registrationCustomizer.register(requestTimeZoneFilter);
    }

    @Bean
    @ConditionalOnMissingBean(com.velox.framework.web.RequestLocaleFilter.class)
    public com.velox.framework.web.RequestLocaleFilter veloxRequestLocaleFilter(
            RequestLocaleResolver requestLocaleResolver) {
        return new com.velox.framework.web.RequestLocaleFilter(requestLocaleResolver);
    }

    @Bean
    @ConditionalOnMissingBean(RequestLocaleFilterRegistrationCustomizer.class)
    public RequestLocaleFilterRegistrationCustomizer requestLocaleFilterRegistrationCustomizer() {
        return new DefaultRequestLocaleFilterRegistrationCustomizer();
    }

    @Bean
    @ConditionalOnMissingBean(name = "requestLocaleFilterRegistration")
    public FilterRegistrationBean<jakarta.servlet.Filter> requestLocaleFilterRegistration(
            com.velox.framework.web.RequestLocaleFilter requestLocaleFilter,
            RequestLocaleFilterRegistrationCustomizer registrationCustomizer) {
        return registrationCustomizer.register(requestLocaleFilter);
    }
}
