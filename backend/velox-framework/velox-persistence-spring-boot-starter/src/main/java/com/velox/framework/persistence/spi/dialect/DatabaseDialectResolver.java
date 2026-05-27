package com.velox.framework.persistence.spi.dialect;

public interface DatabaseDialectResolver {

    DatabaseDialect resolve(String type);
}
