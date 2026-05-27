package com.velox.module.system.id.database.posture;

import com.velox.module.system.id.database.posture.model.DatabaseIdPosture;
public final class DatabaseIdPostureClassifier {

    private static final int MIN_SNOWFLAKE_LENGTH = 15;

    private DatabaseIdPostureClassifier() {
    }

    public static DatabaseIdPosture classify(String sampleValue) {
        String value = sampleValue == null ? "" : sampleValue.trim();
        if (value.isEmpty()) {
            return DatabaseIdPosture.EMPTY;
        }
        if (isNumeric(value)) {
            return isPlausibleSnowflake(value)
                    ? DatabaseIdPosture.SNOWFLAKE
                    : DatabaseIdPosture.DB_AUTO_INCREMENT;
        }
        return DatabaseIdPosture.UNKNOWN;
    }

    private static boolean isNumeric(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (!Character.isDigit(value.charAt(index))) {
                return false;
            }
        }
        return !value.isEmpty();
    }

    private static boolean isPlausibleSnowflake(String value) {
        if (value.length() < MIN_SNOWFLAKE_LENGTH) {
            return false;
        }
        try {
            long id = Long.parseLong(value);
            return id > 0 && (id >> 22) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
