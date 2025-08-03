package com.gabrielluciano.rinha.domain.model;

public record ProcessorPaymentSummary(PaymentProcessorType processor, long totalRequests, double totalAmount) {
}
