package com.gabrielluciano.rinha.infra.exception;

public class PaymentProcessorException extends RuntimeException {
    public PaymentProcessorException(String message) {
        super(message);
    }
}
