package com.velox.module.system.id.frontend;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.velox.module.system")
public class SystemFrontendIdResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final SystemFrontendIdReflectionTransformer transformer;

    public SystemFrontendIdResponseBodyAdvice(SystemFrontendIdReflectionTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        return transformer.encode(body);
    }
}
