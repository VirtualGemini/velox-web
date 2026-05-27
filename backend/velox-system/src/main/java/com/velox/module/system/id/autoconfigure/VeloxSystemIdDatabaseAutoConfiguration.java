package com.velox.module.system.id.autoconfigure;

import com.velox.framework.id.properties.VeloxIdProperties;
import com.velox.framework.id.spi.database.DatabaseIdOperator;
import com.velox.module.system.id.database.SystemDatabaseIdGovernanceProperties;
import com.velox.module.system.id.database.schema.DatabaseIdSchemaBootstrapper;
import com.velox.module.system.id.database.schema.DatabaseIdSequenceOperator;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class VeloxSystemIdDatabaseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DatabaseIdOperator.class)
    public DatabaseIdSequenceOperator databaseIdOperator(
            DataSource dataSource,
            VeloxIdProperties properties
    ) {
        return new DatabaseIdSequenceOperator(dataSource, properties);
    }

    @Bean
    public DatabaseIdSchemaBootstrapper databaseIdSchemaBootstrapper(
            DataSource dataSource,
            VeloxIdProperties properties,
            SystemDatabaseIdGovernanceProperties governanceProperties
    ) {
        return new DatabaseIdSchemaBootstrapper(dataSource, properties, governanceProperties);
    }

    @Bean
    public ApplicationRunner databaseIdSchemaBootstrapRunner(DatabaseIdSchemaBootstrapper bootstrapper) {
        return args -> bootstrapper.bootstrap();
    }
}
