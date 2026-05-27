package com.velox.module.system.id.frontend;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class SystemFrontendIdWebMvcConfig implements WebMvcConfigurer {

    private final SystemFrontendIdArgumentResolver argumentResolver;

    public SystemFrontendIdWebMvcConfig(SystemFrontendIdArgumentResolver argumentResolver) {
        this.argumentResolver = argumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(0, argumentResolver);
    }
}
