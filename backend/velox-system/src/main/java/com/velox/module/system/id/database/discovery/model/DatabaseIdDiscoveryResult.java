package com.velox.module.system.id.database.discovery.model;

import java.util.List;
import java.util.Map;
import com.velox.module.system.id.database.diagnostic.DatabaseIdPlanDiagnostic;

public record DatabaseIdDiscoveryResult(
        Map<String, DatabaseIdManagedTable> managedTables,
        List<DatabaseIdPlanDiagnostic> diagnostics
) {
}
