package com.velox.framework.persistence.spi.datasource;

import com.velox.framework.persistence.api.datasource.PersistenceContext;
import com.velox.framework.persistence.properties.VeloxPersistenceProperties;

public interface PersistenceContextFactory {

    PersistenceContext create(VeloxPersistenceProperties properties);
}
