package com.velox.module.system.id.generator;

import com.velox.framework.id.BusinessIdGenerator;
import org.springframework.stereotype.Component;

@Component
public class SystemEntityIdGenerator {

    private final BusinessIdGenerator businessIdGenerator;
    private final SystemBusinessTypeResolver businessTypeResolver;

    public SystemEntityIdGenerator(
            BusinessIdGenerator businessIdGenerator,
            SystemBusinessTypeResolver businessTypeResolver
    ) {
        this.businessIdGenerator = businessIdGenerator;
        this.businessTypeResolver = businessTypeResolver;
    }

    public String nextId(Class<?> entityClass) {
        return businessIdGenerator.next(businessTypeResolver.resolveEntityBusinessType(entityClass));
    }
}
