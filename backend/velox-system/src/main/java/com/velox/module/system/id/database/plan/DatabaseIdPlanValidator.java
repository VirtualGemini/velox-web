package com.velox.module.system.id.database.plan;

import com.velox.framework.id.exception.VeloxIdGeneratorException;
import com.velox.module.system.id.database.graph.model.DatabaseColumnSnapshot;
import com.velox.module.system.id.database.graph.model.DatabaseForeignKeySnapshot;
import com.velox.module.system.id.database.graph.model.DatabaseTableSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.velox.module.system.id.database.discovery.DatabaseIdDiscoverySupport;
import com.velox.module.system.id.database.discovery.model.DatabaseIdColumnBinding;
import com.velox.module.system.id.database.discovery.model.DatabaseIdManagedTable;
import com.velox.module.system.id.database.discovery.model.DatabaseIdReferenceKind;
import com.velox.module.system.id.database.diagnostic.DatabaseIdDiagnosticSeverity;
import com.velox.module.system.id.database.diagnostic.DatabaseIdPlanDiagnostic;
import com.velox.module.system.id.database.diagnostic.DatabaseIdPlanValidationResult;
import com.velox.module.system.id.database.plan.model.DatabaseIdPlanningContext;
import com.velox.module.system.id.database.internal.DatabaseIdInternals;

public class DatabaseIdPlanValidator {

    public DatabaseIdPlanValidationResult inspect(DatabaseIdPlanningContext context) {
        List<DatabaseIdPlanDiagnostic> diagnostics = new ArrayList<>();
        Map<String, DatabaseIdManagedTable> targetDomains = context.identityDomainsByBusinessType();
        for (DatabaseIdManagedTable table : context.managedTables().values()) {
            DatabaseTableSnapshot tableSnapshot = requireTable(context, table.tableName());
            if (table.identityColumn() != null) {
                DatabaseColumnSnapshot identityColumn = requireColumn(tableSnapshot, table.identityColumn());
                if (!identityColumn.isManageableScalar()) {
                    diagnostics.add(error("IDENTITY_NOT_SCALAR", location(table.tableName(), table.identityColumn()),
                            "Managed identity column must be scalar"));
                }
                if (DatabaseIdInternals.isPathColumn(identityColumn.normalizedColumnName())) {
                    diagnostics.add(error("IDENTITY_PATH_CONTAINER", location(table.tableName(), table.identityColumn()),
                            "Managed identity column must not be a path container"));
                }
                if (tableSnapshot.primaryKeyColumns().stream().noneMatch(column -> column.equalsIgnoreCase(table.identityColumn()))) {
                    diagnostics.add(error("DOMAIN_ID_NOT_PRIMARY_KEY", location(table.tableName(), table.identityColumn()),
                            "Declared identity domain column must be a primary key column"));
                }
            }
            for (DatabaseIdColumnBinding binding : table.bindings()) {
                if (!targetDomains.containsKey(binding.targetBusinessType())) {
                    diagnostics.add(error("TARGET_DOMAIN_MISSING", location(table.tableName(), binding.columnName()),
                            "Managed binding target business type is missing: " + binding.targetBusinessType()));
                    continue;
                }
                DatabaseColumnSnapshot column = requireColumn(tableSnapshot, binding.columnName());
                if (binding.pathLike()) {
                    if (!column.isCharacterLike()) {
                        diagnostics.add(error("PATH_BINDING_NOT_CHARACTER", location(table.tableName(), binding.columnName()),
                                "Path binding column must stay character-like"));
                    }
                    continue;
                }
                if (!column.isManageableScalar()) {
                    diagnostics.add(error("SCALAR_BINDING_NOT_MANAGEABLE", location(table.tableName(), binding.columnName()),
                            "Scalar binding column must be manageable scalar"));
                }
                if (DatabaseIdInternals.isPathColumn(column.normalizedColumnName())) {
                    diagnostics.add(error("SCALAR_BINDING_PATH_LIKE", location(table.tableName(), binding.columnName()),
                            "Path-like binding must not be treated as scalar"));
                }
                DatabaseIdManagedTable targetDomain = targetDomains.get(binding.targetBusinessType());
                if (binding.kind() == DatabaseIdReferenceKind.EXPLICIT_FK) {
                    if (!hasImportedKey(tableSnapshot, binding.columnName(), targetDomain.tableName(), targetDomain.identityColumn())) {
                        diagnostics.add(error("FK_BINDING_METADATA_MISSING", location(table.tableName(), binding.columnName()),
                                "Declared foreign key binding must match a real imported foreign key"));
                    }
                } else if (binding.kind() == DatabaseIdReferenceKind.SELF_REFERENCE) {
                    if (!table.tableName().equals(targetDomain.tableName())) {
                        diagnostics.add(error("SELF_BINDING_TARGET_INVALID", location(table.tableName(), binding.columnName()),
                                "Self reference must target the same identity domain table"));
                    }
                } else if (binding.kind() == DatabaseIdReferenceKind.PATH_REFERENCE && !column.isCharacterLike()) {
                    diagnostics.add(error("PATH_BINDING_NOT_CHARACTER", location(table.tableName(), binding.columnName()),
                            "Path binding column must stay character-like"));
                }
            }
        }
        return new DatabaseIdPlanValidationResult(List.copyOf(diagnostics));
    }

    public void validate(DatabaseIdPlanningContext context) {
        List<String> errors = new ArrayList<>();
        for (DatabaseIdPlanDiagnostic diagnostic : context.discoveryDiagnostics()) {
            if (diagnostic.severity() == DatabaseIdDiagnosticSeverity.ERROR) {
                errors.add(diagnostic.render());
            }
        }
        errors.addAll(context.validationResult().errors().stream().map(DatabaseIdPlanDiagnostic::render).toList());
        if (!errors.isEmpty()) {
            throw new VeloxIdGeneratorException("Database id plan validation failed: " + String.join("; ", errors));
        }
    }

    private DatabaseTableSnapshot requireTable(DatabaseIdPlanningContext context, String tableName) {
        DatabaseTableSnapshot table = context.table(tableName);
        if (table == null) {
            throw new VeloxIdGeneratorException("Managed table snapshot is missing: " + tableName);
        }
        return table;
    }

    private DatabaseColumnSnapshot requireColumn(DatabaseTableSnapshot table, String columnName) {
        DatabaseColumnSnapshot column = table.column(columnName);
        if (column == null) {
            throw new VeloxIdGeneratorException("Managed column snapshot is missing: "
                    + table.table().sqlName() + "." + columnName);
        }
        return column;
    }

    private DatabaseIdPlanDiagnostic error(String code, String location, String message) {
        return new DatabaseIdPlanDiagnostic(DatabaseIdDiagnosticSeverity.ERROR, code, location, message);
    }

    private boolean hasImportedKey(
            DatabaseTableSnapshot table,
            String fkColumn,
            String targetTableName,
            String targetColumnName
    ) {
        for (DatabaseForeignKeySnapshot foreignKey : table.importedKeys()) {
            if (foreignKey.fkColumns().size() != 1 || foreignKey.pkColumns().size() != 1) {
                continue;
            }
            if (!foreignKey.fkColumns().get(0).equalsIgnoreCase(fkColumn)) {
                continue;
            }
            if (!foreignKey.pkTable().sqlName().equalsIgnoreCase(targetTableName)) {
                continue;
            }
            if (!foreignKey.pkColumns().get(0).equalsIgnoreCase(targetColumnName)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private String location(String tableName, String columnName) {
        return tableName + "." + columnName;
    }
}
