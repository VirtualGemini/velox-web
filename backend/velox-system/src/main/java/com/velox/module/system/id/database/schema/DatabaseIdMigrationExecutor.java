package com.velox.module.system.id.database.schema;

import com.velox.module.system.id.database.graph.model.DatabaseColumnSnapshot;
import com.velox.module.system.id.database.graph.model.QualifiedTable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import com.velox.module.system.id.database.discovery.DatabaseIdDiscoverySupport;
import com.velox.module.system.id.database.discovery.model.DatabaseIdColumnBinding;
import com.velox.module.system.id.database.discovery.model.DatabaseIdManagedTable;
import com.velox.module.system.id.database.graph.model.QualifiedColumn;
import com.velox.module.system.id.database.plan.model.DatabaseIdReconciliationPlan;
import com.velox.module.system.id.database.internal.DatabaseIdInternals;

public class DatabaseIdMigrationExecutor {

    private static final int TEMP_CHARACTER_ID_COLUMN_SIZE = 64;

    public void execute(Connection connection, DatabaseIdReconciliationPlan plan) throws SQLException {
        if (!plan.needsMigration()) {
            return;
        }
        ForeignKeyGuard foreignKeyGuard = ForeignKeyGuard.open(connection, plan.availableTables().values());
        try {
            expandManagedCharacterColumns(connection, plan);
            for (DatabaseIdManagedTable table : plan.availableTables().values()) {
                Map<String, String> mappings = plan.idMappings().get(table.identityMappingBusinessType());
                if (table.identityColumn() != null) {
                    applyUpdates(connection, table.tableName(), table.identityColumn(), mappings);
                }
            }
            for (DatabaseIdManagedTable table : plan.availableTables().values()) {
                for (DatabaseIdColumnBinding binding : table.bindings()) {
                    Map<String, String> targetMappings = plan.idMappings().get(binding.targetBusinessType());
                    if (binding.pathLike()) {
                        applyPathUpdates(connection, table.tableName(), binding.columnName(), targetMappings);
                    } else {
                        applyUpdates(connection, table.tableName(), binding.columnName(), targetMappings);
                    }
                }
            }
            alignManagedColumnsToBigint(connection, plan);
        } finally {
            foreignKeyGuard.close();
        }
    }

    private void expandManagedCharacterColumns(Connection connection, DatabaseIdReconciliationPlan plan) throws SQLException {
        for (DatabaseIdManagedTable table : plan.availableTables().values()) {
            if (table.identityColumn() != null) {
                expandManagedCharacterColumn(connection, table.tableName(), table.identityColumn());
            }
            for (DatabaseIdColumnBinding binding : table.bindings()) {
                if (binding.pathLike()) {
                    continue;
                }
                expandManagedCharacterColumn(connection, table.tableName(), binding.columnName());
            }
        }
    }

    private void alignManagedColumnsToBigint(Connection connection, DatabaseIdReconciliationPlan plan) throws SQLException {
        for (DatabaseIdManagedTable table : plan.availableTables().values()) {
            if (table.identityColumn() != null) {
                convertManagedColumnToBigint(connection, table.tableName(), table.identityColumn());
            }
            for (DatabaseIdColumnBinding binding : table.bindings()) {
                if (binding.pathLike()) {
                    continue;
                }
                convertManagedColumnToBigint(connection, table.tableName(), binding.columnName());
            }
        }
    }

    private void applyUpdates(Connection connection, String tableName, String columnName, Map<String, String> mappings)
            throws SQLException {
        if (mappings == null || mappings.isEmpty()) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + tableName + " SET " + columnName + " = ? WHERE " + columnName + " = ?")) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                statement.setString(1, entry.getValue());
                statement.setString(2, entry.getKey());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void applyPathUpdates(Connection connection, String tableName, String columnName, Map<String, String> mappings)
            throws SQLException {
        if (mappings == null || mappings.isEmpty()) {
            return;
        }
        List<String> values = readAllDistinctValues(connection, tableName, columnName);
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + tableName + " SET " + columnName + " = ? WHERE " + columnName + " = ?")) {
            for (String value : values) {
                String rewritten = rewritePathValue(value, mappings);
                if (rewritten == null || rewritten.equals(value)) {
                    continue;
                }
                statement.setString(1, rewritten);
                statement.setString(2, value);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private String rewritePathValue(String currentValue, Map<String, String> mappings) {
        if (currentValue == null || currentValue.isBlank() || mappings == null || mappings.isEmpty()) {
            return currentValue;
        }
        Matcher matcher = DatabaseIdInternals.PATH_TOKEN_PATTERN.matcher(currentValue);
        StringBuilder builder = new StringBuilder();
        int lastIndex = 0;
        boolean changed = false;
        while (matcher.find()) {
            builder.append(currentValue, lastIndex, matcher.start());
            String token = matcher.group();
            String replacement = mappings.get(token);
            if (replacement != null) {
                builder.append(replacement);
                changed = true;
            } else {
                builder.append(token);
            }
            lastIndex = matcher.end();
        }
        if (!changed) {
            return currentValue;
        }
        builder.append(currentValue.substring(lastIndex));
        return builder.toString();
    }

    private void expandManagedCharacterColumn(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseColumnSnapshot columnMetadata = readColumnMetadata(connection, tableName, columnName);
        if (columnMetadata == null || !columnMetadata.isCharacterLike()) {
            return;
        }
        if (columnMetadata.columnSize() >= TEMP_CHARACTER_ID_COLUMN_SIZE) {
            return;
        }
        String nullableClause = columnMetadata.nullable() ? "NULL" : "NOT NULL";
        try (Statement statement = connection.createStatement()) {
            String databaseProduct = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
            if (databaseProduct.contains("mysql")) {
                statement.execute(
                        "ALTER TABLE " + tableName
                                + " MODIFY COLUMN " + columnName + " "
                                + columnMetadata.renderCharacterType(TEMP_CHARACTER_ID_COLUMN_SIZE)
                                + " " + nullableClause
                );
                return;
            }
            statement.execute(
                    "ALTER TABLE " + tableName
                            + " ALTER COLUMN " + columnName + " TYPE "
                            + columnMetadata.renderCharacterType(TEMP_CHARACTER_ID_COLUMN_SIZE)
            );
        }
    }

    private void convertManagedColumnToBigint(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseColumnSnapshot columnMetadata = readColumnMetadata(connection, tableName, columnName);
        if (columnMetadata == null || columnMetadata.isBigintLike()) {
            return;
        }
        String nullableClause = columnMetadata.nullable() ? "NULL" : "NOT NULL";
        String databaseProduct = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
        try (Statement statement = connection.createStatement()) {
            if (databaseProduct.contains("mysql")) {
                statement.execute(
                        "ALTER TABLE " + tableName
                                + " MODIFY COLUMN " + columnName + " bigint " + nullableClause
                );
                return;
            }
            if (databaseProduct.contains("postgresql")) {
                statement.execute(
                        "ALTER TABLE " + tableName
                                + " ALTER COLUMN " + columnName + " TYPE bigint USING "
                                + renderBigintUsingExpression(columnName, columnMetadata)
                );
                return;
            }
            if (databaseProduct.contains("h2")) {
                statement.execute("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " BIGINT");
                return;
            }
            statement.execute(
                    "ALTER TABLE " + tableName
                            + " ALTER COLUMN " + columnName + " TYPE bigint"
            );
        }
    }

    private String renderBigintUsingExpression(String columnName, DatabaseColumnSnapshot columnMetadata) {
        if (columnMetadata.isCharacterLike()) {
            return "NULLIF(BTRIM(" + columnName + "), '')::bigint";
        }
        return columnName + "::bigint";
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

    private DatabaseColumnSnapshot readColumnMetadata(Connection connection, String tableName, String columnName) throws SQLException {
        String catalog = connection.getCatalog();
        String schemaPattern = resolveSchemaPattern(connection);
        try (ResultSet resultSet = connection.getMetaData().getColumns(catalog, schemaPattern, tableName, columnName)) {
            if (resultSet.next()) {
                return toColumnSnapshot(connection, tableName, resultSet);
            }
        }
        try (ResultSet fallback = connection.getMetaData().getColumns(catalog, null, tableName, columnName)) {
            if (!fallback.next()) {
                return null;
            }
            return toColumnSnapshot(connection, tableName, fallback);
        }
    }

    private DatabaseColumnSnapshot toColumnSnapshot(Connection connection, String tableName, ResultSet resultSet) throws SQLException {
        String schemaName = resolveSchemaPattern(connection);
        QualifiedTable table = new QualifiedTable(schemaName, tableName);
        return new DatabaseColumnSnapshot(
                new com.velox.module.system.id.database.graph.model.QualifiedColumn(table, resultSet.getString("COLUMN_NAME")),
                resultSet.getInt("DATA_TYPE"),
                resultSet.getString("TYPE_NAME"),
                resultSet.getInt("COLUMN_SIZE"),
                resultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable,
                "YES".equalsIgnoreCase(resultSet.getString("IS_AUTOINCREMENT")),
                "YES".equalsIgnoreCase(resultSet.getString("IS_GENERATEDCOLUMN"))
        );
    }

    private String resolveSchemaPattern(Connection connection) throws SQLException {
        String schema = connection.getSchema();
        return schema == null || schema.isBlank() ? null : schema;
    }

    private static final class ForeignKeyGuard implements AutoCloseable {

        private final Connection connection;
        private final String databaseProduct;
        private final List<ForeignKeyConstraint> droppedConstraints;

        private ForeignKeyGuard(Connection connection, String databaseProduct, List<ForeignKeyConstraint> droppedConstraints) {
            this.connection = connection;
            this.databaseProduct = databaseProduct;
            this.droppedConstraints = droppedConstraints;
        }

        static ForeignKeyGuard open(Connection connection, Collection<DatabaseIdManagedTable> managedTables) throws SQLException {
            String databaseProduct = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
            if (databaseProduct.contains("h2")) {
                execute(connection, "SET REFERENTIAL_INTEGRITY FALSE");
                return new ForeignKeyGuard(connection, databaseProduct, List.of());
            }
            if (databaseProduct.contains("mysql")) {
                execute(connection, "SET FOREIGN_KEY_CHECKS = 0");
                return new ForeignKeyGuard(connection, databaseProduct, List.of());
            }
            if (databaseProduct.contains("postgresql")) {
                List<ForeignKeyConstraint> constraints = loadForeignKeys(connection, managedTables);
                for (ForeignKeyConstraint constraint : constraints) {
                    execute(connection, constraint.dropSql());
                }
                return new ForeignKeyGuard(connection, databaseProduct, constraints);
            }
            return new ForeignKeyGuard(connection, databaseProduct, List.of());
        }

        @Override
        public void close() throws SQLException {
            if (databaseProduct.contains("h2")) {
                execute(connection, "SET REFERENTIAL_INTEGRITY TRUE");
                return;
            }
            if (databaseProduct.contains("mysql")) {
                execute(connection, "SET FOREIGN_KEY_CHECKS = 1");
                return;
            }
            if (databaseProduct.contains("postgresql")) {
                for (ForeignKeyConstraint constraint : droppedConstraints) {
                    execute(connection, constraint.addSql());
                }
            }
        }

        private static List<ForeignKeyConstraint> loadForeignKeys(Connection connection, Collection<DatabaseIdManagedTable> managedTables)
                throws SQLException {
            java.util.Map<String, ForeignKeyConstraintBuilder> builders = new java.util.LinkedHashMap<>();
            for (DatabaseIdManagedTable table : managedTables) {
                try (ResultSet resultSet = connection.getMetaData().getImportedKeys(null, null, table.tableName())) {
                    while (resultSet.next()) {
                        String fkName = resultSet.getString("FK_NAME");
                        if (fkName == null || fkName.isBlank()) {
                            continue;
                        }
                        String fkTable = resultSet.getString("FKTABLE_NAME");
                        String pkTable = resultSet.getString("PKTABLE_NAME");
                        short updateRule = resultSet.getShort("UPDATE_RULE");
                        short deleteRule = resultSet.getShort("DELETE_RULE");
                        String key = fkTable + ":" + fkName;
                        ForeignKeyConstraintBuilder builder = builders.computeIfAbsent(
                                key,
                                ignored -> new ForeignKeyConstraintBuilder(
                                        fkName,
                                        fkTable,
                                        pkTable,
                                        updateRule,
                                        deleteRule
                                )
                        );
                        builder.addColumn(
                                resultSet.getShort("KEY_SEQ"),
                                resultSet.getString("FKCOLUMN_NAME"),
                                resultSet.getString("PKCOLUMN_NAME")
                        );
                    }
                }
            }
            return builders.values().stream()
                    .map(ForeignKeyConstraintBuilder::build)
                    .sorted(Comparator.comparing(ForeignKeyConstraint::fkTable).thenComparing(ForeignKeyConstraint::name))
                    .toList();
        }

        private static void execute(Connection connection, String sql) throws SQLException {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
    }

    private record ForeignKeyConstraint(
            String name,
            String fkTable,
            String pkTable,
            List<ForeignKeyColumn> columns,
            short updateRule,
            short deleteRule
    ) {

        String dropSql() {
            return "ALTER TABLE " + fkTable + " DROP CONSTRAINT " + name;
        }

        String addSql() {
            String fkColumns = joinColumns(columns.stream().map(ForeignKeyColumn::fkColumn).toList());
            String pkColumns = joinColumns(columns.stream().map(ForeignKeyColumn::pkColumn).toList());
            return "ALTER TABLE " + fkTable
                    + " ADD CONSTRAINT " + name
                    + " FOREIGN KEY (" + fkColumns + ") REFERENCES " + pkTable + " (" + pkColumns + ")"
                    + renderRule(" ON UPDATE ", updateRule)
                    + renderRule(" ON DELETE ", deleteRule);
        }

        private static String joinColumns(List<String> columns) {
            StringJoiner joiner = new StringJoiner(", ");
            for (String column : columns) {
                joiner.add(column);
            }
            return joiner.toString();
        }

        private static String renderRule(String prefix, short rule) {
            return switch (rule) {
                case DatabaseMetaData.importedKeyCascade -> prefix + "CASCADE";
                case DatabaseMetaData.importedKeySetNull -> prefix + "SET NULL";
                case DatabaseMetaData.importedKeySetDefault -> prefix + "SET DEFAULT";
                case DatabaseMetaData.importedKeyRestrict -> prefix + "RESTRICT";
                default -> "";
            };
        }
    }

    private record ForeignKeyColumn(short keySeq, String fkColumn, String pkColumn) {
    }

    private static final class ForeignKeyConstraintBuilder {

        private final String name;
        private final String fkTable;
        private final String pkTable;
        private final short updateRule;
        private final short deleteRule;
        private final List<ForeignKeyColumn> columns = new ArrayList<>();

        private ForeignKeyConstraintBuilder(String name, String fkTable, String pkTable, short updateRule, short deleteRule) {
            this.name = name;
            this.fkTable = fkTable;
            this.pkTable = pkTable;
            this.updateRule = updateRule;
            this.deleteRule = deleteRule;
        }

        void addColumn(short keySeq, String fkColumn, String pkColumn) {
            columns.add(new ForeignKeyColumn(keySeq, fkColumn, pkColumn));
        }

        ForeignKeyConstraint build() {
            List<ForeignKeyColumn> orderedColumns = columns.stream()
                    .sorted(Comparator.comparingInt(ForeignKeyColumn::keySeq))
                    .toList();
            return new ForeignKeyConstraint(name, fkTable, pkTable, orderedColumns, updateRule, deleteRule);
        }
    }
}
