package com.velox.framework.persistence.support.datasource;

import com.velox.framework.persistence.api.datasource.PersistenceContext;
import com.velox.framework.persistence.common.message.PersistenceCommonMessages;
import com.velox.framework.persistence.exception.PersistenceConfigException;
import com.velox.framework.persistence.properties.VeloxPersistenceProperties;
import com.velox.framework.persistence.spi.datasource.DataSourceCustomizer;
import com.velox.framework.persistence.spi.datasource.PersistenceDataSourceFactory;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.List;

public class HikariPersistenceDataSourceFactory implements PersistenceDataSourceFactory {

    private final List<DataSourceCustomizer> dataSourceCustomizers;
    private final List<HikariDataSourceCustomizer> hikariDataSourceCustomizers;
    private static final String HIKARI_POOL_NAME_PREFIX = "velox-";

    public HikariPersistenceDataSourceFactory(List<DataSourceCustomizer> dataSourceCustomizers,
                                              List<HikariDataSourceCustomizer> hikariDataSourceCustomizers) {
        this.dataSourceCustomizers = List.copyOf(dataSourceCustomizers);
        this.hikariDataSourceCustomizers = List.copyOf(hikariDataSourceCustomizers);
        for (DataSourceCustomizer customizer : this.dataSourceCustomizers) {
            if (customizer == null) {
                throw new PersistenceConfigException(PersistenceCommonMessages.DATASOURCE_CUSTOMIZER_MUST_NOT_RETURN_NULL);
            }
        }
        for (HikariDataSourceCustomizer customizer : this.hikariDataSourceCustomizers) {
            if (customizer == null) {
                throw new PersistenceConfigException(PersistenceCommonMessages.HIKARI_DATASOURCE_CUSTOMIZER_MUST_NOT_RETURN_NULL);
            }
        }
    }

    @Override
    public DataSource createDataSource(PersistenceContext context) {
        if (context == null) {
            throw new PersistenceConfigException(PersistenceCommonMessages.PERSISTENCE_CONTEXT_MUST_NOT_BE_NULL);
        }
        VeloxPersistenceProperties.DatabaseConnectionProperties config = context.connectionProperties();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(config.getResolvedDriverClassName(context.dialect()));
        dataSource.setJdbcUrl(config.getUrl());
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        dataSource.setPoolName(HIKARI_POOL_NAME_PREFIX + context.type());
        config.getDataSourceProperties().forEach(dataSource::addDataSourceProperty);
        for (DataSourceCustomizer customizer : dataSourceCustomizers) {
            customizer.customize(dataSource, context);
        }
        for (HikariDataSourceCustomizer customizer : hikariDataSourceCustomizers) {
            customizer.customize(dataSource, context);
        }
        return dataSource;
    }
}
