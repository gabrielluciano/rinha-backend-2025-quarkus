package com.gabrielluciano.rinha.infra.event;

import com.gabrielluciano.rinha.domain.model.Payment;
import com.gabrielluciano.rinha.domain.model.PaymentEvent;
import com.gabrielluciano.rinha.domain.service.PaymentEventConsumer;
import com.gabrielluciano.rinha.domain.service.PaymentProcessorService;
import com.gabrielluciano.rinha.infra.service.DeadLetterQueueService;
import com.gabrielluciano.rinha.infra.service.PaymentProcessorDecisionService;
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
    private final PaymentProcessorDecisionService paymentProcessorDecisionService;
    private final DeadLetterQueueService dlq;

    public DefaultPaymentEventConsumer(PaymentProcessorService paymentProcessorService,
                                       PaymentProcessorDecisionService paymentProcessorDecisionService,
                                       DeadLetterQueueService dlq) {
        this.paymentProcessorService = paymentProcessorService;
        this.paymentProcessorDecisionService = paymentProcessorDecisionService;
        this.dlq = dlq;
    }

    @ConsumeEvent(value = PAYMENT_EVENT_CHANNEL, blocking = true)
    @RunOnVirtualThread
    void consume(PaymentEvent event) {
        log.info("Processing payment event: {}", event);
        try {
            processPaymentEvent(event);
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
        var processor = paymentProcessorDecisionService.getActiveProcessor();
        Payment payment = event.toPayment();
        paymentProcessorService.processPayment(payment, processor);
        log.info("Payment processed successfully: {}", payment);
    }
}
