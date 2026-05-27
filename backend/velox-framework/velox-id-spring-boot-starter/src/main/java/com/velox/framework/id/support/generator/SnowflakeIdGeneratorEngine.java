package com.velox.framework.id.support.generator;

import cn.hutool.core.lang.Snowflake;
import com.velox.framework.id.api.codec.IdCodec;
import com.velox.framework.id.api.model.GeneratedId;
import com.velox.framework.id.api.model.IdGeneratorMetadata;
import com.velox.framework.id.common.type.IdGeneratorStrategies;
import com.velox.framework.id.common.type.IdSourceModes;
import com.velox.framework.id.properties.VeloxIdProperties;
import com.velox.framework.id.spi.generator.AbstractIdGeneratorEngine;

import java.util.Date;

public class SnowflakeIdGeneratorEngine extends AbstractIdGeneratorEngine {

    private final Snowflake snowflake;
    private final IdCodec codec;
    private final IdGeneratorMetadata metadata;

    public SnowflakeIdGeneratorEngine(VeloxIdProperties properties, IdCodec codec) {
        VeloxIdProperties.SnowflakeProperties snowflakeProperties = properties.getSnowflake();
        this.codec = codec;
        this.snowflake = new Snowflake(
                new Date(snowflakeProperties.getTwepoch()),
                properties.getWorkerId(),
                properties.getDatacenterId(),
                snowflakeProperties.isUseSystemClock(),
                snowflakeProperties.getTimeOffset()
        );
        this.metadata = new IdGeneratorMetadata(
                true,
                IdSourceModes.APPLICATION,
                IdGeneratorStrategies.SNOWFLAKE,
                codec.getName(),
                properties.getDatabase().getInitMode(),
                properties.getWorkerId(),
                properties.getDatacenterId(),
                snowflakeProperties.getTwepoch(),
                snowflakeProperties.getTimeOffset(),
                snowflakeProperties.isUseSystemClock()
        );
    }

    @Override
    protected GeneratedId doNextId(String businessType) {
        String sourceValue = String.valueOf(snowflake.nextId());
        return new GeneratedId(businessType, sourceValue, codec.encode(sourceValue), metadata);
    }

    @Override
    public IdGeneratorMetadata metadata() {
        return metadata;
    }
}
