package com.velox.module.system.id.database.graph.model;

import java.util.List;
import java.util.Map;

public record DatabaseTableSnapshot(
        QualifiedTable table,
        List<String> primaryKeyColumns,
        Map<String, DatabaseColumnSnapshot> columns,
        List<DatabaseForeignKeySnapshot> importedKeys,
        List<List<String>> uniqueKeys,
        long rowCount
) {

    public DatabaseColumnSnapshot column(String columnName) {
        if (columnName == null) {
            return null;
        }
        return columns.get(columnName.toLowerCase());
    }
}
