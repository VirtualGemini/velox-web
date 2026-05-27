package com.velox.module.system.id.database.discovery;

import com.velox.module.system.id.database.SystemDatabaseIdGovernanceProperties;
import com.velox.module.system.id.database.graph.model.DatabaseColumnSnapshot;
import com.velox.module.system.id.database.graph.model.DatabaseTableSnapshot;
import com.velox.module.system.id.database.graph.model.QualifiedTable;
import com.velox.module.system.id.database.graph.model.SchemaSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.velox.module.system.id.database.internal.DatabaseIdInternals.normalizeName;
import com.velox.module.system.id.database.discovery.model.DatabaseIdColumnBinding;
import com.velox.module.system.id.database.discovery.model.DatabaseIdDiscoveryResult;
import com.velox.module.system.id.database.discovery.model.DatabaseIdManagedTable;
import com.velox.module.system.id.database.discovery.model.DatabaseIdReferenceKind;
import com.velox.module.system.id.database.diagnostic.DatabaseIdDiagnosticSeverity;
import com.velox.module.system.id.database.diagnostic.DatabaseIdPlanDiagnostic;

public final class DatabaseIdDiscoverySupport {

    private DatabaseIdDiscoverySupport() {
    }

    static Map<String, DatabaseIdManagedTable> discoverManagedTables(
            SchemaSnapshot snapshot,
            Map<String, Set<String>> sampleCache,
            SystemDatabaseIdGovernanceProperties governanceProperties
    ) {
        return discover(snapshot, sampleCache, governanceProperties).managedTables();
    }

    public static DatabaseIdDiscoveryResult discover(
            SchemaSnapshot snapshot,
            Map<String, Set<String>> sampleCache,
            SystemDatabaseIdGovernanceProperties governanceProperties
    ) {
        List<DatabaseIdPlanDiagnostic> diagnostics = new ArrayList<>();
        Map<String, DatabaseIdManagedTableBuilder> builders = new LinkedHashMap<>();
        Map<String, String> domainBusinessTypes = new LinkedHashMap<>();

        loadDeclaredDomains(snapshot, governanceProperties, builders, domainBusinessTypes, diagnostics);
        loadDeclaredReferences(snapshot, governanceProperties, builders, domainBusinessTypes, diagnostics);
        loadImportedForeignKeys(snapshot, builders, diagnostics);
        reportIgnoredTableConflicts(governanceProperties, diagnostics);
        reportUndeclaredTables(snapshot, governanceProperties, diagnostics);

        Map<String, DatabaseIdManagedTable> managedTables = builders.values().stream()
                .map(DatabaseIdManagedTableBuilder::build)
                .sorted(Comparator.comparing(DatabaseIdManagedTable::tableName))
                .collect(Collectors.toMap(
                        table -> normalizeName(table.tableName()),
                        table -> table,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        return new DatabaseIdDiscoveryResult(managedTables, List.copyOf(diagnostics));
    }

    private static void loadDeclaredDomains(
            SchemaSnapshot snapshot,
            SystemDatabaseIdGovernanceProperties governanceProperties,
            Map<String, DatabaseIdManagedTableBuilder> builders,
            Map<String, String> domainBusinessTypes,
            List<DatabaseIdPlanDiagnostic> diagnostics
    ) {
        for (SystemDatabaseIdGovernanceProperties.DomainDeclaration declaration : governanceProperties.getDomains()) {
            if (!declaration.isEnabled()) {
                continue;
            }

            String declaredTable = normalizeName(declaration.getTable());
            if (declaredTable.isBlank()) {
                diagnostics.add(error("DOMAIN_TABLE_MISSING", "<governance.domain>", "Domain declaration table must not be blank"));
                continue;
            }

            String businessType = normalizeBusinessType(
                    declaration.getBusinessType() == null || declaration.getBusinessType().isBlank()
                            ? declaration.getTable()
                            : declaration.getBusinessType()
            );
            if (businessType.isBlank()) {
                diagnostics.add(error("DOMAIN_BUSINESS_TYPE_MISSING", declaration.getTable(),
                        "Domain declaration business type must not be blank"));
                continue;
            }

            DatabaseTableSnapshot table = resolveDeclaredTable(snapshot, declaration.getTable(), diagnostics, "DOMAIN_TABLE_NOT_FOUND");
            if (table == null) {
                continue;
            }
            String normalizedTable = table.table().normalizedName();

            if (builders.containsKey(normalizedTable) && builders.get(normalizedTable).identityColumn() != null) {
                diagnostics.add(error("DUPLICATE_DOMAIN_TABLE", table.table().sqlName(),
                        "Multiple identity domain declarations point to the same table"));
                continue;
            }
            if (domainBusinessTypes.containsKey(businessType)) {
                diagnostics.add(error("DUPLICATE_BUSINESS_TYPE", table.table().sqlName(),
                        "Multiple identity domains declare the same business type: " + businessType));
                continue;
            }

            String identityColumn = resolveColumnName(table, declaration.getIdColumn());
            if (identityColumn == null) {
                diagnostics.add(error("DOMAIN_ID_COLUMN_MISSING", table.table().sqlName(),
                        "Declared identity column does not exist: " + declaration.getIdColumn()));
                continue;
            }

            DatabaseIdManagedTableBuilder existing = builders.get(normalizedTable);
            if (existing != null) {
                builders.put(normalizedTable, new DatabaseIdManagedTableBuilder(
                        table.table(),
                        identityColumn,
                        businessType,
                        existing.bindings()
                ));
            } else {
                builders.put(normalizedTable, new DatabaseIdManagedTableBuilder(table.table(), identityColumn, businessType));
            }
            domainBusinessTypes.put(businessType, table.table().sqlName());
        }
    }

    private static void loadDeclaredReferences(
            SchemaSnapshot snapshot,
            SystemDatabaseIdGovernanceProperties governanceProperties,
            Map<String, DatabaseIdManagedTableBuilder> builders,
            Map<String, String> domainBusinessTypes,
            List<DatabaseIdPlanDiagnostic> diagnostics
    ) {
        for (SystemDatabaseIdGovernanceProperties.TableReferenceDeclaration declaration : governanceProperties.getReferences()) {
            if (!declaration.isEnabled()) {
                continue;
            }

            String declaredTable = normalizeName(declaration.getTable());
            if (declaredTable.isBlank()) {
                diagnostics.add(error("REFERENCE_TABLE_MISSING", "<governance.reference>",
                        "Reference declaration table must not be blank"));
                continue;
            }

            DatabaseTableSnapshot table = resolveDeclaredTable(snapshot, declaration.getTable(), diagnostics, "REFERENCE_TABLE_NOT_FOUND");
            if (table == null) {
                continue;
            }
            String normalizedTable = table.table().normalizedName();

            DatabaseIdManagedTableBuilder sourceBuilder = builders.computeIfAbsent(
                    normalizedTable,
                    ignored -> new DatabaseIdManagedTableBuilder(
                            table.table(),
                            null,
                            normalizeBusinessType(table.table().tableName())
                    )
            );

            for (SystemDatabaseIdGovernanceProperties.ReferenceMappingDeclaration mapping : declaration.getMappings()) {
                if (!mapping.isEnabled()) {
                    continue;
                }

                String columnName = resolveColumnName(table, mapping.getColumn());
                if (columnName == null) {
                    diagnostics.add(error("REFERENCE_COLUMN_MISSING", table.table().sqlName(),
                            "Declared reference column does not exist: " + mapping.getColumn()));
                    continue;
                }

                String targetBusinessType = normalizeBusinessType(mapping.getTargetBusinessType());
                if (targetBusinessType.isBlank() || !domainBusinessTypes.containsKey(targetBusinessType)) {
                    diagnostics.add(error("REFERENCE_TARGET_MISSING",
                            table.table().sqlName() + "." + columnName,
                            "Declared reference target business type is missing: " + mapping.getTargetBusinessType()));
                    continue;
                }

                BindingDescriptor descriptor = resolveBindingDescriptor(mapping.getKind());
                if (descriptor == null) {
                    diagnostics.add(error("REFERENCE_KIND_INVALID",
                            table.table().sqlName() + "." + columnName,
                            "Unsupported declared reference kind: " + mapping.getKind()));
                    continue;
                }

                if (descriptor.kind() == DatabaseIdReferenceKind.SELF_REFERENCE
                        && !targetBusinessType.equals(sourceBuilder.identityMappingBusinessType())) {
                    diagnostics.add(error("SELF_REFERENCE_TARGET_MISMATCH",
                            table.table().sqlName() + "." + columnName,
                            "Self reference must target the source table identity business type"));
                    continue;
                }

                DatabaseIdColumnBinding declaredBinding = new DatabaseIdColumnBinding(
                        columnName,
                        targetBusinessType,
                        descriptor.kind(),
                        descriptor.pathLike()
                );
                DatabaseIdColumnBinding existing = sourceBuilder.binding(columnName);
                if (existing != null) {
                    if (sameBinding(existing, declaredBinding)) {
                        diagnostics.add(warning("REFERENCE_DUPLICATED",
                                table.table().sqlName() + "." + columnName,
                                "Duplicate declared reference mapping was ignored"));
                    } else {
                        diagnostics.add(error("REFERENCE_CONFLICT",
                                table.table().sqlName() + "." + columnName,
                                "Declared reference mapping conflicts with another mapping on the same column"));
                    }
                    continue;
                }
                sourceBuilder.addBinding(declaredBinding);
            }
        }
    }

    private static void loadImportedForeignKeys(
            SchemaSnapshot snapshot,
            Map<String, DatabaseIdManagedTableBuilder> builders,
            List<DatabaseIdPlanDiagnostic> diagnostics
    ) {
        Map<String, DatabaseIdManagedTableBuilder> managedDomainsByTable = builders.values().stream()
                .filter(builder -> builder.identityColumn() != null)
                .collect(Collectors.toMap(
                        builder -> builder.table().normalizedName(),
                        builder -> builder,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        for (DatabaseTableSnapshot table : snapshot.tables().values()) {
            for (var foreignKey : table.importedKeys()) {
                DatabaseIdManagedTableBuilder targetBuilder = managedDomainsByTable.get(foreignKey.pkTable().normalizedName());
                if (targetBuilder == null) {
                    continue;
                }
                if (foreignKey.fkColumns().size() != 1 || foreignKey.pkColumns().size() != 1) {
                    diagnostics.add(error("FK_BINDING_COMPOSITE_UNSUPPORTED", foreignKey.fkTable().sqlName(),
                            "Composite foreign key is not supported for database ID governance"));
                    continue;
                }
                String pkColumn = foreignKey.pkColumns().get(0);
                if (!pkColumn.equalsIgnoreCase(targetBuilder.identityColumn())) {
                    continue;
                }
                DatabaseIdManagedTableBuilder sourceBuilder = builders.computeIfAbsent(
                        table.table().normalizedName(),
                        ignored -> new DatabaseIdManagedTableBuilder(
                                table.table(),
                                null,
                                normalizeBusinessType(table.table().tableName())
                        )
                );
                String fkColumn = foreignKey.fkColumns().get(0);
                DatabaseIdReferenceKind foreignKeyKind = table.table().normalizedName().equals(foreignKey.pkTable().normalizedName())
                        ? DatabaseIdReferenceKind.SELF_REFERENCE
                        : DatabaseIdReferenceKind.EXPLICIT_FK;
                DatabaseIdColumnBinding actualBinding = new DatabaseIdColumnBinding(
                        fkColumn,
                        targetBuilder.identityMappingBusinessType(),
                        foreignKeyKind,
                        false
                );
                DatabaseIdColumnBinding declaredBinding = sourceBuilder.binding(fkColumn);
                if (declaredBinding == null) {
                    sourceBuilder.addBinding(actualBinding);
                    diagnostics.add(warning("FK_REFERENCE_DISCOVERED",
                            table.table().sqlName() + "." + fkColumn,
                            "Imported foreign key was automatically included in the governance graph"));
                    continue;
                }
                if (!normalizeBusinessType(declaredBinding.targetBusinessType()).equals(actualBinding.targetBusinessType())) {
                    diagnostics.add(error("FK_REFERENCE_TARGET_CONFLICT",
                            table.table().sqlName() + "." + fkColumn,
                            "Declared reference target conflicts with imported foreign key target"));
                    continue;
                }
                if (declaredBinding.pathLike()) {
                    diagnostics.add(error("FK_REFERENCE_PATH_CONFLICT",
                            table.table().sqlName() + "." + fkColumn,
                            "Imported foreign key column cannot be declared as a path reference"));
                    continue;
                }
                if (declaredBinding.kind() != actualBinding.kind()) {
                    sourceBuilder.addBinding(actualBinding);
                    diagnostics.add(warning("FK_REFERENCE_KIND_NORMALIZED",
                            table.table().sqlName() + "." + fkColumn,
                            "Declared reference kind was normalized to imported foreign key semantics"));
                }
            }
        }
    }

    public static final String UNDECLARED_TABLE_DIAGNOSTIC_CODE = "UNDECLARED_MANAGED_TABLE";
    public static final String IGNORED_TABLE_CONFLICT_DIAGNOSTIC_CODE = "IGNORED_TABLE_CONFLICT";

    private static void reportIgnoredTableConflicts(
            SystemDatabaseIdGovernanceProperties governanceProperties,
            List<DatabaseIdPlanDiagnostic> diagnostics
    ) {
        Set<String> declared = new java.util.HashSet<>();
        for (SystemDatabaseIdGovernanceProperties.DomainDeclaration domain : governanceProperties.getDomains()) {
            declared.add(normalizeName(domain.getTable()));
        }
        for (SystemDatabaseIdGovernanceProperties.TableReferenceDeclaration reference : governanceProperties.getReferences()) {
            declared.add(normalizeName(reference.getTable()));
        }
        declared.remove("");
        for (String ignored : governanceProperties.getIgnoredTables()) {
            String normalized = normalizeName(ignored);
            if (normalized.isBlank()) {
                continue;
            }
            if (declared.contains(normalized)) {
                diagnostics.add(error(IGNORED_TABLE_CONFLICT_DIAGNOSTIC_CODE, ignored,
                        "Table is both declared under governance.domains/references and listed in governance.ignored-tables; remove it from one side"));
            }
        }
    }

    private static void reportUndeclaredTables(
            SchemaSnapshot snapshot,
            SystemDatabaseIdGovernanceProperties governanceProperties,
            List<DatabaseIdPlanDiagnostic> diagnostics
    ) {
        Set<String> covered = new java.util.HashSet<>();
        for (SystemDatabaseIdGovernanceProperties.DomainDeclaration domain : governanceProperties.getDomains()) {
            covered.add(normalizeName(domain.getTable()));
        }
        for (SystemDatabaseIdGovernanceProperties.TableReferenceDeclaration reference : governanceProperties.getReferences()) {
            covered.add(normalizeName(reference.getTable()));
        }
        for (String ignored : governanceProperties.getIgnoredTables()) {
            covered.add(normalizeName(ignored));
        }
        covered.remove("");

        for (DatabaseTableSnapshot table : snapshot.tables().values()) {
            String normalizedTable = normalizeName(table.table().tableName());
            if (covered.contains(normalizedTable) || covered.contains(table.table().normalizedName())) {
                continue;
            }
            diagnostics.add(warning(UNDECLARED_TABLE_DIAGNOSTIC_CODE, table.table().sqlName(),
                    "Table is not covered by velox.id.database.governance and will be skipped by id reconciliation"));
        }
    }

    private static BindingDescriptor resolveBindingDescriptor(String kind) {
        String normalizedKind = normalizeName(kind);
        return switch (normalizedKind) {
            case "", "explicit", "reference", "scalar", "closure" ->
                    new BindingDescriptor(DatabaseIdReferenceKind.EXPLICIT_REFERENCE, false);
            case "fk", "foreign_key", "foreign-key" ->
                    new BindingDescriptor(DatabaseIdReferenceKind.EXPLICIT_FK, false);
            case "audit" ->
                    new BindingDescriptor(DatabaseIdReferenceKind.AUDIT_REFERENCE, false);
            case "self", "self_reference", "self-reference" ->
                    new BindingDescriptor(DatabaseIdReferenceKind.SELF_REFERENCE, false);
            case "path", "path_reference", "path-reference" ->
                    new BindingDescriptor(DatabaseIdReferenceKind.PATH_REFERENCE, true);
            default -> null;
        };
    }

    private static boolean sameBinding(DatabaseIdColumnBinding left, DatabaseIdColumnBinding right) {
        return normalizeName(left.columnName()).equals(normalizeName(right.columnName()))
                && normalizeBusinessType(left.targetBusinessType()).equals(normalizeBusinessType(right.targetBusinessType()))
                && left.kind() == right.kind()
                && left.pathLike() == right.pathLike();
    }

    private static String resolveColumnName(DatabaseTableSnapshot table, String configuredColumn) {
        String normalizedColumn = normalizeName(configuredColumn);
        if (normalizedColumn.isBlank()) {
            return null;
        }
        for (DatabaseColumnSnapshot column : table.columns().values()) {
            if (column.normalizedColumnName().equals(normalizedColumn)) {
                return column.columnName();
            }
        }
        return null;
    }

    private static DatabaseTableSnapshot resolveDeclaredTable(
            SchemaSnapshot snapshot,
            String declaredTable,
            List<DatabaseIdPlanDiagnostic> diagnostics,
            String notFoundCode
    ) {
        String normalizedTable = normalizeName(declaredTable);
        DatabaseTableSnapshot exact = snapshot.tables().get(normalizedTable);
        if (exact != null) {
            return exact;
        }

        List<DatabaseTableSnapshot> matches = snapshot.tables().values().stream()
                .filter(table -> normalizeName(table.table().tableName()).equals(normalizedTable))
                .sorted(Comparator.comparing(table -> table.table().normalizedName()))
                .toList();
        if (matches.isEmpty()) {
            diagnostics.add(error(notFoundCode, declaredTable, "Declared table does not exist"));
            return null;
        }
        if (matches.size() > 1) {
            diagnostics.add(error("DECLARED_TABLE_AMBIGUOUS", declaredTable,
                    "Declared table matches multiple schema-qualified tables: "
                            + matches.stream().map(table -> table.table().sqlName()).collect(Collectors.joining(", "))));
            return null;
        }
        return matches.get(0);
    }

    private static String normalizeBusinessType(String businessType) {
        return businessType == null ? "" : businessType.trim().toLowerCase(Locale.ROOT);
    }

    private static DatabaseIdPlanDiagnostic error(String code, String location, String message) {
        return new DatabaseIdPlanDiagnostic(DatabaseIdDiagnosticSeverity.ERROR, code, location, message);
    }

    private static DatabaseIdPlanDiagnostic warning(String code, String location, String message) {
        return new DatabaseIdPlanDiagnostic(DatabaseIdDiagnosticSeverity.WARNING, code, location, message);
    }

    private record BindingDescriptor(
            DatabaseIdReferenceKind kind,
            boolean pathLike
    ) {
    }

    record DatabaseIdManagedTableBuilder(
            QualifiedTable table,
            String identityColumn,
            String identityMappingBusinessType,
            Map<String, DatabaseIdColumnBinding> bindings
    ) {

        DatabaseIdManagedTableBuilder(QualifiedTable table, String identityColumn, String identityMappingBusinessType) {
            this(table, identityColumn, identityMappingBusinessType, new LinkedHashMap<>());
        }

        void addBinding(DatabaseIdColumnBinding binding) {
            bindings.put(normalizeName(binding.columnName()), binding);
        }

        DatabaseIdColumnBinding binding(String columnName) {
            return bindings.get(normalizeName(columnName));
        }

        DatabaseIdManagedTable build() {
            List<DatabaseIdColumnBinding> orderedBindings = bindings.values().stream()
                    .sorted(Comparator.comparing(DatabaseIdColumnBinding::columnName))
                    .toList();
            return new DatabaseIdManagedTable(
                    table.sqlName(),
                    identityColumn,
                    identityMappingBusinessType,
                    List.copyOf(orderedBindings)
            );
        }
    }
}
