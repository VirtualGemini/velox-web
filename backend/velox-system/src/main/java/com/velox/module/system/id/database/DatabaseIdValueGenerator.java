package com.velox.module.system.id.database;

import cn.hutool.core.lang.Snowflake;
import com.velox.framework.id.exception.VeloxIdGeneratorException;
import com.velox.framework.id.properties.VeloxIdProperties;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.velox.module.system.id.database.posture.DatabaseIdPostureClassifier;
import com.velox.module.system.id.database.posture.model.DatabaseIdPosture;

public class DatabaseIdValueGenerator {

    private static final int MAX_ATTEMPTS = 1024;

    private final VeloxIdProperties properties;
    private final Snowflake snowflake;

    public DatabaseIdValueGenerator(VeloxIdProperties properties) {
        this.properties = properties;
        if (properties.isEnabled()) {
            VeloxIdProperties.SnowflakeProperties snowflakeProperties = properties.getSnowflake();
            this.snowflake = new Snowflake(
                    new Date(snowflakeProperties.getTwepoch()),
                    properties.getWorkerId(),
                    properties.getDatacenterId(),
                    snowflakeProperties.isUseSystemClock(),
                    snowflakeProperties.getTimeOffset()
            );
            return;
        }
        this.snowflake = null;
    }

    public DatabaseIdPosture targetPosture() {
        return properties.isEnabled() ? DatabaseIdPosture.SNOWFLAKE : DatabaseIdPosture.DB_AUTO_INCREMENT;
    }

    public boolean isTargetPosture(String value) {
        return DatabaseIdPostureClassifier.classify(value) == targetPosture();
    }

    public String resolveTargetId(String currentValue, Set<String> reservedValues) {
        if (isTargetPosture(currentValue)) {
            return currentValue;
        }
        return nextId(reservedValues);
    }

    public String nextId(Set<String> reservedValues) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = nextCandidate(reservedValues);
            if (reservedValues.add(candidate)) {
                return candidate;
            }
        }
        throw new VeloxIdGeneratorException("Failed to generate a unique id during database reconciliation");
    }

    public long decodeSnowflakeId(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new VeloxIdGeneratorException("Snowflake id value must not be blank");
        }
        if (DatabaseIdPostureClassifier.classify(normalized) == DatabaseIdPosture.SNOWFLAKE) {
            return Long.parseLong(normalized);
        }
        throw new VeloxIdGeneratorException("Snowflake id value must be a valid snowflake string");
    }

    private String nextCandidate(Set<String> reservedValues) {
        DatabaseIdPosture target = targetPosture();
        if (target == DatabaseIdPosture.SNOWFLAKE) {
            if (snowflake == null) {
                throw new VeloxIdGeneratorException("Snowflake generator is not initialized");
            }
            return String.valueOf(snowflake.nextId());
        }
        return nextSequenceString(reservedValues);
    }

    private String nextSequenceString(Set<String> reservedValues) {
        long max = 0L;
        for (String value : new LinkedHashSet<>(reservedValues)) {
            if (DatabaseIdPostureClassifier.classify(value) == DatabaseIdPosture.DB_AUTO_INCREMENT) {
                max = Math.max(max, Long.parseLong(value));
            }
        }
        return String.valueOf(max + 1);
    }
}
