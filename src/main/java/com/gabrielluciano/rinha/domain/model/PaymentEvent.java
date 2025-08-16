package com.gabrielluciano.rinha.domain.model;

public record PaymentEvent(String correlationId, double amount) {

    public static PaymentEvent from(Payment payment) {
        return new PaymentEvent(payment.getCorrelationId(), payment.getAmount());
    }

    public Payment toPayment() {
        return new Payment(correlationId(), amount());
    }
}
