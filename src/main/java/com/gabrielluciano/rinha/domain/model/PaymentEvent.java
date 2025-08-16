package com.gabrielluciano.rinha.domain.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.Serializable;

@RegisterForReflection
public class PaymentEvent implements Serializable {

    private String correlationId;
    private double amount;

    public PaymentEvent() {
    }

    public PaymentEvent(String correlationId, double amount) {
        this.correlationId = correlationId;
        this.amount = amount;
    }

    public static PaymentEvent from(Payment payment) {
        return new PaymentEvent(payment.getCorrelationId(), payment.getAmount());
    }

    public Payment toPayment() {
        return new Payment(correlationId, amount);
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
