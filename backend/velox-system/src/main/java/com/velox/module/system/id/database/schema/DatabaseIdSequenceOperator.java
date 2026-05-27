package com.velox.module.system.id.database.schema;

import com.velox.framework.id.common.business.IdBusinessTypes;
import com.velox.framework.id.common.message.IdGeneratorCommonMessages;
import com.velox.framework.id.common.type.IdDatabaseInitModes;
import com.velox.framework.id.exception.VeloxIdGeneratorException;
import com.velox.framework.id.properties.VeloxIdProperties;
import com.velox.framework.id.spi.database.DatabaseIdOperator;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseIdSequenceOperator implements DatabaseIdOperator {

    private final DataSource dataSource;
    private final boolean allowOnDemandInit;

    public DatabaseIdSequenceOperator(DataSource dataSource, VeloxIdProperties properties) {
        this.dataSource = dataSource;
        this.allowOnDemandInit = IdDatabaseInitModes.UPDATE.equals(properties.getDatabase().getInitMode());
    }

    @Override
    public String nextSourceId(String businessType) {
        String normalizedBusinessType = normalizeBusinessType(businessType);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Long currentValue = readCurrentValue(connection, normalizedBusinessType);
                if (currentValue == null) {
                    if (!allowOnDemandInit) {
                        throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.DATABASE_ID_NOT_INITIALIZED);
                    }
                    insertSeedRow(connection, normalizedBusinessType);
                    currentValue = 0L;
                }

                long nextValue = Math.addExact(currentValue, 1L);
                updateCurrentValue(connection, normalizedBusinessType, nextValue);
                connection.commit();
                return Long.toString(nextValue);
            } catch (SQLException | RuntimeException exception) {
                rollbackQuietly(connection);
                if (exception instanceof VeloxIdGeneratorException veloxIdGeneratorException) {
                    throw veloxIdGeneratorException;
                }
                throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.DATABASE_ID_ISSUE_FAILED, exception);
            }
        } catch (SQLException exception) {
            throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.DATABASE_ID_ISSUE_FAILED, exception);
        }
    }

    private Long readCurrentValue(Connection connection, String businessType) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT current_value FROM sys_id_sequence WHERE business_type = ? FOR UPDATE")) {
            statement.setString(1, businessType);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getLong(1);
            }
        }
    }

    private void updateCurrentValue(Connection connection, String businessType, long nextValue) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE sys_id_sequence SET current_value = ?, update_time = CURRENT_TIMESTAMP WHERE business_type = ?")) {
            statement.setLong(1, nextValue);
            statement.setString(2, businessType);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.DATABASE_ID_NOT_INITIALIZED);
            }
        }
    }

    private void insertSeedRow(Connection connection, String businessType) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO sys_id_sequence (business_type, current_value) VALUES (?, 0)")) {
            statement.setString(1, businessType);
            statement.executeUpdate();
        }
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // best effort rollback
        }
    }

    private String normalizeBusinessType(String businessType) {
        if (!StringUtils.hasText(businessType)) {
            return IdBusinessTypes.DEFAULT;
        }
        return businessType.trim();
    }
}
