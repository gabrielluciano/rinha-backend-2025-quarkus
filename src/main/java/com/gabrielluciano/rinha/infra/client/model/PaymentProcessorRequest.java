package com.gabrielluciano.rinha.infra.client.model;

import com.gabrielluciano.rinha.domain.model.Payment;

import java.time.LocalDateTime;

public record PaymentProcessorRequest(String correlationId, double amount, LocalDateTime requestedAt) {

    public static PaymentProcessorRequest fromPayment(Payment payment) {
        return new PaymentProcessorRequest(payment.getCorrelationId(), payment.getAmount(), payment.getTimestamp());
    }
}
