package com.velox.module.system.id.database.graph.model;

import java.util.Locale;

public record QualifiedTable(
        String schemaName,
        String tableName
) {

    public String normalizedName() {
        if (schemaName == null || schemaName.isBlank()) {
            return tableName.toLowerCase(Locale.ROOT);
        }
        return schemaName.toLowerCase(Locale.ROOT) + "." + tableName.toLowerCase(Locale.ROOT);
    }

    public String sqlName() {
        if (schemaName == null || schemaName.isBlank()) {
            return tableName;
        }
        return schemaName + "." + tableName;
    }
}
