package com.velox.framework.redis.noop;

import com.velox.framework.redis.common.message.RedisCommonMessages;
import com.velox.framework.redis.common.type.RedisDisabledStrategyType;
import com.velox.framework.redis.exception.VeloxRedisException;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class DisabledRedisCacheWriter implements RedisCacheWriter {

    private static final Logger log = LoggerFactory.getLogger(DisabledRedisCacheWriter.class);

    private final RedisDisabledStrategyType disabledStrategy;
    private final CacheStatisticsCollector statisticsCollector;

    public DisabledRedisCacheWriter(RedisDisabledStrategyType disabledStrategy) {
        this(disabledStrategy, CacheStatisticsCollector.none());
    }

    private DisabledRedisCacheWriter(
            RedisDisabledStrategyType disabledStrategy,
            CacheStatisticsCollector statisticsCollector) {
        this.disabledStrategy = disabledStrategy;
        this.statisticsCollector = statisticsCollector;
    }

    @Override
    public byte[] get(String name, byte[] key) {
        handleDisabled("cache.get");
        statisticsCollector.incGets(name);
        statisticsCollector.incMisses(name);
        return null;
    }

    @Override
    public CompletableFuture<byte[]> retrieve(String name, byte[] key, Duration ttl) {
        return CompletableFuture.completedFuture(get(name, key));
    }

    @Override
    public void put(String name, byte[] key, byte[] value, Duration ttl) {
        handleDisabled("cache.put");
        statisticsCollector.incPuts(name);
    }

    @Override
    public CompletableFuture<Void> store(String name, byte[] key, byte[] value, Duration ttl) {
        put(name, key, value, ttl);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
        handleDisabled("cache.putIfAbsent");
        statisticsCollector.incGets(name);
        statisticsCollector.incMisses(name);
        return null;
    }

    @Override
    public void remove(String name, byte[] key) {
        handleDisabled("cache.remove");
        statisticsCollector.incDeletes(name);
    }

    @Override
    public void clean(String name, byte[] pattern) {
        handleDisabled("cache.clean");
        statisticsCollector.incDeletes(name);
    }

    @Override
    public void clearStatistics(String name) {
        statisticsCollector.reset(name);
    }

    @Override
    public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector statisticsCollector) {
        return new DisabledRedisCacheWriter(disabledStrategy, statisticsCollector);
    }

    @Override
    public CacheStatistics getCacheStatistics(String cacheName) {
        return statisticsCollector.getCacheStatistics(cacheName);
    }

    private void handleDisabled(String operation) {
        if (disabledStrategy == RedisDisabledStrategyType.NOOP) {
            log.warn(RedisCommonMessages.REDIS_CACHE_DISABLED_OPERATION, operation);
            return;
        }
        throw new VeloxRedisException(RedisCommonMessages.REDIS_CACHE_CAPABILITY_DISABLED);
    }
}
