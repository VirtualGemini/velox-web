package com.velox.framework.id.api.model;

public record IdGeneratorMetadata(
        boolean enabled,
        String sourceMode,
        String strategy,
        String frontendCodec,
        String databaseInitMode,
        Long workerId,
        Long datacenterId,
        Long twepoch,
        Long timeOffset,
        boolean systemClockEnabled
) {
}
