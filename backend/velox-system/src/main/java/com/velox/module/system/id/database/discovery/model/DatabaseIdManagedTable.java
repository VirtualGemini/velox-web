package com.velox.module.system.id.database.discovery.model;

import java.util.List;

public record DatabaseIdManagedTable(
        String tableName,
        String identityColumn,
        String identityMappingBusinessType,
        List<DatabaseIdColumnBinding> bindings
) {
}
