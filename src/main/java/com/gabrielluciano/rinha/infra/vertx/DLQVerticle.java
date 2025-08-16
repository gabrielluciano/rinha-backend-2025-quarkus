package com.gabrielluciano.rinha.infra.vertx;

import com.gabrielluciano.rinha.infra.service.DeadLetterQueueService;
import io.vertx.core.AbstractVerticle;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.Executors;

@ApplicationScoped
public class DLQVerticle extends AbstractVerticle {

    private final DeadLetterQueueService dlqService;

    public DLQVerticle(DeadLetterQueueService dlqService) {
        this.dlqService = dlqService;
    }

    @Override
    public void start() {
        vertx.executeBlocking(Executors.callable(dlqService::runDLQLoop), false);
    }
}
