package com.velox.framework.persistence.core.dialect;

import com.velox.framework.persistence.common.message.PersistenceCommonMessages;
import com.velox.framework.persistence.exception.PersistenceConfigException;
import com.velox.framework.persistence.spi.dialect.DatabaseDialect;
import com.velox.framework.persistence.spi.dialect.DatabaseDialectResolver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DefaultDatabaseDialectResolver implements DatabaseDialectResolver {

    private static final String DEFAULT_DIALECT = "";
    private final Map<String, DatabaseDialect> dialects = new LinkedHashMap<>();

    public DefaultDatabaseDialectResolver(List<? extends DatabaseDialect> dialects) {
        for (DatabaseDialect dialect : dialects) {
            if (dialect == null) {
                continue;
            }
            String type = normalize(dialect.getType());
            DatabaseDialect existing = this.dialects.putIfAbsent(type, dialect);
            if (existing != null) {
                throw new PersistenceConfigException(PersistenceCommonMessages.DUPLICATE_DATABASE_DIALECT.formatted(type));
            }
        }
    }

    @Override
    public DatabaseDialect resolve(String type) {
        DatabaseDialect dialect = dialects.get(normalize(type));
        if (dialect == null) {
            throw new PersistenceConfigException(PersistenceCommonMessages.UNSUPPORTED_DATABASE_TYPE.formatted(type));
        }
        return dialect;
    }

    private String normalize(String type) {
        return type == null ? DEFAULT_DIALECT : type.trim().toLowerCase(Locale.ROOT);
    }
}
