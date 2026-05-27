package com.velox.framework.persistence.spi.datasource;

import com.velox.framework.persistence.api.datasource.PersistenceContext;

import javax.sql.DataSource;

public interface PersistenceDataSourceFactory {

    DataSource createDataSource(PersistenceContext context);
}
