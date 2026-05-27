package com.velox.module.system.id.database.discovery.model;

public record DatabaseIdColumnBinding(
        String columnName,
        String targetBusinessType,
        DatabaseIdReferenceKind kind,
        boolean pathLike
) {
}
