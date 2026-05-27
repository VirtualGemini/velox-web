package com.velox.framework.persistence.support.datasource;

import com.velox.framework.persistence.api.datasource.PersistenceContext;
import com.zaxxer.hikari.HikariDataSource;

public interface HikariDataSourceCustomizer {

    void customize(HikariDataSource dataSource, PersistenceContext context);
}
