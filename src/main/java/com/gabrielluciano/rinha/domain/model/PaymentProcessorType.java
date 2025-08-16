package com.gabrielluciano.rinha.domain.model;

public enum PaymentProcessorType {
    DEFAULT("default"), FALLBACK("fallback"), NONE("none");

    private final String name;

    PaymentProcessorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
