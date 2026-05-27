package com.velox.framework.persistence.spi.datasource;

import com.velox.framework.persistence.api.datasource.PersistenceContext;

import javax.sql.DataSource;

public interface DataSourceCustomizer {

    void customize(DataSource dataSource, PersistenceContext context);
}
