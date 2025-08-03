package com.gabrielluciano.rinha.application.dto;

import com.gabrielluciano.rinha.domain.model.Payment;

public record PaymentRequest(String correlationId, double amount) {

    public Payment toDomainModel() {
        return new Payment(correlationId, amount);
    }
}
