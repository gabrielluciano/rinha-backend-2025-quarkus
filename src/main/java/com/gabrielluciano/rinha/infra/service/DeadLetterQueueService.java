package com.gabrielluciano.rinha.infra.service;

import com.gabrielluciano.rinha.domain.model.PaymentEvent;
import com.gabrielluciano.rinha.domain.model.PaymentProcessorType;
import com.gabrielluciano.rinha.domain.repository.PaymentRepository;
import com.gabrielluciano.rinha.domain.service.PaymentProcessorService;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@ApplicationScoped
public class DeadLetterQueueService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueService.class);

    private final Map<String, PaymentEvent> dlq = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Semaphore semaphore = new Semaphore(50);
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final PaymentProcessorDecisionService paymentProcessorDecisionService;
    private final PaymentProcessorService paymentProcessorService;
    private final PaymentRepository repository;

    public DeadLetterQueueService(PaymentProcessorDecisionService paymentProcessorDecisionService,
                                  PaymentProcessorService paymentProcessorService,
                                  PaymentRepository repository) {
        this.paymentProcessorDecisionService = paymentProcessorDecisionService;
        this.paymentProcessorService = paymentProcessorService;
        this.repository = repository;
    }

    public void addToDeadLetterQueue(PaymentEvent event) {
        dlq.put(event.correlationId(), event);
    }

    @Scheduled(every = "2s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    @Blocking
    void processDeadLetterQueue() {
        if (dlq.isEmpty()) {
            return;
        }

        var processor = paymentProcessorDecisionService.getActiveProcessor();
        if (processor == null || processor == PaymentProcessorType.NONE) {
            log.warn("No active payment processor available, skipping DLQ processing");
            return;
        }

        Map<String, PaymentEvent> snapshot;
        synchronized (dlq) {
            snapshot = new LinkedHashMap<>(dlq);
        }

        // Create a list of futures for all tasks
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<String, PaymentEvent> entry : snapshot.entrySet()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    semaphore.acquire();
                    String correlationId = entry.getKey();
                    PaymentEvent event = entry.getValue();
                    log.info("Retrying event from DLQ: {}", correlationId);
                    processPaymentEvent(event);
                    dlq.remove(correlationId);
                } catch (Exception e) {
                    log.error("Exception during DLQ processing for {}: {}", entry.getKey(), e.getMessage());
                } finally {
                    semaphore.release();
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all tasks to complete before exiting the method
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void processPaymentEvent(PaymentEvent event) {
        var payment = event.toPayment();
        paymentProcessorService.processPayment(payment);
        repository.savePayment(payment);
    }
}
