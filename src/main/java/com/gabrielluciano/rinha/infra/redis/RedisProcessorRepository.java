package com.gabrielluciano.rinha.infra.redis;

import com.gabrielluciano.rinha.domain.model.PaymentProcessorType;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RedisProcessorRepository {

    private static final String KEY = "processor";

    private final ValueCommands<String, PaymentProcessorType> commands;

    public RedisProcessorRepository(RedisDataSource dataSource) {
        this.commands = dataSource.value(String.class, PaymentProcessorType.class);
    }

    public void saveProcessor(PaymentProcessorType processor) {
        commands.set(KEY, processor);
    }

    public PaymentProcessorType getActiveProcessor() {
        return commands.get(KEY);
    }
}
