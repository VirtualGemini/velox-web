package com.velox.module.system.id.frontend;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;

@RestControllerAdvice(basePackages = "com.velox.module.system")
public class SystemFrontendIdRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private final SystemFrontendIdReflectionTransformer transformer;

    public SystemFrontendIdRequestBodyAdvice(SystemFrontendIdReflectionTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return transformer.decode(body);
    }
}
