package com.velox.framework.id.common.message;

public final class IdGeneratorCommonMessages {

    public static final String ID_GENERATOR_DISABLED =
            "Velox id capability is disabled and no database-backed generator is available; enable velox.id.snowflake.enabled or provide a custom engine bean";
    public static final String DB_ID_OPERATOR_REQUIRED =
            "Velox id capability is configured for database-default mode but no database id operator is available";
    public static final String ENCODED_ID_MUST_NOT_BE_BLANK = "Encoded id must not be blank";
    public static final String SOURCE_ID_MUST_NOT_BE_BLANK = "Source id must not be blank";
    public static final String RAW_ID_MUST_NOT_BE_NEGATIVE = "Raw id must be greater than or equal to 0";
    public static final String ENCODED_ID_EXCEEDS_LONG_RANGE = "Encoded id exceeds Long range";
    public static final String SOURCE_ID_MUST_BE_NUMERIC = "Source id must be a numeric string";
    public static final String SOURCE_ID_EXCEEDS_LONG_RANGE = "Source id exceeds Long range";
    public static final String RAW_ID_NOT_AVAILABLE_FOR_CURRENT_STRATEGY =
            "Raw numeric id is only available when the current strategy produces numeric source values";
    public static final String UNSUPPORTED_DATABASE_PRODUCT =
            "Velox id database mode supports only MySQL and PostgreSQL";
    public static final String DATABASE_ID_NOT_INITIALIZED =
            "Velox id database objects are not initialized; set velox.id.database.init-mode=update or initialize the database manually";
    public static final String DATABASE_ID_METADATA_READ_FAILED =
            "Failed to read velox id database metadata";
    public static final String DATABASE_ID_INITIALIZATION_FAILED =
            "Failed to initialize velox id database objects";
    public static final String DATABASE_ID_ISSUE_FAILED =
            "Failed to issue id from database-default strategy";
    public static final String DATABASE_ID_COLUMN_DISCOVERY_FAILED =
            "Failed to read id column metadata from database";
    public static final String DATABASE_ID_COLUMN_NOT_FOUND =
            "Managed id column metadata was not found in database";
    public static final String DATABASE_ID_POSTURE_READ_FAILED =
            "Failed to inspect database id posture";
    public static final String DATABASE_ID_PROD_MISMATCH =
            "Database id posture does not match the current production YAML configuration";
    public static final String DATABASE_ID_UNSUPPORTED_INIT_MODE =
            "Unsupported velox.id.database.init-mode value";
    public static final String ID_STRATEGY_UNSUPPORTED =
            "Unsupported velox.id.strategy value";
    private static final String INVALID_BASE62_CHARACTER_TEMPLATE =
            "Encoded id contains unsupported Base62 character: %s";
    private static final String SNOWFLAKE_WORKER_ID_OUT_OF_RANGE_TEMPLATE =
            "Snowflake workerId must be between 0 and %d";
    private static final String SNOWFLAKE_DATACENTER_ID_OUT_OF_RANGE_TEMPLATE =
            "Snowflake datacenterId must be between 0 and %d";
    private static final String UNKNOWN_DATABASE_PRODUCT_TEMPLATE =
            "Unsupported database product for velox id database mode: %s";
    private static final String UNKNOWN_ID_STRATEGY_TEMPLATE =
            "Unsupported velox.id.strategy value: %s";
    private static final String UNKNOWN_DATABASE_INIT_MODE_TEMPLATE =
            "Unsupported velox.id.database.init-mode value: %s";
    private static final String ID_COLUMN_METADATA_NOT_FOUND_TEMPLATE =
            "Managed id column metadata not found for %s.%s";
    private static final String PROD_DATABASE_ID_MISMATCH_TEMPLATE =
            "Production startup rejected: YAML expects %s ids but database posture is %s%s";
    public static final String SNOWFLAKE_TWEPOCH_MUST_BE_POSITIVE = "Snowflake twepoch must be greater than 0";
    public static final String SNOWFLAKE_TIME_OFFSET_MUST_NOT_BE_NEGATIVE =
            "Snowflake timeOffset must be greater than or equal to 0";

    private IdGeneratorCommonMessages() {
    }

    public static String invalidBase62Character(char value) {
        return INVALID_BASE62_CHARACTER_TEMPLATE.formatted(value);
    }

    public static String snowflakeWorkerIdOutOfRange(long maxWorkerId) {
        return SNOWFLAKE_WORKER_ID_OUT_OF_RANGE_TEMPLATE.formatted(maxWorkerId);
    }

    public static String snowflakeDatacenterIdOutOfRange(long maxDatacenterId) {
        return SNOWFLAKE_DATACENTER_ID_OUT_OF_RANGE_TEMPLATE.formatted(maxDatacenterId);
    }

    public static String unsupportedDatabaseProduct(String databaseProduct) {
        return UNKNOWN_DATABASE_PRODUCT_TEMPLATE.formatted(databaseProduct);
    }

    public static String unsupportedIdStrategy(String strategy) {
        return UNKNOWN_ID_STRATEGY_TEMPLATE.formatted(strategy);
    }

    public static String unsupportedDatabaseInitMode(String initMode) {
        return UNKNOWN_DATABASE_INIT_MODE_TEMPLATE.formatted(initMode);
    }

    public static String missingIdColumnMetadata(String tableName, String columnName) {
        return ID_COLUMN_METADATA_NOT_FOUND_TEMPLATE.formatted(tableName, columnName);
    }

    public static String prodDatabaseIdMismatch(String expectedMode, String actualMode, String sampleDetail) {
        return PROD_DATABASE_ID_MISMATCH_TEMPLATE.formatted(expectedMode, actualMode, sampleDetail);
    }
}
