package com.velox.module.system.id.database.plan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import com.velox.module.system.id.database.DatabaseIdValueGenerator;
import com.velox.module.system.id.database.discovery.model.DatabaseIdManagedTable;
import com.velox.module.system.id.database.diagnostic.DatabaseIdPlanDiagnostic;
import com.velox.module.system.id.database.plan.model.DatabaseIdPlanningContext;
import com.velox.module.system.id.database.plan.model.DatabaseIdReconciliationPlan;
import com.velox.module.system.id.database.posture.DatabaseIdPostureClassifier;
import com.velox.module.system.id.database.posture.model.DatabaseIdPosture;

public class DatabaseIdMappingPlanner {

    public DatabaseIdReconciliationPlan plan(
            Connection connection,
            DatabaseIdPlanningContext context,
            DatabaseIdValueGenerator valueGenerator
    ) throws SQLException {
        Map<String, LinkedHashMap<String, String>> idMappings = new LinkedHashMap<>();
        boolean hasDetectedData = false;

        for (DatabaseIdManagedTable table : context.managedTables().values()) {
            if (table.identityColumn() == null) {
                continue;
            }
            List<String> currentIds = readAllDistinctValues(connection, table.tableName(), table.identityColumn());
            if (!currentIds.isEmpty()) {
                hasDetectedData = true;
            }
            LinkedHashSet<String> reservedValues = new LinkedHashSet<>(currentIds);
            LinkedHashMap<String, String> mapping = new LinkedHashMap<>();
            for (String currentId : currentIds) {
                DatabaseIdPosture currentPosture = DatabaseIdPostureClassifier.classify(currentId);
                if (currentPosture == DatabaseIdPosture.EMPTY || currentPosture == valueGenerator.targetPosture()) {
                    continue;
                }
                mapping.put(currentId, valueGenerator.resolveTargetId(currentId, reservedValues));
            }
            if (!mapping.isEmpty()) {
                idMappings.put(table.identityMappingBusinessType(), mapping);
            }
        }

        return new DatabaseIdReconciliationPlan(
                hasDetectedData,
                !idMappings.isEmpty(),
                valueGenerator.targetPosture(),
                context.managedTables(),
                idMappings,
                mergeDiagnostics(context)
        );
    }

    private List<DatabaseIdPlanDiagnostic> mergeDiagnostics(DatabaseIdPlanningContext context) {
        List<DatabaseIdPlanDiagnostic> diagnostics = new ArrayList<>();
        diagnostics.addAll(context.discoveryDiagnostics());
        diagnostics.addAll(context.validationResult().diagnostics());
        return List.copyOf(diagnostics);
    }

    private List<String> readAllDistinctValues(Connection connection, String tableName, String columnName) throws SQLException {
        List<String> values = new ArrayList<>();
        String sql = "SELECT DISTINCT " + columnName + " FROM " + tableName + " WHERE " + columnName
                + " IS NOT NULL ORDER BY " + columnName;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                values.add(resultSet.getString(1));
            }
        }
        return values;
    }
}
