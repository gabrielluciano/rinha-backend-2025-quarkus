package com.gabrielluciano.rinha.infra.vertx;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class DLQDeployer {

    public void init(@Observes StartupEvent e, Vertx vertx, DLQVerticle dlqVerticle) {
        vertx.deployVerticle(dlqVerticle, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD));
    }
}