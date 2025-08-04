package com.gabrielluciano.rinha.infra.trigger;

import com.gabrielluciano.rinha.infra.service.PaymentProcessorDecisionService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProcessorHealthCheckTriggerScheduler {

    private final PaymentProcessorDecisionService paymentProcessorDecisionService;

    public ProcessorHealthCheckTriggerScheduler(PaymentProcessorDecisionService paymentProcessorDecisionService) {
        this.paymentProcessorDecisionService = paymentProcessorDecisionService;
    }

    @Scheduled(every = "5s")
    void checkProcessorHealth() {
        paymentProcessorDecisionService.updateActiveProcessor();
    }
}
