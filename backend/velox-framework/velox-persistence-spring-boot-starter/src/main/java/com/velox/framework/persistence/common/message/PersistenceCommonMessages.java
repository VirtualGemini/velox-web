package com.velox.framework.persistence.common.message;

public final class PersistenceCommonMessages {

    public static final String DATABASE_TYPE_MUST_NOT_BE_BLANK = "velox.datasource.type must not be blank";
    public static final String DATASOURCE_CONFIGS_MUST_NOT_BE_EMPTY = "velox.datasource.configs must not be empty";
    public static final String DATASOURCE_CONFIG_MISSING = "Missing datasource config for database type: %s";
    public static final String UNSUPPORTED_DATABASE_TYPE = "Unsupported database type: %s";
    public static final String DUPLICATE_DATABASE_DIALECT = "Multiple database dialect beans found for type: %s";
    public static final String DATASOURCE_URL_MUST_NOT_BE_BLANK = "velox.datasource.configs.%s.url must not be blank";
    public static final String PERSISTENCE_PROPERTIES_MUST_NOT_BE_NULL = "persistenceProperties must not be null";
    public static final String DATABASE_DIALECT_MUST_NOT_BE_NULL = "databaseDialect must not be null";
    public static final String DATABASE_CONNECTION_PROPERTIES_MUST_NOT_BE_NULL = "databaseConnectionProperties must not be null";
    public static final String DATABASE_DIALECT_RESOLVER_MUST_NOT_BE_NULL = "databaseDialectResolver must not be null";
    public static final String PERSISTENCE_CONTEXT_MUST_NOT_BE_NULL = "persistenceContext must not be null";
    public static final String DATASOURCE_CUSTOMIZER_MUST_NOT_RETURN_NULL = "DataSourceCustomizer list must not contain null";
    public static final String HIKARI_DATASOURCE_CUSTOMIZER_MUST_NOT_RETURN_NULL = "HikariDataSourceCustomizer list must not contain null";
    public static final String UNSUPPORTED_MYBATIS_DB_TYPE = "Unsupported MyBatis Plus DbType for database type: %s";

    private PersistenceCommonMessages() {
    }
}
