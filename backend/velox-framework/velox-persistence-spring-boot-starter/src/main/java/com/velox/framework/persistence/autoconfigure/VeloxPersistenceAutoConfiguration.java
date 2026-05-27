package com.velox.framework.persistence.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.velox.framework.persistence.api.datasource.PersistenceContext;
import com.velox.framework.persistence.core.datasource.DefaultPersistenceContextFactory;
import com.velox.framework.persistence.core.dialect.DefaultDatabaseDialectResolver;
import com.velox.framework.persistence.properties.VeloxPersistenceProperties;
import com.velox.framework.persistence.spi.datasource.DataSourceCustomizer;
import com.velox.framework.persistence.spi.datasource.PersistenceContextFactory;
import com.velox.framework.persistence.spi.datasource.PersistenceDataSourceFactory;
import com.velox.framework.persistence.spi.dialect.DatabaseDialect;
import com.velox.framework.persistence.spi.dialect.DatabaseDialectResolver;
import com.velox.framework.persistence.support.datasource.HikariDataSourceCustomizer;
import com.velox.framework.persistence.support.datasource.HikariPersistenceDataSourceFactory;
import com.velox.framework.persistence.support.dialect.MySqlDatabaseDialect;
import com.velox.framework.persistence.support.dialect.PostgreSqlDatabaseDialect;
import com.velox.framework.persistence.support.mybatis.DefaultMybatisPlusInterceptorFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.List;

@AutoConfiguration
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(VeloxPersistenceProperties.class)
public class VeloxPersistenceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DatabaseDialectResolver databaseDialectResolver(ObjectProvider<DatabaseDialect> dialectsProvider) {
        return new DefaultDatabaseDialectResolver(dialectsProvider.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean
    public PersistenceContextFactory persistenceContextFactory(DatabaseDialectResolver databaseDialectResolver) {
        return new DefaultPersistenceContextFactory(databaseDialectResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public PersistenceContext persistenceContext(VeloxPersistenceProperties properties,
                                                 PersistenceContextFactory persistenceContextFactory) {
        return persistenceContextFactory.create(properties);
    }

    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource(PersistenceContext context,
                                 PersistenceDataSourceFactory dataSourceFactory) {
        return dataSourceFactory.createDataSource(context);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "com.zaxxer.hikari.HikariDataSource")
    static class HikariSupportConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public PersistenceDataSourceFactory persistenceDataSourceFactory(
                ObjectProvider<DataSourceCustomizer> dataSourceCustomizersProvider,
                ObjectProvider<HikariDataSourceCustomizer> hikariDataSourceCustomizersProvider) {
            List<DataSourceCustomizer> dataSourceCustomizers = dataSourceCustomizersProvider.orderedStream().toList();
            List<HikariDataSourceCustomizer> hikariDataSourceCustomizers = hikariDataSourceCustomizersProvider.orderedStream().toList();
            return new HikariPersistenceDataSourceFactory(dataSourceCustomizers, hikariDataSourceCustomizers);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "com.mysql.cj.jdbc.Driver")
    static class MySqlSupportConfiguration {

        @Bean
        @Order(Ordered.LOWEST_PRECEDENCE)
        public DatabaseDialect mySqlDatabaseDialect() {
            return new MySqlDatabaseDialect();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.postgresql.Driver")
    static class PostgreSqlSupportConfiguration {

        @Bean
        @Order(Ordered.LOWEST_PRECEDENCE)
        public DatabaseDialect postgreSqlDatabaseDialect() {
            return new PostgreSqlDatabaseDialect();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(MybatisPlusInterceptor.class)
    static class MybatisPlusSupportConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public DefaultMybatisPlusInterceptorFactory defaultMybatisPlusInterceptorFactory() {
            return new DefaultMybatisPlusInterceptorFactory();
        }

        @Bean
        @ConditionalOnMissingBean
        public MybatisPlusInterceptor mybatisPlusInterceptor(PersistenceContext context,
                                                             DefaultMybatisPlusInterceptorFactory factory) {
            return factory.create(context);
        }

    }
}
