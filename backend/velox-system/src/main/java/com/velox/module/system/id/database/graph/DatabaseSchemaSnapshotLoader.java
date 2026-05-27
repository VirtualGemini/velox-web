package com.velox.module.system.id.database.graph;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import com.velox.module.system.id.database.graph.model.DatabaseColumnSnapshot;
import com.velox.module.system.id.database.graph.model.DatabaseForeignKeySnapshot;
import com.velox.module.system.id.database.graph.model.DatabaseTableSnapshot;
import com.velox.module.system.id.database.graph.model.QualifiedColumn;
import com.velox.module.system.id.database.graph.model.QualifiedTable;
import com.velox.module.system.id.database.graph.model.SchemaSnapshot;

public class DatabaseSchemaSnapshotLoader {

    public SchemaSnapshot load(Connection connection) throws SQLException {
        String catalog = connection.getCatalog();
        String schemaPattern = resolveSchemaPattern(connection);
        Map<String, MutableTable> tables = new LinkedHashMap<>();

        try (ResultSet resultSet = connection.getMetaData().getTables(catalog, schemaPattern, "%", new String[]{"TABLE"})) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String schemaName = firstNonBlank(resultSet.getString("TABLE_SCHEM"), resultSet.getString("TABLE_CAT"));
                if (isSystemTable(tableName)) {
                    continue;
                }
                QualifiedTable table = new QualifiedTable(schemaName, tableName);
                tables.put(table.normalizedName(), new MutableTable(table));
            }
        }

        for (MutableTable table : tables.values()) {
            loadColumns(connection, table);
            loadPrimaryKeys(connection, table);
            loadImportedKeys(connection, table);
            loadUniqueKeys(connection, table);
            table.rowCount = readRowCount(connection, table.table.sqlName());
        }

        Map<String, DatabaseTableSnapshot> snapshots = tables.values().stream()
                .sorted(Comparator.comparing(table -> table.table.normalizedName()))
                .map(MutableTable::build)
                .collect(LinkedHashMap::new, (map, snapshot) -> map.put(snapshot.table().normalizedName(), snapshot), Map::putAll);
        return new SchemaSnapshot(Map.copyOf(snapshots));
    }

    private void loadColumns(Connection connection, MutableTable table) throws SQLException {
        String catalog = connection.getCatalog();
        String schemaPattern = resolveSchemaPattern(connection);
        try (ResultSet resultSet = connection.getMetaData().getColumns(catalog, schemaPattern, table.table.tableName(), "%")) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                QualifiedColumn column = new QualifiedColumn(table.table, columnName);
                table.columns.put(column.normalizedName(), new DatabaseColumnSnapshot(
                        column,
                        resultSet.getInt("DATA_TYPE"),
                        resultSet.getString("TYPE_NAME"),
                        resultSet.getInt("COLUMN_SIZE"),
                        resultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable,
                        "YES".equalsIgnoreCase(resultSet.getString("IS_AUTOINCREMENT")),
                        "YES".equalsIgnoreCase(resultSet.getString("IS_GENERATEDCOLUMN"))
                ));
            }
        }
    }

    private void loadPrimaryKeys(Connection connection, MutableTable table) throws SQLException {
        String catalog = connection.getCatalog();
        String schemaPattern = resolveSchemaPattern(connection);
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(catalog, schemaPattern, table.table.tableName())) {
            Map<Short, String> ordered = new LinkedHashMap<>();
            while (resultSet.next()) {
                ordered.put(resultSet.getShort("KEY_SEQ"), resultSet.getString("COLUMN_NAME"));
            }
            ordered.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> table.primaryKeyColumns.add(entry.getValue()));
        }
    }

    private void loadImportedKeys(Connection connection, MutableTable table) throws SQLException {
        String catalog = connection.getCatalog();
        String schemaPattern = resolveSchemaPattern(connection);
        Map<String, MutableForeignKey> builders = new LinkedHashMap<>();
        try (ResultSet resultSet = connection.getMetaData().getImportedKeys(catalog, schemaPattern, table.table.tableName())) {
            while (resultSet.next()) {
                String name = resultSet.getString("FK_NAME");
                String fkTableName = resultSet.getString("FKTABLE_NAME");
                String fkSchemaName = firstNonBlank(resultSet.getString("FKTABLE_SCHEM"), resultSet.getString("FKTABLE_CAT"));
                String pkTableName = resultSet.getString("PKTABLE_NAME");
                String pkSchemaName = firstNonBlank(resultSet.getString("PKTABLE_SCHEM"), resultSet.getString("PKTABLE_CAT"));
                short updateRule = resultSet.getShort("UPDATE_RULE");
                short deleteRule = resultSet.getShort("DELETE_RULE");
                String key = (name == null ? fkTableName + "->" + pkTableName : name).toLowerCase(Locale.ROOT);
                MutableForeignKey foreignKey = builders.computeIfAbsent(key, ignored -> new MutableForeignKey(
                        name,
                        new QualifiedTable(fkSchemaName, fkTableName),
                        new QualifiedTable(pkSchemaName, pkTableName),
                        updateRule,
                        deleteRule
                ));
                foreignKey.addColumn(
                        resultSet.getShort("KEY_SEQ"),
                        resultSet.getString("FKCOLUMN_NAME"),
                        resultSet.getString("PKCOLUMN_NAME")
                );
            }
        }
        builders.values().stream()
                .map(MutableForeignKey::build)
                .sorted(Comparator.comparing(foreignKey -> foreignKey.pkTable().normalizedName()))
                .forEach(table.importedKeys::add);
    }

    private void loadUniqueKeys(Connection connection, MutableTable table) throws SQLException {
        String catalog = connection.getCatalog();
        String schemaPattern = resolveSchemaPattern(connection);
        Map<String, Map<Short, String>> uniqueIndexes = new LinkedHashMap<>();
        try (ResultSet resultSet = connection.getMetaData().getIndexInfo(catalog, schemaPattern, table.table.tableName(), true, false)) {
            while (resultSet.next()) {
                if (resultSet.getShort("TYPE") == DatabaseMetaData.tableIndexStatistic) {
                    continue;
                }
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");
                if (indexName == null || columnName == null) {
                    continue;
                }
                uniqueIndexes.computeIfAbsent(indexName, ignored -> new LinkedHashMap<>())
                        .put(resultSet.getShort("ORDINAL_POSITION"), columnName);
            }
        }
        for (Map<Short, String> indexColumns : uniqueIndexes.values()) {
            List<String> columns = indexColumns.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .toList();
            if (!columns.isEmpty()) {
                table.uniqueKeys.add(columns);
            }
        }
    }

    private long readRowCount(Connection connection, String tableName) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM " + tableName);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (SQLException ignored) {
            // best-effort statistics
        }
        return -1L;
    }

    private boolean isSystemTable(String tableName) {
        String normalized = normalizeName(tableName);
        return normalized.startsWith("pg_")
                || normalized.startsWith("information_schema");
    }

    private String resolveSchemaPattern(Connection connection) throws SQLException {
        String schema = connection.getSchema();
        return schema == null || schema.isBlank() ? null : schema;
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private static final class MutableTable {

        private final QualifiedTable table;
        private final List<String> primaryKeyColumns = new ArrayList<>();
        private final Map<String, DatabaseColumnSnapshot> columns = new LinkedHashMap<>();
        private final List<DatabaseForeignKeySnapshot> importedKeys = new ArrayList<>();
        private final List<List<String>> uniqueKeys = new ArrayList<>();
        private long rowCount;

        private MutableTable(QualifiedTable table) {
            this.table = table;
        }

        private DatabaseTableSnapshot build() {
            return new DatabaseTableSnapshot(
                    table,
                    List.copyOf(primaryKeyColumns),
                    Map.copyOf(columns),
                    List.copyOf(importedKeys),
                    List.copyOf(uniqueKeys),
                    rowCount
            );
        }
    }

    private static final class MutableForeignKey {

        private final String name;
        private final QualifiedTable fkTable;
        private final QualifiedTable pkTable;
        private final short updateRule;
        private final short deleteRule;
        private final Map<Short, String> fkColumns = new LinkedHashMap<>();
        private final Map<Short, String> pkColumns = new LinkedHashMap<>();

        private MutableForeignKey(
                String name,
                QualifiedTable fkTable,
                QualifiedTable pkTable,
                short updateRule,
                short deleteRule
        ) {
            this.name = name;
            this.fkTable = fkTable;
            this.pkTable = pkTable;
            this.updateRule = updateRule;
            this.deleteRule = deleteRule;
        }

        private void addColumn(short keySeq, String fkColumn, String pkColumn) {
            fkColumns.put(keySeq, fkColumn);
            pkColumns.put(keySeq, pkColumn);
        }

        private DatabaseForeignKeySnapshot build() {
            return new DatabaseForeignKeySnapshot(
                    name,
                    fkTable,
                    orderedColumns(fkColumns),
                    pkTable,
                    orderedColumns(pkColumns),
                    updateRule,
                    deleteRule
            );
        }

        private List<String> orderedColumns(Map<Short, String> columns) {
            return columns.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .toList();
        }
    }
}
