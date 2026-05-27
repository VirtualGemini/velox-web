package com.velox.framework.persistence.spi.dialect;

public interface DatabaseDialect {

    String getType();

    String getDefaultDriverClassName();
}
