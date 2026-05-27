package com.velox.framework.persistence.support.dialect;

import com.velox.framework.persistence.spi.dialect.DatabaseDialect;

public class PostgreSqlDatabaseDialect implements DatabaseDialect {

    private static final String DATABASE_TYPE = "postgresql";
    private static final String DEFAULT_DRIVER_CLASS_NAME = "org.postgresql.Driver";

    @Override
    public String getType() {
        return DATABASE_TYPE;
    }

    @Override
    public String getDefaultDriverClassName() {
        return DEFAULT_DRIVER_CLASS_NAME;
    }
}
