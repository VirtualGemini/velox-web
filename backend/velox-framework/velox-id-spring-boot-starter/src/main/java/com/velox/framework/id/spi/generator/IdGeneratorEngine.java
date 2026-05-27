package com.velox.framework.id.spi.generator;

import com.velox.framework.id.api.model.GeneratedId;
import com.velox.framework.id.api.model.IdGeneratorMetadata;

public interface IdGeneratorEngine {

    GeneratedId nextId(String businessType);

    IdGeneratorMetadata metadata();
}
