package com.velox.module.system.id.database.graph.model;

import java.sql.Types;
import java.util.Locale;

public record DatabaseColumnSnapshot(
        QualifiedColumn column,
        int dataType,
        String typeName,
        int columnSize,
        boolean nullable,
        boolean autoIncrement,
        boolean generated
) {

    public String columnName() {
        return column.columnName();
    }

    public String normalizedColumnName() {
        return column.normalizedName();
    }

    public boolean isCharacterLike() {
        return dataType == Types.VARCHAR
                || dataType == Types.CHAR
                || dataType == Types.LONGVARCHAR
                || dataType == Types.NVARCHAR
                || dataType == Types.NCHAR
                || dataType == Types.LONGNVARCHAR
                || dataType == Types.CLOB
                || dataType == Types.NCLOB;
    }

    public boolean isIntegralLike() {
        return dataType == Types.BIGINT
                || dataType == Types.INTEGER
                || dataType == Types.SMALLINT
                || dataType == Types.TINYINT
                || dataType == Types.NUMERIC
                || dataType == Types.DECIMAL;
    }

    public boolean isBigintLike() {
        String normalizedType = typeName == null ? "" : typeName.toLowerCase(Locale.ROOT);
        return dataType == Types.BIGINT || "bigint".equals(normalizedType) || "int8".equals(normalizedType);
    }

    public boolean isManageableScalar() {
        return isCharacterLike() || isIntegralLike();
    }

    public String renderCharacterType(int targetSize) {
        String normalizedType = typeName == null ? "varchar" : typeName.toLowerCase(Locale.ROOT);
        if (normalizedType.contains("text")) {
            return normalizedType;
        }
        if (normalizedType.contains("char")) {
            return normalizedType + "(" + targetSize + ")";
        }
        return "varchar(" + targetSize + ")";
    }
}
