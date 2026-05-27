package com.velox.framework.id.common.type;

import java.util.Locale;

public final class IdGeneratorStrategies {

    public static final String SNOWFLAKE = "snowflake";
    public static final String DATABASE = "database";

    private IdGeneratorStrategies() {
    }

    public static String normalize(String strategy) {
        if (strategy == null) {
            return SNOWFLAKE;
        }
        return strategy.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isSupported(String strategy) {
        String normalized = normalize(strategy);
        return SNOWFLAKE.equals(normalized) || DATABASE.equals(normalized);
    }
}
