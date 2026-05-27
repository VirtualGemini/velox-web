package com.velox.module.system.auth.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class UserSessionSchemaBootstrapper implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserSessionSchemaBootstrapper.class);
    private static final String TABLE_NAME = "sys_user_session";

    private final DataSource dataSource;

    public UserSessionSchemaBootstrapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            if (tableExists(connection)) {
                return;
            }
            createTable(connection);
            log.info("Initialized {} for per-browser login isolation", TABLE_NAME);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize sys_user_session table", exception);
        }
    }

    private boolean tableExists(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM " + TABLE_NAME + " WHERE 1 = 0")) {
            statement.executeQuery();
            return true;
        } catch (SQLException exception) {
            return false;
        }
    }

    private void createTable(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS sys_user_session (
                  id bigint NOT NULL PRIMARY KEY,
                  user_id bigint NOT NULL,
                  token_hash varchar(64) NOT NULL,
                  status smallint NOT NULL DEFAULT 1,
                  login_time timestamp NULL,
                  last_active_time timestamp NULL,
                  logout_time timestamp NULL,
                  presence_expire_time timestamp NULL,
                  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
                  update_time timestamp DEFAULT CURRENT_TIMESTAMP,
                  create_by bigint NULL,
                  update_by bigint NULL,
                  deleted smallint NOT NULL DEFAULT 0
                )
                """)) {
            statement.executeUpdate();
        }
    }
}
