package com.gabrielluciano.rinha.infra.client.model;

public record PaymentProcessorHealthResponse(boolean failing, int minResponseTIme) {
}
