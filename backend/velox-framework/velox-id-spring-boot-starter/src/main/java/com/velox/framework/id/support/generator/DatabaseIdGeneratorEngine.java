package com.velox.framework.id.support.generator;

import com.velox.framework.id.api.codec.IdCodec;
import com.velox.framework.id.api.model.GeneratedId;
import com.velox.framework.id.api.model.IdGeneratorMetadata;
import com.velox.framework.id.common.type.IdGeneratorStrategies;
import com.velox.framework.id.common.type.IdSourceModes;
import com.velox.framework.id.properties.VeloxIdProperties;
import com.velox.framework.id.spi.database.DatabaseIdOperator;
import com.velox.framework.id.spi.generator.AbstractIdGeneratorEngine;

public class DatabaseIdGeneratorEngine extends AbstractIdGeneratorEngine {

    private final IdCodec codec;
    private final DatabaseIdOperator databaseIdOperator;
    private final IdGeneratorMetadata metadata;

    public DatabaseIdGeneratorEngine(VeloxIdProperties properties, IdCodec codec, DatabaseIdOperator databaseIdOperator) {
        this.codec = codec;
        this.databaseIdOperator = databaseIdOperator;
        VeloxIdProperties.SnowflakeProperties snowflake = properties.getSnowflake();
        this.metadata = new IdGeneratorMetadata(
                false,
                IdSourceModes.DATABASE_DEFAULT,
                properties.getStrategy(),
                codec.getName(),
                properties.getDatabase().getInitMode(),
                IdGeneratorStrategies.SNOWFLAKE.equals(properties.getStrategy()) ? properties.getWorkerId() : null,
                IdGeneratorStrategies.SNOWFLAKE.equals(properties.getStrategy()) ? properties.getDatacenterId() : null,
                IdGeneratorStrategies.SNOWFLAKE.equals(properties.getStrategy()) ? snowflake.getTwepoch() : null,
                IdGeneratorStrategies.SNOWFLAKE.equals(properties.getStrategy()) ? snowflake.getTimeOffset() : null,
                IdGeneratorStrategies.SNOWFLAKE.equals(properties.getStrategy()) && snowflake.isUseSystemClock()
        );
    }

    @Override
    protected GeneratedId doNextId(String businessType) {
        String sourceValue = databaseIdOperator.nextSourceId(businessType);
        return new GeneratedId(businessType, sourceValue, codec.encode(sourceValue), metadata);
    }

    @Override
    public IdGeneratorMetadata metadata() {
        return metadata;
    }
}
