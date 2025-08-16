package com.gabrielluciano.rinha.infra.event;

import com.gabrielluciano.rinha.domain.model.PaymentEvent;
import com.gabrielluciano.rinha.domain.repository.PaymentRepository;
import com.gabrielluciano.rinha.domain.service.PaymentEventConsumer;
import com.gabrielluciano.rinha.domain.service.PaymentProcessorService;
import com.gabrielluciano.rinha.infra.service.DeadLetterQueueService;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class DefaultPaymentEventConsumer implements PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DefaultPaymentEventConsumer.class);
    public static final String PAYMENT_EVENT_CHANNEL = "payment.event";

    private final PaymentProcessorService paymentProcessorService;
    private final PaymentRepository repository;
    private final DeadLetterQueueService dlq;

    public DefaultPaymentEventConsumer(PaymentProcessorService paymentProcessorService,
                                       PaymentRepository repository,
                                       DeadLetterQueueService dlq) {
        this.paymentProcessorService = paymentProcessorService;
        this.repository = repository;
        this.dlq = dlq;
    }

    @ConsumeEvent(value = PAYMENT_EVENT_CHANNEL, blocking = true)
    @RunOnVirtualThread
    void consume(PaymentEvent event) {
        log.info("Processing payment event: {}", event);
        try {
            processPaymentEvent(event);
            log.debug("Event processed successfully: {}", event);
        } catch (Exception e) {
            log.error("Processing failed, sending to DLQ: {}", event, e);
            sendToDLQ(event);
        }
    }

    private void sendToDLQ(PaymentEvent event) {
        dlq.addToDeadLetterQueue(event);
    }

    @Override
    public void processPaymentEvent(PaymentEvent event) {
        var payment = event.toPayment();
        paymentProcessorService.processPayment(payment);
        repository.savePayment(payment);
    }
}
