package com.velox.framework.persistence.support.dialect;

import com.velox.framework.persistence.spi.dialect.DatabaseDialect;

public class MySqlDatabaseDialect implements DatabaseDialect {

    private static final String DATABASE_TYPE = "mysql";
    private static final String DEFAULT_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

    @Override
    public String getType() {
        return DATABASE_TYPE;
    }

    @Override
    public String getDefaultDriverClassName() {
        return DEFAULT_DRIVER_CLASS_NAME;
    }
}
