package com.gabrielluciano.rinha.infra.client.model;

import com.gabrielluciano.rinha.domain.model.Payment;

import java.time.Instant;

public record PaymentProcessorRequest(String correlationId, double amount, Instant requestedAt) {

    public static PaymentProcessorRequest fromPayment(Payment payment) {
        return new PaymentProcessorRequest(payment.getCorrelationId(), payment.getAmount(), payment.getTimestamp());
    }
}
