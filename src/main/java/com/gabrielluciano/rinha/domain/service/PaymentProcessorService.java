package com.gabrielluciano.rinha.domain.service;

import com.gabrielluciano.rinha.domain.model.Payment;

public interface PaymentProcessorService {

    void processPayment(Payment payment);
}
