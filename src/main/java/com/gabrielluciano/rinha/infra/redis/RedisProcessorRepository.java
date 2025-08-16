package com.gabrielluciano.rinha.infra.redis;

import com.gabrielluciano.rinha.domain.model.PaymentProcessorType;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RedisProcessorRepository {

    private static final String KEY_PREFIX = "processor";
    private static final String LOCK_KEY = "processor-lock";
    public static final String LOCKED = "locked";

    private final ValueCommands<String, Boolean> commands;
    private final RedisDataSource redisDataSource;

    public RedisProcessorRepository(RedisDataSource dataSource) {
        this.redisDataSource = dataSource;
        this.commands = dataSource.value(String.class, Boolean.class);
    }

    public void saveProcessorHealthy(PaymentProcessorType processor, boolean healthy) {
        final var key = KEY_PREFIX + ":" + processor.getName();
        commands.set(key, healthy);
    }

    public boolean isProcessorHealthy(PaymentProcessorType processor) {
        final var key = KEY_PREFIX + ":" + processor.getName();
        return commands.get(key);
    }

    public boolean tryAcquireLock() {
        return redisDataSource.execute("SET", LOCK_KEY, LOCKED, "NX", "PX", "3000") != null;
    }
}
