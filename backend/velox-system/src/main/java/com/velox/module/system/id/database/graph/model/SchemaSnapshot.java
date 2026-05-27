package com.velox.module.system.id.database.graph.model;

import java.util.Map;

public record SchemaSnapshot(
        Map<String, DatabaseTableSnapshot> tables
) {
}
