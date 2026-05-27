package com.velox.framework.id;

import com.velox.framework.id.api.codec.IdCodec;
import com.velox.framework.id.api.model.GeneratedId;
import com.velox.framework.id.api.model.IdGeneratorMetadata;
import com.velox.framework.id.common.business.IdBusinessTypes;
import com.velox.framework.id.common.message.IdGeneratorCommonMessages;
import com.velox.framework.id.exception.VeloxIdGeneratorException;
import com.velox.framework.id.spi.generator.IdGeneratorEngine;
import org.springframework.util.StringUtils;

/**
 * Public business-facing facade that preserves the historical injection contract while delegating
 * generation and encoding to starter-managed SPI beans.
 */
public class BusinessIdGenerator {

    private final IdGeneratorEngine engine;
    private final IdCodec codec;

    public BusinessIdGenerator(IdGeneratorEngine engine, IdCodec codec) {
        this.engine = engine;
        this.codec = codec;
    }

    public String next() {
        return next(IdBusinessTypes.DEFAULT);
    }

    public String next(String businessType) {
        return nextGeneratedId(businessType).sourceValue();
    }

    public String nextSourceId() {
        return nextSourceId(IdBusinessTypes.DEFAULT);
    }

    public String nextSourceId(String businessType) {
        return nextGeneratedId(businessType).sourceValue();
    }

    public long nextRawId() {
        return nextRawId(IdBusinessTypes.DEFAULT);
    }

    public long nextRawId(String businessType) {
        return parseRawLong(nextGeneratedId(businessType).sourceValue());
    }

    public GeneratedId nextGeneratedId(String businessType) {
        return engine.nextId(normalizeBusinessType(businessType));
    }

    public String encode(String sourceId) {
        return codec.encode(sourceId);
    }

    public String encode(long rawId) {
        return codec.encode(String.valueOf(rawId));
    }

    public String decode(String encodedId) {
        return codec.decode(encodedId);
    }

    public long decodeToLong(String encodedId) {
        return parseRawLong(codec.decode(encodedId));
    }

    public IdGeneratorMetadata metadata() {
        return engine.metadata();
    }

    private String normalizeBusinessType(String businessType) {
        return StringUtils.hasText(businessType) ? businessType.trim() : IdBusinessTypes.DEFAULT;
    }

    private long parseRawLong(String sourceValue) {
        if (!StringUtils.hasText(sourceValue)) {
            throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.RAW_ID_NOT_AVAILABLE_FOR_CURRENT_STRATEGY);
        }
        try {
            return Long.parseLong(sourceValue.trim());
        } catch (NumberFormatException exception) {
            throw new VeloxIdGeneratorException(
                    IdGeneratorCommonMessages.RAW_ID_NOT_AVAILABLE_FOR_CURRENT_STRATEGY,
                    exception
            );
        }
    }
}
