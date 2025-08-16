package com.gabrielluciano.rinha.application.dto;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.gabrielluciano.rinha.domain.model.Payment;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.io.Serializable;

@JSONCompiled
@RegisterForReflection
public class PaymentRequest implements Serializable {

    private String correlationId;
    private double amount;

    public PaymentRequest() {
    }

    public PaymentRequest(String correlationId, double amount) {
        this.correlationId = correlationId;
        this.amount = amount;
    }

    public Payment toDomainModel() {
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
