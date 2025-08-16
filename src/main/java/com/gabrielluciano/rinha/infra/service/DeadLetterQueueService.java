package com.gabrielluciano.rinha.infra.service;

import com.gabrielluciano.rinha.domain.model.Payment;
import com.gabrielluciano.rinha.domain.model.PaymentEvent;
import com.gabrielluciano.rinha.domain.model.PaymentProcessorType;
import com.gabrielluciano.rinha.domain.service.PaymentProcessorService;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@ApplicationScoped
public class DeadLetterQueueService {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueService.class);

    private final Queue<PaymentEvent> dlq = new ArrayDeque<>();
    private final Semaphore semaphore = new Semaphore(50);
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final PaymentProcessorDecisionService paymentProcessorDecisionService;
    private final PaymentProcessorService paymentProcessorService;

    public DeadLetterQueueService(PaymentProcessorDecisionService paymentProcessorDecisionService,
                                  PaymentProcessorService paymentProcessorService) {
        this.paymentProcessorDecisionService = paymentProcessorDecisionService;
        this.paymentProcessorService = paymentProcessorService;
    }

    public synchronized void addToDeadLetterQueue(PaymentEvent event) {
        dlq.add(event);
    }

    public void runDLQLoop() {
        while (true) {
            try {
                if (dlq.isEmpty()) {
                    log.info("DLQ is empty, waiting for events...");
                    Thread.sleep(100);
                    continue;
                }

                var processor = paymentProcessorDecisionService.getActiveProcessor();
                if (processor == null || processor == PaymentProcessorType.NONE) {
                    PaymentEvent event = null;
                    synchronized (this) {
                        if (!dlq.isEmpty()) {
                            event = dlq.peek();
                        }
                    }

                    if (event != null) {
                        log.debug("Probing DLQ event: {}", event.getCorrelationId());
                        boolean success = tryWithDefaultProcessor(event);
                        if (!success) {
                            success = tryWithFallbackProcessor(event);
                        }

                        if (success) {
                            synchronized (this) {
                                dlq.poll();
                            }
                        }
                    }

                    Thread.sleep(100); // short interval for probe mode
                } else {
                    List<PaymentEvent> batch;
                    synchronized (this) {
                        batch = new ArrayList<>(dlq);
                    }

                    // Create a list of futures for all tasks
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    for (PaymentEvent event : batch) {
                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            try {
                                semaphore.acquire();
                                String correlationId = event.getCorrelationId();
                                log.info("Retrying event from DLQ: {}", correlationId);
                                processPaymentEvent(event, paymentProcessorDecisionService.getActiveProcessor());
                                synchronized (this) {
                                    dlq.remove(event);
                                }
                            } catch (Exception e) {
                                log.error("Exception during DLQ processing for {}: {}", event, e.getMessage());
                            } finally {
                                semaphore.release();
                            }
                        }, executor);
                        futures.add(future);
                    }

                    // Wait for all tasks to complete before exiting the method
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("DLQ retry loop interrupted", e);
                break;
            } catch (Exception e) {
                log.error("Unexpected error in DLQ retry loop", e);
            }
        }
    }


    private boolean tryWithDefaultProcessor(PaymentEvent event) {
        try {
            processPaymentEvent(event, PaymentProcessorType.DEFAULT);
            paymentProcessorDecisionService.setProcessorHealth(PaymentProcessorType.DEFAULT, true);
            return true;
        } catch (Exception e) {
            log.debug("Default processor failed during DLQ probe for {}: {}", event.getCorrelationId(), e.getMessage());
            return false;
        }
    }

    private boolean tryWithFallbackProcessor(PaymentEvent event) {
        try {
            processPaymentEvent(event, PaymentProcessorType.FALLBACK);
            paymentProcessorDecisionService.setProcessorHealth(PaymentProcessorType.FALLBACK, true);
            return true;
        } catch (Exception e) {
            log.debug("Fallback processor failed during DLQ probe for {}: {}", event.getCorrelationId(), e.getMessage());
            return false;
        }
    }

    private void processPaymentEvent(PaymentEvent event, PaymentProcessorType processor) {
        Payment payment = event.toPayment();
        paymentProcessorService.processPayment(payment, processor);
    }
}
