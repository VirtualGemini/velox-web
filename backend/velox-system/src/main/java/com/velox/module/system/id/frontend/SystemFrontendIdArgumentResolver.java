package com.velox.module.system.id.frontend;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class SystemFrontendIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final SystemFrontendIdCodecSupport codecSupport;

    public SystemFrontendIdArgumentResolver(SystemFrontendIdCodecSupport codecSupport) {
        this.codecSupport = codecSupport;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (!codecSupport.isEnabled()) {
            return false;
        }
        String parameterName = resolveParameterName(parameter);
        if (!codecSupport.isIdentifierName(parameterName)) {
            return false;
        }
        Class<?> parameterType = parameter.getParameterType();
        return parameterType == String.class
                || parameterType == String[].class
                || Collection.class.isAssignableFrom(parameterType);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable org.springframework.web.bind.support.WebDataBinderFactory binderFactory
    ) throws Exception {
        String parameterName = resolveParameterName(parameter);
        if (parameter.getParameterType() == String.class) {
            return codecSupport.decodeIdentifier(resolveSingleValue(parameter, parameterName, webRequest));
        }
        List<String> values = resolveValues(parameter, parameterName, webRequest);
        List<String> decoded = codecSupport.decodeIdentifiers(values);
        if (parameter.getParameterType() == String[].class) {
            return decoded.toArray(String[]::new);
        }
        return decoded;
    }

    private String resolveSingleValue(MethodParameter parameter, String parameterName, NativeWebRequest webRequest)
            throws ServletRequestBindingException {
        List<String> values = resolveValues(parameter, parameterName, webRequest);
        if (values.isEmpty()) {
            if (isRequired(parameter)) {
                throw new ServletRequestBindingException("Missing required id parameter: " + parameterName);
            }
            return null;
        }
        return values.get(0);
    }

    private List<String> resolveValues(MethodParameter parameter, String parameterName, NativeWebRequest webRequest)
            throws ServletRequestBindingException {
        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if (request == null) {
                return List.of();
            }
            Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (!(attribute instanceof Map<?, ?> variables)) {
                return List.of();
            }
            Object value = variables.get(parameterName);
            if (value == null) {
                if (isRequired(parameter)) {
                    throw new ServletRequestBindingException("Missing required path variable: " + parameterName);
                }
                return List.of();
            }
            return List.of(String.valueOf(value));
        }

        String[] parameterValues = webRequest.getParameterValues(parameterName);
        if (parameterValues == null || parameterValues.length == 0) {
            if (isRequired(parameter)) {
                throw new ServletRequestBindingException("Missing required request parameter: " + parameterName);
            }
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (String parameterValue : parameterValues) {
            if (parameterValue == null) {
                continue;
            }
            if (parameter.getParameterType() == String.class) {
                values.add(parameterValue);
                continue;
            }
            values.addAll(Arrays.stream(parameterValue.split(","))
                    .map(String::trim)
                    .filter(token -> !token.isEmpty())
                    .toList());
        }
        return values;
    }

    private boolean isRequired(MethodParameter parameter) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            return requestParam.required();
        }
        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        return pathVariable == null || pathVariable.required();
    }

    private String resolveParameterName(MethodParameter parameter) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            if (!requestParam.name().isBlank()) {
                return requestParam.name();
            }
            if (!requestParam.value().isBlank()) {
                return requestParam.value();
            }
        }
        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            if (!pathVariable.name().isBlank()) {
                return pathVariable.name();
            }
            if (!pathVariable.value().isBlank()) {
                return pathVariable.value();
            }
        }
        return parameter.getParameterName();
    }
}
