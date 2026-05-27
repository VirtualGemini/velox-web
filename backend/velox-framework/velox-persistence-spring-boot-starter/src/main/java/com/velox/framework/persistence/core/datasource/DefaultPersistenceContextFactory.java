package com.velox.framework.persistence.core.datasource;

import com.velox.framework.persistence.api.datasource.PersistenceContext;
import com.velox.framework.persistence.common.message.PersistenceCommonMessages;
import com.velox.framework.persistence.exception.PersistenceConfigException;
import com.velox.framework.persistence.properties.VeloxPersistenceProperties;
import com.velox.framework.persistence.spi.datasource.PersistenceContextFactory;
import com.velox.framework.persistence.spi.dialect.DatabaseDialect;
import com.velox.framework.persistence.spi.dialect.DatabaseDialectResolver;

public class DefaultPersistenceContextFactory implements PersistenceContextFactory {

    private final DatabaseDialectResolver databaseDialectResolver;

    public DefaultPersistenceContextFactory(DatabaseDialectResolver databaseDialectResolver) {
        if (databaseDialectResolver == null) {
            throw new PersistenceConfigException(PersistenceCommonMessages.DATABASE_DIALECT_RESOLVER_MUST_NOT_BE_NULL);
        }
        this.databaseDialectResolver = databaseDialectResolver;
    }

    @Override
    public PersistenceContext create(VeloxPersistenceProperties properties) {
        if (properties == null) {
            throw new PersistenceConfigException(PersistenceCommonMessages.PERSISTENCE_PROPERTIES_MUST_NOT_BE_NULL);
        }
        properties.validate();
        String type = properties.getNormalizedType();
        DatabaseDialect dialect = databaseDialectResolver.resolve(type);
        return new DefaultPersistenceContext(type, dialect, properties, properties.getActiveConfig());
    }
}
