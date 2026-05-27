package com.velox.framework.id.properties;

import cn.hutool.core.lang.Snowflake;
import com.velox.framework.id.common.prefix.IdPropertyPrefixes;
import com.velox.framework.id.common.message.IdGeneratorCommonMessages;
import com.velox.framework.id.common.type.IdDatabaseInitModes;
import com.velox.framework.id.common.type.IdGeneratorStrategies;
import com.velox.framework.id.exception.VeloxIdGeneratorException;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = IdPropertyPrefixes.ID)
public class VeloxIdProperties {
    /**
     * Top-level snowflake worker id.
     */
    private long workerId = 1L;

    /**
     * Top-level snowflake datacenter id.
     */
    private long datacenterId = 1L;

    /**
     * Database-default id governance.
     */
    private final DatabaseProperties database = new DatabaseProperties();

    /**
     * Snowflake strategy configuration.
     */
    private final SnowflakeProperties snowflake = new SnowflakeProperties();

    public boolean isEnabled() {
        return snowflake.isEnabled();
    }

    public long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(long datacenterId) {
        this.datacenterId = datacenterId;
    }

    public DatabaseProperties getDatabase() {
        return database;
    }

    public SnowflakeProperties getSnowflake() {
        return snowflake;
    }

    public void validate() {
        database.validate();
        validateSnowflakeCoordinates();
        if (isEnabled()) {
            snowflake.validate();
        }
    }

    public String getStrategy() {
        return isEnabled() ? IdGeneratorStrategies.SNOWFLAKE : IdGeneratorStrategies.DATABASE;
    }

    public boolean isSnowflakeStrategy() {
        return isEnabled();
    }

    private void validateSnowflakeCoordinates() {
        if (workerId < 0 || workerId > Snowflake.MAX_WORKER_ID) {
            throw new VeloxIdGeneratorException(
                    IdGeneratorCommonMessages.snowflakeWorkerIdOutOfRange(Snowflake.MAX_WORKER_ID));
        }
        if (datacenterId < 0 || datacenterId > Snowflake.MAX_DATA_CENTER_ID) {
            throw new VeloxIdGeneratorException(
                    IdGeneratorCommonMessages.snowflakeDatacenterIdOutOfRange(Snowflake.MAX_DATA_CENTER_ID));
        }
    }

    public static class DatabaseProperties {

        /**
         * Whether startup should reconcile id database objects.
         */
        private String initMode = IdDatabaseInitModes.NONE;

        public String getInitMode() {
            return initMode;
        }

        public void setInitMode(String initMode) {
            this.initMode = initMode;
        }

        void validate() {
            initMode = IdDatabaseInitModes.normalize(initMode);
            if (!IdDatabaseInitModes.isSupported(initMode)) {
                throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.unsupportedDatabaseInitMode(initMode));
            }
        }
    }

    public static class SnowflakeProperties {

        private boolean enabled = true;
        private long twepoch = Snowflake.DEFAULT_TWEPOCH;
        private long timeOffset = Snowflake.DEFAULT_TIME_OFFSET;
        private boolean useSystemClock;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getTwepoch() {
            return twepoch;
        }

        public void setTwepoch(long twepoch) {
            this.twepoch = twepoch;
        }

        public long getTimeOffset() {
            return timeOffset;
        }

        public void setTimeOffset(long timeOffset) {
            this.timeOffset = timeOffset;
        }

        public boolean isUseSystemClock() {
            return useSystemClock;
        }

        public void setUseSystemClock(boolean useSystemClock) {
            this.useSystemClock = useSystemClock;
        }

        void validate() {
            if (twepoch <= 0) {
                throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.SNOWFLAKE_TWEPOCH_MUST_BE_POSITIVE);
            }
            if (timeOffset < 0) {
                throw new VeloxIdGeneratorException(
                        IdGeneratorCommonMessages.SNOWFLAKE_TIME_OFFSET_MUST_NOT_BE_NEGATIVE);
            }
        }
    }
}
