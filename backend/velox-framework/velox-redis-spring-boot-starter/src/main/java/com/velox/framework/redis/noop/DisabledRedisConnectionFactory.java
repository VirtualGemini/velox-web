package com.velox.framework.redis.noop;

import com.velox.framework.redis.common.message.RedisCommonMessages;
import com.velox.framework.redis.exception.VeloxRedisException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;

public class DisabledRedisConnectionFactory implements RedisConnectionFactory {

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return false;
    }

    @Override
    public RedisConnection getConnection() {
        throw disabled();
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        throw disabled();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        throw disabled();
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException exception) {
        return exception instanceof DataAccessException dataAccessException ? dataAccessException : null;
    }

    private VeloxRedisException disabled() {
        return new VeloxRedisException(RedisCommonMessages.REDIS_CAPABILITY_DISABLED);
    }
}
