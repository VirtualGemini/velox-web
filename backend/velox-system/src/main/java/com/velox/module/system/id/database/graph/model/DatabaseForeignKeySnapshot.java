package com.velox.module.system.id.database.graph.model;

import java.util.List;

public record DatabaseForeignKeySnapshot(
        String name,
        QualifiedTable fkTable,
        List<String> fkColumns,
        QualifiedTable pkTable,
        List<String> pkColumns,
        short updateRule,
        short deleteRule
) {
}
