package com.velox.module.system.id.database.discovery.model;

public enum DatabaseIdReferenceKind {

    EXPLICIT_REFERENCE,
    EXPLICIT_FK,
    SHARED_PRIMARY_KEY,
    SELF_REFERENCE,
    AUDIT_REFERENCE,
    PATH_REFERENCE
}
