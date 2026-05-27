package com.velox.framework.id.spi.database;

public interface DatabaseIdOperator {

    String nextSourceId(String businessType);
}
