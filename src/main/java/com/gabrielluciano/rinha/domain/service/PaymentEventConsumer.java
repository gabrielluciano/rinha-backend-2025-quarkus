package com.gabrielluciano.rinha.domain.service;

import com.gabrielluciano.rinha.domain.model.PaymentEvent;

public interface PaymentEventConsumer {

    void processPaymentEvent(PaymentEvent event);
}
