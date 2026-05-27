package com.velox.module.system.id.database.schema;

import com.velox.framework.id.properties.VeloxIdProperties;
import com.velox.module.system.id.database.SystemDatabaseIdGovernanceProperties;
import com.velox.module.system.id.database.graph.model.DatabaseColumnSnapshot;
import com.velox.module.system.id.database.graph.DatabaseSchemaSnapshotLoader;
import com.velox.module.system.id.database.graph.model.DatabaseTableSnapshot;
import com.velox.module.system.id.database.graph.model.SchemaSnapshot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.velox.module.system.id.database.DatabaseIdValueGenerator;
import com.velox.module.system.id.database.discovery.DatabaseIdDiscoverySupport;
import com.velox.module.system.id.database.discovery.model.DatabaseIdDiscoveryResult;
import com.velox.module.system.id.database.discovery.model.DatabaseIdManagedTable;
import com.velox.module.system.id.database.plan.DatabaseIdMappingPlanner;
import com.velox.module.system.id.database.plan.DatabaseIdPlanValidator;
import com.velox.module.system.id.database.diagnostic.DatabaseIdPlanValidationResult;
import com.velox.module.system.id.database.plan.model.DatabaseIdPlanningContext;
import com.velox.module.system.id.database.plan.model.DatabaseIdReconciliationPlan;
import com.velox.module.system.id.database.posture.DatabaseIdPostureClassifier;
import com.velox.module.system.id.database.posture.model.DatabaseIdPosture;
import com.velox.module.system.id.database.internal.DatabaseIdInternals;

public class DatabaseIdSchemaReconciler {

    private static final int SAMPLE_LIMIT = 50;

    private final DatabaseIdValueGenerator valueGenerator;
    private final SystemDatabaseIdGovernanceProperties governanceProperties;
    private final DatabaseSchemaSnapshotLoader snapshotLoader = new DatabaseSchemaSnapshotLoader();
    private final DatabaseIdPlanValidator planValidator = new DatabaseIdPlanValidator();
    private final DatabaseIdMappingPlanner mappingPlanner = new DatabaseIdMappingPlanner();
    private final DatabaseIdMigrationExecutor migrationExecutor = new DatabaseIdMigrationExecutor();

    public DatabaseIdSchemaReconciler(
            VeloxIdProperties properties,
            SystemDatabaseIdGovernanceProperties governanceProperties
    ) {
        this.valueGenerator = new DatabaseIdValueGenerator(properties);
        this.governanceProperties = governanceProperties;
    }

    public DatabaseIdReconciliationPlan plan(Connection connection) throws SQLException {
        DatabaseIdPlanningContext context = discover(connection);
        return mappingPlanner.plan(connection, context, valueGenerator);
    }

    public void reconcile(Connection connection, DatabaseIdReconciliationPlan plan) throws SQLException {
        migrationExecutor.execute(connection, plan);
    }

    public Map<String, Long> resolveSnowflakeSequenceValues(Connection connection, Collection<String> businessTypes)
            throws SQLException {
        return resolveSequenceValues(connection, businessTypes, true);
    }

    public Map<String, Long> resolveDatabaseSequenceValues(Connection connection, Collection<String> businessTypes)
            throws SQLException {
        return resolveSequenceValues(connection, businessTypes, false);
    }

    public List<String> resolveManagedBusinessTypes(Connection connection) throws SQLException {
        return discover(connection).managedTables().values().stream()
                .map(DatabaseIdManagedTable::identityMappingBusinessType)
                .sorted()
                .toList();
    }

    private DatabaseIdPlanningContext discover(Connection connection) throws SQLException {
        SchemaSnapshot schemaSnapshot = snapshotLoader.load(connection);
        Map<String, Set<String>> sampleCache = loadSampleCache(connection, schemaSnapshot);
        DatabaseIdDiscoveryResult discovery = DatabaseIdDiscoverySupport.discover(schemaSnapshot, sampleCache, governanceProperties);
        DatabaseIdPlanningContext context = new DatabaseIdPlanningContext(
                schemaSnapshot,
                sampleCache,
                discovery.managedTables(),
                discovery.diagnostics(),
                new DatabaseIdPlanValidationResult(List.of())
        );
        DatabaseIdPlanValidationResult validationResult = planValidator.inspect(context);
        DatabaseIdPlanningContext validatedContext = new DatabaseIdPlanningContext(
                schemaSnapshot,
                sampleCache,
                discovery.managedTables(),
                discovery.diagnostics(),
                validationResult
        );
        planValidator.validate(validatedContext);
        return validatedContext;
    }

    private Map<String, Long> resolveSequenceValues(
            Connection connection,
            Collection<String> businessTypes,
            boolean snowflake
    ) throws SQLException {
        Map<String, Long> values = businessTypes.stream().collect(java.util.stream.Collectors.toMap(
                businessType -> businessType,
                businessType -> 0L,
                (left, right) -> left,
                LinkedHashMap::new
        ));
        DatabaseIdPlanningContext context = discover(connection);
        for (DatabaseIdManagedTable table : context.managedTables().values()) {
            if (table.identityColumn() == null) {
                continue;
            }
            String businessType = table.identityMappingBusinessType();
            if (!values.containsKey(businessType)) {
                continue;
            }
            long maxValue = 0L;
            for (String currentId : readAllDistinctValues(connection, table.tableName(), table.identityColumn())) {
                DatabaseIdPosture posture = DatabaseIdPostureClassifier.classify(currentId);
                if (snowflake) {
                    if (posture != DatabaseIdPosture.SNOWFLAKE) {
                        continue;
                    }
                    maxValue = Math.max(maxValue, valueGenerator.decodeSnowflakeId(currentId));
                } else if (posture == DatabaseIdPosture.DB_AUTO_INCREMENT) {
                    maxValue = Math.max(maxValue, Long.parseLong(currentId));
                }
            }
            values.put(businessType, maxValue);
        }
        return values;
    }

    private Map<String, Set<String>> loadSampleCache(Connection connection, SchemaSnapshot snapshot) throws SQLException {
        Map<String, Set<String>> samples = new LinkedHashMap<>();
        for (DatabaseTableSnapshot table : snapshot.tables().values()) {
            for (DatabaseColumnSnapshot column : table.columns().values()) {
                if (!column.isManageableScalar() && !DatabaseIdInternals.isPathColumn(column.normalizedColumnName())) {
                    continue;
                }
                samples.put(
                        DatabaseIdInternals.sampleKey(table.table(), column.columnName()),
                        new LinkedHashSet<>(readSampleValues(connection, table.table().sqlName(), column.columnName()))
                );
            }
        }
        return samples;
    }

    private List<String> readSampleValues(Connection connection, String tableName, String columnName) throws SQLException {
        List<String> values = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT " + columnName + " FROM " + tableName + " WHERE " + columnName
                + " IS NOT NULL ORDER BY " + columnName + " LIMIT " + SAMPLE_LIMIT;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                values.add(resultSet.getString(1));
            }
        }
        return values;
    }

    private List<String> readAllDistinctValues(Connection connection, String tableName, String columnName) throws SQLException {
        List<String> values = new java.util.ArrayList<>();
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
