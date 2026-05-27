package com.velox.module.system.id.database.plan.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.velox.module.system.id.database.diagnostic.DatabaseIdPlanDiagnostic;
import com.velox.module.system.id.database.discovery.model.DatabaseIdManagedTable;
import com.velox.module.system.id.database.posture.model.DatabaseIdPosture;

public record DatabaseIdReconciliationPlan(
        boolean hasDetectedData,
        boolean needsMigration,
        DatabaseIdPosture targetPosture,
        Map<String, DatabaseIdManagedTable> availableTables,
        Map<String, LinkedHashMap<String, String>> idMappings,
        List<DatabaseIdPlanDiagnostic> diagnostics
) {

    public boolean shouldBootstrap() {
        return !hasDetectedData || needsMigration;
    }
}
