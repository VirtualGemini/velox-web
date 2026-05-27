package com.velox.framework.persistence.properties;

import com.velox.framework.persistence.common.prefix.PersistencePropertyPrefixes;
import com.velox.framework.persistence.common.message.PersistenceCommonMessages;
import com.velox.framework.persistence.exception.PersistenceConfigException;
import com.velox.framework.persistence.spi.dialect.DatabaseDialect;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Validated
@ConfigurationProperties(prefix = PersistencePropertyPrefixes.DATASOURCE)
public class VeloxPersistenceProperties {

    private static final String DEFAULT_DIALECT = "";

    private String type;

    @Valid
    private Map<String, DatabaseConnectionProperties> configs = new LinkedHashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, DatabaseConnectionProperties> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, DatabaseConnectionProperties> configs) {
        this.configs = configs;
    }

    public void validate() {
        String normalizedType = normalize(type);
        if (normalizedType.isEmpty()) {
            throw new PersistenceConfigException(PersistenceCommonMessages.DATABASE_TYPE_MUST_NOT_BE_BLANK);
        }
        if (configs == null || configs.isEmpty()) {
            throw new PersistenceConfigException(PersistenceCommonMessages.DATASOURCE_CONFIGS_MUST_NOT_BE_EMPTY);
        }
        DatabaseConnectionProperties config = configs.get(normalizedType);
        if (config == null) {
            throw new PersistenceConfigException(PersistenceCommonMessages.DATASOURCE_CONFIG_MISSING.formatted(normalizedType));
        }
        config.validate(normalizedType);
    }

    public DatabaseConnectionProperties getActiveConfig() {
        validate();
        return configs.get(normalize(type));
    }

    public String getNormalizedType() {
        validate();
        return normalize(type);
    }

    @Validated
    public static class DatabaseConnectionProperties {

        private String driverClassName;

        @NotBlank
        private String url;

        private String username;

        private String password;

        private Map<String, String> dataSourceProperties = new LinkedHashMap<>();

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getResolvedDriverClassName(DatabaseDialect dialect) {
            return Objects.requireNonNullElse(driverClassName, dialect.getDefaultDriverClassName());
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Map<String, String> getDataSourceProperties() {
            return dataSourceProperties;
        }

        public void setDataSourceProperties(Map<String, String> dataSourceProperties) {
            this.dataSourceProperties = dataSourceProperties;
        }

        public void validate(String type) {
            if (url == null || url.isBlank()) {
                throw new PersistenceConfigException(PersistenceCommonMessages.DATASOURCE_URL_MUST_NOT_BE_BLANK.formatted(type));
            }
        }
    }

    private String normalize(String value) {
        return value == null ? DEFAULT_DIALECT : value.trim().toLowerCase(Locale.ROOT);
    }
}
