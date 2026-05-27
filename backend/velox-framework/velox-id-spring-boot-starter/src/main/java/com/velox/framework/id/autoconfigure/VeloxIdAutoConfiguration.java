package com.velox.framework.id.autoconfigure;

import com.velox.framework.id.BusinessIdGenerator;
import com.velox.framework.id.api.codec.IdCodec;
import com.velox.framework.id.noop.DisabledIdGeneratorEngine;
import com.velox.framework.id.properties.VeloxIdProperties;
import com.velox.framework.id.spi.database.DatabaseIdOperator;
import com.velox.framework.id.spi.generator.IdGeneratorEngine;
import com.velox.framework.id.support.codec.Base62IdCodec;
import com.velox.framework.id.support.generator.DatabaseIdGeneratorEngine;
import com.velox.framework.id.support.generator.SnowflakeIdGeneratorEngine;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(VeloxIdProperties.class)
public class VeloxIdAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdCodec idCodec(VeloxIdProperties properties) {
        properties.validate();
        return new Base62IdCodec();
    }

    @Bean
    @ConditionalOnMissingBean(IdGeneratorEngine.class)
    public IdGeneratorEngine idGeneratorEngine(
            VeloxIdProperties properties,
            IdCodec codec,
            ObjectProvider<DatabaseIdOperator> databaseIdOperatorProvider
    ) {
        properties.validate();
        if (properties.isEnabled()) {
            return new SnowflakeIdGeneratorEngine(properties, codec);
        }

        DatabaseIdOperator databaseIdOperator = databaseIdOperatorProvider.getIfAvailable();
        if (databaseIdOperator != null) {
            return new DatabaseIdGeneratorEngine(properties, codec, databaseIdOperator);
        }
        return new DisabledIdGeneratorEngine(properties, codec);
    }

    @Bean
    @ConditionalOnMissingBean
    public BusinessIdGenerator businessIdGenerator(IdGeneratorEngine idGeneratorEngine, IdCodec codec) {
        return new BusinessIdGenerator(idGeneratorEngine, codec);
    }
}
