package com.gabrielluciano.rinha.domain.service;

import com.gabrielluciano.rinha.domain.model.Payment;
import com.gabrielluciano.rinha.domain.model.PaymentProcessorType;

public interface PaymentProcessorService {

    void processPayment(Payment payment, PaymentProcessorType processor);
}
