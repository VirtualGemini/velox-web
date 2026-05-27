package com.velox.framework.persistence.core.datasource;

import com.velox.framework.persistence.api.datasource.PersistenceContext;
import com.velox.framework.persistence.common.message.PersistenceCommonMessages;
import com.velox.framework.persistence.exception.PersistenceConfigException;
import com.velox.framework.persistence.properties.VeloxPersistenceProperties;
import com.velox.framework.persistence.spi.dialect.DatabaseDialect;

public record DefaultPersistenceContext(
        String type,
        DatabaseDialect dialect,
        VeloxPersistenceProperties properties,
        VeloxPersistenceProperties.DatabaseConnectionProperties connectionProperties
) implements PersistenceContext {

    public DefaultPersistenceContext {
        if (type == null || type.isBlank()) {
            throw new PersistenceConfigException(PersistenceCommonMessages.DATABASE_TYPE_MUST_NOT_BE_BLANK);
        }
        if (dialect == null) {
            throw new PersistenceConfigException(PersistenceCommonMessages.DATABASE_DIALECT_MUST_NOT_BE_NULL);
        }
        if (properties == null) {
            throw new PersistenceConfigException(PersistenceCommonMessages.PERSISTENCE_PROPERTIES_MUST_NOT_BE_NULL);
        }
        if (connectionProperties == null) {
            throw new PersistenceConfigException(PersistenceCommonMessages.DATABASE_CONNECTION_PROPERTIES_MUST_NOT_BE_NULL);
        }
    }
}
