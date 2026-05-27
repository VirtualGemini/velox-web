package com.velox.module.system.id.database.schema;

import com.velox.framework.id.common.business.IdBusinessTypes;
import com.velox.framework.id.common.message.IdGeneratorCommonMessages;
import com.velox.framework.id.common.type.IdDatabaseInitModes;
import com.velox.framework.id.exception.VeloxIdGeneratorException;
import com.velox.framework.id.properties.VeloxIdProperties;
import com.velox.module.system.id.database.SystemDatabaseIdGovernanceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.velox.module.system.id.database.discovery.DatabaseIdDiscoverySupport;
import com.velox.module.system.id.database.diagnostic.DatabaseIdDiagnosticSeverity;
import com.velox.module.system.id.database.diagnostic.DatabaseIdPlanDiagnostic;
import com.velox.module.system.id.database.diagnostic.DatabaseIdPlanReport;
import com.velox.module.system.id.database.plan.model.DatabaseIdReconciliationPlan;
import org.springframework.util.StringUtils;

import static com.velox.module.system.id.database.internal.DatabaseIdInternals.normalizeName;

public class DatabaseIdSchemaBootstrapper {

    private static final Logger log = LoggerFactory.getLogger(DatabaseIdSchemaBootstrapper.class);
    private static final String TABLE_NAME = "sys_id_sequence";

    private final DataSource dataSource;
    private final VeloxIdProperties properties;
    private final SystemDatabaseIdGovernanceProperties governanceProperties;
    private final DatabaseIdSchemaReconciler reconciler;

    public DatabaseIdSchemaBootstrapper(
            DataSource dataSource,
            VeloxIdProperties properties,
            SystemDatabaseIdGovernanceProperties governanceProperties
    ) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.governanceProperties = governanceProperties;
        this.reconciler = new DatabaseIdSchemaReconciler(properties, governanceProperties);
    }

    public void bootstrap() {
        boolean updateMode = IdDatabaseInitModes.UPDATE.equals(properties.getDatabase().getInitMode());
        try (Connection connection = dataSource.getConnection()) {
            DatabaseIdReconciliationPlan reconciliationPlan;
            try {
                reconciliationPlan = executeStage(
                        "plan database id reconciliation",
                        () -> reconciler.plan(connection)
                );
            } catch (VeloxIdGeneratorException exception) {
                if (updateMode) {
                    throw exception;
                }
                log.warn("Velox database id reconciliation skipped; non-update mode tolerates plan failure: {}",
                        exception.getMessage());
                return;
            }
            emitPlanReport(buildPlanReport(reconciliationPlan), updateMode);
            emitDiagnostics(reconciliationPlan, updateMode);
            if (!updateMode) {
                return;
            }
            if (!reconciliationPlan.shouldBootstrap()) {
                return;
            }
            guardAgainstUndeclaredTables(reconciliationPlan);

            if (!tableExists(connection)) {
                executeStage("create sys_id_sequence", () -> {
                    createTable(connection);
                    return null;
                });
                log.info("Initialized {} for Velox database id sequencing", TABLE_NAME);
            }

            if (reconciliationPlan.needsMigration()) {
                executeStage("reconcile managed database ids", () -> {
                    reconciler.reconcile(connection, reconciliationPlan);
                    return null;
                });
                log.info("Reconciled managed database ids to {}", reconciliationPlan.targetPosture().name().toLowerCase());
            }
            Map<String, Long> sequenceSeeds = executeStage(
                    "resolve sys_id_sequence seed values",
                    () -> determineSequenceSeeds(connection)
            );
            executeStage("seed sys_id_sequence", () -> {
                seedBusinessTypes(connection, sequenceSeeds);
                return null;
            });
        } catch (SQLException exception) {
            throw wrapSqlException("open bootstrap connection", exception);
        }
    }

    private DatabaseIdPlanReport buildPlanReport(DatabaseIdReconciliationPlan reconciliationPlan) {
        int managedTableCount = reconciliationPlan.availableTables().size();
        int identityDomainCount = (int) reconciliationPlan.availableTables().values().stream()
                .filter(table -> table.identityColumn() != null)
                .count();
        int migrationCount = reconciliationPlan.idMappings().values().stream()
                .mapToInt(Map::size)
                .sum();
        return new DatabaseIdPlanReport(
                reconciliationPlan.targetPosture(),
                managedTableCount,
                identityDomainCount,
                migrationCount,
                reconciliationPlan.diagnostics()
        );
    }

    private void emitPlanReport(DatabaseIdPlanReport report, boolean updateMode) {
        if (!governanceProperties.isReportEnabled()) {
            return;
        }
        String mode = updateMode ? "update" : "none";
        log.info(
                "Velox database id plan [mode={}, targetPosture={}, managedTables={}, identityDomains={}, migrations={}, warnings={}, errors={}]",
                mode,
                report.targetPosture().name().toLowerCase(),
                report.managedTableCount(),
                report.identityDomainCount(),
                report.migrationCount(),
                report.warningCount(),
                report.errorCount()
        );
    }

    private void emitDiagnostics(DatabaseIdReconciliationPlan reconciliationPlan, boolean updateMode) {
        for (DatabaseIdPlanDiagnostic diagnostic : reconciliationPlan.diagnostics()) {
            if (diagnostic.severity() == DatabaseIdDiagnosticSeverity.ERROR) {
                log.error("Velox database id diagnostic [{}] {} - {}", diagnostic.code(), diagnostic.location(), diagnostic.message());
                continue;
            }
            if (updateMode) {
                log.warn("Velox database id diagnostic [{}] {} - {}", diagnostic.code(), diagnostic.location(), diagnostic.message());
            } else {
                log.info("Velox database id diagnostic [{}] {} - {}", diagnostic.code(), diagnostic.location(), diagnostic.message());
            }
        }
    }

    private void guardAgainstUndeclaredTables(DatabaseIdReconciliationPlan reconciliationPlan) {
        if (!reconciliationPlan.needsMigration()) {
            return;
        }
        List<String> undeclared = reconciliationPlan.diagnostics().stream()
                .filter(diagnostic -> DatabaseIdDiscoverySupport.UNDECLARED_TABLE_DIAGNOSTIC_CODE.equals(diagnostic.code()))
                .map(DatabaseIdPlanDiagnostic::location)
                .toList();
        if (undeclared.isEmpty()) {
            return;
        }
        throw new VeloxIdGeneratorException(
                "Velox database id reconciliation requires migration but the following tables are not covered by "
                        + "velox.id.database.governance: " + String.join(", ", undeclared)
                        + ". Either declare them under governance.domains (and governance.references when needed) "
                        + "or add them to governance.ignored-tables to opt out."
        );
    }

    private <T> T executeStage(String stage, SqlSupplier<T> supplier) throws SQLException {
        try {
            return supplier.get();
        } catch (SQLException exception) {
            throw wrapSqlException(stage, exception);
        }
    }

    private VeloxIdGeneratorException wrapSqlException(String stage, SQLException exception) {
        String sqlState = exception.getSQLState() == null ? "-" : exception.getSQLState();
        String message = exception.getMessage() == null ? "-" : exception.getMessage();
        return new VeloxIdGeneratorException(
                IdGeneratorCommonMessages.DATABASE_ID_INITIALIZATION_FAILED
                        + " [stage=" + stage
                        + ", sqlState=" + sqlState
                        + ", errorCode=" + exception.getErrorCode()
                        + ", message=" + message + "]",
                exception
        );
    }

    private boolean tableExists(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT business_type FROM " + TABLE_NAME + " WHERE 1 = 0")) {
            statement.executeQuery();
            return true;
        } catch (SQLException exception) {
            return false;
        }
    }

    private void createTable(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS sys_id_sequence (
                  business_type varchar(64) NOT NULL PRIMARY KEY,
                  current_value bigint NOT NULL DEFAULT 0,
                  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
                  update_time timestamp DEFAULT CURRENT_TIMESTAMP
                )
                """)) {
            statement.executeUpdate();
        }
    }

    private void seedBusinessTypes(Connection connection, Map<String, Long> targetValues) throws SQLException {
        for (Map.Entry<String, Long> entry : targetValues.entrySet()) {
            String businessType = entry.getKey();
            long currentValue = entry.getValue();
            if (!hasBusinessType(connection, businessType)) {
                insertBusinessType(connection, businessType, currentValue);
                continue;
            }
            updateBusinessType(connection, businessType, currentValue);
        }
    }

    private boolean hasBusinessType(Connection connection, String businessType) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT business_type FROM sys_id_sequence WHERE business_type = ?")) {
            statement.setString(1, businessType);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void insertBusinessType(Connection connection, String businessType, long currentValue) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO sys_id_sequence (business_type, current_value) VALUES (?, ?)")) {
            statement.setString(1, businessType);
            statement.setLong(2, currentValue);
            statement.executeUpdate();
        }
    }

    private void updateBusinessType(Connection connection, String businessType, long currentValue) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE sys_id_sequence SET current_value = ?, update_time = CURRENT_TIMESTAMP WHERE business_type = ? AND current_value < ?")) {
            statement.setLong(1, currentValue);
            statement.setString(2, businessType);
            statement.setLong(3, currentValue);
            statement.executeUpdate();
        }
    }

    private Map<String, Long> determineSequenceSeeds(Connection connection) throws SQLException {
        Map<String, Long> values = defaultSequenceSeeds(connection);
        if (properties.isEnabled()) {
            return values;
        }
        values.putAll(reconciler.resolveDatabaseSequenceValues(connection, values.keySet()));
        return values;
    }

    private Map<String, Long> defaultSequenceSeeds(Connection connection) throws SQLException {
        Map<String, Long> values = new LinkedHashMap<>();
        values.put(IdBusinessTypes.DEFAULT, 0L);
        for (SystemDatabaseIdGovernanceProperties.DomainDeclaration declaration : governanceProperties.getDomains()) {
            if (!declaration.isEnabled()) {
                continue;
            }
            String businessType = resolveBusinessType(declaration);
            if (businessType.isBlank()) {
                continue;
            }
            values.putIfAbsent(businessType, 0L);
        }
        for (String businessType : reconciler.resolveManagedBusinessTypes(connection)) {
            values.putIfAbsent(businessType, 0L);
        }
        return values;
    }

    private String resolveBusinessType(SystemDatabaseIdGovernanceProperties.DomainDeclaration declaration) {
        if (StringUtils.hasText(declaration.getBusinessType())) {
            return normalizeName(declaration.getBusinessType());
        }
        return normalizeName(declaration.getTable());
    }

    @FunctionalInterface
    private interface SqlSupplier<T> {

        T get() throws SQLException;
    }
}
