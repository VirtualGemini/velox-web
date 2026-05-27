package com.velox.framework.web.core.mvc;

import com.velox.framework.web.common.message.WebCommonMessages;
import com.velox.framework.web.common.path.WebPathConstants;
import com.velox.framework.web.exception.WebConfigException;
import com.velox.framework.web.properties.VeloxWebProperties;
import com.velox.framework.web.spi.mvc.VeloxWebMvcConfigurer;
import com.velox.framework.web.spi.path.ApiPathPrefixResolver;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

import java.util.List;

public class DefaultVeloxWebMvcConfigurer implements VeloxWebMvcConfigurer {

    private final VeloxWebProperties properties;
    private final ApiPathPrefixResolver apiPathPrefixResolver;
    private final HandlerInterceptor requestLogInterceptor;

    public DefaultVeloxWebMvcConfigurer(VeloxWebProperties properties,
                                        ApiPathPrefixResolver apiPathPrefixResolver,
                                        HandlerInterceptor requestLogInterceptor) {
        if (properties == null) {
            throw new WebConfigException(WebCommonMessages.WEB_PROPERTIES_MUST_NOT_BE_NULL);
        }
        if (apiPathPrefixResolver == null) {
            throw new WebConfigException(WebCommonMessages.API_PATH_PREFIX_RESOLVER_MUST_NOT_BE_NULL);
        }
        if (requestLogInterceptor == null) {
            throw new WebConfigException(WebCommonMessages.VELOX_REQUEST_LOG_INTERCEPTOR_MUST_NOT_BE_NULL);
        }
        this.properties = properties;
        this.apiPathPrefixResolver = apiPathPrefixResolver;
        this.requestLogInterceptor = requestLogInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        VeloxWebProperties.Cors cors = properties.getWeb().getCors();
        List<String> allowedOriginPatterns = cors.getAllowedOriginPatterns();
        if (allowedOriginPatterns == null || allowedOriginPatterns.isEmpty()) {
            return;
        }

        registry.addMapping(WebPathConstants.ALL_MVC_PATH_PATTERNS)
                .allowedOriginPatterns(allowedOriginPatterns.toArray(new String[0]))
                .allowedMethods(cors.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(cors.getAllowedHeaders().toArray(new String[0]))
                .exposedHeaders(cors.getExposedHeaders().toArray(new String[0]))
                .allowCredentials(cors.isAllowCredentials())
                .maxAge(cors.getMaxAge());
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        String resolvedPrefix = apiPathPrefixResolver.resolve(properties);
        if (!StringUtils.hasText(resolvedPrefix)) {
            return;
        }
        configurer.addPathPrefix(
                resolvedPrefix,
                HandlerTypePredicate.forBasePackage(WebPathConstants.CONTROLLER_BASE_PACKAGE)
                        .and(HandlerTypePredicate.forAnnotation(RestController.class))
        );
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLogInterceptor).addPathPatterns(WebPathConstants.ALL_MVC_PATH_PATTERNS);
    }
}
