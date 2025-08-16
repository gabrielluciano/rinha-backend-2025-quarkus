package com.gabrielluciano.rinha.domain.service;

import com.gabrielluciano.rinha.domain.model.Payment;
import com.gabrielluciano.rinha.domain.model.PaymentEvent;
import com.gabrielluciano.rinha.domain.model.PaymentProcessorType;
import com.gabrielluciano.rinha.domain.model.ProcessorPaymentSummary;
import com.gabrielluciano.rinha.domain.repository.PaymentRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(PaymentRepository repository,
                          PaymentEventPublisher paymentEventPublisher) {
        this.repository = repository;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    public Uni<Void> processPayment(Payment payment) {
        return paymentEventPublisher.publishPaymentEvent(PaymentEvent.from(payment));
    }

    public List<ProcessorPaymentSummary> getPaymentSummary(Instant from, Instant to) {
        if (from == null || to == null)
            throw new IllegalArgumentException("Both 'from' and 'to' parameters must be provided");
        return createPaymentSummary(repository.getPayments(from, to));
    }

    private List<ProcessorPaymentSummary> createPaymentSummary(List<Payment> payments) {
        double defaultTotalAmount = 0.0;
        double fallbackTotalAmount = 0.0;

        long defaultTotalRequests = 0;
        long fallbackTotalRequests = 0;

        for (Payment payment : payments) {
            if (payment.getProcessor() == PaymentProcessorType.DEFAULT) {
                defaultTotalAmount += payment.getAmount();
                defaultTotalRequests++;
            } else {
                fallbackTotalAmount += payment.getAmount();
                fallbackTotalRequests++;
            }
        }
        return List.of(
                new ProcessorPaymentSummary(PaymentProcessorType.DEFAULT, defaultTotalRequests, defaultTotalAmount),
                new ProcessorPaymentSummary(PaymentProcessorType.FALLBACK, fallbackTotalRequests, fallbackTotalAmount)
        );
    }
}
