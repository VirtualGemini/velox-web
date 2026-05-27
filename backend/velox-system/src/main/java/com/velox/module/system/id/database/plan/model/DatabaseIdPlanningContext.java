package com.velox.module.system.id.database.plan.model;

import com.velox.module.system.id.database.graph.model.DatabaseTableSnapshot;
import com.velox.module.system.id.database.graph.model.SchemaSnapshot;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.velox.module.system.id.database.diagnostic.DatabaseIdPlanDiagnostic;
import com.velox.module.system.id.database.diagnostic.DatabaseIdPlanValidationResult;
import com.velox.module.system.id.database.discovery.model.DatabaseIdManagedTable;
import com.velox.module.system.id.database.internal.DatabaseIdInternals;

public record DatabaseIdPlanningContext(
        SchemaSnapshot snapshot,
        Map<String, Set<String>> sampleCache,
        Map<String, DatabaseIdManagedTable> managedTables,
        List<DatabaseIdPlanDiagnostic> discoveryDiagnostics,
        DatabaseIdPlanValidationResult validationResult
) {

    public static DatabaseIdPlanningContext of(
            SchemaSnapshot snapshot,
            Map<String, Set<String>> sampleCache,
            Map<String, DatabaseIdManagedTable> managedTables
    ) {
        return new DatabaseIdPlanningContext(snapshot, sampleCache, managedTables, List.of(), new DatabaseIdPlanValidationResult(List.of()));
    }

    public DatabaseTableSnapshot table(String tableName) {
        return snapshot.tables().get(DatabaseIdInternals.normalizeName(tableName));
    }

    public Set<String> samples(String tableName, String columnName) {
        DatabaseTableSnapshot table = table(tableName);
        if (table == null) {
            return Set.of();
        }
        return sampleCache.getOrDefault(DatabaseIdInternals.sampleKey(table.table(), columnName), Set.of());
    }

    public Map<String, DatabaseIdManagedTable> identityDomainsByBusinessType() {
        return managedTables.values().stream()
                .filter(table -> table.identityColumn() != null)
                .collect(Collectors.toMap(
                        DatabaseIdManagedTable::identityMappingBusinessType,
                        table -> table,
                        (left, right) -> left
                ));
    }
}
