package com.gabrielluciano.rinha.domain.model;

import java.time.LocalDateTime;

public class Payment {

    private String correlationId;
    private double amount;
    private LocalDateTime timestamp;
    private PaymentProcessorType processor;

    public Payment() {
    }

    public Payment(String correlationId, double amount) {
        this(correlationId, amount, null);
    }

    public Payment(String correlationId, double amount, PaymentProcessorType processor) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.processor = processor;
        this.timestamp = LocalDateTime.now();
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

    public PaymentProcessorType getProcessor() {
        return processor;
    }

    public void setProcessor(PaymentProcessorType processor) {
        this.processor = processor;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
