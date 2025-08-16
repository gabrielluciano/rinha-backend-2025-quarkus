package com.gabrielluciano.rinha.infra.trigger;

import com.gabrielluciano.rinha.infra.service.PaymentProcessorDecisionService;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ProcessorHealthCheckTriggerScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProcessorHealthCheckTriggerScheduler.class);
    private final PaymentProcessorDecisionService paymentProcessorDecisionService;

    public ProcessorHealthCheckTriggerScheduler(PaymentProcessorDecisionService paymentProcessorDecisionService) {
        this.paymentProcessorDecisionService = paymentProcessorDecisionService;
    }

    @Scheduled(every = "6s")
    @Blocking
    @RunOnVirtualThread
    void checkProcessorHealth() {
        log.info("Triggering health check for payment processors");
        paymentProcessorDecisionService.updateProcessorsHealth();
    }
}
