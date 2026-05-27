package com.velox.module.system.id.database.internal;

import com.velox.module.system.id.database.graph.model.QualifiedTable;

import java.util.Locale;
import java.util.regex.Pattern;

public final class DatabaseIdInternals {

    public static final Pattern PATH_TOKEN_PATTERN = Pattern.compile("[^/,;|>\\s]+");

    private DatabaseIdInternals() {
    }

    public static String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static String sampleKey(QualifiedTable table, String columnName) {
        return table.normalizedName() + "|" + normalizeName(columnName);
    }

    public static boolean isPathColumn(String columnName) {
        String normalized = normalizeName(columnName);
        return normalized.contains("path")
                || normalized.endsWith("_ids")
                || normalized.endsWith("_path")
                || normalized.contains("ancestor")
                || normalized.contains("descendant");
    }
}
