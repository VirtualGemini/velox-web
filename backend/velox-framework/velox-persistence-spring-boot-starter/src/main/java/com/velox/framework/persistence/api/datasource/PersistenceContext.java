package com.velox.framework.persistence.api.datasource;

import com.velox.framework.persistence.properties.VeloxPersistenceProperties;
import com.velox.framework.persistence.spi.dialect.DatabaseDialect;

public interface PersistenceContext {

    String type();

    DatabaseDialect dialect();

    VeloxPersistenceProperties properties();

    VeloxPersistenceProperties.DatabaseConnectionProperties connectionProperties();
}
