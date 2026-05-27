package com.velox.framework.id.spi.generator;

import com.velox.framework.id.api.model.GeneratedId;
import com.velox.framework.id.common.business.IdBusinessTypes;
import org.springframework.util.StringUtils;

public abstract class AbstractIdGeneratorEngine implements IdGeneratorEngine {

    @Override
    public final GeneratedId nextId(String businessType) {
        return doNextId(normalizeBusinessType(businessType));
    }

    protected abstract GeneratedId doNextId(String businessType);

    protected String normalizeBusinessType(String businessType) {
        return StringUtils.hasText(businessType) ? businessType.trim() : IdBusinessTypes.DEFAULT;
    }
}
