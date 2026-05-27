package com.velox.framework.id.api.model;

public record GeneratedId(
        String businessType,
        String sourceValue,
        String encodedValue,
        IdGeneratorMetadata metadata
) {
}
