package com.velox.framework.id.noop;

import com.velox.framework.id.api.model.GeneratedId;
import com.velox.framework.id.api.model.IdGeneratorMetadata;
import com.velox.framework.id.common.message.IdGeneratorCommonMessages;
import com.velox.framework.id.common.type.IdSourceModes;
import com.velox.framework.id.exception.VeloxIdGeneratorException;
import com.velox.framework.id.properties.VeloxIdProperties;
import com.velox.framework.id.spi.generator.AbstractIdGeneratorEngine;
import com.velox.framework.id.api.codec.IdCodec;

public class DisabledIdGeneratorEngine extends AbstractIdGeneratorEngine {

    private final IdGeneratorMetadata metadata;

    public DisabledIdGeneratorEngine(VeloxIdProperties properties, IdCodec codec) {
        VeloxIdProperties.SnowflakeProperties snowflake = properties.getSnowflake();
        this.metadata = new IdGeneratorMetadata(
                false,
                IdSourceModes.DATABASE_DEFAULT,
                properties.getStrategy(),
                codec.getName(),
                properties.getDatabase().getInitMode(),
                null,
                null,
                null,
                null,
                false
        );
    }

    @Override
    protected GeneratedId doNextId(String businessType) {
        throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.DB_ID_OPERATOR_REQUIRED);
    }

    @Override
    public IdGeneratorMetadata metadata() {
        return metadata;
    }
}
