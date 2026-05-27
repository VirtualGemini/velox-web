package com.velox.module.system.id.database.graph.model;

public record QualifiedColumn(
        QualifiedTable table,
        String columnName
) {

    public String normalizedName() {
        return columnName == null ? "" : columnName.toLowerCase();
    }

    public String sqlName() {
        return table.sqlName() + "." + columnName;
    }
}
